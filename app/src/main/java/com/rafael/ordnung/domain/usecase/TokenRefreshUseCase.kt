package com.rafael.ordination.domain.usecase

import com.rafael.ordination.data.datasource.GoogleAuthDataSource
import com.rafael.ordination.data.repository.AuthRepository
import com.rafael.ordination.domain.model.AuthResult
import com.rafael.ordination.domain.model.AuthSession
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ViewModelScoped
class TokenRefreshUseCase @Inject constructor(
    private val googleAuthDataSource: GoogleAuthDataSource,
    private val authRepository: AuthRepository
) {
    
    suspend fun refreshTokenIfNeeded(): AuthResult {
        return try {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                return AuthResult(
                    success = false,
                    error = "No authenticated user found"
                )
            }
            
            val needsRefresh = authRepository.refreshTokenIfNeeded(currentUser.id)
            if (!needsRefresh) {
                // Token is still valid
                val currentSession = authRepository.getAuthSession(currentUser.id)
                return if (currentSession != null) {
                    AuthResult(success = true, session = currentSession)
                } else {
                    AuthResult(
                        success = false,
                        error = "No valid session found"
                    )
                }
            }
            
            // Token needs refresh
            val refreshResult = googleAuthDataSource.refreshToken()
            if (refreshResult.success && refreshResult.session != null) {
                // Update stored token
                authRepository.updateToken(refreshResult.session.token, refreshResult.session.user.id)
                
                AuthResult(
                    success = true,
                    session = refreshResult.session
                )
            } else {
                AuthResult(
                    success = false,
                    error = refreshResult.error ?: "Token refresh failed"
                )
            }
            
        } catch (e: Exception) {
            AuthResult(
                success = false,
                error = "Token refresh error: ${e.message}"
            )
        }
    }
    
    suspend fun forceRefreshToken(): AuthResult {
        return try {
            val refreshResult = googleAuthDataSource.refreshToken()
            if (refreshResult.success && refreshResult.session != null) {
                // Update stored token
                authRepository.updateToken(refreshResult.session.token, refreshResult.session.user.id)
                
                AuthResult(
                    success = true,
                    session = refreshResult.session
                )
            } else {
                AuthResult(
                    success = false,
                    error = refreshResult.error ?: "Force token refresh failed"
                )
            }
        } catch (e: Exception) {
            AuthResult(
                success = false,
                error = "Force token refresh error: ${e.message}"
            )
        }
    }
    
    suspend fun isTokenValid(): Boolean {
        return try {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) return false
            
            val needsRefresh = authRepository.refreshTokenIfNeeded(currentUser.id)
            !needsRefresh
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun cleanupExpiredTokens() {
        try {
            authRepository.cleanupExpiredTokens()
        } catch (e: Exception) {
            // Log error but don't fail
        }
    }
}