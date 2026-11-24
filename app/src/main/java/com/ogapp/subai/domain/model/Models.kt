package com.ogapp.subai.domain.model

/**
 * Domain models for SubAI. All plain data — no Android dependencies — so the
 * whole calculation layer is unit-testable on the JVM.
 */

/** A subwoofer's voice-coil arrangement. */
enum class CoilType(val coilCount: Int, val label: String) {
    SVC(1, "Single Voice Coil"),
    DVC(2, "Dual Voice Coil")
}

/** How a set of identical elements (coils or subs) are joined. */
enum class Connection(val label: String) {
    SERIES("Series"),
    PARALLEL("Parallel"),
    SERIES_PARALLEL("Series-Parallel")
}

/** Amplifier topology class — drives efficiency used for current estimates. */
enum class AmpClass(val label: String, val typicalEfficiency: Double) {
    CLASS_D("Class D (digital)", 0.85),
    CLASS_AB("Class A/B", 0.55),
    UNKNOWN("Not sure", 0.65)
}

/** How many amp channels are driving the subs and how they're configured. */
enum class AmpChannelMode(val label: String) {
    MONO("Mono / 1-channel (bridged mono)"),
    BRIDGED_2CH("2-channel bridged to 1"),
    STEREO_2CH("2-channel, one sub per channel"),
    MULTI("4+ channel")
}

/**
 * The subwoofer stage of a build. [count] identical subs, each [coilType] with
 * [coilOhms] nominal per coil.
 */
data class SubStage(
    val count: Int,
    val coilType: CoilType,
    val coilOhms: Double,
    val rmsWattsEach: Int? = null
) {
    init {
        require(count in 1..8) { "Sub count must be 1..8" }
        require(coilOhms > 0) { "Coil impedance must be positive" }
    }
}

/**
 * A single achievable wiring outcome: how the coils connect, how the subs
 * connect, and the resulting total impedance the amp will see.
 */
data class WiringOption(
    val totalOhms: Double,
    val coilConnection: Connection?,   // null when SVC single-sub (no coil choice)
    val subConnection: Connection?,    // null for a single sub
    val title: String,
    val detail: String,
    val diagramKey: String
)

/** Verdict of matching a [WiringOption] to a specific amplifier. */
enum class MatchVerdict { OPTIMAL, SAFE, RISKY_LOW, TOO_HIGH }

data class MatchedOption(
    val option: WiringOption,
    val verdict: MatchVerdict,
    val note: String
)

/** Power/ground wire recommendation. */
data class WireRecommendation(
    val gauge: String,            // e.g. "4 AWG"
    val designCurrentAmps: Int,
    val runLengthFt: Double,
    val voltageDropVolts: Double,
    val isCca: Boolean,
    val mainFuseAmps: Int,
    val groundGauge: String,
    val notes: List<String>
)

/** A single aftermarket head-unit harness wire and its job. */
data class HarnessWire(
    val color: String,
    val meaning: String,
    val hookUp: String
)

/** One node in the troubleshooting decision tree. */
data class TroubleStep(
    val id: String,
    val question: String,
    val help: String,
    val yesNext: String?,   // id of next step, or null = terminal
    val noNext: String?,
    val diagnosisIfTerminal: String? = null
)
