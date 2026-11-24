package com.ogapp.subai.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Encrypted storage for the user's own AI API key (bring-your-own-key model).
 * Backed by Android Keystore via EncryptedSharedPreferences, so the key is not
 * readable from plain prefs or a rooted file browser dump.
 */
object SecurePrefs {

    private const val FILE = "subai_secure_prefs"
    private const val KEY_API = "api_key"
    private const val KEY_PROVIDER = "ai_provider"

    private fun prefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getApiKey(context: Context): String = prefs(context).getString(KEY_API, "").orEmpty()

    fun setApiKey(context: Context, value: String) {
        prefs(context).edit().putString(KEY_API, value.trim()).apply()
    }

    fun getProvider(context: Context): String =
        prefs(context).getString(KEY_PROVIDER, AiProvider.GEMINI.id) ?: AiProvider.GEMINI.id

    fun setProvider(context: Context, id: String) {
        prefs(context).edit().putString(KEY_PROVIDER, id).apply()
    }

    fun hasKey(context: Context): Boolean = getApiKey(context).isNotBlank()
}

/** Supported bring-your-own-key vision providers. */
enum class AiProvider(val id: String, val label: String, val keyHint: String) {
    GEMINI("gemini", "Google Gemini", "Starts with AIza… — free tier available at aistudio.google.com"),
    CLAUDE("claude", "Anthropic Claude", "Starts with sk-ant-… from console.anthropic.com"),
    OPENAI("openai", "OpenAI", "Starts with sk-… from platform.openai.com");

    companion object {
        fun fromId(id: String): AiProvider = entries.firstOrNull { it.id == id } ?: GEMINI
    }
}
