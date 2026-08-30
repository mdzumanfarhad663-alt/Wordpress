package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.AudioRoutingMode
import com.example.ui.theme.CoralSpeech
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.FrostedBorder
import com.example.ui.theme.FrostedBorderSlate
import com.example.ui.theme.FrostedGlassWhite
import com.example.ui.theme.IndigoGlow
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueUltraLight
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMedium
import com.example.ui.theme.SlateMuted

@Composable
fun SettingsDialog(
    uiState: TranslationUiState,
    routingMode: AudioRoutingMode,
    onRoutingModeChange: (AudioRoutingMode) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onSpeechPitchChange: (Float) -> Unit,
    onAutoPlayTtsChange: (Boolean) -> Unit,
    onPreferOfflineChange: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("settings_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = FrostedGlassWhite,
            border = BorderStroke(1.dp, FrostedBorder),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SkyBlueUltraLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "Settings",
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Translator Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = "Audio routing & AI speech",
                            fontSize = 12.sp,
                            color = SlateMuted
                        )
                    }
                }

                // Audio Routing Mode Selection
                Text(
                    text = "Bluetooth Earbud Audio Routing",
                    style = MaterialTheme.typography.labelLarge,
                    color = SkyBlueDark,
                    fontWeight = FontWeight.Bold
                )

                AudioRoutingMode.values().forEach { mode ->
                    val isSelected = routingMode == mode
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onRoutingModeChange(mode) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) SkyBlueUltraLight.copy(alpha = 0.8f) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (isSelected) SkyBluePrimary.copy(alpha = 0.6f) else FrostedBorderSlate)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (mode == AudioRoutingMode.SPLIT_EARBUD_PHONE) Icons.Default.Headphones else Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = if (isSelected) SkyBluePrimary else SlateMedium,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = SlateDark
                                )
                                Text(
                                    text = mode.subtitle,
                                    fontSize = 11.sp,
                                    color = SlateMuted
                                )
                            }
                        }
                    }
                }

                // Auto-Play Voice Translation Toggle
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, FrostedBorderSlate)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = EmeraldActive, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Auto-Speak Translations", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = SlateDark)
                                Text("Speak out translations instantly via TTS", fontSize = 11.sp, color = SlateMuted)
                            }
                        }
                        Switch(
                            checked = uiState.isTtsAutoPlayEnabled,
                            onCheckedChange = onAutoPlayTtsChange,
                            modifier = Modifier.testTag("auto_speak_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SkyBluePrimary
                            )
                        )
                    }
                }

                // Offline-First Mode Toggle
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, FrostedBorderSlate)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Prefer Offline Engine", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = SlateDark)
                                Text("Use instant zero-data local neural dictionary", fontSize = 11.sp, color = SlateMuted)
                            }
                        }
                        Switch(
                            checked = uiState.preferOffline,
                            onCheckedChange = onPreferOfflineChange,
                            modifier = Modifier.testTag("offline_engine_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SkyBluePrimary
                            )
                        )
                    }
                }

                // Voice Speech Rate
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Speech Speed", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = SlateDark)
                        }
                        Text(String.format("%.2fx", uiState.speechRate), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SkyBlueDark)
                    }
                    Slider(
                        value = uiState.speechRate,
                        onValueChange = onSpeechRateChange,
                        valueRange = 0.7f..1.4f,
                        steps = 7,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = SkyBluePrimary,
                            activeTrackColor = SkyBluePrimary
                        )
                    )
                }

                // Voice Pitch
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Audiotrack, contentDescription = null, tint = EmeraldActive, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Voice Pitch", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = SlateDark)
                        }
                        Text(String.format("%.2fx", uiState.speechPitch), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldActive)
                    }
                    Slider(
                        value = uiState.speechPitch,
                        onValueChange = onSpeechPitchChange,
                        valueRange = 0.7f..1.3f,
                        steps = 6,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldActive,
                            activeTrackColor = EmeraldActive
                        )
                    )
                }

                // Clear History Button
                OutlinedButton(
                    onClick = { showClearConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clear_history_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralSpeech),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CoralSpeech.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All Conversation History", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("close_settings_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Conversations?", fontWeight = FontWeight.Bold, color = SlateDark) },
            text = { Text("This will remove all saved translations from your local history. Starred items will also be removed.", color = SlateMuted) },
            containerColor = FrostedGlassWhite,
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralSpeech),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear Everything", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = SlateMedium)
                }
            }
        )
    }
}

