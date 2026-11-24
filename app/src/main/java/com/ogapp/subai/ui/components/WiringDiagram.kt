package com.ogapp.subai.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ogapp.subai.domain.model.Connection
import com.ogapp.subai.domain.model.CoilType
import com.ogapp.subai.domain.model.WiringOption
import com.ogapp.subai.ui.theme.BassCyan
import com.ogapp.subai.ui.theme.CarbonSurfaceHi
import com.ogapp.subai.ui.theme.SteelGray
import com.ogapp.subai.ui.theme.WireRed

private val POS = WireRed            // positive (+) wire
private val NEG = Color(0xFF9AA4B2)  // negative (-) wire

/**
 * Animated schematic of a [WiringOption]: the amp's +/- output, each subwoofer,
 * and the series/parallel links between them, with marching current-flow dashes.
 * Adapts to 1-4 subs and SVC/DVC. Positive wires are red, negative are steel.
 */
@Composable
fun WiringDiagram(
    option: WiringOption,
    count: Int,
    coilType: CoilType,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    val transition = rememberInfiniteTransition(label = "flow")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(tween(650, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    Canvas(
        modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        drawSchematic(option, count, coilType, phase, measurer)
    }
}

private fun DrawScope.drawSchematic(
    option: WiringOption,
    count: Int,
    coilType: CoilType,
    phase: Float,
    measurer: TextMeasurer
) {
    val w = size.width
    val h = size.height

    // --- Amp block (top center) ---
    val ampW = (w * 0.44f).coerceAtLeast(150f)
    val ampH = 44f
    val ampLeft = (w - ampW) / 2f
    val ampBottom = ampH + 6f
    drawRoundRect(
        color = CarbonSurfaceHi,
        topLeft = Offset(ampLeft, 6f),
        size = androidx.compose.ui.geometry.Size(ampW, ampH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
    )
    label(measurer, "AMP", ampLeft + ampW / 2f, 6f + ampH / 2f, BassCyan, 15, center = true)

    val ampPos = Offset(ampLeft + ampW * 0.30f, ampBottom)
    val ampNeg = Offset(ampLeft + ampW * 0.70f, ampBottom)
    terminal(ampPos, POS); terminal(ampNeg, NEG)
    label(measurer, "+", ampPos.x - 4f, ampBottom - 26f, POS, 15)
    label(measurer, "–", ampNeg.x - 4f, ampBottom - 26f, NEG, 15)

    // --- Sub boxes (bottom row) ---
    val n = count.coerceIn(1, 4)
    val gap = 14f
    val subW = ((w - gap * (n + 1)) / n).coerceAtLeast(40f)
    val subH = 56f
    val subTop = h - subH - 22f

    data class Sub(val pos: Offset, val neg: Offset, val cx: Float)
    val subs = ArrayList<Sub>(n)
    for (i in 0 until n) {
        val left = gap + i * (subW + gap)
        drawRoundRect(
            color = CarbonSurfaceHi,
            topLeft = Offset(left, subTop),
            size = androidx.compose.ui.geometry.Size(subW, subH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
        )
        // speaker cone glyph
        val cx = left + subW / 2f
        val cy = subTop + subH / 2f + 4f
        drawCircle(SteelGray, radius = subH * 0.24f, center = Offset(cx, cy), style = Stroke(2f))
        drawCircle(BassCyan, radius = subH * 0.10f, center = Offset(cx, cy))

        val p = Offset(left + subW * 0.28f, subTop)
        val ng = Offset(left + subW * 0.72f, subTop)
        terminal(p, POS); terminal(ng, NEG)
        subs += Sub(p, ng, cx)
        label(measurer, "${i + 1}", cx - 3f, subTop + subH - 12f, SteelGray, 12, center = true)
    }

    // --- Wiring between amp and subs ---
    val sub = option.subConnection
    when {
        n == 1 || sub == Connection.PARALLEL -> {
            // parallel rails
            val posRail = subTop - 30f
            val negRail = subTop - 14f
            // amp down to rails
            wire(ampPos, Offset(ampPos.x, posRail), POS, phase)
            wire(ampNeg, Offset(ampNeg.x, negRail), NEG, phase)
            val xs = subs.map { it.pos.x } + ampPos.x
            val xe = subs.map { it.neg.x } + ampNeg.x
            wire(Offset(xs.min(), posRail), Offset(xs.max(), posRail), POS, phase)
            wire(Offset(xe.min(), negRail), Offset(xe.max(), negRail), NEG, phase)
            subs.forEach {
                wire(Offset(it.pos.x, posRail), it.pos, POS, phase)
                wire(Offset(it.neg.x, negRail), it.neg, NEG, phase)
            }
        }
        sub == Connection.SERIES -> {
            // amp+ -> sub1+ ; subK- -> sub(K+1)+ ; subN- -> amp-
            routeTop(ampPos, subs.first().pos, POS, phase, subTop - 30f)
            for (k in 0 until subs.size - 1) {
                routeTop(subs[k].neg, subs[k + 1].pos, POS, phase, subTop - 16f - k * 10f)
            }
            routeTop(subs.last().neg, ampNeg, NEG, phase, subTop - 30f)
        }
        else -> { // SERIES_PARALLEL, n == 4: pairs (0,1) and (2,3) in series, paired in parallel
            val posRail = subTop - 30f
            val negRail = subTop - 14f
            wire(ampPos, Offset(ampPos.x, posRail), POS, phase)
            wire(ampNeg, Offset(ampNeg.x, negRail), NEG, phase)
            val pairStartPos = listOf(subs[0].pos, subs[2].pos)
            val pairEndNeg = listOf(subs[1].neg, subs[3].neg)
            val railXsP = pairStartPos.map { it.x } + ampPos.x
            val railXsN = pairEndNeg.map { it.x } + ampNeg.x
            wire(Offset(railXsP.min(), posRail), Offset(railXsP.max(), posRail), POS, phase)
            wire(Offset(railXsN.min(), negRail), Offset(railXsN.max(), negRail), NEG, phase)
            pairStartPos.forEach { wire(Offset(it.x, posRail), it, POS, phase) }
            pairEndNeg.forEach { wire(Offset(it.x, negRail), it, NEG, phase) }
            routeTop(subs[0].neg, subs[1].pos, POS, phase, subTop - 16f)
            routeTop(subs[2].neg, subs[3].pos, POS, phase, subTop - 16f)
        }
    }

    // total load label, bottom-right
    label(measurer, option.title, w - 70f, h - 14f, BassCyan, 16)
}

/** Draw an elbowed wire from a to b routed up to railY then across. */
private fun DrawScope.routeTop(a: Offset, b: Offset, color: Color, phase: Float, railY: Float) {
    wire(a, Offset(a.x, railY), color, phase)
    wire(Offset(a.x, railY), Offset(b.x, railY), color, phase)
    wire(Offset(b.x, railY), b, color, phase)
}

private fun DrawScope.wire(a: Offset, b: Offset, color: Color, phase: Float) {
    // static base
    drawLine(color.copy(alpha = 0.35f), a, b, strokeWidth = 5f, cap = StrokeCap.Round)
    // animated current flow
    drawLine(
        color = color, start = a, end = b, strokeWidth = 3f, cap = StrokeCap.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), -phase)
    )
}

private fun DrawScope.terminal(at: Offset, color: Color) {
    drawCircle(color, radius = 5f, center = at)
    drawCircle(Color.White.copy(alpha = 0.6f), radius = 2f, center = at)
}

private fun DrawScope.label(
    m: TextMeasurer, text: String, x: Float, y: Float, color: Color, size: Int, center: Boolean = false
) {
    val style = TextStyle(color = color, fontSize = size.sp)
    val layout = m.measure(text, style)
    val ox = if (center) x - layout.size.width / 2f else x
    val oy = y - layout.size.height / 2f
    drawText(layout, color = color, topLeft = Offset(ox, oy))
}
