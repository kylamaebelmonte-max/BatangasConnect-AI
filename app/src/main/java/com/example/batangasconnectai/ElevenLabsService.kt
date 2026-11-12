package com.example.batangasconnectai

import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class ElevenLabsService {
    private val client = OkHttpClient()
    private var mediaPlayer: MediaPlayer? = null

    // Ensure your real ElevenLabs API key and Voice ID are set here
    private val apiKey = "sk_c9c669186fa59957229797d14628743b380197e6ca6f1dfb"
    private val voiceId = "21m00Tcm4TlvDq8ikWAM"

    fun speak(text: String) {
        if (text.isBlank()) return

        Log.d("ElevenLabs", "speak() function triggered!")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://api.elevenlabs.io/v1/text-to-speech/$voiceId"

                val jsonBody = JSONObject().apply {
                    put("text", text)
                    put("model_id", "eleven_multilingual_v2")
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("xi-api-key", apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val bytes = response.body?.bytes()
                    if (bytes != null) {
                        val tempFile = File.createTempFile("tts_audio", ".mp3")
                        val fos = FileOutputStream(tempFile)
                        fos.write(bytes)
                        fos.close()

                        CoroutineScope(Dispatchers.Main).launch {
                            mediaPlayer?.release()
                            mediaPlayer = MediaPlayer().apply {
                                setAudioStreamType(AudioManager.STREAM_MUSIC)
                                setDataSource(tempFile.absolutePath)
                                prepare()
                                start()
                            }
                        }
                    }
                } else {
                    Log.e("ElevenLabs", "API Error ${response.code}: ${response.body?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ElevenLabs", "TTS Exception", e)
            }
        }
    }
}