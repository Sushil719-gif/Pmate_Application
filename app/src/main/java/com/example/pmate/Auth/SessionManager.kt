package com.example.pmate.Auth



import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        val KEY_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_ROLE = stringPreferencesKey("role")
        val KEY_EMAIL = stringPreferencesKey("email")
    }

    suspend fun saveUserSession(userId: String, email: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOGGED_IN] = true
            prefs[KEY_USER_ID] = userId
            prefs[KEY_EMAIL] = email
            prefs[KEY_ROLE] = role
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return context.dataStore.data.map { it[KEY_LOGGED_IN] ?: false }.first()
    }

    suspend fun getUserRole(): String {
        return context.dataStore.data.map { it[KEY_ROLE] ?: "" }.first()
    }

    suspend fun getUserId(): String {
        return context.dataStore.data.map { it[KEY_USER_ID] ?: "" }.first()
    }
}
