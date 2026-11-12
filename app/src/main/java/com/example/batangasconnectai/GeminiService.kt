package com.example.batangasconnectai

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService {
    // Put your API key from Google AI Studio here
    private val apiKey = "YOUR_GEMINI_API_KEY_HERE"

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    suspend fun getTourismResponse(userQuery: String): String = withContext(Dispatchers.IO) {
        val systemPrompt = """
            You are BatangasConnect AI, an enthusiastic tourist guide for Batangas, Philippines.
            Give helpful, friendly recommendations in 2-3 short sentences max.
            User asked: $userQuery
        """.trimIndent()

        return@withContext try {
            val response = model.generateContent(systemPrompt)
            response.text ?: "I'm ready to help you explore Batangas!"
        } catch (e: Exception) {
            "I couldn't fetch live info right now, but Batangas has incredible spots to explore!"
        }
    }
}