package com.ogapp.subai.domain.engine

import com.ogapp.subai.domain.model.AmpClass
import com.ogapp.subai.domain.model.WireRecommendation
import kotlin.math.ceil

/**
 * Deterministic power/ground wire + fuse sizing.
 *
 * Two inputs can drive it:
 *  1. Sum of the amps' on-board fuse ratings  -> the true max current the amps
 *     can pull, the most reliable basis for wire + main-fuse sizing.
 *  2. Total RMS watts + amp class              -> an estimate when fuse ratings
 *     are unknown, using current = RMS / efficiency / system voltage.
 *
 * Wire is chosen by ampacity first, then stepped up if voltage drop over the
 * run is too high. The main fuse protects the WIRE (never exceeds its ampacity)
 * while still covering the design current. CCA wire is bumped one size heavier.
 */
object PowerWiringCalculator {

    private data class Gauge(val name: String, val ampacity: Int, val ohmsPerFt: Double)

    // Thin -> thick. Ampacity anchored to conservative car-audio fuse pairings;
    // resistance is annealed-copper DC resistance per foot.
    private val GAUGES = listOf(
        Gauge("10 AWG", 30, 0.0009989),
        Gauge("8 AWG", 50, 0.0006282),
        Gauge("6 AWG", 80, 0.0003951),
        Gauge("4 AWG", 125, 0.0002485),
        Gauge("2 AWG", 175, 0.0001563),
        Gauge("1/0 AWG", 275, 0.00009827)
    )

    private val STANDARD_FUSES = listOf(20, 25, 30, 40, 50, 60, 80, 100, 125, 150, 175, 200, 250, 300)

    private const val SYSTEM_VOLTAGE = 13.8          // running, conservative for current est.
    private const val MAX_VOLTAGE_DROP = 0.5         // volts, ~3.5% of 14.4V charging

    /** Estimate peak current (amps) from total RMS power and amp class. */
    fun currentFromRms(totalRmsWatts: Int, ampClass: AmpClass): Int {
        require(totalRmsWatts > 0)
        val amps = totalRmsWatts / ampClass.typicalEfficiency / SYSTEM_VOLTAGE
        return ceil(amps).toInt()
    }

    /**
     * Recommend wire + fuse.
     * @param designCurrentAmps peak current the system can draw (from fuse sum or RMS estimate)
     * @param runLengthFt one-way length of the main power run (battery -> amp)
     * @param isCca true if using copper-clad aluminium instead of pure copper (OFC)
     */
    fun recommend(designCurrentAmps: Int, runLengthFt: Double, isCca: Boolean): WireRecommendation {
        require(designCurrentAmps > 0) { "Design current must be positive" }
        require(runLengthFt > 0) { "Run length must be positive" }

        // 1) smallest gauge whose ampacity covers the current.
        var idx = GAUGES.indexOfFirst { it.ampacity >= designCurrentAmps }
        if (idx == -1) idx = GAUGES.lastIndex // beyond our table; recommend biggest + note

        // 2) step up while voltage drop over the run is too high.
        while (idx < GAUGES.lastIndex &&
            vDrop(designCurrentAmps, GAUGES[idx].ohmsPerFt, runLengthFt) > MAX_VOLTAGE_DROP
        ) idx++

        // 3) CCA conducts ~61% of copper; bump one size heavier.
        if (isCca && idx < GAUGES.lastIndex) idx++

        val chosen = GAUGES[idx]
        val drop = vDrop(designCurrentAmps, chosen.ohmsPerFt, runLengthFt)

        val fuse = mainFuseFor(designCurrentAmps, chosen.ampacity)

        val notes = buildList {
            add("Fuse protects the WIRE, not the amp. Mount the main fuse within 18 in (45 cm) of the battery positive terminal.")
            add("Run the ground wire the SAME gauge as power, keep it under ~18 in, and bolt it to bare, sanded metal (not paint).")
            if (isCca) add("CCA (copper-clad aluminium) chosen: sized one gauge heavier than copper would need. Pure OFC copper is safer for high current if you can get it.")
            if (designCurrentAmps >= 150) add("At this current, do the Big 3 wiring upgrade and check your alternator output, or your voltage will sag and lights will dim on bass hits.")
            if (chosen == GAUGES.last() && chosen.ampacity < designCurrentAmps)
                add("Your current need exceeds a single 1/0 run — use dual 1/0 runs or a larger cable, and split fusing accordingly.")
            add("Voltage drop on this run ≈ ${"%.2f".format(drop)} V. Under ${"%.1f".format(MAX_VOLTAGE_DROP)} V is the target.")
        }

        return WireRecommendation(
            gauge = chosen.name,
            designCurrentAmps = designCurrentAmps,
            runLengthFt = runLengthFt,
            voltageDropVolts = Math.round(drop * 100.0) / 100.0,
            isCca = isCca,
            mainFuseAmps = fuse,
            groundGauge = chosen.name,
            notes = notes
        )
    }

    private fun vDrop(amps: Int, ohmsPerFt: Double, lengthFt: Double): Double =
        amps * ohmsPerFt * lengthFt

    /** Smallest standard fuse >= design current, capped at wire ampacity. */
    private fun mainFuseFor(designCurrent: Int, wireAmpacity: Int): Int {
        val candidate = STANDARD_FUSES.firstOrNull { it >= designCurrent } ?: STANDARD_FUSES.last()
        // never let the fuse exceed what the wire can carry
        val capped = STANDARD_FUSES.filter { it <= wireAmpacity }.maxOrNull() ?: STANDARD_FUSES.first()
        return minOf(candidate, capped)
    }
}
