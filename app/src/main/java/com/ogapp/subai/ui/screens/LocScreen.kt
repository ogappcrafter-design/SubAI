package com.ogapp.subai.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ogapp.subai.domain.engine.LocAdvisor
import com.ogapp.subai.ui.components.GradientTitle
import com.ogapp.subai.ui.components.ScreenScaffold
import com.ogapp.subai.ui.components.SubCard
import com.ogapp.subai.ui.theme.AmpAmber
import com.ogapp.subai.ui.theme.WireRed

@Composable
fun LocScreen(onBack: () -> Unit) {
    ScreenScaffold(title = "LOC Setup", onBack = onBack) {
        Spacer(Modifier.height(4.dp))
        Text(
            "A Line Output Converter turns factory speaker-level output into RCA signal for an aftermarket amp.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        NumberedCard("Where to tap the signal", LocAdvisor.tapPoints)
        Spacer(Modifier.height(12.dp))
        NumberedCard("Getting turn-on (REM) right", LocAdvisor.turnOnMethods)
        Spacer(Modifier.height(12.dp))
        NumberedCard("Wiring steps", LocAdvisor.wiringSteps)
        Spacer(Modifier.height(12.dp))
        SubCard {
            GradientTitle("Watch out for", fontSize = 18)
            Spacer(Modifier.height(8.dp))
            LocAdvisor.warnings.forEach {
                Row(Modifier.padding(vertical = 3.dp)) {
                    Text("⚠  ", color = WireRed)
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun NumberedCard(title: String, items: List<String>) {
    SubCard {
        GradientTitle(title, fontSize = 18)
        Spacer(Modifier.height(8.dp))
        items.forEachIndexed { i, s ->
            Row(Modifier.padding(vertical = 4.dp)) {
                Text("${i + 1}.  ", color = AmpAmber, fontWeight = FontWeight.Bold)
                Text(s, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
