package com.example.pmate.Auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
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
        val KEY_COLLEGE_ID = stringPreferencesKey("college_id")
        val KEY_LAST_JOBS_VISIT = longPreferencesKey("last_jobs_visit")

    }

    // ✅ In-memory collegeId used everywhere in app
    var currentCollegeId: String = "college_1"
        private set

    // ✅ Save full session including collegeId
    suspend fun saveUserSession(
        userId: String,
        email: String,
        role: String,
        collegeId: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOGGED_IN] = true
            prefs[KEY_USER_ID] = userId
            prefs[KEY_EMAIL] = email
            prefs[KEY_ROLE] = role
            prefs[KEY_COLLEGE_ID] = collegeId
        }

        // keep in memory
        currentCollegeId = collegeId
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
        currentCollegeId = "college_1"
    }

    suspend fun isLoggedIn(): Boolean {
        return context.dataStore.data
            .map { it[KEY_LOGGED_IN] ?: false }
            .first()
    }

    suspend fun getUserRole(): String {
        return context.dataStore.data
            .map { it[KEY_ROLE] ?: "" }
            .first()
    }

    suspend fun getUserId(): String {
        return context.dataStore.data
            .map { it[KEY_USER_ID] ?: "" }
            .first()
    }

    //  Call once when app starts OR after login
    suspend fun loadCollegeId() {
        val id = context.dataStore.data
            .map { it[KEY_COLLEGE_ID] ?: "college_1" }
            .first()

        currentCollegeId = id
    }

    //For new badge

    suspend fun getLastJobsVisit(): Long {
        return context.dataStore.data
            .map { it[KEY_LAST_JOBS_VISIT] ?: 0L }
            .first()
    }

    suspend fun setLastJobsVisit(time: Long) {
        context.dataStore.edit {
            it[KEY_LAST_JOBS_VISIT] = time
        }
    }

}
