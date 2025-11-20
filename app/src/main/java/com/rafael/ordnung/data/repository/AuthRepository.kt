package com.rafael.ordination.data.repository

import com.rafael.ordination.data.database.AuthTokenDao
import com.rafael.ordination.data.database.UserDao
import com.rafael.ordination.data.model.toDomainModel
import com.rafael.ordination.data.model.toEntity
import com.rafael.ordination.domain.model.AuthSession
import com.rafael.ordination.domain.model.AuthToken
import com.rafael.ordination.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val authTokenDao: AuthTokenDao
) {
    
    suspend fun saveAuthSession(session: AuthSession) {
        // Save user
        userDao.insertUser(session.user.toEntity())
        
        // Save auth token
        authTokenDao.insertToken(session.token.toEntity(session.user.id))
    }
    
    suspend fun getAuthSession(userId: String): AuthSession? {
        val userEntity = userDao.getUserById(userId) ?: return null
        val tokenEntity = authTokenDao.getTokenByUserId(userId) ?: return null
        
        return AuthSession(
            user = userEntity.toDomainModel(),
            token = tokenEntity.toDomainModel(),
            isActive = !isTokenExpired(tokenEntity.expiresAt)
        )
    }
    
    suspend fun getCurrentUser(): User? {
        // For simplicity, get the first user (in real app, you'd track current user ID)
        return userDao.getAllUsers().map { users ->
            users.firstOrNull()?.toDomainModel()
        }.value
    }
    
    suspend fun getCurrentToken(): AuthToken? {
        // For simplicity, get the first token (in real app, you'd track current user ID)
        return authTokenDao.getAllTokens().map { tokens ->
            tokens.firstOrNull()?.toDomainModel()
        }.value
    }
    
    suspend fun updateToken(token: AuthToken, userId: String) {
        authTokenDao.updateToken(token.toEntity(userId))
    }
    
    suspend fun clearAuthData() {
        authTokenDao.deleteAllTokens()
        userDao.deleteAllUsers()
    }
    
    suspend fun clearAuthData(userId: String) {
        authTokenDao.deleteTokenByUserId(userId)
        userDao.deleteUserById(userId)
    }
    
    suspend fun cleanupExpiredTokens() {
        authTokenDao.deleteExpiredTokens(LocalDateTime.now())
    }
    
    fun isTokenExpired(expiresAt: LocalDateTime): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }
    
    suspend fun refreshTokenIfNeeded(userId: String): Boolean {
        val token = authTokenDao.getTokenByUserId(userId) ?: return false
        
        return if (isTokenExpired(token.expiresAt)) {
            // Token is expired, needs refresh
            true
        } else {
            // Check if token expires within next 5 minutes
            val fiveMinutesFromNow = LocalDateTime.now().plusMinutes(5)
            token.expiresAt.isBefore(fiveMinutesFromNow)
        }
    }
}