package com.rafael.ordnung.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_tokens")
data class AuthTokenEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val token: String,
    val refreshToken: String? = null,
    val expiresAt: Long,
    val createdAt: Long = System.currentTimeMillis()
)