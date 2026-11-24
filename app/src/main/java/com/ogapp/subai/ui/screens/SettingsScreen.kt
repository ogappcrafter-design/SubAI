package com.ogapp.subai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.ogapp.subai.data.AiProvider
import com.ogapp.subai.data.SecurePrefs
import com.ogapp.subai.ui.components.GradientTitle
import com.ogapp.subai.ui.components.ScreenScaffold
import com.ogapp.subai.ui.components.StatusPill
import com.ogapp.subai.ui.components.SubCard
import com.ogapp.subai.ui.theme.AmpAmber
import com.ogapp.subai.ui.theme.BassCyan
import com.ogapp.subai.ui.theme.SteelGray
import com.ogapp.subai.ui.theme.WireGreenOk

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var provider by remember { mutableStateOf(AiProvider.fromId(SecurePrefs.getProvider(context))) }
    var key by remember { mutableStateOf(SecurePrefs.getApiKey(context)) }
    var reveal by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    ScreenScaffold(title = "Settings", onBack = onBack) {
        SubCard {
            GradientTitle("AI Diagnosis Key")
            Spacer(Modifier.height(8.dp))
            Text(
                "SubAI uses YOUR own AI key for photo diagnosis, so there's no subscription and you control the cost (usually pennies per scan). Everything else in the app works offline with no key.",
                style = MaterialTheme.typography.bodyMedium, color = SteelGray
            )
            Spacer(Modifier.height(14.dp))
            Text("Provider", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                AiProvider.entries.forEach { p ->
                    FilterChip(
                        selected = provider == p,
                        onClick = { provider = p; saved = false },
                        label = { Text(p.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmpAmber,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            Text(provider.keyHint, style = MaterialTheme.typography.bodyMedium, color = BassCyan)

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = key,
                onValueChange = { key = it; saved = false },
                label = { Text("API key") },
                singleLine = true,
                visualTransformation = if (reveal) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { reveal = !reveal }) {
                Text(if (reveal) "Hide" else "Show")
            }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    SecurePrefs.setProvider(context, provider.id)
                    SecurePrefs.setApiKey(context, key)
                    saved = true
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Save key", fontWeight = FontWeight.Bold) }

            if (saved) {
                Spacer(Modifier.height(10.dp))
                StatusPill("Saved & encrypted on this device", WireGreenOk)
            }
        }

        Spacer(Modifier.height(12.dp))
        SubCard {
            GradientTitle("About", fontSize = 18)
            Spacer(Modifier.height(8.dp))
            Text(
                "SubAI v1.0 — your car-audio co-pilot. Wiring math is deterministic and verified, not guessed. Always double-check against your amp's manual before powering on.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}
