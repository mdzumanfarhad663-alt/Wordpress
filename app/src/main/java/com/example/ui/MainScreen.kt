package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.CoralSpeech
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.FrostedBackground
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bluetoothStatus by viewModel.bluetoothStatus.collectAsStateWithLifecycle()
    val allTranslations by viewModel.allTranslations.collectAsStateWithLifecycle()
    val starredTranslations by viewModel.starredTranslations.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val soundLevel by viewModel.soundLevel.collectAsStateWithLifecycle()
    val liveCaption by viewModel.liveCaption.collectAsStateWithLifecycle()
    val testDialogueState by viewModel.testDialogueState.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }

    // Dynamic Permission Requests
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] == true
        viewModel.bluetoothController.checkConnectedDevices()
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SkyBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = "App Icon",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AI TRANSLATOR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = SkyBlueDark
                            )
                            Text(
                                text = "Voice Bridge",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateDark
                            )
                        }
                    }
                },
                actions = {
                    // AI / Offline Mode Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (uiState.preferOffline) {
                            FrostedGlassWhite
                        } else {
                            SkyBlueUltraLight
                        },
                        border = BorderStroke(1.dp, if (uiState.preferOffline) FrostedBorderSlate else FrostedBorder),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (uiState.preferOffline) Icons.Default.CloudOff else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (uiState.preferOffline) SlateMuted else SkyBluePrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.preferOffline) "Offline" else "Gemini AI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.preferOffline) SlateMuted else SkyBluePrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(FrostedGlassWhite)
                            .border(BorderStroke(1.dp, FrostedBorder), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.testTag("settings_icon_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = SlateDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = SlateDark
                )
            )
        },
        bottomBar = {
            Surface(
                color = FrostedGlassWhite,
                tonalElevation = 6.dp,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, FrostedBorder)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = uiState.activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        icon = { Icon(Icons.Default.Translate, contentDescription = "Live Translate") },
                        label = { Text("Translate", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SkyBluePrimary,
                            selectedTextColor = SkyBluePrimary,
                            indicatorColor = SkyBlueUltraLight,
                            unselectedIconColor = SlateMuted,
                            unselectedTextColor = SlateMuted
                        ),
                        modifier = Modifier.testTag("tab_translate")
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        icon = { Icon(Icons.Default.SwapVert, contentDescription = "Face-to-Face") },
                        label = { Text("Face-to-Face", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SkyBluePrimary,
                            selectedTextColor = SkyBluePrimary,
                            indicatorColor = SkyBlueUltraLight,
                            unselectedIconColor = SlateMuted,
                            unselectedTextColor = SlateMuted
                        ),
                        modifier = Modifier.testTag("tab_facetoface")
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == 2,
                        onClick = { viewModel.setActiveTab(2) },
                        icon = { Icon(Icons.Default.Headphones, contentDescription = "Earbuds") },
                        label = { Text("Earbuds", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SkyBluePrimary,
                            selectedTextColor = SkyBluePrimary,
                            indicatorColor = SkyBlueUltraLight,
                            unselectedIconColor = SlateMuted,
                            unselectedTextColor = SlateMuted
                        ),
                        modifier = Modifier.testTag("tab_earbuds")
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == 3,
                        onClick = { viewModel.setActiveTab(3) },
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = "Phrases") },
                        label = { Text("Phrases", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SkyBluePrimary,
                            selectedTextColor = SkyBluePrimary,
                            indicatorColor = SkyBlueUltraLight,
                            unselectedIconColor = SlateMuted,
                            unselectedTextColor = SlateMuted
                        ),
                        modifier = Modifier.testTag("tab_phrases")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dismissable Error Banner
            AnimatedVisibility(visible = uiState.errorBanner != null) {
                uiState.errorBanner?.let { err ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = CoralSpeech.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = err,
                                fontSize = 12.sp,
                                color = CoralSpeech,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearErrorBanner() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = CoralSpeech, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Tab Content
            when (uiState.activeTab) {
                0 -> ConversationScreen(
                    uiState = uiState,
                    bluetoothStatus = bluetoothStatus,
                    translations = allTranslations,
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    soundLevel = soundLevel,
                    liveCaption = liveCaption,
                    onStartBanglaMic = { viewModel.startListeningBangla() },
                    onStartEnglishMic = { viewModel.startListeningEnglish() },
                    onStopMic = { viewModel.stopListening() },
                    onToggleAutoLoop = { viewModel.toggleContinuousAutoLoop() },
                    onManualTranslate = { text, lang, role -> viewModel.translateAndSpeak(text, lang, role) },
                    onReplayAudio = { text, lang, role -> viewModel.speakTranslation(text, lang, role) },
                    onToggleStar = { entity -> viewModel.toggleStarred(entity) },
                    onOpenEarbudCompanion = { viewModel.setActiveTab(2) },
                    onModeChange = { mode -> viewModel.setConversationMode(mode) },
                    onOpenTestDialogue = { viewModel.openTestDialogue() }
                )

                1 -> FaceToFaceScreen(
                    uiState = uiState,
                    translations = allTranslations,
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    soundLevel = soundLevel,
                    liveCaption = liveCaption,
                    onStartBanglaMic = { viewModel.startListeningBangla() },
                    onStartEnglishMic = { viewModel.startListeningEnglish() },
                    onStopMic = { viewModel.stopListening() },
                    onReplayAudio = { text, lang, role -> viewModel.speakTranslation(text, lang, role) }
                )

                2 -> EarbudCompanionScreen(
                    bluetoothStatus = bluetoothStatus,
                    onRoutingModeChange = { mode -> viewModel.setAudioRoutingMode(mode) },
                    onRefreshBluetooth = { viewModel.bluetoothController.checkConnectedDevices() },
                    onTestEarbudAudio = { isBanglaToEarbud ->
                        if (isBanglaToEarbud) {
                            viewModel.speakTranslation("নমস্কার! আপনার ইয়ারবাড কানেকশন সফলভাবে কাজ করছে।", "bn", "YOU_BANGLA")
                        } else {
                            viewModel.speakTranslation("Hello! The loudspeaker audio translation is working properly.", "en", "PARTNER_ENGLISH")
                        }
                    },
                    onOpenTestDialogue = { viewModel.openTestDialogue() }
                )

                3 -> PhrasebookScreen(
                    starredTranslations = starredTranslations,
                    onSpeakPhrase = { phrase, speakBangla -> viewModel.speakPhrase(phrase, speakBangla) },
                    onReplayAudio = { text, lang, role -> viewModel.speakTranslation(text, lang, role) },
                    onToggleStar = { entity -> viewModel.toggleStarred(entity) }
                )
            }
        }
    }

    if (testDialogueState.isOpen) {
        TestDialogueDialog(
            testState = testDialogueState,
            onStartTestMic = { viewModel.startTestMicBangla() },
            onStopTestMic = { viewModel.stopTestMic() },
            onRunTestWithText = { text -> viewModel.runBanglaDialogueTest(text) },
            onReplayUserEnglish = { viewModel.replayTestUserEnglish() },
            onReplayPartnerEnglish = { viewModel.replayTestEnglishSpeaker() },
            onReplayBanglaEarbud = { viewModel.replayTestBanglaEarbud() },
            onDismiss = { viewModel.closeTestDialogue() }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            uiState = uiState,
            routingMode = bluetoothStatus.routingMode,
            onRoutingModeChange = { mode -> viewModel.setAudioRoutingMode(mode) },
            onSpeechRateChange = { rate -> viewModel.setSpeechRate(rate) },
            onSpeechPitchChange = { pitch -> viewModel.setSpeechPitch(pitch) },
            onAutoPlayTtsChange = { enabled -> viewModel.toggleAutoPlayTts(enabled) },
            onPreferOfflineChange = { offline -> viewModel.togglePreferOffline(offline) },
            onClearHistory = { viewModel.clearAllHistory() },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
