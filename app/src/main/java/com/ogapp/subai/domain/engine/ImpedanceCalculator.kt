package com.ogapp.subai.domain.engine

import com.ogapp.subai.domain.model.Connection
import com.ogapp.subai.domain.model.CoilType
import com.ogapp.subai.domain.model.MatchVerdict
import com.ogapp.subai.domain.model.MatchedOption
import com.ogapp.subai.domain.model.SubStage
import com.ogapp.subai.domain.model.WiringOption
import kotlin.math.abs

/**
 * Deterministic subwoofer impedance / wiring calculator.
 *
 * Rules (identical drivers assumed, which is how real multi-sub builds are done):
 *  - DVC coils in SERIES   -> 2 x coil ohms
 *  - DVC coils in PARALLEL -> coil ohms / 2
 *  - N identical subs in SERIES   -> N x per-sub ohms
 *  - N identical subs in PARALLEL -> per-sub ohms / N
 *  - 4 subs can also be SERIES-PARALLEL (two parallel pairs in series) -> per-sub ohms
 *
 * This is hard-coded physics, never AI. Getting it wrong can destroy an amp.
 */
object ImpedanceCalculator {

    private fun round2(v: Double): Double = Math.round(v * 100.0) / 100.0

    /** Per-sub impedance for a given coil connection. */
    private fun perSubOhms(stage: SubStage, coil: Connection?): Double = when (stage.coilType) {
        CoilType.SVC -> stage.coilOhms
        CoilType.DVC -> when (coil) {
            Connection.SERIES -> stage.coilOhms * 2
            Connection.PARALLEL -> stage.coilOhms / 2
            else -> stage.coilOhms // defensive; DVC always has a coil choice
        }
    }

    /** Combine [count] identical [perSub] impedances with [sub] connection. */
    private fun combineSubs(perSub: Double, count: Int, sub: Connection): Double = when (sub) {
        Connection.SERIES -> perSub * count
        Connection.PARALLEL -> perSub / count
        Connection.SERIES_PARALLEL -> perSub // only used for groups of 4 (2s2p) / 2x2
    }

    /**
     * Enumerate every realistic wiring option for the stage, with the resulting
     * total impedance and a plain-English description.
     */
    fun options(stage: SubStage): List<WiringOption> {
        val coilChoices: List<Connection?> = when (stage.coilType) {
            CoilType.SVC -> listOf(null)
            CoilType.DVC -> listOf(Connection.SERIES, Connection.PARALLEL)
        }

        val out = mutableListOf<WiringOption>()
        for (coil in coilChoices) {
            val perSub = perSubOhms(stage, coil)
            val coilTxt = coil?.let { "coils in ${it.label.lowercase()}" }

            if (stage.count == 1) {
                out += WiringOption(
                    totalOhms = round2(perSub),
                    coilConnection = coil,
                    subConnection = null,
                    title = formatOhms(perSub),
                    detail = buildString {
                        append("Single ${stage.coilType.label}")
                        if (coilTxt != null) append(", $coilTxt")
                    },
                    diagramKey = diagramKey(stage, coil, null)
                )
            } else {
                val subChoices = buildList {
                    add(Connection.SERIES)
                    add(Connection.PARALLEL)
                    if (stage.count == 4) add(Connection.SERIES_PARALLEL)
                }
                for (sub in subChoices) {
                    val total = combineSubs(perSub, stage.count, sub)
                    out += WiringOption(
                        totalOhms = round2(total),
                        coilConnection = coil,
                        subConnection = sub,
                        title = formatOhms(total),
                        detail = buildString {
                            append("${stage.count}x ${stage.coilType.label}")
                            if (coilTxt != null) append(", $coilTxt")
                            append(", subs in ${sub.label.lowercase()}")
                        },
                        diagramKey = diagramKey(stage, coil, sub)
                    )
                }
            }
        }
        // De-duplicate identical total+topology, keep stable order by ohms then detail.
        return out
            .distinctBy { "${it.totalOhms}|${it.coilConnection}|${it.subConnection}" }
            .sortedWith(compareBy({ it.totalOhms }, { it.detail }))
    }

    /**
     * Match every option against an amp's minimum stable impedance for this
     * channel configuration. [ampMinOhms] is the lowest load the amp is rated
     * to drive on the relevant output (e.g. 1.0 for "1-ohm stable mono").
     */
    fun matchToAmp(stage: SubStage, ampMinOhms: Double, tolerance: Double = 0.05): List<MatchedOption> {
        val opts = options(stage)
        val matched = opts.map { opt ->
            val verdict = when {
                opt.totalOhms < ampMinOhms - tolerance -> MatchVerdict.RISKY_LOW
                abs(opt.totalOhms - ampMinOhms) <= tolerance -> MatchVerdict.OPTIMAL
                else -> MatchVerdict.SAFE
            }
            MatchedOption(opt, verdict, verdictNote(verdict, opt.totalOhms, ampMinOhms))
        }
        // Promote the lowest SAFE load (closest to, but >= min) to OPTIMAL if no
        // exact match exists — that's where the amp makes its rated power.
        if (matched.none { it.verdict == MatchVerdict.OPTIMAL }) {
            val bestSafe = matched
                .filter { it.verdict == MatchVerdict.SAFE }
                .minByOrNull { it.option.totalOhms }
            if (bestSafe != null) {
                return matched.map {
                    if (it === bestSafe) it.copy(
                        verdict = MatchVerdict.OPTIMAL,
                        note = "Best match: lowest safe load for this amp, so it makes the most power without going below its rating."
                    ) else it
                }
            }
        }
        return matched
    }

    private fun verdictNote(v: MatchVerdict, total: Double, min: Double): String = when (v) {
        MatchVerdict.OPTIMAL -> "Lands right on the amp's rated load — maximum rated power, fully safe."
        MatchVerdict.SAFE -> "Safe. Load is above the amp's ${formatOhms(min)} minimum, so you'll get less than max power but the amp runs cool."
        MatchVerdict.RISKY_LOW -> "DO NOT USE: ${formatOhms(total)} is below the amp's ${formatOhms(min)} minimum. This overheats and can destroy the amp."
        MatchVerdict.TOO_HIGH -> "Load is much higher than ideal — very little power. Usable but weak."
    }

    private fun diagramKey(stage: SubStage, coil: Connection?, sub: Connection?): String {
        val c = stage.coilType.name.lowercase()
        val co = coil?.name?.lowercase() ?: "x"
        val su = sub?.name?.lowercase() ?: "single"
        return "diag_${c}_${stage.count}_${co}_$su"
    }

    fun formatOhms(ohms: Double): String {
        val r = round2(ohms)
        return if (r == Math.floor(r)) "${r.toInt()} Ω" else "$r Ω"
    }
}
