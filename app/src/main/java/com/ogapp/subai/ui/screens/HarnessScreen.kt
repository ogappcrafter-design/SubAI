package com.ogapp.subai.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ogapp.subai.domain.engine.HeadUnitHarness
import com.ogapp.subai.domain.model.HarnessWire
import com.ogapp.subai.ui.components.GradientTitle
import com.ogapp.subai.ui.components.ScreenScaffold
import com.ogapp.subai.ui.components.SubCard
import com.ogapp.subai.ui.theme.AmpAmber
import com.ogapp.subai.ui.theme.BassCyan
import com.ogapp.subai.ui.theme.SteelGray

@Composable
fun HarnessScreen(onBack: () -> Unit) {
    ScreenScaffold(title = "Head Unit Harness", onBack = onBack) {
        Spacer(Modifier.height(4.dp))
        Text(
            "Standard aftermarket radio wire colors. Use a vehicle-specific harness adapter so you never cut factory wires.",
            style = MaterialTheme.typography.bodyMedium, color = SteelGray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        SubCard {
            GradientTitle("Power & Control", fontSize = 18)
            Spacer(Modifier.height(8.dp))
            HeadUnitHarness.powerWires.forEach { WireRow(it) }
        }
        Spacer(Modifier.height(12.dp))
        SubCard {
            GradientTitle("Speakers", fontSize = 18)
            Spacer(Modifier.height(8.dp))
            HeadUnitHarness.speakerWires.forEach { WireRow(it) }
        }
        Spacer(Modifier.height(12.dp))
        SubCard {
            GradientTitle("Golden Rules", fontSize = 18)
            Spacer(Modifier.height(8.dp))
            HeadUnitHarness.goldenRules.forEach {
                Text("•  $it", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 3.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun WireRow(w: HarnessWire) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(w.color, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = AmpAmber)
        Text(w.meaning, style = MaterialTheme.typography.bodyMedium, color = BassCyan)
        Text(w.hookUp, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
