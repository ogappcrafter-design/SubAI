package com.ogapp.subai.domain

/** The symptom the user is reporting, to focus the AI's attention. */
enum class Symptom(val label: String, val hint: String) {
    NO_POWER("Amp won't turn on", "no lights at all on the amp"),
    PROTECT("Amp in protect mode", "red/protect light on, output shut down"),
    NO_SOUND("Powers on, no sound", "amp has a normal light but subs are silent"),
    CUTS_OUT("Cuts out / shuts off", "plays then drops, especially on bass"),
    DISTORTED("Distorted / weak bass", "farty, rattly, or quiet output"),
    NOISE("Whine / engine noise", "alternator whine or buzzing through the subs"),
    SETUP_CHECK("Just check my wiring", "is this install done correctly?"),
    OTHER("Something else", "describe it below")
}

object DiagnosisPrompt {

    private const val SYSTEM = """You are a master car-audio installer with 20 years of hands-on experience with subwoofers, amplifiers, line output converters (LOCs), head units, big-3 upgrades and second batteries. You are looking at a photo of a user's real install and helping them fix it.

Rules:
- Be direct, practical and specific to WHAT YOU SEE in the photo. Point out the actual wires, terminals, fuses and components visible.
- SAFETY FIRST: flag anything dangerous (undersized/unfused power wire, loose high-current connections, impedance likely below the amp's rating, exposed copper, bad grounds).
- Never guess a fuse size or wire gauge as fact from a blurry photo — say what to verify with a meter.
- If you can't tell from the photo, say exactly what additional photo or measurement you need.
- Keep it usable by someone in their driveway. No fluff.

Answer in this structure:
1) WHAT I SEE — the components and wiring visible.
2) MOST LIKELY ISSUE — your top 1-3 suspects, most likely first.
3) CHECK THIS NOW — concrete steps/measurements, in order.
4) THE FIX — what to change.
5) SAFETY — anything that could damage gear or start a fire."""

    fun build(symptom: Symptom, userNotes: String, gearSummary: String?): String = buildString {
        append(SYSTEM)
        append("\n\n---\n")
        append("Reported symptom: ${symptom.label} (${symptom.hint}).")
        if (!gearSummary.isNullOrBlank()) {
            append("\nKnown gear/setup: $gearSummary")
        }
        if (userNotes.isNotBlank()) {
            append("\nUser's own notes: ${userNotes.trim()}")
        }
        append("\n\nDiagnose from the attached photo.")
    }
}
