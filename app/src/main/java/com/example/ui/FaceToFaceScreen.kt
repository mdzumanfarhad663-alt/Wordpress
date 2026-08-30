package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TranslationEntity
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
fun FaceToFaceScreen(
    uiState: TranslationUiState,
    translations: List<TranslationEntity>,
    isListening: Boolean,
    isSpeaking: Boolean,
    soundLevel: Float,
    liveCaption: String,
    onStartBanglaMic: () -> Unit,
    onStartEnglishMic: () -> Unit,
    onStopMic: () -> Unit,
    onReplayAudio: (text: String, lang: String, role: String) -> Unit
) {
    val lastYouTranslation = translations.firstOrNull { it.speakerRole == "YOU_BANGLA" }
    val lastPartnerTranslation = translations.firstOrNull { it.speakerRole == "PARTNER_ENGLISH" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .testTag("face_to_face_screen")
    ) {
        // TOP HALF: Rotated 180 degrees for Partner (English Speaker)
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .graphicsLayer(rotationZ = 180f)
                .testTag("partner_top_card"),
            shape = RoundedCornerShape(28.dp),
            color = FrostedGlassWhite,
            border = BorderStroke(1.dp, FrostedBorder),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(IndigoGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("EN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "English Speaker (Partner)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SlateDark
                        )
                    }

                    if (isListening && uiState.currentActiveSpeaker == "PARTNER_ENGLISH") {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = CoralSpeech.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, CoralSpeech.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "LISTENING...",
                                color = CoralSpeech,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Speech Display Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isListening && uiState.currentActiveSpeaker == "PARTNER_ENGLISH" && liveCaption.isNotBlank()) {
                        Text(
                            text = "\"$liveCaption\"",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateDark,
                            textAlign = TextAlign.Center
                        )
                    } else if (lastYouTranslation != null) {
                        // Display what the Bangla speaker just said translated to English
                        Text(
                            text = lastYouTranslation.translatedText,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "(Bangla: ${lastYouTranslation.sourceText})",
                            fontSize = 12.sp,
                            color = SlateMuted,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "Tap the button below to speak English. Instant translation will speak in Bangla.",
                            fontSize = 13.sp,
                            color = SlateMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Partner Mic Action Button
                Button(
                    onClick = {
                        if (isListening && uiState.currentActiveSpeaker == "PARTNER_ENGLISH") {
                            onStopMic()
                        } else {
                            onStartEnglishMic()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("partner_f2f_mic_btn"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening && uiState.currentActiveSpeaker == "PARTNER_ENGLISH") CoralSpeech else IndigoGlow
                    )
                ) {
                    Icon(
                        imageVector = if (isListening && uiState.currentActiveSpeaker == "PARTNER_ENGLISH") Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isListening && uiState.currentActiveSpeaker == "PARTNER_ENGLISH") "Stop Speaking" else "Tap to Speak English",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Center Divider with visual indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = FrostedBorderSlate
            )
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = FrostedGlassWhite,
                border = BorderStroke(1.dp, FrostedBorder),
                modifier = Modifier.padding(horizontal = 8.dp),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "Face-to-Face Dual View",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkyBlueDark,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // BOTTOM HALF: Upright for You (Bangla Speaker)
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("you_bottom_card"),
            shape = RoundedCornerShape(28.dp),
            color = SkyBlueUltraLight.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, Color(0xFFBAE6FD).copy(alpha = 0.7f)),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SkyBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("BN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "আপনি (বাংলা স্পিকার)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SlateDark
                        )
                    }

                    if (isListening && uiState.currentActiveSpeaker == "YOU_BANGLA") {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = CoralSpeech.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, CoralSpeech.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "শুনছি...",
                                color = CoralSpeech,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Speech Display Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isListening && uiState.currentActiveSpeaker == "YOU_BANGLA" && liveCaption.isNotBlank()) {
                        Text(
                            text = "\"$liveCaption\"",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateDark,
                            textAlign = TextAlign.Center
                        )
                    } else if (lastPartnerTranslation != null) {
                        // Display what the English speaker just said translated to Bangla
                        Text(
                            text = lastPartnerTranslation.translatedText,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "(English: ${lastPartnerTranslation.sourceText})",
                            fontSize = 12.sp,
                            color = SlateMuted,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "নিচের বাটনে চাপ দিয়ে বাংলায় কথা বলুন। এটি সাথে সাথে ইংরেজিতে রূপান্তর হবে।",
                            fontSize = 13.sp,
                            color = SlateMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // You (Bangla) Mic Action Button
                Button(
                    onClick = {
                        if (isListening && uiState.currentActiveSpeaker == "YOU_BANGLA") {
                            onStopMic()
                        } else {
                            onStartBanglaMic()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("you_f2f_mic_btn"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening && uiState.currentActiveSpeaker == "YOU_BANGLA") CoralSpeech else SkyBluePrimary
                    )
                ) {
                    Icon(
                        imageVector = if (isListening && uiState.currentActiveSpeaker == "YOU_BANGLA") Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isListening && uiState.currentActiveSpeaker == "YOU_BANGLA") "কথা থামান" else "বাংলায় কথা বলুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

