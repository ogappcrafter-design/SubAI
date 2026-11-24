package com.ogapp.subai.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ogapp.subai.data.AiProvider
import com.ogapp.subai.data.ImageUtils
import com.ogapp.subai.data.SecurePrefs
import com.ogapp.subai.data.ai.AiDiagnostician
import com.ogapp.subai.domain.DiagnosisPrompt
import com.ogapp.subai.domain.Symptom
import com.ogapp.subai.ui.components.GradientTitle
import com.ogapp.subai.ui.components.ScreenScaffold
import com.ogapp.subai.ui.components.StatusPill
import com.ogapp.subai.ui.components.SubCard
import com.ogapp.subai.ui.theme.AmpAmber
import com.ogapp.subai.ui.theme.BassCyan
import com.ogapp.subai.ui.theme.SteelGray
import com.ogapp.subai.ui.theme.WireRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface DiagState {
    data object Idle : DiagState
    data object Loading : DiagState
    data class Success(val text: String) : DiagState
    data class Error(val message: String) : DiagState
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiagnoseScreen(onBack: () -> Unit, onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCapture by remember { mutableStateOf<Uri?>(null) }
    var symptom by remember { mutableStateOf(Symptom.NO_SOUND) }
    var notes by remember { mutableStateOf("") }
    var state by remember { mutableStateOf<DiagState>(DiagState.Idle) }

    val hasKey = remember { SecurePrefs.hasKey(context) }
    val provider = remember { AiProvider.fromId(SecurePrefs.getProvider(context)) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { imageUri = uri; state = DiagState.Idle }
    }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) { imageUri = pendingCapture; state = DiagState.Idle }
    }
    val requestCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = ImageUtils.newCaptureUri(context)
            pendingCapture = uri
            takePicture.launch(uri)
        }
    }

    ScreenScaffold(title = "AI Photo Diagnosis", onBack = onBack) {
        if (!hasKey) {
            SubCard {
                GradientTitle("Add your AI key first", fontSize = 18)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Photo diagnosis uses your own ${provider.label} key so you control the cost. Add it once in Settings and you're set.",
                    style = MaterialTheme.typography.bodyMedium, color = SteelGray
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Settings", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
            return@ScreenScaffold
        }

        // --- Photo ---
        SubCard {
            GradientTitle("1. Snap your setup", fontSize = 18)
            Spacer(Modifier.height(10.dp))
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Your setup",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(14.dp))
                )
                Spacer(Modifier.height(10.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { requestCamera.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoCamera, null); Spacer(Modifier.width(6.dp)); Text("Camera")
                }
                OutlinedButton(
                    onClick = { pickImage.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, null); Spacer(Modifier.width(6.dp)); Text("Gallery")
                }
            }
            Text(
                "Good light, get the amp terminals + wiring in frame. More in frame = better diagnosis.",
                style = MaterialTheme.typography.bodyMedium, color = SteelGray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // --- Symptom ---
        SubCard {
            GradientTitle("2. What's wrong?", fontSize = 18)
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Symptom.entries.forEach { s ->
                    androidx.compose.material3.FilterChip(
                        selected = symptom == s,
                        onClick = { symptom = s },
                        label = { Text(s.label) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmpAmber,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Extra details (gear, what you tried)") },
                keyboardOptions = KeyboardOptions.Default,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- Diagnose action ---
        Button(
            onClick = {
                val uri = imageUri ?: return@Button
                state = DiagState.Loading
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        val b64 = ImageUtils.loadScaledBase64(context, uri)
                            ?: return@withContext Result.failure(RuntimeException("Couldn't read that image. Try another."))
                        val prompt = DiagnosisPrompt.build(symptom, notes, null)
                        AiDiagnostician.diagnose(
                            provider = provider,
                            apiKey = SecurePrefs.getApiKey(context),
                            base64Jpeg = b64,
                            prompt = prompt
                        )
                    }
                    state = result.fold(
                        onSuccess = { DiagState.Success(it) },
                        onFailure = { DiagState.Error(it.message ?: "Something went wrong.") }
                    )
                }
            },
            enabled = imageUri != null && state != DiagState.Loading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AmpAmber)
        ) {
            Text(
                if (state == DiagState.Loading) "Diagnosing…" else "Diagnose it",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            is DiagState.Loading -> SubCard {
                Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AmpAmber)
                        Spacer(Modifier.height(12.dp))
                        Text("Reading your setup…", color = SteelGray)
                    }
                }
            }
            is DiagState.Success -> SubCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    GradientTitle("Diagnosis", fontSize = 18)
                    StatusPill(provider.label, BassCyan)
                }
                Spacer(Modifier.height(10.dp))
                Text(s.text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                Text(
                    "AI can be wrong — verify anything electrical with a meter before powering on.",
                    style = MaterialTheme.typography.bodyMedium, color = SteelGray
                )
            }
            is DiagState.Error -> SubCard {
                GradientTitle("Couldn't diagnose", fontSize = 18)
                Spacer(Modifier.height(8.dp))
                Text(s.message, style = MaterialTheme.typography.bodyMedium, color = WireRed)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Check your key in Settings, your connection, and that the provider you picked matches your key.",
                    style = MaterialTheme.typography.bodyMedium, color = SteelGray
                )
            }
            DiagState.Idle -> Unit
        }
        Spacer(Modifier.height(24.dp))
    }
}
