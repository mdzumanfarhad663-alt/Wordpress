package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioRoutingMode
import com.example.audio.BluetoothStatus
import com.example.data.local.TranslationEntity
import com.example.ui.theme.AmberWave
import com.example.ui.theme.CoralSpeech
import com.example.ui.theme.CoralSpeechBg
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.EmeraldPill
import com.example.ui.theme.EmeraldPillBg
import com.example.ui.theme.FrostedBorder
import com.example.ui.theme.FrostedBorderSlate
import com.example.ui.theme.FrostedGlassSubtle
import com.example.ui.theme.FrostedGlassTranslucent
import com.example.ui.theme.FrostedGlassWhite
import com.example.ui.theme.IndigoGlow
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSecondary
import com.example.ui.theme.SkyBlueUltraLight
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMedium
import com.example.ui.theme.SlateMuted

@Composable
fun ConversationScreen(
    uiState: TranslationUiState,
    bluetoothStatus: BluetoothStatus,
    translations: List<TranslationEntity>,
    isListening: Boolean,
    isSpeaking: Boolean,
    soundLevel: Float,
    liveCaption: String,
    onStartBanglaMic: () -> Unit,
    onStartEnglishMic: () -> Unit,
    onStopMic: () -> Unit,
    onToggleAutoLoop: () -> Unit,
    onManualTranslate: (text: String, lang: String, role: String) -> Unit,
    onReplayAudio: (text: String, lang: String, role: String) -> Unit,
    onToggleStar: (TranslationEntity) -> Unit,
    onOpenEarbudCompanion: () -> Unit,
    onModeChange: (ConversationMode) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var isKeyboardInputVisible by remember { mutableStateOf(false) }
    var typedText by remember { mutableStateOf("") }
    var typedLanguage by remember { mutableStateOf("bn") } // "bn" or "en"

    LaunchedEffect(translations.size) {
        if (translations.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("conversation_screen")
    ) {
        // Frosted Connected Device Card (from HTML design)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable { onOpenEarbudCompanion() }
                .testTag("earbud_status_card"),
            shape = RoundedCornerShape(24.dp),
            color = SkyBlueUltraLight.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, Color(0xFFBAE6FD).copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SkyBluePrimary)
                            .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = SkyBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "Bluetooth Status",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (bluetoothStatus.isHeadsetConnected) "Connected Device" else "Target Audio Device",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SkyBlueDark
                        )
                        Text(
                            text = bluetoothStatus.deviceName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = if (bluetoothStatus.isHeadsetConnected) EmeraldPillBg else FrostedGlassWhite,
                    border = BorderStroke(1.dp, if (bluetoothStatus.isHeadsetConnected) EmeraldActive.copy(alpha = 0.4f) else FrostedBorderSlate)
                ) {
                    Text(
                        text = if (bluetoothStatus.isHeadsetConnected) "ACTIVE" else "CONFIG",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bluetoothStatus.isHeadsetConnected) EmeraldPill else SlateMuted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Frosted Language Selector Bar (HTML design style)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(50.dp),
            color = FrostedGlassWhite,
            border = BorderStroke(1.dp, FrostedBorder),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50.dp),
                    color = if (uiState.currentActiveSpeaker == "YOU_BANGLA") SkyBluePrimary else Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "বাংলা (Bangla)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.currentActiveSpeaker == "YOU_BANGLA") Color.White else SlateMedium
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Swap Language Direction",
                    tint = SlateLight,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(20.dp)
                )

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50.dp),
                    color = if (uiState.currentActiveSpeaker == "PARTNER_ENGLISH") SkyBluePrimary else Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "English (US)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.currentActiveSpeaker == "PARTNER_ENGLISH") Color.White else SlateMedium
                        )
                    }
                }
            }
        }

        // Mode Switching Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.conversationMode == ConversationMode.DUAL_PUSH_TO_TALK,
                onClick = { onModeChange(ConversationMode.DUAL_PUSH_TO_TALK) },
                label = { Text("Push-to-Talk", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                leadingIcon = { Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(15.dp)) },
                shape = RoundedCornerShape(50.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SkyBlueUltraLight,
                    selectedLabelColor = SkyBlueDark,
                    containerColor = FrostedGlassWhite
                ),
                border = BorderStroke(1.dp, if (uiState.conversationMode == ConversationMode.DUAL_PUSH_TO_TALK) SkyBluePrimary.copy(alpha = 0.5f) else FrostedBorder)
            )

            FilterChip(
                selected = uiState.conversationMode == ConversationMode.CONTINUOUS_AUTO_LOOP,
                onClick = { onModeChange(ConversationMode.CONTINUOUS_AUTO_LOOP) },
                label = { Text("Hands-Free Auto", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(15.dp)) },
                shape = RoundedCornerShape(50.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPillBg,
                    selectedLabelColor = EmeraldPill,
                    containerColor = FrostedGlassWhite
                ),
                border = BorderStroke(1.dp, if (uiState.conversationMode == ConversationMode.CONTINUOUS_AUTO_LOOP) EmeraldActive.copy(alpha = 0.5f) else FrostedBorder)
            )

            FilterChip(
                selected = isKeyboardInputVisible,
                onClick = { isKeyboardInputVisible = !isKeyboardInputVisible },
                label = { Text("Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                leadingIcon = { Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(15.dp)) },
                shape = RoundedCornerShape(50.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SkyBlueUltraLight,
                    selectedLabelColor = SkyBlueDark,
                    containerColor = FrostedGlassWhite
                ),
                border = BorderStroke(1.dp, FrostedBorder)
            )
        }

        // Live Audio & Waveform Active Banner (Frosted Glass)
        AnimatedVisibility(visible = isListening || isSpeaking || uiState.isTranslating) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                color = FrostedGlassWhite,
                border = BorderStroke(1.dp, FrostedBorder),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isTranslating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = SkyBluePrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini Neural AI Translating...",
                                fontWeight = FontWeight.Bold,
                                color = SkyBlueDark,
                                fontSize = 13.sp
                            )
                        } else if (isSpeaking) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Playing translated audio in ${if (uiState.currentActiveSpeaker == "YOU_BANGLA") "English (Speaker)" else "Bangla (Earbud)"}",
                                fontWeight = FontWeight.Bold,
                                color = SkyBluePrimary,
                                fontSize = 13.sp
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CoralSpeech)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Listening: ${if (uiState.currentActiveSpeaker == "YOU_BANGLA") "বাংলা (Bangla Speech)" else "English Speech"}",
                                fontWeight = FontWeight.Bold,
                                color = CoralSpeech,
                                fontSize = 13.sp
                            )
                        }
                    }

                    WaveformVisualizer(
                        isListening = isListening,
                        isSpeaking = isSpeaking,
                        soundLevel = soundLevel,
                        speakerRole = uiState.currentActiveSpeaker,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    if (liveCaption.isNotBlank()) {
                        Text(
                            text = "\"$liveCaption\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateDark,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Optional Keyboard Typing Card (Frosted Glass)
        AnimatedVisibility(visible = isKeyboardInputVisible) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                color = FrostedGlassWhite,
                border = BorderStroke(1.dp, FrostedBorder),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Type & Translate", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateDark)
                        Row {
                            FilterChip(
                                selected = typedLanguage == "bn",
                                onClick = { typedLanguage = "bn" },
                                label = { Text("বাংলা", fontSize = 11.sp) },
                                shape = RoundedCornerShape(50.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            FilterChip(
                                selected = typedLanguage == "en",
                                onClick = { typedLanguage = "en" },
                                label = { Text("English", fontSize = 11.sp) },
                                shape = RoundedCornerShape(50.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = typedText,
                            onValueChange = { typedText = it },
                            placeholder = { Text(if (typedLanguage == "bn") "এখানে বাংলায় লিখুন..." else "Type message in English...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("typed_text_input"),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SkyBluePrimary,
                                unfocusedBorderColor = FrostedBorderSlate
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (typedText.isNotBlank()) {
                                    val role = if (typedLanguage == "bn") "YOU_BANGLA" else "PARTNER_ENGLISH"
                                    onManualTranslate(typedText, typedLanguage, role)
                                    typedText = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SkyBluePrimary)
                                .testTag("send_typed_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Conversation Stream List
        if (translations.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = FrostedGlassWhite,
                    border = BorderStroke(1.dp, FrostedBorder),
                    shadowElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(SkyBlueUltraLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "Smart Earbuds AI Translator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SlateDark,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Seamless bilingual voice bridging between Bangla & English with your HOCO earbuds.\n\n" +
                                    "1. Speak in Bangla: The app translates and speaks crisp English.\n" +
                                    "2. Partner speaks English: Translated Bangla plays straight into your earbud!",
                            fontSize = 13.sp,
                            color = SlateMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(translations, key = { it.id }) { item ->
                    TranslationBubbleItem(
                        item = item,
                        onReplayAudio = {
                            onReplayAudio(
                                item.translatedText,
                                item.targetLanguage,
                                if (item.speakerRole == "YOU_BANGLA") "PARTNER_ENGLISH" else "YOU_BANGLA"
                            )
                        },
                        onToggleStar = { onToggleStar(item) },
                        onCopyText = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Translation", item.translatedText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Translation copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Frosted Glass Floating Control Deck (HTML Design Layout)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.conversationMode == ConversationMode.CONTINUOUS_AUTO_LOOP) {
                // Continuous Auto Loop Deck
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = FrostedGlassWhite,
                    border = BorderStroke(1.dp, FrostedBorder),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.isAutoLoopActive) EmeraldActive else CoralSpeech)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.isAutoLoopActive) "Continuous Loop Active" else "Auto Mode Paused",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SlateDark
                                )
                            }
                            Text(
                                text = "Auto detects speech & translates back and forth",
                                fontSize = 11.sp,
                                color = SlateMuted
                            )
                        }

                        Button(
                            onClick = onToggleAutoLoop,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isAutoLoopActive) CoralSpeech else EmeraldActive
                            ),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.testTag("toggle_auto_loop_btn")
                        ) {
                            Icon(
                                imageVector = if (uiState.isAutoLoopActive) Icons.Default.Stop else Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (uiState.isAutoLoopActive) "Stop" else "Start Auto", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Dual PTT Frosted Glass Buttons Deck
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Button: English Partner
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                if (isListening && uiState.currentActiveSpeaker == "PARTNER_ENGLISH") {
                                    onStopMic()
                                } else {
                                    onStartEnglishMic()
                                }
                            }
                            .testTag("speak_english_btn"),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isListening && uiState.currentActiveSpeaker == "PARTNER_ENGLISH") CoralSpeech else FrostedGlassWhite,
                        border = BorderStroke(1.dp, if (isListening && uiState.currentActiveSpeaker == "PARTNER_ENGLISH") CoralSpeech else FrostedBorder),
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isListening && uiState.currentActiveSpeaker == "PARTNER_ENGLISH") Color.White.copy(alpha = 0.3f) else IndigoGlow),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isListening && uiState.currentActiveSpeaker == "PARTNER_ENGLISH") Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "English",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isListening && uiState.currentActiveSpeaker == "PARTNER_ENGLISH") Color.White else SlateDark
                                )
                                Text(
                                    "Partner speaks",
                                    fontSize = 10.sp,
                                    color = if (isListening && uiState.currentActiveSpeaker == "PARTNER_ENGLISH") Color.White.copy(alpha = 0.85f) else SlateMuted
                                )
                            }
                        }
                    }

                    // Right Button: You (Bangla Speaker) - Hero Primary Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                if (isListening && uiState.currentActiveSpeaker == "YOU_BANGLA") {
                                    onStopMic()
                                } else {
                                    onStartBanglaMic()
                                }
                            }
                            .testTag("speak_bangla_btn"),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isListening && uiState.currentActiveSpeaker == "YOU_BANGLA") CoralSpeech else SkyBluePrimary,
                        border = BorderStroke(1.dp, if (isListening && uiState.currentActiveSpeaker == "YOU_BANGLA") CoralSpeech else SkyBlueLight.copy(alpha = 0.5f)),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isListening && uiState.currentActiveSpeaker == "YOU_BANGLA") Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "বাংলায় বলুন",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    "You (Earbud mic)",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            // Pulsing status subtitle (from HTML design)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isListening) CoralSpeech else SkyBluePrimary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isListening) "Listening for conversation..." else "Voice bridge active & ready",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = SkyBlueDark
                )
            }
        }
    }
}

@Composable
fun TranslationBubbleItem(
    item: TranslationEntity,
    onReplayAudio: () -> Unit,
    onToggleStar: () -> Unit,
    onCopyText: () -> Unit
) {
    val isYou = item.speakerRole == "YOU_BANGLA"

    if (isYou) {
        // You (Bangla Speaker): Clean Frosted White Card with Left Alignment + English Translated Sky Blue Bubble
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("translation_item_${item.id}"),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Source Bangla Bubble
            Surface(
                modifier = Modifier.fillMaxWidth(0.88f),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                color = FrostedGlassWhite,
                border = BorderStroke(1.dp, FrostedBorder),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BANGLA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = SlateLight
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SkyBlueUltraLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = item.engineUsed, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SkyBlueDark)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.sourceText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = SlateDark,
                        fontSize = 16.sp
                    )
                }
            }

            // AI Voice English Translated Bubble (Right Aligned Sky Blue)
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .align(Alignment.End),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                color = SkyBluePrimary,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ENGLISH (AI VOICE)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = SkyBlueLight
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onCopyText, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                            }
                            IconButton(onClick = onToggleStar, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = if (item.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star",
                                    tint = if (item.isStarred) AmberWave else Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = onReplayAudio,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f))
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${item.translatedText}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    } else {
        // Partner (English Speaker): Partner Spoken English + AI Translated Bangla to Earbud
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("translation_item_${item.id}"),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Source English Bubble
            Surface(
                modifier = Modifier.fillMaxWidth(0.88f),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                color = FrostedGlassWhite,
                border = BorderStroke(1.dp, FrostedBorder),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ENGLISH (PARTNER)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = SlateLight
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(FrostedGlassTranslucent)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = item.engineUsed, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SlateMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.sourceText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = SlateDark,
                        fontSize = 16.sp
                    )
                }
            }

            // AI Voice Bangla Translated Bubble (Played into Earbud)
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .align(Alignment.End),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                color = SkyBlueDark,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BANGLA (EARBUD AUDIO)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = SkyBlueLight
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onCopyText, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                            }
                            IconButton(onClick = onToggleStar, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = if (item.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star",
                                    tint = if (item.isStarred) AmberWave else Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = onReplayAudio,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f))
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${item.translatedText}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
