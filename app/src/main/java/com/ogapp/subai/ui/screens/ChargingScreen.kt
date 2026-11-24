package com.ogapp.subai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ogapp.subai.domain.engine.ElectricalAdvisor
import com.ogapp.subai.ui.components.GradientTitle
import com.ogapp.subai.ui.components.ScreenScaffold
import com.ogapp.subai.ui.components.StatusPill
import com.ogapp.subai.ui.components.SubCard
import com.ogapp.subai.ui.theme.AmpAmber
import com.ogapp.subai.ui.theme.BassCyan
import com.ogapp.subai.ui.theme.WireGreenOk

@Composable
fun ChargingScreen(onBack: () -> Unit) {
    var watts by remember { mutableStateOf(1000) }
    val advice = ElectricalAdvisor.advise(watts)

    ScreenScaffold(title = "Charging & Big 3", onBack = onBack) {
        SubCard {
            GradientTitle("Total system power")
            Spacer(Modifier.height(10.dp))
            Chips(listOf(300, 600, 1000, 1500, 2000, 2500, 4000), watts) { watts = it }
            Spacer(Modifier.height(10.dp))
            Text(advice.summary, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(12.dp))
        SubCard {
            GradientTitle("What you need", fontSize = 18)
            Spacer(Modifier.height(8.dp))
            Flag("Big 3 upgrade", advice.needsBig3)
            Flag("Second battery (AGM)", advice.considerSecondBattery)
            Flag("High-output alternator", advice.considerHighOutputAlt)
            Spacer(Modifier.height(8.dp))
            advice.steps.forEach {
                Text("•  $it", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 3.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        SubCard {
            GradientTitle("The Big 3, step by step", fontSize = 18)
            Spacer(Modifier.height(8.dp))
            ElectricalAdvisor.big3Steps.forEach {
                Text(it, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 3.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Flag(label: String, on: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        StatusPill(if (on) "YES" else "not yet", if (on) WireGreenOk else BassCyan)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Chips(options: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { v ->
            FilterChip(
                selected = selected == v,
                onClick = { onSelect(v) },
                label = { Text("${v}W", fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AmpAmber,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
