package com.ogapp.subai.domain.engine

import com.ogapp.subai.domain.model.TroubleStep

/**
 * Offline, deterministic troubleshooting decision tree for "my subs aren't
 * working." Built on the standard isolation method: confirm power/ground/remote,
 * read the protect light, then strip the amp to bare power and add circuits back
 * one at a time. Works with zero network calls; the AI photo path layers on top.
 */
object Troubleshooter {

    const val START = "start"

    private val steps: Map<String, TroubleStep> = buildList {
        add(
            TroubleStep(
                id = "start",
                question = "Does the amplifier show ANY light when the stereo is on?",
                help = "Look at the amp's status LED. No light at all points to a power, ground, or remote problem.",
                yesNext = "light_type",
                noNext = "no_power"
            )
        )
        // ---- No power branch ----
        add(
            TroubleStep(
                id = "no_power",
                question = "With a multimeter, do you read ~12V at the amp's POWER (+12V) terminal to ground?",
                help = "Red probe on the amp power terminal, black on amp ground. Expect 12–14.4V. Zero means a blown main fuse or bad power run.",
                yesNext = "no_power_rem",
                noNext = "no_power_fuse"
            )
        )
        add(
            TroubleStep(
                id = "no_power_fuse",
                question = "Is the main fuse near the battery (and the amp's on-board fuse) intact?",
                help = "Check the inline ANL/AGU fuse within ~18in of the battery AND the fuse on the amp itself.",
                yesNext = null,
                noNext = null,
                diagnosisIfTerminal = "No 12V at the amp. Replace any blown fuse. If it blows again, you have a short in the power wire (pinched/grounded) — inspect where it passes through the firewall/metal. If the fuse is fine but there's still no 12V, the power wire or its battery connection is bad."
            )
        )
        add(
            TroubleStep(
                id = "no_power_rem",
                question = "Do you read ~12V at the amp's REMOTE (REM) terminal with the stereo ON?",
                help = "This is the trigger wire. Aftermarket radios use the Blue/White wire; a LOC may generate its own remote.",
                yesNext = "no_power_ground",
                noNext = null,
                diagnosisIfTerminal = "Power is present but REMOTE is dead — that's why the amp stays off. Fix the turn-on: aftermarket radio → run Blue/White to REM; factory radio → use a signal-sensing LOC or a switched/ACC 12V source. Never use a constant-hot source (battery drain)."
            )
        )
        add(
            TroubleStep(
                id = "no_power_ground",
                question = "Is the ground wire bolted to clean, bare metal (paint/rust scraped away)?",
                help = "A bad ground is one of the most common faults. Short, same-gauge as power, to sanded bare chassis metal.",
                yesNext = null,
                noNext = null,
                diagnosisIfTerminal = "You have 12V and remote but no light — redo the ground to bare metal with a solid same-gauge wire under ~18in. If the ground is genuinely good and it still won't light, the amp's internal power supply has likely failed."
            )
        )
        // ---- Has light branch ----
        add(
            TroubleStep(
                id = "light_type",
                question = "Is the light the PROTECT indicator (usually red), not the normal power light (blue/green)?",
                help = "Check the amp's labels. Protect mode means the amp powered up but shut its output down to save itself.",
                yesNext = "protect_isolate",
                noNext = "has_power_no_sound"
            )
        )
        // ---- Protect branch ----
        add(
            TroubleStep(
                id = "protect_isolate",
                question = "Disconnect the SUB wires AND the RCA inputs. Power on. Does it STILL show protect?",
                help = "This strips the amp to just power, ground, and remote. If protect clears, the fault is downstream (subs/wiring/impedance). If it stays, the fault is the amp or its power/ground.",
                yesNext = "protect_still",
                noNext = "protect_cleared"
            )
        )
        add(
            TroubleStep(
                id = "protect_still",
                question = "Is the amp touching bare metal anywhere, or is its ground/power loose or undersized?",
                help = "With nothing connected, protect = internal fault, a grounded chassis, or a bad power/ground feed. Check the amp isn't screwed through to metal.",
                yesNext = null,
                noNext = null,
                diagnosisIfTerminal = "Protect with everything disconnected means the amp itself or its power/ground. Re-check for a short to the amp chassis, tighten and up-size power/ground, verify 12V+remote under load. If all are good, the amp has an internal fault and needs service."
            )
        )
        add(
            TroubleStep(
                id = "protect_cleared",
                question = "Measure the total sub load with the leads OFF the amp. Is it AT or ABOVE the amp's minimum ohm rating?",
                help = "Protect cleared when you removed the load → likely an impedance or wiring fault. Use the SubAI impedance calculator to confirm your wiring lands at a safe load.",
                yesNext = "protect_short",
                noNext = null,
                diagnosisIfTerminal = "Your sub load is BELOW the amp's minimum impedance — that overload trips protect. Rewire the voice coils/subs to a safe load (use the impedance calculator). Example: don't feed a 1-ohm-min amp a 0.5-ohm load."
            )
        )
        add(
            TroubleStep(
                id = "protect_short",
                question = "With subs reconnected, is any speaker wire strand touching the chassis or the other terminal?",
                help = "A single stray strand shorting + to − or to ground will trip protect the instant it sees load.",
                yesNext = null,
                noNext = null,
                diagnosisIfTerminal = "Impedance is fine, so hunt for a short: a frayed speaker wire strand touching ground or the opposite terminal, or a blown/shorted voice coil. Re-terminate the sub wiring cleanly; test the sub's coils with a meter (near-0 or infinite ohms = blown coil)."
            )
        )
        // ---- Power OK but no sound branch ----
        add(
            TroubleStep(
                id = "has_power_no_sound",
                question = "On the head unit, is the subwoofer output ENABLED and the sub level / fader not zeroed or faded away?",
                help = "Many 'no sub' cases are just the sub output turned off, sub level at minimum, or fader pulled to the front.",
                yesNext = "no_sound_rca",
                noNext = null,
                diagnosisIfTerminal = "Enable the subwoofer output, raise the sub level, and center the fader. Check any bass-knob/remote-level control isn't at zero. Then re-test."
            )
        )
        add(
            TroubleStep(
                id = "no_sound_rca",
                question = "Are the RCA cables (or LOC signal) fully seated at BOTH the source and the amp input?",
                help = "A half-seated RCA or a LOC with no signal input = no music reaches the amp even though it's powered.",
                yesNext = "no_sound_swap",
                noNext = null,
                diagnosisIfTerminal = "Reseat the RCAs at both ends. If using a LOC, confirm it actually has speaker-level signal in and RCA out — verify the tapped speaker pair is full-range and live."
            )
        )
        add(
            TroubleStep(
                id = "no_sound_swap",
                question = "Known-good test: touch a 9V battery to the sub terminals — does the cone jump?",
                help = "This proves the sub + its wiring are alive. A tiny pop/jump = good. Nothing = open circuit or blown coil.",
                yesNext = "no_sound_gain",
                noNext = null,
                diagnosisIfTerminal = "No cone movement from a 9V test means the sub or its wiring is open/blown. Check continuity of each voice coil and the wire run from the amp to the sub."
            )
        )
        add(
            TroubleStep(
                id = "no_sound_gain",
                question = "Is the amp gain turned up at least partway (not fully down)?",
                help = "Gain fully down = no output. Set it properly: source ~75%, raise gain to just below distortion. Don't max it (that's clipping, which blows subs).",
                yesNext = null,
                noNext = null,
                diagnosisIfTerminal = "Raise the gain from zero using the 75%-volume method. If gain is already up and everything above checked out, suspect the amp's output stage or a low-pass filter set so low nothing passes — verify the crossover (LPF) is set around 70–90 Hz for subs, not at its lowest."
            )
        )
    }.associateBy { it.id }

    fun step(id: String): TroubleStep =
        steps[id] ?: error("Unknown troubleshooting step: $id")

    fun allSteps(): Collection<TroubleStep> = steps.values

    /** Validate the tree: every non-terminal branch points at a real step. */
    fun validateGraph(): List<String> {
        val problems = mutableListOf<String>()
        for (s in steps.values) {
            val terminal = s.yesNext == null && s.noNext == null
            if (terminal && s.diagnosisIfTerminal == null)
                problems += "${s.id}: terminal step with no diagnosis"
            s.yesNext?.let { if (it !in steps) problems += "${s.id}: yesNext '$it' missing" }
            s.noNext?.let { if (it !in steps) problems += "${s.id}: noNext '$it' missing" }
        }
        return problems
    }
}
