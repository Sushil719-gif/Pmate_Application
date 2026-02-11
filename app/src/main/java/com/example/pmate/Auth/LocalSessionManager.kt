package com.example.pmate.Auth



import androidx.compose.runtime.compositionLocalOf

val LocalSessionManager = compositionLocalOf<SessionManager> {
    error("SessionManager not provided")
}
