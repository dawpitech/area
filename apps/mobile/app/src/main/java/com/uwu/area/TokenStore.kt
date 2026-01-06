package com.uwu.area

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenStore {
    private const val PREFS_NAME = "com.uwu.area.prefs"
    private const val TOKEN_KEY = "token"

    private lateinit var appContext: Context
    private val prefs: SharedPreferences by lazy { createPrefs(appContext) }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun getToken(): String? {
        return try {
            prefs.getString(TOKEN_KEY, null)
        } catch (e: Exception) {
            Log.w("TokenStore", "getToken failed", e)
            null
        }
    }

    fun setToken(token: String?) {
        try {
            prefs.edit().apply {
                if (token == null) remove(TOKEN_KEY) else putString(TOKEN_KEY, token)
            }.apply()
        } catch (e: Exception) {
            Log.w("TokenStore", "setToken failed", e)
        }
    }

    fun clearToken() {
        setToken(null)
    }

    private fun createPrefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.w("TokenStore", "EncryptedSharedPreferences unavailable, falling back to plain SharedPreferences", e)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
}
