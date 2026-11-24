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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ogapp.subai.domain.engine.ImpedanceCalculator
import com.ogapp.subai.domain.model.CoilType
import com.ogapp.subai.domain.model.MatchVerdict
import com.ogapp.subai.domain.model.SubStage
import com.ogapp.subai.ui.components.GradientTitle
import com.ogapp.subai.ui.components.InfoRow
import com.ogapp.subai.ui.components.ScreenScaffold
import com.ogapp.subai.ui.components.StatusPill
import com.ogapp.subai.ui.components.SubCard
import com.ogapp.subai.ui.theme.AmpAmber
import com.ogapp.subai.ui.theme.BassCyan
import com.ogapp.subai.ui.theme.SteelGray
import com.ogapp.subai.ui.theme.WireGreenOk
import com.ogapp.subai.ui.theme.WireRed

@Composable
fun ImpedanceScreen(onBack: () -> Unit) {
    var count by remember { mutableStateOf(1) }
    var coilType by remember { mutableStateOf(CoilType.DVC) }
    var coilOhms by remember { mutableStateOf(2.0) }
    var ampMin by remember { mutableStateOf(1.0) }

    ScreenScaffold(title = "Impedance & Wiring", onBack = onBack) {
        SubCard {
            GradientTitle("Your Subs")
            Spacer(Modifier.height(12.dp))

            LabeledChoices("How many subs?", listOf(1, 2, 3, 4), count, { "$it" }) { count = it }
            LabeledChoices("Voice coil type", CoilType.entries, coilType, { it.name }) { coilType = it }
            LabeledChoices(
                "Ohms per coil",
                listOf(1.0, 2.0, 4.0),
                coilOhms,
                { ohmLabel(it) }
            ) { coilOhms = it }

            Spacer(Modifier.height(4.dp))
            GradientTitle("Your Amp")
            Spacer(Modifier.height(12.dp))
            LabeledChoices(
                "Amp's minimum stable load",
                listOf(0.5, 1.0, 2.0, 4.0),
                ampMin,
                { ohmLabel(it) }
            ) { ampMin = it }
            Text(
                "Find this on the amp or its manual (e.g. \"1-ohm stable mono\"). Going below it cooks the amp.",
                style = MaterialTheme.typography.bodyMedium,
                color = SteelGray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        val stage = runCatching { SubStage(count, coilType, coilOhms) }.getOrNull()
        val matched = stage?.let { ImpedanceCalculator.matchToAmp(it, ampMin) }.orEmpty()

        GradientTitle("Wiring Options", Modifier.padding(start = 4.dp, bottom = 8.dp))

        matched.forEach { m ->
            val (color, label) = when (m.verdict) {
                MatchVerdict.OPTIMAL -> WireGreenOk to "BEST"
                MatchVerdict.SAFE -> BassCyan to "SAFE"
                MatchVerdict.RISKY_LOW -> WireRed to "DANGER"
                MatchVerdict.TOO_HIGH -> AmpAmber to "WEAK"
            }
            SubCard(Modifier.padding(bottom = 12.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        ImpedanceCalculator.formatOhms(m.option.totalOhms),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatusPill(label, color)
                }
                Text(
                    m.option.detail,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                )
                Text(m.note, style = MaterialTheme.typography.bodyMedium, color = color)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

private fun ohmLabel(v: Double): String =
    if (v == Math.floor(v)) "${v.toInt()} Ω" else "$v Ω"

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> LabeledChoices(
    label: String,
    options: List<T>,
    selected: T,
    render: (T) -> String,
    onSelect: (T) -> Unit
) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { opt ->
                FilterChip(
                    selected = selected == opt,
                    onClick = { onSelect(opt) },
                    label = { Text(render(opt)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmpAmber,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}
