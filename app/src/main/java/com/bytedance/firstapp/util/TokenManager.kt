package com.bytedance.firstapp.util

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_EXPIRY_TIME = "expiry_time"
        private const val EXPIRY_DURATION_MS = 3 * 24 * 60 * 60 * 1000L // 3 days
    }

    fun saveToken(token: String, username: String) {
        val expiryTime = System.currentTimeMillis() + EXPIRY_DURATION_MS
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USERNAME, username)
            .putLong(KEY_EXPIRY_TIME, expiryTime)
            .apply()
    }

    fun getToken(): String? {
        val expiryTime = prefs.getLong(KEY_EXPIRY_TIME, 0)
        if (System.currentTimeMillis() > expiryTime) {
            clearToken()
            return null
        }
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun clearToken() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}
