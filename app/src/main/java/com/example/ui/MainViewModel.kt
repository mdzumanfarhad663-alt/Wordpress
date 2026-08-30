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

    val ttsHelper = TtsEngineHelper(application)
    val isSpeaking: StateFlow<Boolean> = ttsHelper.isSpeaking

    private var speechHelper: SpeechRecognizerHelper? = null
    val isListening: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val soundLevel: MutableStateFlow<Float> = MutableStateFlow(0f)
    val liveCaption: MutableStateFlow<String> = MutableStateFlow("")

    private var autoLoopJob: Job? = null

    init {
        speechHelper = SpeechRecognizerHelper(
            context = application,
            onFinalText = { text, language ->
                onSpeechRecognized(text, language)
            },
            onErrorOccurred = { error ->
                _uiState.value = _uiState.value.copy(errorBanner = error)
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

    override fun onCleared() {
        super.onCleared()
        speechHelper?.destroy()
        ttsHelper.shutdown()
        bluetoothController.release()
    }
}
