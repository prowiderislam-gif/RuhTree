package com.example.data.security

import android.content.Context
import android.content.SharedPreferences

class SecurityManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("family_tree_security", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN = "security_pin"
        private const val KEY_PIN_ENABLED = "security_pin_enabled"
        const val DEFAULT_PIN = "1234"
    }

    fun isPinEnabled(): Boolean {
        return prefs.getBoolean(KEY_PIN_ENABLED, true)
    }

    fun setPinEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PIN_ENABLED, enabled).apply()
    }

    fun getPin(): String {
        return prefs.getString(KEY_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun setPin(newPin: String) {
        prefs.edit().putString(KEY_PIN, newPin).apply()
    }

    fun verifyPin(inputPin: String): Boolean {
        if (!isPinEnabled()) return true
        val currentPin = getPin()
        return inputPin == currentPin
    }
}
