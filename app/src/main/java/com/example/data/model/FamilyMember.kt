package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other")
}

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val photoUri: String? = null,
    val dateOfBirth: String? = null, // YYYY-MM-DD
    val timeOfBirth: String? = null, // HH:mm
    val gender: String = Gender.MALE.name,
    val description: String? = null,
    val isDeceased: Boolean = false,
    val dateOfDeath: String? = null, // YYYY-MM-DD
    val isDivorced: Boolean = false,
    val fatherId: Long? = null,
    val motherId: Long? = null,
    val generationLevel: Int = 0,
    val branchTag: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
