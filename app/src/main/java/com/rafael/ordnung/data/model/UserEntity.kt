package com.rafael.ordnung.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val name: String,
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)