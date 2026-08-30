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

    private var retryCount = 0
    private var isFallbackMode = false

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
            Log.w(TAG, "Speech Recognizer Error code: $error")
            _isListening.value = false
            _soundLevel.value = 0f

            // Error 12: ERROR_LANGUAGE_NOT_SUPPORTED (introduced in Android 13/14)
            // Error 5: ERROR_CLIENT
            // If offline speech recognition pack for Bangla is missing, auto-fallback without offline constraint
            if ((error == 12 || error == 13 || error == SpeechRecognizer.ERROR_CLIENT) && retryCount < 1) {
                retryCount++
                isFallbackMode = true
                Log.i(TAG, "Attempting automatic recovery for speech recognition error ($error)")
                mainHandler.postDelayed({
                    recreateRecognizer()
                    startListeningInternal(_currentLanguage.value, useFallback = true)
                }, 300)
                return
            }

            retryCount = 0
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Check microphone."
                SpeechRecognizer.ERROR_CLIENT -> "Microphone service busy. Tap again or use Test Mode."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                SpeechRecognizer.ERROR_NETWORK -> "Network connection issue. Trying offline engine."
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out. Please try speaking again."
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak closer to the mic."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy. Reconnecting..."
                SpeechRecognizer.ERROR_SERVER -> "Recognition server error. Switching to fallback."
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected."
                12 -> "Bangla offline voice pack not found on device. Using online/test speech recognition."
                13 -> "Language unavailable on device speech engine. You can also use the Test Option."
                14 -> "Cannot verify language support."
                else -> "Speech recognition notice ($error). Please speak again or use the Test Option."
            }

            // Always recreate recognizer cleanly after client or busy errors
            if (error == SpeechRecognizer.ERROR_CLIENT || error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == 12) {
                recreateRecognizer()
            }

            if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                onErrorOccurred(message)
            }
        }

        override fun onResults(results: Bundle?) {
            _isListening.value = false
            _soundLevel.value = 0f
            retryCount = 0
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
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SpeechRecognizer: ${e.message}")
        }
    }

    private fun recreateRecognizer() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(recognitionListener)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error recreating SpeechRecognizer: ${e.message}")
        }
    }

    fun startListening(languageCode: String) {
        retryCount = 0
        isFallbackMode = false
        startListeningInternal(languageCode, useFallback = false)
    }

    private fun startListeningInternal(languageCode: String, useFallback: Boolean) {
        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    initRecognizer()
                }

                // Stop any ongoing session cleanly
                try {
                    speechRecognizer?.cancel()
                } catch (ignored: Exception) {}

                _currentLanguage.value = languageCode
                _liveCaption.value = ""
                _soundLevel.value = 0.15f

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)

                    val primaryLocaleTag = if (languageCode == "bn") {
                        if (useFallback) "bn" else "bn-BD"
                    } else {
                        "en-US"
                    }

                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, primaryLocaleTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, primaryLocaleTag)

                    // Add multilingual fallbacks
                    if (languageCode == "bn") {
                        putExtra(
                            "android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES",
                            arrayOf("bn-BD", "bn-IN", "bn", "en-US")
                        )
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "বাংলায় কথা বলুন...")
                    } else {
                        putExtra(
                            "android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES",
                            arrayOf("en-US", "en-GB", "en-IN")
                        )
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak in English...")
                    }

                    // Only request offline preference if fallback mode is false
                    if (!useFallback) {
                        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
                    }
                }

                speechRecognizer?.startListening(intent)
                _isListening.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error starting speech recognition: ${e.message}")
                _isListening.value = false
                onErrorOccurred("Could not start microphone: ${e.message}")
                recreateRecognizer()
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

    fun simulateSpokenText(text: String, languageCode: String) {
        mainHandler.post {
            _liveCaption.value = text
            _isListening.value = false
            _soundLevel.value = 0f
            onFinalText(text, languageCode)
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

