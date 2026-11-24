package com.ogapp.subai.domain.engine

/**
 * Line Output Converter (LOC) guidance — turning factory speaker-level output
 * into a low-level RCA signal for an aftermarket amp, including the turn-on
 * problem (the thing that trips most people up on a factory-radio install).
 */
object LocAdvisor {

    /** Where to tap the signal, best to worst. */
    val tapPoints: List<String> = listOf(
        "Behind the factory radio, on the harness speaker wires — cleanest access, use a harness T-tap or adapter.",
        "At the rear-deck / door speaker wires — easy if the radio is hard to reach.",
        "At the factory amplifier's speaker-level OUTPUT (not input) — needed when a factory amp sits between the radio and speakers."
    )

    /**
     * How the amp/LOC gets its turn-on signal. Order = what to try first.
     */
    val turnOnMethods: List<String> = listOf(
        "Aftermarket radio: run its Blue/White remote wire straight to the LOC/amp REM terminal. Easiest and most reliable.",
        "Factory radio, LOC with signal-sensing (DC-offset or audio-sense) auto turn-on: let the LOC generate its own remote output — wire LOC's remote-out to the amp's REM. No extra wire needed.",
        "Factory radio, no sense feature: tap a switched/ACC +12V source (fuse box 'add-a-fuse' on an ACC circuit) for the remote. Avoid a constant-hot source or the amp never shuts off and drains the battery."
    )

    val wiringSteps: List<String> = listOf(
        "Identify a full-range speaker pair (front or rear). Avoid a factory-crossed-over tweeter or a sub channel.",
        "Connect LOC speaker-level inputs to that pair, matching polarity (+ to +, − to −).",
        "Connect the LOC's RCA outputs to your amp's RCA inputs with a quality shielded RCA run (route away from power wire to avoid noise).",
        "Sort out remote turn-on (see turn-on methods) — wire LOC/amp REM correctly.",
        "Set the LOC output level LOW to start. Many factory decks apply EQ/loudness and can clip near max volume.",
        "Set levels: factory volume to ~75%, then raise LOC level until just before distortion, THEN set amp gain. Never set gain by ear at full blast."
    )

    val warnings: List<String> = listOf(
        "Factory amplified systems often apply equalization — tap AFTER the factory amp (speaker outputs) or use a LOC with built-in EQ/summing for full-range, flat signal.",
        "Some factory systems use a 'load-sense' that needs the speakers still connected, or they fault. A LOC with load resistors or 'factory integration' handles this.",
        "A constant-hot remote source will drain your battery. Use a SWITCHED source or signal-sense turn-on.",
        "Ground the LOC to the same ground as the amp to prevent alternator whine."
    )
}
