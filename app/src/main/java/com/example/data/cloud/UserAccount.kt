package com.example.data.cloud

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserAccount(
    val userId: String,
    val passwordHash: String,
    val displayName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncTime: Long = 0L,
    val autoSyncEnabled: Boolean = true
)

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR,
    OFFLINE
}
