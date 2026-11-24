package com.ogapp.subai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ogapp.subai.domain.engine.Troubleshooter
import com.ogapp.subai.ui.components.GradientTitle
import com.ogapp.subai.ui.components.ScreenScaffold
import com.ogapp.subai.ui.components.StatusPill
import com.ogapp.subai.ui.components.SubCard
import com.ogapp.subai.ui.theme.BassCyan
import com.ogapp.subai.ui.theme.SteelGray
import com.ogapp.subai.ui.theme.WireGreenOk
import com.ogapp.subai.ui.theme.WireRed

@Composable
fun TroubleshootScreen(onBack: () -> Unit) {
    var currentId by remember { mutableStateOf(Troubleshooter.START) }
    val history = remember { mutableStateListOf<String>() }

    val step = Troubleshooter.step(currentId)
    val terminal = step.yesNext == null && step.noNext == null

    ScreenScaffold(title = "Troubleshoot", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            GradientTitle(if (terminal) "Diagnosis" else "Check This")
            StatusPill("Step ${history.size + 1}", BassCyan)
        }
        Spacer(Modifier.height(12.dp))

        if (!terminal) {
            SubCard {
                Text(
                    step.question,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                Text(step.help, style = MaterialTheme.typography.bodyLarge, color = SteelGray)
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { step.yesNext?.let { history.add(currentId); currentId = it } },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WireGreenOk)
                ) { Text("YES", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) }
                Button(
                    onClick = { step.noNext?.let { history.add(currentId); currentId = it } },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WireRed)
                ) { Text("NO", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) }
            }
        } else {
            SubCard {
                Text(
                    step.diagnosisIfTerminal ?: "No diagnosis available.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { currentId = Troubleshooter.START; history.clear() },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Start over", fontWeight = FontWeight.Bold) }
        }

        Spacer(Modifier.height(12.dp))
        if (history.isNotEmpty()) {
            OutlinedButton(
                onClick = { currentId = history.removeAt(history.lastIndex) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("← Previous question") }
        }
        Spacer(Modifier.height(24.dp))
    }
}
