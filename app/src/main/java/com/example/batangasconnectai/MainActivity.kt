package com.example.batangasconnectai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {

    private lateinit var elevenLabsService: ElevenLabsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        elevenLabsService = ElevenLabsService()

        setContent {
            MaterialTheme {
                var isLoggedIn by remember { mutableStateOf(false) }

                if (!isLoggedIn) {
                    AuthScreen(onLoginSuccess = { isLoggedIn = true })
                } else {
                    MainDashboardScreen(
                        elevenLabsService = elevenLabsService,
                        onLogout = { isLoggedIn = false }
                    )
                }
            }
        }
    }
}
