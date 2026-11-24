package com.ogapp.subai.domain.engine

/**
 * Charging-system guidance: the Big 3 upgrade, when to add a second battery,
 * and when the alternator becomes the real bottleneck. Thresholds are the
 * commonly accepted car-audio rules of thumb, tied to total RMS power.
 */
object ElectricalAdvisor {

    data class ChargingAdvice(
        val needsBig3: Boolean,
        val considerSecondBattery: Boolean,
        val considerHighOutputAlt: Boolean,
        val summary: String,
        val steps: List<String>
    )

    val big3Steps: List<String> = listOf(
        "1) Alternator positive (B+) → battery positive — upgrade to 1/0 AWG (or 4 AWG minimum).",
        "2) Battery negative → chassis/engine ground — same gauge.",
        "3) Engine block → chassis ground — same gauge. This one is skipped most and matters most.",
        "Keep the factory cables in place and ADD the new ones in parallel; use OFC copper and proper ring terminals."
    )

    /**
     * @param totalRmsWatts total continuous (RMS) power of all amplifiers combined.
     */
    fun advise(totalRmsWatts: Int): ChargingAdvice {
        val big3 = totalRmsWatts >= 800
        val secondBattery = totalRmsWatts >= 1500
        val hiAlt = totalRmsWatts >= 2000

        val summary = when {
            totalRmsWatts < 800 ->
                "Factory charging is usually fine at this power. Do the Big 3 anyway if you see headlights dim on bass — it's cheap insurance."
            totalRmsWatts < 1500 ->
                "Do the Big 3 upgrade. Stock alternator and battery can handle it with good wiring."
            totalRmsWatts < 2000 ->
                "Do the Big 3 and add a second battery (AGM). Watch your voltage under load — it shouldn't drop below ~12.0V."
            else ->
                "High power: Big 3 + a high-output alternator + extra battery capacity. Target keeping voltage above 12.5V while playing."
        }

        val steps = buildList {
            if (big3) add("Perform the Big 3 wiring upgrade (see steps).")
            if (secondBattery) add("Add an AGM second battery near the amps, wired with the same gauge as your main power run, with its own fused connection.")
            if (hiAlt) add("Upgrade to a high-output alternator rated above your total current draw; the stock alt can't keep up.")
            add("Add a voltage meter so you can SEE sag. Chronic sag below ~11.5V under load cooks amps and batteries.")
        }

        return ChargingAdvice(big3, secondBattery, hiAlt, summary, steps)
    }
}
