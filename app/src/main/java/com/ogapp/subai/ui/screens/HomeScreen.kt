package com.ogapp.subai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Troubleshoot
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ogapp.subai.ui.components.BrandWordmark
import com.ogapp.subai.ui.components.HubTile
import com.ogapp.subai.ui.nav.Routes

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().height(64.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BrandWordmark(fontSize = 38)
                IconButton(onClick = { onNavigate(Routes.SETTINGS) }) {
                    Icon(Icons.Filled.Settings, "Settings", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
            Text(
                "Your co-pilot for subs, amps, LOCs, batteries and head-unit wiring. Set it up right, or find out why it's not banging.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Tile("AI Photo Diagnosis", "Snap your setup — AI reads it and finds the problem", Icons.Filled.CameraAlt) { onNavigate(Routes.DIAGNOSE) }
            Gap()
            Tile("Troubleshoot It", "My system isn't working — walk me through it", Icons.Filled.Troubleshoot) { onNavigate(Routes.TROUBLESHOOT) }
            Gap()
            Tile("Impedance / Wiring", "Match your subs' voice coils to your amp safely", Icons.Filled.GraphicEq) { onNavigate(Routes.IMPEDANCE) }
            Gap()
            Tile("Power Wire & Fuse", "Correct gauge + fuse for your amp's current", Icons.Filled.ElectricBolt) { onNavigate(Routes.POWER) }
            Gap()
            Tile("LOC Setup", "Line output converter — factory radio to amp", Icons.Filled.Tune) { onNavigate(Routes.LOC) }
            Gap()
            Tile("Head Unit Harness", "Aftermarket radio wire colors & what they do", Icons.Filled.Radio) { onNavigate(Routes.HARNESS) }
            Gap()
            Tile("Charging & Big 3", "Second battery, alternator, voltage sag", Icons.Filled.BatteryChargingFull) { onNavigate(Routes.CHARGING) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable private fun Tile(t: String, s: String, i: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) =
    HubTile(title = t, subtitle = s, icon = i, onClick = onClick)

@Composable private fun Gap() = Spacer(Modifier.height(12.dp))
