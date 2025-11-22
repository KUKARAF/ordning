package com.rafael.ordnung.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rafael.ordnung.domain.model.AuthToken
import java.time.LocalDateTime

@Entity(tableName = "auth_tokens")
data class AuthTokenEntity(
    @PrimaryKey
    val userId: String,
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String?,
    val expiresAt: LocalDateTime,
    val tokenType: String,
    val scope: String?,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

// Extension functions for mapping
fun AuthTokenEntity.toDomainModel(): AuthToken {
    return AuthToken(
        accessToken = accessToken,
        refreshToken = refreshToken,
        idToken = idToken,
        expiresAt = expiresAt,
        tokenType = tokenType,
        scope = scope
    )
}

fun AuthToken.toEntity(userId: String): AuthTokenEntity {
    return AuthTokenEntity(
        userId = userId,
        accessToken = accessToken,
        refreshToken = refreshToken,
        idToken = idToken,
        expiresAt = expiresAt,
        tokenType = tokenType,
        scope = scope
    )
}