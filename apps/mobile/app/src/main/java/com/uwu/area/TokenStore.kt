package com.uwu.area

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TokenStore(private val context: Context) {
    private val prefsName = "secure_prefs"
    private val keyToken = "auth_token"

    private val prefs by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            prefsName,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun saveToken(token: String) {
        withContext(Dispatchers.IO) {
            prefs.edit().putString(keyToken, token).apply()
        }
    }

    fun getToken(): String? = prefs.getString(keyToken, null)

    suspend fun clearToken() {
        withContext(Dispatchers.IO) {
            prefs.edit().remove(keyToken).apply()
        }
    }
}
