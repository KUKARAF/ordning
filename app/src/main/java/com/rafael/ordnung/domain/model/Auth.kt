package com.rafael.ordnung.domain.model

import java.time.LocalDateTime

data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean = false
)

data class AuthToken(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String?,
    val expiresAt: LocalDateTime,
    val tokenType: String = "Bearer",
    val scope: String?
)

data class AuthSession(
    val user: User,
    val token: AuthToken,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class AuthState {
    UNAUTHENTICATED,
    AUTHENTICATING,
    AUTHENTICATED,
    ERROR
}

data class AuthResult(
    val success: Boolean,
    val session: AuthSession? = null,
    val error: String? = null
)