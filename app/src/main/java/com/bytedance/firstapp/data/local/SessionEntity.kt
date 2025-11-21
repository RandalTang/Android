package com.bytedance.firstapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val lastMessagePreview: String,
    val userId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
