package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechRecognizerHelper(
    private val context: Context,
    private val onFinalText: (text: String, language: String) -> Unit,
    private val onErrorOccurred: (message: String) -> Unit
) {
    private val TAG = "SpeechRecognizerHelper"
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _currentLanguage = MutableStateFlow("bn")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _liveCaption = MutableStateFlow("")
    val liveCaption: StateFlow<String> = _liveCaption.asStateFlow()

    private val _soundLevel = MutableStateFlow(0f)
    val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "onReadyForSpeech")
            _isListening.value = true
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech")
            _liveCaption.value = ""
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Normalize dB value (-2 to 10 dB typically) to 0.0 .. 1.0 range for animation
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _soundLevel.value = normalized
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech")
            _isListening.value = false
            _soundLevel.value = 0f
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                SpeechRecognizer.ERROR_NETWORK -> "Network connection issue"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                SpeechRecognizer.ERROR_SERVER -> "Recognition server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
                else -> "Speech recognition error ($error)"
            }
            Log.w(TAG, "Speech Recognizer Error: $error -> $message")
            _isListening.value = false
            _soundLevel.value = 0f
            if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                onErrorOccurred(message)
            }
        }

        override fun onResults(results: Bundle?) {
            _isListening.value = false
            _soundLevel.value = 0f
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val spokenText = matches[0].trim()
                if (spokenText.isNotBlank()) {
                    _liveCaption.value = spokenText
                    onFinalText(spokenText, _currentLanguage.value)
                }
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                _liveCaption.value = matches[0]
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    init {
        mainHandler.post {
            initRecognizer()
        }
    }

    private fun initRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(recognitionListener)
                }
            } else {
                onErrorOccurred("Speech recognition not available on this device")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SpeechRecognizer: ${e.message}")
        }
    }

    fun startListening(languageCode: String) {
        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    initRecognizer()
                }
                _currentLanguage.value = languageCode
                _liveCaption.value = ""
                _soundLevel.value = 0.1f

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    
                    val localeTag = if (languageCode == "bn") "bn-BD" else "en-US"
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("bn-BD", "bn-IN", "en-US", "en-GB"))
                    putExtra(RecognizerIntent.EXTRA_PROMPT, if (languageCode == "bn") "বাংলায় কথা বলুন..." else "Speak in English...")
                    // Optimize for fast zero-cost on-device recognition if available
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                }

                speechRecognizer?.startListening(intent)
                _isListening.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error starting speech recognition: ${e.message}")
                _isListening.value = false
                onErrorOccurred("Could not start microphone: ${e.message}")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping listening: ${e.message}")
            } finally {
                _isListening.value = false
                _soundLevel.value = 0f
            }
        }
    }

    fun cancel() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling: ${e.message}")
            } finally {
                _isListening.value = false
                _soundLevel.value = 0f
                _liveCaption.value = ""
            }
        }
    }

    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.e(TAG, "Error destroying speech recognizer: ${e.message}")
            }
        }
    }
}
