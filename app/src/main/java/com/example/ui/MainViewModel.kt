package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioRoutingMode
import com.example.audio.BluetoothAudioController
import com.example.audio.BluetoothStatus
import com.example.audio.SpeechRecognizerHelper
import com.example.audio.TtsEngineHelper
import com.example.data.local.AppDatabase
import com.example.data.local.TranslationEntity
import com.example.translator.PhraseItem
import com.example.translator.TranslationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ConversationMode {
    DUAL_PUSH_TO_TALK,
    CONTINUOUS_AUTO_LOOP,
    FACE_TO_FACE
}

data class TestDialogueState(
    val isOpen: Boolean = false,
    val isRunning: Boolean = false,
    val currentStep: Int = 0, // 0: Ready, 1: Spoken Bangla, 2: Translated to English, 3: Partner English Reply, 4: Hear Bangla in Earbud, 5: Complete
    val stepDescription: String = "Select or speak a Bangla phrase to test",
    val spokenBanglaText: String = "",
    val translatedEnglishText: String = "",
    val partnerEnglishReply: String = "",
    val partnerBanglaReply: String = "",
    val isListeningToMic: Boolean = false
)

data class TranslationUiState(
    val conversationMode: ConversationMode = ConversationMode.DUAL_PUSH_TO_TALK,
    val isAutoLoopActive: Boolean = false,
    val currentActiveSpeaker: String? = null, // "YOU_BANGLA" or "PARTNER_ENGLISH"
    val isTranslating: Boolean = false,
    val liveSpokenText: String = "",
    val lastTranslationResult: String = "",
    val errorBanner: String? = null,
    val isTtsAutoPlayEnabled: Boolean = true,
    val preferOffline: Boolean = false,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val activeTab: Int = 0 // 0: Live Translate, 1: Face-to-Face, 2: Earbud Companion, 3: Saved & Phrases
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "MainViewModel"
    private val database = AppDatabase.getDatabase(application)
    private val translationDao = database.translationDao()
    private val translationService = TranslationService()

    val bluetoothController = BluetoothAudioController(application)
    val bluetoothStatus: StateFlow<BluetoothStatus> = bluetoothController.status

    private val _uiState = MutableStateFlow(TranslationUiState())
    val uiState: StateFlow<TranslationUiState> = _uiState.asStateFlow()

    val allTranslations: StateFlow<List<TranslationEntity>> = translationDao.getAllTranslations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val starredTranslations: StateFlow<List<TranslationEntity>> = translationDao.getStarredTranslations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _testDialogueState = MutableStateFlow(TestDialogueState())
    val testDialogueState: StateFlow<TestDialogueState> = _testDialogueState.asStateFlow()

    val ttsHelper = TtsEngineHelper(application)
    val isSpeaking: StateFlow<Boolean> = ttsHelper.isSpeaking

    private var speechHelper: SpeechRecognizerHelper? = null
    val isListening: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val soundLevel: MutableStateFlow<Float> = MutableStateFlow(0f)
    val liveCaption: MutableStateFlow<String> = MutableStateFlow("")

    private var autoLoopJob: Job? = null
    private var dialogueTestJob: Job? = null

    init {
        speechHelper = SpeechRecognizerHelper(
            context = application,
            onFinalText = { text, language ->
                if (_testDialogueState.value.isListeningToMic) {
                    _testDialogueState.value = _testDialogueState.value.copy(isListeningToMic = false)
                    runBanglaDialogueTest(text)
                } else {
                    onSpeechRecognized(text, language)
                }
            },
            onErrorOccurred = { error ->
                if (_testDialogueState.value.isListeningToMic) {
                    _testDialogueState.value = _testDialogueState.value.copy(
                        isListeningToMic = false,
                        stepDescription = error
                    )
                } else {
                    _uiState.value = _uiState.value.copy(errorBanner = error)
                }
                isListening.value = false
                soundLevel.value = 0f
            }
        )

        // Observe speech helper flows
        viewModelScope.launch {
            speechHelper?.isListening?.collect { listening ->
                isListening.value = listening
            }
        }
        viewModelScope.launch {
            speechHelper?.soundLevel?.collect { level ->
                soundLevel.value = level
            }
        }
        viewModelScope.launch {
            speechHelper?.liveCaption?.collect { caption ->
                liveCaption.value = caption
            }
        }
    }

    fun setActiveTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tabIndex)
    }

    fun setConversationMode(mode: ConversationMode) {
        if (_uiState.value.isAutoLoopActive) {
            stopContinuousAutoLoop()
        }
        _uiState.value = _uiState.value.copy(conversationMode = mode)
    }

    fun startListeningBangla() {
        ttsHelper.stop()
        _uiState.value = _uiState.value.copy(
            currentActiveSpeaker = "YOU_BANGLA",
            errorBanner = null
        )
        // If Bluetooth SCO is available and enabled, start SCO mic
        bluetoothController.startScoMic()
        ttsHelper.playEarbudCue()
        speechHelper?.startListening("bn")
    }

    fun startListeningEnglish() {
        ttsHelper.stop()
        _uiState.value = _uiState.value.copy(
            currentActiveSpeaker = "PARTNER_ENGLISH",
            errorBanner = null
        )
        ttsHelper.playEarbudCue()
        speechHelper?.startListening("en")
    }

    fun stopListening() {
        speechHelper?.stopListening()
        bluetoothController.stopScoMic()
    }

    fun toggleContinuousAutoLoop() {
        val nextState = !_uiState.value.isAutoLoopActive
        _uiState.value = _uiState.value.copy(isAutoLoopActive = nextState)
        if (nextState) {
            startListeningBangla()
        } else {
            stopContinuousAutoLoop()
        }
    }

    private fun stopContinuousAutoLoop() {
        _uiState.value = _uiState.value.copy(isAutoLoopActive = false)
        autoLoopJob?.cancel()
        speechHelper?.stopListening()
        ttsHelper.stop()
        bluetoothController.stopScoMic()
    }

    private fun onSpeechRecognized(text: String, language: String) {
        val speaker = _uiState.value.currentActiveSpeaker ?: if (language == "bn") "YOU_BANGLA" else "PARTNER_ENGLISH"
        translateAndSpeak(text, sourceLang = language, speakerRole = speaker)
    }

    fun translateAndSpeak(
        text: String,
        sourceLang: String, // "bn" or "en"
        speakerRole: String // "YOU_BANGLA" or "PARTNER_ENGLISH"
    ) {
        if (text.isBlank()) return

        val targetLang = if (sourceLang == "bn") "en" else "bn"
        _uiState.value = _uiState.value.copy(
            isTranslating = true,
            liveSpokenText = text
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = translationService.translate(
                    text = text,
                    sourceLang = sourceLang,
                    targetLang = targetLang,
                    preferOffline = _uiState.value.preferOffline
                )

                _uiState.value = _uiState.value.copy(
                    isTranslating = false,
                    lastTranslationResult = result.translatedText
                )

                // Save to Room DB
                val entity = TranslationEntity(
                    sourceText = text,
                    translatedText = result.translatedText,
                    sourceLanguage = sourceLang,
                    targetLanguage = targetLang,
                    speakerRole = speakerRole,
                    engineUsed = result.engineUsed
                )
                translationDao.insert(entity)

                // Audio Routing & Text-To-Speech
                if (_uiState.value.isTtsAutoPlayEnabled) {
                    val targetSpeakerRole = if (speakerRole == "YOU_BANGLA") "PARTNER_ENGLISH" else "YOU_BANGLA"
                    // Route audio to Earbud (for Bangla speaker) or Phone Speaker (for English speaker)
                    bluetoothController.routeAudioForSpeaker(targetSpeakerRole)
                    
                    ttsHelper.speak(result.translatedText, targetLang)

                    // If Continuous Conversation Mode is active, queue next turn after speech finishes
                    if (_uiState.value.isAutoLoopActive) {
                        autoLoopJob?.cancel()
                        autoLoopJob = launch {
                            // Wait for TTS to finish
                            delay(2500)
                            if (_uiState.value.isAutoLoopActive) {
                                if (speakerRole == "YOU_BANGLA") {
                                    startListeningEnglish()
                                } else {
                                    startListeningBangla()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Translation error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isTranslating = false,
                    errorBanner = "Translation failed: ${e.message}"
                )
            }
        }
    }

    fun speakTranslation(text: String, lang: String, targetRole: String) {
        bluetoothController.routeAudioForSpeaker(targetRole)
        ttsHelper.speak(text, lang)
    }

    fun speakPhrase(phrase: PhraseItem, speakBangla: Boolean) {
        if (speakBangla) {
            bluetoothController.routeAudioForSpeaker("YOU_BANGLA")
            ttsHelper.speak(phrase.banglaText, "bn")
        } else {
            bluetoothController.routeAudioForSpeaker("PARTNER_ENGLISH")
            ttsHelper.speak(phrase.englishText, "en")
        }
    }

    fun toggleStarred(translation: TranslationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            translationDao.setStarred(translation.id, !translation.isStarred)
        }
    }

    fun deleteTranslation(translation: TranslationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            translationDao.delete(translation)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            translationDao.clearAll()
        }
    }

    fun setSpeechRate(rate: Float) {
        _uiState.value = _uiState.value.copy(speechRate = rate)
        ttsHelper.speechRate = rate
    }

    fun setSpeechPitch(pitch: Float) {
        _uiState.value = _uiState.value.copy(speechPitch = pitch)
        ttsHelper.speechPitch = pitch
    }

    fun toggleAutoPlayTts(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isTtsAutoPlayEnabled = enabled)
    }

    fun togglePreferOffline(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(preferOffline = enabled)
    }

    fun setAudioRoutingMode(mode: AudioRoutingMode) {
        bluetoothController.setRoutingMode(mode)
    }

    fun clearErrorBanner() {
        _uiState.value = _uiState.value.copy(errorBanner = null)
    }

    fun openTestDialogue() {
        _testDialogueState.value = _testDialogueState.value.copy(isOpen = true)
    }

    fun closeTestDialogue() {
        dialogueTestJob?.cancel()
        ttsHelper.stop()
        speechHelper?.stopListening()
        _testDialogueState.value = _testDialogueState.value.copy(
            isOpen = false,
            isRunning = false,
            isListeningToMic = false
        )
    }

    fun startTestMicBangla() {
        ttsHelper.stop()
        _testDialogueState.value = _testDialogueState.value.copy(
            isListeningToMic = true,
            stepDescription = "Listening for Bangla voice input... Speak now 🎤"
        )
        speechHelper?.startListening("bn")
    }

    fun stopTestMic() {
        _testDialogueState.value = _testDialogueState.value.copy(isListeningToMic = false)
        speechHelper?.stopListening()
    }

    fun runBanglaDialogueTest(banglaText: String) {
        if (banglaText.isBlank()) return
        dialogueTestJob?.cancel()
        ttsHelper.stop()

        dialogueTestJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                // Step 1: User says Bangla
                _testDialogueState.value = _testDialogueState.value.copy(
                    isOpen = true,
                    isRunning = true,
                    currentStep = 1,
                    spokenBanglaText = banglaText,
                    translatedEnglishText = "",
                    partnerEnglishReply = "",
                    partnerBanglaReply = "",
                    stepDescription = "Step 1/4: You spoke in Bangla"
                )
                delay(700)

                // Step 2: Translate user's Bangla to English
                val enTranslation = translationService.translate(
                    text = banglaText,
                    sourceLang = "bn",
                    targetLang = "en",
                    preferOffline = _uiState.value.preferOffline
                ).translatedText

                _testDialogueState.value = _testDialogueState.value.copy(
                    currentStep = 2,
                    translatedEnglishText = enTranslation,
                    stepDescription = "Step 2/4: Speaking English Translation on Phone Speaker..."
                )

                // Route to Partner (Phone Speaker) and Speak English
                bluetoothController.routeAudioForSpeaker("PARTNER_ENGLISH")
                ttsHelper.speak(enTranslation, "en")
                delay(2600)

                // Step 3: Partner replies in English (Conversational Engine)
                val partnerEn = generatePartnerEnglishReply(banglaText, enTranslation)
                _testDialogueState.value = _testDialogueState.value.copy(
                    currentStep = 3,
                    partnerEnglishReply = partnerEn,
                    stepDescription = "Step 3/4: Partner is replying in English..."
                )
                bluetoothController.routeAudioForSpeaker("PARTNER_ENGLISH")
                ttsHelper.speak(partnerEn, "en")
                delay(2800)

                // Step 4: Translate Partner's English to Bangla & speak into EARBUD
                val partnerBn = translationService.translate(
                    text = partnerEn,
                    sourceLang = "en",
                    targetLang = "bn",
                    preferOffline = _uiState.value.preferOffline
                ).translatedText

                _testDialogueState.value = _testDialogueState.value.copy(
                    currentStep = 4,
                    partnerBanglaReply = partnerBn,
                    stepDescription = "Step 4/4: Hearing Bangla Translation in your Earbud! 🎧"
                )

                // Route to Earbud (Bangla speaker) and Speak Bangla!
                bluetoothController.routeAudioForSpeaker("YOU_BANGLA")
                ttsHelper.speak(partnerBn, "bn")
                delay(2800)

                // Step 5: Test Finished
                _testDialogueState.value = _testDialogueState.value.copy(
                    currentStep = 5,
                    isRunning = false,
                    stepDescription = "Test Complete! You heard the Bangla translation in your earbud."
                )
            } catch (e: Exception) {
                Log.e(TAG, "Dialogue test error: ${e.message}")
                _testDialogueState.value = _testDialogueState.value.copy(
                    isRunning = false,
                    stepDescription = "Test Notice: ${e.message}"
                )
            }
        }
    }

    private fun generatePartnerEnglishReply(banglaText: String, enTranslation: String): String {
        val lowerBn = banglaText.lowercase()
        val lowerEn = enTranslation.lowercase()
        return when {
            lowerBn.contains("কেমন") || lowerEn.contains("how are you") ->
                "I am doing very well, thank you! How can I help you today?"
            lowerBn.contains("ভাড়া") || lowerBn.contains("দাম") || lowerEn.contains("how much") || lowerEn.contains("ticket") || lowerEn.contains("cost") ->
                "The ticket costs five dollars. The next train departs in ten minutes."
            lowerBn.contains("সাহায্য") || lowerEn.contains("help") ->
                "Of course! Please let me know what you need, and I will gladly assist you."
            lowerBn.contains("বিমানবন্দর") || lowerBn.contains("এয়ারপোর্ট") || lowerEn.contains("airport") ->
                "The airport is about ten kilometers away. You can take the express shuttle bus."
            lowerBn.contains("মেনু") || lowerBn.contains("খাবার") || lowerEn.contains("menu") || lowerEn.contains("food") ->
                "Here is our menu. Would you like to order food or drinks first?"
            lowerBn.contains("নাম") || lowerEn.contains("name") ->
                "My name is Alex! It is very nice to meet you."
            lowerBn.contains("ধন্যবাদ") || lowerEn.contains("thank") ->
                "You are very welcome! Have a wonderful day."
            lowerBn.contains("কোথায়") || lowerEn.contains("where") ->
                "It is located two blocks ahead on your right side."
            else ->
                "Understood! I am happy to assist you with your request."
        }
    }

    fun replayTestBanglaEarbud() {
        val text = _testDialogueState.value.partnerBanglaReply
        if (text.isNotBlank()) {
            bluetoothController.routeAudioForSpeaker("YOU_BANGLA")
            ttsHelper.speak(text, "bn")
        }
    }

    fun replayTestEnglishSpeaker() {
        val text = _testDialogueState.value.partnerEnglishReply
        if (text.isNotBlank()) {
            bluetoothController.routeAudioForSpeaker("PARTNER_ENGLISH")
            ttsHelper.speak(text, "en")
        }
    }

    fun replayTestUserEnglish() {
        val text = _testDialogueState.value.translatedEnglishText
        if (text.isNotBlank()) {
            bluetoothController.routeAudioForSpeaker("PARTNER_ENGLISH")
            ttsHelper.speak(text, "en")
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechHelper?.destroy()
        ttsHelper.shutdown()
        bluetoothController.release()
    }
}
