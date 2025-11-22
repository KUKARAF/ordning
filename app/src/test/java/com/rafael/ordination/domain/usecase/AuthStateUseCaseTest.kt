package com.rafael.ordnung.domain.usecase

import com.rafael.ordnung.data.datasource.GoogleAuthDataSource
import com.rafael.ordnung.data.repository.AuthRepository
import com.rafael.ordnung.domain.model.AuthResult
import com.rafael.ordnung.domain.model.AuthSession
import com.rafael.ordnung.domain.model.AuthState
import com.rafael.ordnung.domain.model.AuthToken
import com.rafael.ordnung.domain.model.User
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthStateUseCaseTest {
    
    private lateinit var googleAuthDataSource: GoogleAuthDataSource
    private lateinit var authRepository: AuthRepository
    private lateinit var tokenRefreshUseCase: TokenRefreshUseCase
    private lateinit var authStateUseCase: AuthStateUseCase
    
    @BeforeEach
    fun setup() {
        googleAuthDataSource = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        tokenRefreshUseCase = mockk(relaxed = true)
        
        authStateUseCase = AuthStateUseCase(
            googleAuthDataSource,
            authRepository,
            tokenRefreshUseCase
        )
    }
    
    @Test
    fun `initial state is UNAUTHENTICATED when not signed in`() {
        // Given
        every { googleAuthDataSource.isSignedIn() } returns false
        
        // When
        val authState = authStateUseCase.authState.value
        val currentUser = authStateUseCase.currentUser.value
        
        // Then
        assertEquals(AuthState.UNAUTHENTICATED, authState)
        assertNull(currentUser)
    }
    
    @Test
    fun `initial state is AUTHENTICATED when signed in`() = runTest {
        // Given
        every { googleAuthDataSource.isSignedIn() } returns true
        val user = User(id = "123", email = "test@example.com", displayName = "Test")
        every { authRepository.getCurrentUser() } returns user
        
        // Create new instance to trigger init
        authStateUseCase = AuthStateUseCase(
            googleAuthDataSource,
            authRepository,
            tokenRefreshUseCase
        )
        
        // When
        val authState = authStateUseCase.authState.value
        val currentUser = authStateUseCase.currentUser.value
        
        // Then
        assertEquals(AuthState.AUTHENTICATED, authState)
        assertEquals(user, currentUser)
    }
    
    @Test
    fun `signIn returns success and updates state`() = runTest {
        // Given
        val session = AuthSession(
            user = User(id = "123", email = "test@example.com"),
            token = AuthToken(
                accessToken = "token",
                expiresAt = java.time.LocalDateTime.now().plusHours(1)
            )
        )
        
        val authResult = AuthResult(success = true, session = session)
        coEvery { googleAuthDataSource.signIn() } returns authResult
        coEvery { authRepository.saveAuthSession(any()) } just Runs
        
        // When
        val result = authStateUseCase.signIn()
        
        // Then
        assertTrue(result.success)
        assertEquals(AuthState.AUTHENTICATED, authStateUseCase.authState.value)
        assertEquals(session.user, authStateUseCase.currentUser.value)
        coVerify { authRepository.saveAuthSession(session) }
    }
    
    @Test
    fun `signIn returns failure and updates state to ERROR`() = runTest {
        // Given
        val authResult = AuthResult(success = false, error = "Sign in failed")
        coEvery { googleAuthDataSource.signIn() } returns authResult
        
        // When
        val result = authStateUseCase.signIn()
        
        // Then
        assertFalse(result.success)
        assertEquals(AuthState.ERROR, authStateUseCase.authState.value)
    }
    
    @Test
    fun `signOut clears state and user data`() = runTest {
        // Given
        val authResult = AuthResult(success = true)
        coEvery { googleAuthDataSource.signOut() } returns authResult
        coEvery { authRepository.clearAuthData() } just Runs
        
        // Set initial authenticated state
        every { googleAuthDataSource.isSignedIn() } returns true
        val user = User(id = "123", email = "test@example.com")
        every { authRepository.getCurrentUser() } returns user
        authStateUseCase = AuthStateUseCase(
            googleAuthDataSource,
            authRepository,
            tokenRefreshUseCase
        )
        
        // When
        val result = authStateUseCase.signOut()
        
        // Then
        assertTrue(result.success)
        assertEquals(AuthState.UNAUTHENTICATED, authStateUseCase.authState.value)
        assertNull(authStateUseCase.currentUser.value)
        coVerify { authRepository.clearAuthData() }
    }
    
    @Test
    fun `refreshToken updates state when successful`() = runTest {
        // Given
        val session = AuthSession(
            user = User(id = "123", email = "test@example.com"),
            token = AuthToken(
                accessToken = "new_token",
                expiresAt = java.time.LocalDateTime.now().plusHours(1)
            )
        )
        
        val authResult = AuthResult(success = true, session = session)
        coEvery { tokenRefreshUseCase.refreshTokenIfNeeded() } returns authResult
        
        // When
        val result = authStateUseCase.refreshToken()
        
        // Then
        assertTrue(result.success)
        assertEquals(AuthState.AUTHENTICATED, authStateUseCase.authState.value)
        assertEquals(session.user, authStateUseCase.currentUser.value)
    }
    
    @Test
    fun `refreshToken sets UNAUTHENTICATED when failed`() = runTest {
        // Given
        val authResult = AuthResult(success = false, error = "Token expired")
        coEvery { tokenRefreshUseCase.refreshTokenIfNeeded() } returns authResult
        
        // When
        val result = authStateUseCase.refreshToken()
        
        // Then
        assertFalse(result.success)
        assertEquals(AuthState.UNAUTHENTICATED, authStateUseCase.authState.value)
        assertNull(authStateUseCase.currentUser.value)
    }
    
    @Test
    fun `validateToken returns true when authenticated and token valid`() = runTest {
        // Given
        every { googleAuthDataSource.isSignedIn() } returns true
        val user = User(id = "123", email = "test@example.com")
        every { authRepository.getCurrentUser() } returns user
        coEvery { tokenRefreshUseCase.isTokenValid() } returns true
        
        authStateUseCase = AuthStateUseCase(
            googleAuthDataSource,
            authRepository,
            tokenRefreshUseCase
        )
        
        // When
        val result = authStateUseCase.validateToken()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `validateToken returns false when not authenticated`() = runTest {
        // Given
        every { googleAuthDataSource.isSignedIn() } returns false
        
        authStateUseCase = AuthStateUseCase(
            googleAuthDataSource,
            authRepository,
            tokenRefreshUseCase
        )
        
        // When
        val result = authStateUseCase.validateToken()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `resetError clears ERROR state`() = runTest {
        // Given
        val authResult = AuthResult(success = false, error = "Error")
        coEvery { googleAuthDataSource.signIn() } returns authResult
        authStateUseCase.signIn() // Trigger error state
        
        assertEquals(AuthState.ERROR, authStateUseCase.authState.value)
        
        // When
        authStateUseCase.resetError()
        
        // Then
        assertEquals(AuthState.UNAUTHENTICATED, authStateUseCase.authState.value)
    }
    
    @Test
    fun `isAuthenticated returns true when state is AUTHENTICATED`() {
        // Given - set up authenticated state through constructor
        every { googleAuthDataSource.isSignedIn() } returns true
        val user = User(id = "123", email = "test@example.com")
        every { authRepository.getCurrentUser() } returns user
        
        authStateUseCase = AuthStateUseCase(
            googleAuthDataSource,
            authRepository,
            tokenRefreshUseCase
        )
        
        // When
        val result = authStateUseCase.isAuthenticated()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isAuthenticating returns true when state is AUTHENTICATING`() = runTest {
        // Given
        coEvery { googleAuthDataSource.signIn() } returns AuthResult(success = false, error = "Processing")
        
        // When
        authStateUseCase.signIn()
        val result = authStateUseCase.isAuthenticating()
        
        // Then
        assertTrue(result)
    }
}