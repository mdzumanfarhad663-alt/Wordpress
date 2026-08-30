package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translations")
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceText: String,
    val translatedText: String,
    val sourceLanguage: String, // "bn" or "en"
    val targetLanguage: String, // "en" or "bn"
    val speakerRole: String, // "YOU_BANGLA" or "PARTNER_ENGLISH"
    val isStarred: Boolean = false,
    val engineUsed: String = "AI Gemini", // "AI Gemini" or "Offline Engine"
    val timestamp: Long = System.currentTimeMillis()
)
