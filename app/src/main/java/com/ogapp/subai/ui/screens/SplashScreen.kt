package com.ogapp.subai.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ogapp.subai.ui.components.BrandWordmark
import com.ogapp.subai.ui.theme.AmpAmber
import com.ogapp.subai.ui.theme.BassCyan
import com.ogapp.subai.ui.theme.CarbonBlack
import com.ogapp.subai.ui.theme.SteelGray
import kotlinx.coroutines.delay

/**
 * Cinematic splash: a subwoofer cone that "pulses" like it's hitting bass,
 * concentric soundwave rings radiating out, with the gradient wordmark rising in.
 */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    val entrance = remember { Animatable(0f) }
    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "wave"
    )
    val cone by infinite.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(520, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "cone"
    )

    LaunchedEffect(Unit) {
        entrance.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        delay(1200)
        onDone()
    }

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(contentAlignment = Alignment.Center) {
                // radiating soundwave rings
                Canvas(Modifier.size(180.dp)) {
                    val maxR = size.minDimension / 2f
                    for (i in 0..2) {
                        val p = (pulse + i / 3f) % 1f
                        drawCircle(
                            color = BassCyan.copy(alpha = (1f - p) * 0.5f),
                            radius = maxR * (0.45f + p * 0.55f),
                            center = Offset(size.width / 2f, size.height / 2f),
                            style = Stroke(width = 3f)
                        )
                    }
                }
                // subwoofer cone
                Box(
                    Modifier
                        .size(96.dp)
                        .scale(cone * entrance.value),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val c = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(
                            brush = Brush.radialGradient(listOf(AmpAmber, Color(0xFF7A4E00))),
                            radius = size.minDimension / 2f, center = c
                        )
                        drawCircle(color = CarbonBlack, radius = size.minDimension / 3.4f, center = c)
                        drawCircle(
                            brush = Brush.radialGradient(listOf(BassCyan, Color(0xFF0A5247))),
                            radius = size.minDimension / 6f, center = c
                        )
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            BrandWordmark(modifier = Modifier.alpha(entrance.value), fontSize = 52)
            Spacer(Modifier.height(6.dp))
            androidx.compose.material3.Text(
                "WIRE IT RIGHT. HIT HARDER.",
                color = SteelGray,
                modifier = Modifier.alpha(entrance.value),
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
            )
        }
    }
}
