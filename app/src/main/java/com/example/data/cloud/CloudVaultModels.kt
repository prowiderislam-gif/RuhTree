package com.example.data.cloud

import com.example.data.model.FamilyMember
import com.example.data.model.SpouseRelation
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CloudVaultPayload(
    val userId: String,
    val passwordHash: String,
    val vaultTitle: String = "Ruh Tree Family Vault",
    val members: List<FamilyMember> = emptyList(),
    val spouses: List<SpouseRelation> = emptyList(),
    val securityPin: String = "1234",
    val pinEnabled: Boolean = true,
    val backupTimestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0",
    val checksum: String = ""
)

@JsonClass(generateAdapter = true)
data class CloudSyncResult(
    val success: Boolean,
    val message: String,
    val syncedMembersCount: Int = 0,
    val syncedSpousesCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class CloudRestoreResult(
    val success: Boolean,
    val message: String,
    val payload: CloudVaultPayload? = null
)
