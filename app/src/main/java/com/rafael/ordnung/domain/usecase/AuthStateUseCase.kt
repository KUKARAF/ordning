package com.rafael.ordination.domain.usecase

import com.rafael.ordination.data.datasource.GoogleAuthDataSource
import com.rafael.ordination.data.repository.AuthRepository
import com.rafael.ordination.domain.model.AuthResult
import com.rafael.ordination.domain.model.AuthSession
import com.rafael.ordination.domain.model.AuthState
import com.rafael.ordination.domain.model.User
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@ViewModelScoped
class AuthStateUseCase @Inject constructor(
    private val googleAuthDataSource: GoogleAuthDataSource,
    private val authRepository: AuthRepository,
    private val tokenRefreshUseCase: TokenRefreshUseCase
) {
    
    private val _authState = MutableStateFlow(AuthState.UNAUTHENTICATED)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    init {
        checkInitialAuthState()
    }
    
    private fun checkInitialAuthState() {
        if (googleAuthDataSource.isSignedIn()) {
            _authState.value = AuthState.AUTHENTICATED
            loadCurrentUser()
        } else {
            _authState.value = AuthState.UNAUTHENTICATED
            _currentUser.value = null
        }
    }
    
    private fun loadCurrentUser() {
        try {
            val user = authRepository.getCurrentUser()
            _currentUser.value = user
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR
        }
    }
    
    suspend fun signIn(): AuthResult {
        return try {
            _authState.value = AuthState.AUTHENTICATING
            
            val result = googleAuthDataSource.signIn()
            if (result.success && result.session != null) {
                // Save session
                authRepository.saveAuthSession(result.session)
                
                _authState.value = AuthState.AUTHENTICATED
                _currentUser.value = result.session.user
                
                AuthResult(success = true, session = result.session)
            } else {
                _authState.value = AuthState.ERROR
                result
            }
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR
            AuthResult(success = false, error = "Sign-in failed: ${e.message}")
        }
    }
    
    suspend fun handleSignInResult(idToken: String?): AuthResult {
        return try {
            _authState.value = AuthState.AUTHENTICATING
            
            val result = googleAuthDataSource.handleSignInResult(idToken)
            if (result.success && result.session != null) {
                // Save session
                authRepository.saveAuthSession(result.session)
                
                _authState.value = AuthState.AUTHENTICATED
                _currentUser.value = result.session.user
                
                AuthResult(success = true, session = result.session)
            } else {
                _authState.value = AuthState.ERROR
                result
            }
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR
            AuthResult(success = false, error = "Sign-in failed: ${e.message}")
        }
    }
    
    suspend fun signOut(): AuthResult {
        return try {
            val result = googleAuthDataSource.signOut()
            
            // Clear local data
            authRepository.clearAuthData()
            
            _authState.value = AuthState.UNAUTHENTICATED
            _currentUser.value = null
            
            result
        } catch (e: Exception) {
            AuthResult(success = false, error = "Sign-out failed: ${e.message}")
        }
    }
    
    suspend fun refreshToken(): AuthResult {
        return try {
            val result = tokenRefreshUseCase.refreshTokenIfNeeded()
            if (result.success) {
                // User is still authenticated
                _authState.value = AuthState.AUTHENTICATED
                if (result.session != null) {
                    _currentUser.value = result.session.user
                }
            } else {
                // Token refresh failed, user needs to sign in again
                _authState.value = AuthState.UNAUTHENTICATED
                _currentUser.value = null
            }
            result
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR
            AuthResult(success = false, error = "Token refresh failed: ${e.message}")
        }
    }
    
    suspend fun revokeAccess(): AuthResult {
        return try {
            val result = googleAuthDataSource.revokeAccess()
            
            // Clear local data
            authRepository.clearAuthData()
            
            _authState.value = AuthState.UNAUTHENTICATED
            _currentUser.value = null
            
            result
        } catch (e: Exception) {
            AuthResult(success = false, error = "Revoke access failed: ${e.message}")
        }
    }
    
    fun isAuthenticated(): Boolean {
        return _authState.value == AuthState.AUTHENTICATED
    }
    
    fun isAuthenticating(): Boolean {
        return _authState.value == AuthState.AUTHENTICATING
    }
    
    fun hasError(): Boolean {
        return _authState.value == AuthState.ERROR
    }
    
    suspend fun validateToken(): Boolean {
        return if (isAuthenticated()) {
            tokenRefreshUseCase.isTokenValid()
        } else {
            false
        }
    }
    
    suspend fun cleanupExpiredTokens() {
        tokenRefreshUseCase.cleanupExpiredTokens()
    }
    
    fun resetError() {
        if (_authState.value == AuthState.ERROR) {
            _authState.value = AuthState.UNAUTHENTICATED
        }
    }
}