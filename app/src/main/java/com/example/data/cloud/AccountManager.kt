package com.example.data.cloud

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.security.MessageDigest

class AccountManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("ruh_tree_accounts", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val accountAdapter = moshi.adapter(UserAccount::class.java)

    companion object {
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_USER_PREFIX = "user_account_"
        private const val KEY_USER_PW_PREFIX = "user_pw_hash_"
        private const val SALT = "RuhTreeSecuritySalt2026@"
    }

    fun hashPassword(password: String, userId: String): String {
        val input = "$SALT:$userId:${password.trim()}"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun getCurrentUserId(): String? {
        return prefs.getString(KEY_CURRENT_USER_ID, null)
    }

    fun getCurrentAccount(): UserAccount? {
        val currentId = getCurrentUserId() ?: return null
        val json = prefs.getString(KEY_USER_PREFIX + currentId.lowercase().trim(), null) ?: return null
        return try {
            accountAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    fun isUserLoggedIn(): Boolean {
        return getCurrentUserId() != null
    }

    fun registerAccount(userId: String, rawPassword: String, displayName: String): Result<UserAccount> {
        val cleanId = userId.trim().lowercase()
        if (cleanId.length < 3) {
            return Result.failure(IllegalArgumentException("User ID must be at least 3 characters long"))
        }
        if (rawPassword.trim().length < 4) {
            return Result.failure(IllegalArgumentException("Password must be at least 4 characters long"))
        }

        val existingHash = prefs.getString(KEY_USER_PW_PREFIX + cleanId, null)
        val passwordHash = hashPassword(rawPassword, cleanId)

        val account = UserAccount(
            userId = cleanId,
            passwordHash = passwordHash,
            displayName = if (displayName.isNotBlank()) displayName.trim() else "User $cleanId",
            createdAt = System.currentTimeMillis(),
            lastSyncTime = 0L,
            autoSyncEnabled = true
        )

        val json = accountAdapter.toJson(account)
        prefs.edit()
            .putString(KEY_USER_PREFIX + cleanId, json)
            .putString(KEY_USER_PW_PREFIX + cleanId, passwordHash)
            .putString(KEY_CURRENT_USER_ID, cleanId)
            .apply()

        return Result.success(account)
    }

    fun signInAccount(userId: String, rawPassword: String): Result<UserAccount> {
        val cleanId = userId.trim().lowercase()
        if (cleanId.isBlank() || rawPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter both User ID and Password"))
        }

        val storedHash = prefs.getString(KEY_USER_PW_PREFIX + cleanId, null)
        val computedHash = hashPassword(rawPassword, cleanId)

        if (storedHash != null && storedHash != computedHash) {
            return Result.failure(SecurityException("Incorrect Password for User ID '$cleanId'"))
        }

        var account = getCurrentAccount()
        if (account == null || account.userId != cleanId) {
            val json = prefs.getString(KEY_USER_PREFIX + cleanId, null)
            account = if (json != null) {
                try { accountAdapter.fromJson(json) } catch (e: Exception) { null }
            } else {
                UserAccount(
                    userId = cleanId,
                    passwordHash = computedHash,
                    displayName = "User $cleanId",
                    createdAt = System.currentTimeMillis()
                )
            }
        }

        if (account != null) {
            val updated = account.copy(passwordHash = computedHash)
            prefs.edit()
                .putString(KEY_USER_PREFIX + cleanId, accountAdapter.toJson(updated))
                .putString(KEY_USER_PW_PREFIX + cleanId, computedHash)
                .putString(KEY_CURRENT_USER_ID, cleanId)
                .apply()
            return Result.success(updated)
        }

        return Result.failure(IllegalStateException("Failed to activate user account"))
    }

    fun updateLastSyncTime(timestamp: Long) {
        val account = getCurrentAccount() ?: return
        val updated = account.copy(lastSyncTime = timestamp)
        prefs.edit()
            .putString(KEY_USER_PREFIX + account.userId, accountAdapter.toJson(updated))
            .apply()
    }

    fun isFirstTimeSetupCompleted(): Boolean {
        return prefs.getBoolean("has_completed_first_time_setup", false)
    }

    fun setFirstTimeSetupCompleted(completed: Boolean = true) {
        prefs.edit().putBoolean("has_completed_first_time_setup", completed).apply()
    }

    fun logout() {
        prefs.edit().remove(KEY_CURRENT_USER_ID).apply()
    }
}
