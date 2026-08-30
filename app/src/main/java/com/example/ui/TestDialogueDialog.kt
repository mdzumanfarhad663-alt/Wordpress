package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CoralSpeech
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.EmeraldPill
import com.example.ui.theme.EmeraldPillBg
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TestDialogueDialog(
    testState: TestDialogueState,
    onStartTestMic: () -> Unit,
    onStopTestMic: () -> Unit,
    onRunTestWithText: (String) -> Unit,
    onReplayUserEnglish: () -> Unit,
    onReplayPartnerEnglish: () -> Unit,
    onReplayBanglaEarbud: () -> Unit,
    onDismiss: () -> Unit
) {
    var customBanglaInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val quickTestPhrases = listOf(
        "আপনি কেমন আছেন?",
        "এই ট্রেনের ভাড়া কত?",
        "আমার একটি সাহায্য দরকার",
        "আমি বিমানবন্দরে যেতে চাই",
        "খাবারের মেনু দেখতে পারি?",
        "এটার দাম কত?"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("test_dialogue_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = FrostedGlassWhite,
            border = BorderStroke(1.dp, FrostedBorder),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
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
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(SkyBluePrimary, IndigoGlow)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "🧪 Interactive Test Option",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkyBlueDark,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Talk BN ➜ Reply EN ➜ Hear BN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SkyBlueUltraLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SlateDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Explanatory Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SkyBlueUltraLight.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color(0xFFBAE6FD).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Speak or select a Bangla phrase. The app translates to English for your partner, generates their English reply, and translates it back into Bangla so you hear it inside your earbud!",
                            fontSize = 12.sp,
                            color = SlateDark,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Step Flow Visualization Cards
                if (testState.currentStep > 0) {
                    Text(
                        text = "Live Conversation Flow",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SkyBlueDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 1: Your Spoken Bangla
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = FrostedGlassWhite,
                        border = BorderStroke(
                            1.dp,
                            if (testState.currentStep == 1) SkyBluePrimary else FrostedBorderSlate
                        ),
                        shadowElevation = if (testState.currentStep == 1) 4.dp else 1.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(SkyBluePrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("1", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("You Spoke (Bangla)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SkyBlueDark)
                                }
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = SkyBlueUltraLight,
                                    border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                                ) {
                                    Text("MIC / INPUT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SkyBlueDark, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = testState.spokenBanglaText.ifBlank { "Speaking..." },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step 2: Translated to English (Speaker)
                    if (testState.currentStep >= 2) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = FrostedGlassWhite,
                            border = BorderStroke(
                                1.dp,
                                if (testState.currentStep == 2) IndigoGlow else FrostedBorderSlate
                            ),
                            shadowElevation = if (testState.currentStep == 2) 4.dp else 1.dp
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(IndigoGlow),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("2", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Translated for Partner (English)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IndigoGlow)
                                    }
                                    IconButton(
                                        onClick = onReplayUserEnglish,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = "Play", tint = IndigoGlow, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = testState.translatedEnglishText.ifBlank { "Translating..." },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SlateDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("📢 Spoken on Phone Speakerphone", fontSize = 11.sp, color = SlateMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Step 3: Partner's English Response
                    if (testState.currentStep >= 3) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = FrostedGlassWhite,
                            border = BorderStroke(
                                1.dp,
                                if (testState.currentStep == 3) CoralSpeech else FrostedBorderSlate
                            ),
                            shadowElevation = if (testState.currentStep == 3) 4.dp else 1.dp
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(CoralSpeech),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("3", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Partner Replied (English)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CoralSpeech)
                                    }
                                    IconButton(
                                        onClick = onReplayPartnerEnglish,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = "Play", tint = CoralSpeech, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = testState.partnerEnglishReply.ifBlank { "Partner speaking..." },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SlateDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Step 4: Bangla Heard in Earbud!
                    if (testState.currentStep >= 4) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = EmeraldPillBg.copy(alpha = 0.8f),
                            border = BorderStroke(
                                1.5.dp,
                                if (testState.currentStep == 4) EmeraldActive else EmeraldActive.copy(alpha = 0.4f)
                            ),
                            shadowElevation = 6.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldActive),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Headphones, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("You Hear in Earbud (Bangla)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldPill)
                                    }
                                    Button(
                                        onClick = onReplayBanglaEarbud,
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldActive),
                                        shape = RoundedCornerShape(50.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Hear Again", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = testState.partnerBanglaReply.ifBlank { "Translating to Bangla voice..." },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("🎧 Audio played directly into your Bluetooth Earbud", fontSize = 11.sp, color = EmeraldPill, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Step Status Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = SkyBlueUltraLight,
                        border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (testState.isRunning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = SkyBluePrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldActive, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = testState.stepDescription,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SkyBlueDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Choose a Test Phrase or Speak
                Text(
                    text = "1. Tap a Quick Bangla Test Phrase:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SlateDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickTestPhrases.forEach { phrase ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = FrostedGlassWhite,
                            border = BorderStroke(1.dp, FrostedBorder),
                            modifier = Modifier.clickable {
                                onRunTestWithText(phrase)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = phrase,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SlateDark
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // OR Speak Bangla Mic Button
                Text(
                    text = "2. Or Speak Live into Mic:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SlateDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (testState.isListeningToMic) {
                            onStopTestMic()
                        } else {
                            onStartTestTestMicSafe(onStartTestMic)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .scale(if (testState.isListeningToMic) pulseScale else 1.0f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (testState.isListeningToMic) CoralSpeech else SkyBluePrimary
                    )
                ) {
                    Icon(
                        imageVector = if (testState.isListeningToMic) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (testState.isListeningToMic) "Stop & Run Translation Test" else "Speak Bangla in Microphone 🎙️",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // OR Type Custom Bangla Text
                Text(
                    text = "3. Or Type Custom Bangla Text:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SlateDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customBanglaInput,
                        onValueChange = { customBanglaInput = it },
                        placeholder = { Text("এখানে বাংলায় লিখুন...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_bangla_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = FrostedBorderSlate
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customBanglaInput.isNotBlank()) {
                                onRunTestWithText(customBanglaInput)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Run Test")
                    }
                }
            }
        }
    }
}

private fun onStartTestTestMicSafe(onStartTestMic: () -> Unit) {
    onStartTestMic()
}
