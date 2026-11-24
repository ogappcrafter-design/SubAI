package com.ogapp.subai.domain.engine

import com.ogapp.subai.domain.model.HarnessWire

/**
 * Standard aftermarket head-unit (radio) harness wiring. These colors are the
 * near-universal ISO/aftermarket convention used by Metra, Scosche, Pioneer,
 * Sony, Kenwood, Alpine, JVC, etc. A vehicle-specific harness adapter maps the
 * factory plug to these colors so you never cut factory wires.
 */
object HeadUnitHarness {

    val powerWires: List<HarnessWire> = listOf(
        HarnessWire(
            "Yellow", "Constant +12V (battery / memory, B+)",
            "Always-hot 12V. Keeps presets and clock. Connect to the adapter's yellow."
        ),
        HarnessWire(
            "Red", "Switched +12V (accessory / ignition, ACC)",
            "Hot only with key in ACC/ON. Turns the radio on/off with the key. Connect to adapter's red."
        ),
        HarnessWire(
            "Black", "Ground", "Chassis ground. Connect to adapter's black or a clean bare-metal bolt."
        ),
        HarnessWire(
            "Blue", "Power antenna / antenna trigger",
            "Raises a motorized antenna. Usually unused on modern cars — do NOT confuse with remote turn-on."
        ),
        HarnessWire(
            "Blue / White stripe", "Amplifier remote turn-on (REM)",
            "THIS is what triggers your amp/LOC on. Run it to the amp's REM terminal. 12V only when radio is on."
        ),
        HarnessWire(
            "Orange", "Illumination / dimmer",
            "Dims the display with headlights. Optional."
        ),
        HarnessWire(
            "Orange / White stripe", "Illumination (constant)",
            "Alternate illumination feed. Optional."
        ),
        HarnessWire(
            "Pink", "Vehicle speed sense (VSS)",
            "Only on nav/advanced units. Leave unused otherwise."
        ),
        HarnessWire(
            "Light Green", "Parking brake",
            "For video units — enables video only in Park. Follow local laws."
        ),
        HarnessWire(
            "Brown", "Mute / phone mute trigger",
            "Optional; mutes audio on an external trigger."
        )
    )

    /** Speaker pairs: each has a + and a - (striped) wire. */
    val speakerWires: List<HarnessWire> = listOf(
        HarnessWire("White", "Left Front +", "To left front speaker positive."),
        HarnessWire("White / Black stripe", "Left Front −", "To left front speaker negative."),
        HarnessWire("Gray", "Right Front +", "To right front speaker positive."),
        HarnessWire("Gray / Black stripe", "Right Front −", "To right front speaker negative."),
        HarnessWire("Green", "Left Rear +", "To left rear speaker positive."),
        HarnessWire("Green / Black stripe", "Left Rear −", "To left rear speaker negative."),
        HarnessWire("Purple", "Right Rear +", "To right rear speaker positive."),
        HarnessWire("Purple / Black stripe", "Right Rear −", "To right rear speaker negative.")
    )

    val goldenRules: List<String> = listOf(
        "Use a vehicle-specific harness adapter + the matching antenna adapter so you never cut a single factory wire.",
        "Match color-to-function, not color-to-color blindly — crimp or solder + heat-shrink every joint.",
        "Blue/White = amp remote. Plain Blue = power antenna. Mixing these is the #1 'my amp won't turn on' mistake.",
        "If the factory plug has a data/CAN wire or needs an interface (steering controls, chimes, amp retention), use the correct harness interface for your car.",
        "Ground the radio to the same point as your amp if you can, to avoid ground-loop whine."
    )
}
