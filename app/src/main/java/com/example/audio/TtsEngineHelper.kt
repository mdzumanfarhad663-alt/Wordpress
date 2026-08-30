package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsEngineHelper(
    private val context: Context,
    private val onInitComplete: (Boolean) -> Unit = {}
) : TextToSpeech.OnInitListener {

    private val TAG = "TtsEngineHelper"
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentUtteranceId = MutableStateFlow<String?>(null)
    val currentUtteranceId: StateFlow<String?> = _currentUtteranceId.asStateFlow()

    var speechRate: Float = 1.0f
        set(value) {
            field = value
            tts?.setSpeechRate(value)
        }

    var speechPitch: Float = 1.0f
        set(value) {
            field = value
            tts?.setPitch(value)
        }

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TTS: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    _currentUtteranceId.value = utteranceId
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentUtteranceId.value = null
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentUtteranceId.value = null
                    Log.w(TAG, "TTS Utterance error: $utteranceId")
                }
            })

            // Set audio attributes suitable for voice communication / media
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            tts?.setAudioAttributes(audioAttributes)
            onInitComplete(true)
        } else {
            isInitialized = false
            Log.e(TAG, "TTS Init failed with code: $status")
            onInitComplete(false)
        }
    }

    fun speak(text: String, languageCode: String, utteranceId: String = System.currentTimeMillis().toString()) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not initialized yet")
            return
        }

        val targetLocale = if (languageCode == "bn") {
            Locale("bn", "BD")
        } else {
            Locale.US
        }

        val result = tts?.setLanguage(targetLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback for Bengali to generic Bangla or Default
            if (languageCode == "bn") {
                tts?.setLanguage(Locale("bn"))
            } else {
                tts?.setLanguage(Locale.ENGLISH)
            }
        }

        tts?.setSpeechRate(speechRate)
        tts?.setPitch(speechPitch)

        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, android.media.AudioManager.STREAM_VOICE_CALL)
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        try {
            tts?.stop()
            _isSpeaking.value = false
            _currentUtteranceId.value = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS: ${e.message}")
        }
    }

    fun playEarbudCue(toneType: Int = ToneGenerator.TONE_PROP_BEEP) {
        try {
            val toneGen = ToneGenerator(android.media.AudioManager.STREAM_VOICE_CALL, 80)
            toneGen.startTone(toneType, 120)
        } catch (e: Exception) {
            Log.w(TAG, "Could not play earbud cue: ${e.message}")
        }
    }

    fun shutdown() {
        try {
            stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS: ${e.message}")
        }
    }
}
