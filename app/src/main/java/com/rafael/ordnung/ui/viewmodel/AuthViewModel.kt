package com.rafael.ordnung.ui.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.ordnung.data.datasource.GoogleAuthDataSource
import com.rafael.ordnung.data.repository.AuthRepository
import com.rafael.ordnung.domain.model.AuthState
import com.rafael.ordnung.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
    val errorMessage: String? = null,
    val signInIntent: Intent? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val googleAuthDataSource: GoogleAuthDataSource,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            if (googleAuthDataSource.isSignedIn()) {
                val currentUser = authRepository.getCurrentUser()
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = true,
                    currentUser = currentUser
                )
            }
        }
    }
    
    fun signIn() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val signInIntent = googleAuthDataSource.getSignInIntent()
                _uiState.value = _uiState.value.copy(
                    signInIntent = signInIntent,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to start sign-in: ${e.message}"
                )
            }
        }
    }
    
    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Extract ID token from the result
                val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                val idToken = account.idToken
                
                val authResult = googleAuthDataSource.handleSignInResult(idToken)
                
                if (authResult.success && authResult.session != null) {
                    // Save auth session
                    authRepository.saveAuthSession(authResult.session)
                    
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        currentUser = authResult.session.user,
                        isLoading = false,
                        errorMessage = null,
                        signInIntent = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = authResult.error ?: "Sign-in failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Sign-in failed: ${e.message}"
                )
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Sign out from Google
                googleAuthDataSource.signOut()
                
                // Clear local auth data
                authRepository.clearAuthData()
                
                _uiState.value = AuthUiState(
                    isAuthenticated = false,
                    currentUser = null,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Sign-out failed: ${e.message}"
                )
            }
        }
    }
    
    fun refreshToken() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val authResult = googleAuthDataSource.refreshToken()
                
                if (authResult.success && authResult.session != null) {
                    // Update token in repository
                    authRepository.updateToken(
                        authResult.session.token,
                        authResult.session.user.id
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = authResult.error ?: "Token refresh failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Token refresh failed: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}