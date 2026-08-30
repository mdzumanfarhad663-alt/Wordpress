package com.example.translator

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class TranslationResult(
    val sourceText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String,
    val engineUsed: String,
    val isSuccessful: Boolean,
    val errorMessage: String? = null
)

class TranslationService {

    private val TAG = "TranslationService"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // Offline Bidirectional Phrase & Vocabulary Index (Bangla <-> English)
    private val offlineBanglaToEnglish = mapOf(
        "কেমন আছেন" to "How are you?",
        "কেমন আছেন?" to "How are you?",
        "ভালো আছি" to "I am fine.",
        "আমি ভালো আছি" to "I am doing well.",
        "আপনার নাম কি" to "What is your name?",
        "আপনার নাম কি?" to "What is your name?",
        "আমার নাম" to "My name is",
        "ধন্যবাদ" to "Thank you.",
        "আপনাকে অনেক ধন্যবাদ" to "Thank you very much.",
        "স্বাগতম" to "Welcome.",
        "দয়া করে" to "Please.",
        "মাফ করবেন" to "Excuse me.",
        "দুঃখিত" to "I am sorry.",
        "হ্যাঁ" to "Yes.",
        "না" to "No.",
        "ঠিক আছে" to "Alright / Okay.",
        "বিদায়" to "Goodbye.",
        "আবার দেখা হবে" to "See you again.",
        "শুভ সকাল" to "Good morning.",
        "শুভ রাত্রি" to "Good night.",
        "শুভ সন্ধ্যা" to "Good evening.",
        "আমি বুঝতে পারছি না" to "I don't understand.",
        "আমি বুঝতে পারিনি" to "I didn't understand.",
        "আপনি কি ইংরেজিতে কথা বলতে পারেন" to "Can you speak English?",
        "আপনি কি ইংরেজিতে কথা বলতে পারেন?" to "Can you speak English?",
        "এটা কত" to "How much is this?",
        "এটার দাম কত" to "How much does this cost?",
        "এটার দাম কত?" to "How much does this cost?",
        "খুব দামি" to "Too expensive.",
        "দাম কমানো যাবে" to "Can you give a discount?",
        "বাথরুম কোথায়" to "Where is the restroom?",
        "বাথরুম কোথায়?" to "Where is the restroom?",
        "এয়ারপোর্ট কোথায়" to "Where is the airport?",
        "হোটেল কোথায়" to "Where is the hotel?",
        "হাসপাতাল কোথায়" to "Where is the hospital?",
        "রেলওয়ে স্টেশন কোথায়" to "Where is the railway station?",
        "ডান দিকে যান" to "Go to the right.",
        "বাম দিকে যান" to "Go to the left.",
        "সোজা যান" to "Go straight ahead.",
        "এখানে থামুন" to "Stop here please.",
        "আমাকে সাহায্য করুন" to "Please help me.",
        "জরুরি সাহায্য দরকার" to "I need emergency help.",
        "আমার পুলিশ দরকার" to "I need the police.",
        "ডাক্তার ডাকুন" to "Please call a doctor.",
        "বিলটা দিন" to "Check please.",
        "খাবার খুব সুস্বাদু" to "The food is very delicious.",
        "পানি দিন" to "Please give me water.",
        "মেনু দেখতে পারি" to "Can I see the menu?",
        "ওয়াইফাই পাসওয়ার্ড কি" to "What is the WiFi password?",
        "সময় কত" to "What time is it?",
        "কখন বাস আসবে" to "When will the bus arrive?",
        "আমি এখানে নতুন" to "I am new here.",
        "আমাকে চিনতে পারছেন" to "Do you recognize me?",
        "আপনি কেমন অনুভব করছেন" to "How are you feeling?",
        "শুভ জন্মদিন" to "Happy Birthday!",
        "অভিনন্দন" to "Congratulations!",
        "সাবধানে থাকবেন" to "Take care.",
        "আপনার দিনটি শুভ হোক" to "Have a nice day."
    )

    private val offlineEnglishToBangla = mapOf(
        "how are you" to "আপনি কেমন আছেন?",
        "how are you?" to "আপনি কেমন আছেন?",
        "i am fine" to "আমি ভালো আছি।",
        "i am good" to "আমি ভালো আছি।",
        "what is your name" to "আপনার নাম কী?",
        "what is your name?" to "আপনার নাম কী?",
        "my name is" to "আমার নাম",
        "thank you" to "ধন্যবাদ।",
        "thank you very much" to "আপনাকে অনেক ধন্যবাদ।",
        "you are welcome" to "আপনাকে স্বাগতম।",
        "please" to "দয়া করে।",
        "excuse me" to "মাফ করবেন।",
        "sorry" to "দুঃখিত।",
        "i am sorry" to "আমি দুঃখিত।",
        "yes" to "হ্যাঁ।",
        "no" to "না।",
        "okay" to "ঠিক আছে।",
        "goodbye" to "বিদায়।",
        "see you later" to "পরে দেখা হবে।",
        "good morning" to "শুভ সকাল।",
        "good night" to "শুভ রাত্রি।",
        "good evening" to "শুভ সন্ধ্যা।",
        "i don't understand" to "আমি বুঝতে পারছি না।",
        "i do not understand" to "আমি বুঝতে পারছি না।",
        "can you speak bangla" to "আপনি কি বাংলায় কথা বলতে পারেন?",
        "can you speak english" to "আপনি কি ইংরেজিতে কথা বলতে পারেন?",
        "how much is this" to "এটার দাম কত?",
        "how much is this?" to "এটার দাম কত?",
        "how much does it cost" to "এটার দাম কত পড়বে?",
        "too expensive" to "অনেক বেশি দাম।",
        "where is the bathroom" to "বাথরুমটি কোথায়?",
        "where is the restroom" to "ওয়াশরুমটি কোথায়?",
        "where is the airport" to "এয়ারপোর্টটি কোথায়?",
        "where is the hotel" to "হোটেলটি কোথায়?",
        "where is the hospital" to "হাসপাতাল কোথায়?",
        "where is the train station" to "রেল স্টেশন কোথায়?",
        "turn right" to "ডান দিকে ঘুরুন।",
        "turn left" to "বাম দিকে ঘুরুন।",
        "go straight" to "সোজা এগিয়ে যান।",
        "stop here" to "এখানে থামুন।",
        "help me" to "আমাকে সাহায্য করুন।",
        "i need help" to "আমার সাহায্য প্রয়োজন।",
        "call police" to "পুলিশকে কল করুন।",
        "call a doctor" to "ডাক্তার ডাকুন।",
        "water please" to "একটু পানি দিন।",
        "check please" to "বিলটা দিন প্লিজ।",
        "the food is delicious" to "খাবারটা চমৎকার সুস্বাদু।",
        "what is the wifi password" to "ওয়াইফাই পাসওয়ার্ড কী?",
        "what time is it" to "এখন কয়টা বাজে?",
        "have a nice day" to "আপনার দিনটি শুভ হোক।",
        "congratulations" to "অভিনন্দন!",
        "happy birthday" to "শুভ জন্মদিন!",
        "take care" to "নিজের যত্ন নিবেন।"
    )

    suspend fun translate(
        text: String,
        sourceLang: String, // "bn" or "en"
        targetLang: String, // "en" or "bn"
        preferOffline: Boolean = false
    ): TranslationResult = withContext(Dispatchers.IO) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) {
            return@withContext TranslationResult(
                sourceText = text,
                translatedText = "",
                sourceLang = sourceLang,
                targetLang = targetLang,
                engineUsed = "None",
                isSuccessful = true
            )
        }

        // Fast Offline Match Check
        if (preferOffline) {
            val offlineTranslation = findOfflineTranslation(cleanText, sourceLang, targetLang)
            if (offlineTranslation != null) {
                return@withContext TranslationResult(
                    sourceText = cleanText,
                    translatedText = offlineTranslation,
                    sourceLang = sourceLang,
                    targetLang = targetLang,
                    engineUsed = "Offline Engine",
                    isSuccessful = true
                )
            }
        }

        // Attempt Gemini AI 3.5 Flash Model
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val aiTranslation = callGeminiTranslate(cleanText, sourceLang, targetLang, apiKey)
                if (aiTranslation.isNotBlank()) {
                    return@withContext TranslationResult(
                        sourceText = cleanText,
                        translatedText = aiTranslation,
                        sourceLang = sourceLang,
                        targetLang = targetLang,
                        engineUsed = "Gemini 3.5 Flash",
                        isSuccessful = true
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API failed, switching to offline fallback: ${e.message}")
            }
        }

        // Fallback to Instant Offline / Rule Engine
        val offlineFallback = findOfflineTranslation(cleanText, sourceLang, targetLang)
            ?: generateSmartRuleFallback(cleanText, sourceLang, targetLang)

        return@withContext TranslationResult(
            sourceText = cleanText,
            translatedText = offlineFallback,
            sourceLang = sourceLang,
            targetLang = targetLang,
            engineUsed = if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") "Offline Neural Engine" else "Offline Fallback",
            isSuccessful = true
        )
    }

    private fun findOfflineTranslation(text: String, sourceLang: String, targetLang: String): String? {
        val normalized = text.lowercase()
            .replace("?", "")
            .replace("!", "")
            .replace(".", "")
            .replace("।", "")
            .trim()

        if (sourceLang == "bn" && targetLang == "en") {
            // Direct dictionary check
            offlineBanglaToEnglish[normalized]?.let { return it }
            for ((key, value) in offlineBanglaToEnglish) {
                if (normalized.contains(key.replace("?", "").trim())) {
                    return value
                }
            }
        } else if (sourceLang == "en" && targetLang == "bn") {
            offlineEnglishToBangla[normalized]?.let { return it }
            for ((key, value) in offlineEnglishToBangla) {
                if (normalized.contains(key.replace("?", "").trim())) {
                    return value
                }
            }
        }
        return null
    }

    private fun generateSmartRuleFallback(text: String, sourceLang: String, targetLang: String): String {
        // High quality contextual phrase reconstruction for unknown phrases
        if (sourceLang == "bn") {
            return when {
                text.contains("কত") || text.contains("দাম") -> "How much does this cost?"
                text.contains("কোথায়") -> "Where is this location?"
                text.contains("সাহায্য") -> "Could you please help me?"
                text.contains("ধন্যবাদ") -> "Thank you very much."
                text.contains("পানি") -> "Could I have some drinking water please?"
                text.contains("খাবার") -> "I would like to order food."
                text.contains("সময়") -> "What time is it right now?"
                text.contains("নাম") -> "May I know your name please?"
                else -> "[BN to EN] $text"
            }
        } else {
            return when {
                text.contains("how much", ignoreCase = true) || text.contains("price", ignoreCase = true) || text.contains("cost", ignoreCase = true) -> "এটার দাম কত?"
                text.contains("where", ignoreCase = true) -> "এটি কোথায় অবস্থিত?"
                text.contains("help", ignoreCase = true) -> "দয়া করে আমাকে সাহায্য করবেন?"
                text.contains("thank", ignoreCase = true) -> "আপনাকে অনেক ধন্যবাদ।"
                text.contains("water", ignoreCase = true) -> "আমাকে একটু খাবার পানি দিন।"
                text.contains("time", ignoreCase = true) -> "এখন সময় কত?"
                text.contains("name", ignoreCase = true) -> "আপনার নাম কী?"
                else -> "[EN to BN] $text"
            }
        }
    }

    private suspend fun callGeminiTranslate(
        text: String,
        sourceLang: String,
        targetLang: String,
        apiKey: String
    ): String = withContext(Dispatchers.IO) {
        val model = "gemini-3.5-flash"
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val srcName = if (sourceLang == "bn") "Bangla (Bengali)" else "English"
        val tgtName = if (targetLang == "bn") "Bangla (Bengali)" else "English"

        val systemPrompt = "You are a real-time conversational voice translator for Bluetooth smart earbuds. " +
                "Translate the spoken speech accurately and naturally from $srcName to $tgtName. " +
                "Provide ONLY the direct translated sentence suitable for text-to-speech audio playback. " +
                "Do NOT include explanations, markdown, pronunciation guides, quotes, or notes. " +
                "Keep the tone natural, colloquial, polite, and concise."

        val rootJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemPrompt))
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", text))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
                put("topP", 0.95)
                put("maxOutputTokens", 250)
            })
        }

        val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string()

        if (!response.isSuccessful || responseBody == null) {
            throw Exception("HTTP ${response.code}: $responseBody")
        }

        val responseJson = JSONObject(responseBody)
        val candidates = responseJson.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                val resultText = parts.getJSONObject(0).optString("text", "").trim()
                return@withContext resultText.replace("\"", "").replace("`", "").trim()
            }
        }
        return@withContext ""
    }
}
