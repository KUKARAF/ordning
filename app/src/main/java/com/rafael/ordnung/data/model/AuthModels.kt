package com.rafael.ordination.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rafael.ordination.domain.model.AuthToken
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

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean = false,
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

fun UserEntity.toDomainModel(): com.rafael.ordination.domain.model.User {
    return com.rafael.ordination.domain.model.User(
        id = id,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
        isEmailVerified = isEmailVerified
    )
}

fun com.rafael.ordination.domain.model.User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
        isEmailVerified = isEmailVerified
    )
}