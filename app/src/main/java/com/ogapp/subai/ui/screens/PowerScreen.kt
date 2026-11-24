package com.ogapp.subai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ogapp.subai.domain.engine.PowerWiringCalculator
import com.ogapp.subai.domain.model.AmpClass
import com.ogapp.subai.ui.components.GradientTitle
import com.ogapp.subai.ui.components.InfoRow
import com.ogapp.subai.ui.components.ScreenScaffold
import com.ogapp.subai.ui.components.StatusPill
import com.ogapp.subai.ui.components.SubCard
import com.ogapp.subai.ui.theme.AmpAmber
import com.ogapp.subai.ui.theme.BassCyan
import com.ogapp.subai.ui.theme.SteelGray

@Composable
fun PowerScreen(onBack: () -> Unit) {
    var useFuseSum by remember { mutableStateOf(false) }
    var rmsWatts by remember { mutableStateOf(1000) }
    var ampClass by remember { mutableStateOf(AmpClass.CLASS_D) }
    var fuseSum by remember { mutableStateOf(100) }
    var runFt by remember { mutableStateOf(17.0) }
    var isCca by remember { mutableStateOf(false) }

    ScreenScaffold(title = "Power Wire & Fuse", onBack = onBack) {
        SubCard {
            GradientTitle("How to size it")
            Spacer(Modifier.height(10.dp))
            Chips(
                listOf(false to "By total RMS watts", true to "By amp fuse rating"),
                useFuseSum
            ) { useFuseSum = it }

            Spacer(Modifier.height(8.dp))
            if (useFuseSum) {
                Label("Sum of all amps' on-board fuses (amps)")
                Chips(listOf(30, 40, 60, 80, 100, 150, 200, 300).map { it to "${it}A" }, fuseSum) { fuseSum = it }
                Text(
                    "Add up the fuse values printed on your amp(s). This is the most accurate basis.",
                    style = MaterialTheme.typography.bodyMedium, color = SteelGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Label("Total RMS power (all amps)")
                Chips(listOf(250, 500, 750, 1000, 1500, 2000, 3000).map { it to "${it}W" }, rmsWatts) { rmsWatts = it }
                Spacer(Modifier.height(6.dp))
                Label("Amp class")
                Chips(AmpClass.entries.map { it to it.label }, ampClass) { ampClass = it }
            }

            Spacer(Modifier.height(10.dp))
            Label("Power run length (battery → amp)")
            Chips(listOf(3.0, 6.0, 10.0, 13.0, 17.0, 20.0).map { it to "${it.toInt()} ft" }, runFt) { runFt = it }

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Using CCA wire?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text("Copper-clad aluminium (cheaper, thinner conductor)", style = MaterialTheme.typography.bodyMedium, color = SteelGray)
                }
                Switch(checked = isCca, onCheckedChange = { isCca = it })
            }
        }

        Spacer(Modifier.height(16.dp))

        val current = if (useFuseSum) fuseSum else PowerWiringCalculator.currentFromRms(rmsWatts, ampClass)
        val rec = PowerWiringCalculator.recommend(current, runFt, isCca)

        SubCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GradientTitle("Recommendation")
                StatusPill(rec.gauge, AmpAmber)
            }
            Spacer(Modifier.height(10.dp))
            InfoRow("Design current", "${rec.designCurrentAmps} A")
            InfoRow("Power wire", rec.gauge, BassCyan)
            InfoRow("Ground wire", rec.groundGauge, BassCyan)
            InfoRow("Main fuse (at battery)", "${rec.mainFuseAmps} A", AmpAmber)
            InfoRow("Voltage drop on run", "${rec.voltageDropVolts} V")
        }

        Spacer(Modifier.height(12.dp))
        SubCard {
            GradientTitle("Do this right", fontSize = 18)
            Spacer(Modifier.height(8.dp))
            rec.notes.forEach { note ->
                Row(Modifier.padding(vertical = 4.dp)) {
                    Text("•  ", color = AmpAmber, fontWeight = FontWeight.Bold)
                    Text(note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable private fun Label(t: String) =
    Text(t, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> Chips(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AmpAmber,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
