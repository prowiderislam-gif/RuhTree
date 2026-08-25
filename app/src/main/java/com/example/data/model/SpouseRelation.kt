package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spouse_relations")
data class SpouseRelation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId1: Long,
    val memberId2: Long,
    val isDivorced: Boolean = false,
    val marriageDate: String? = null,
    val notes: String? = null
)
