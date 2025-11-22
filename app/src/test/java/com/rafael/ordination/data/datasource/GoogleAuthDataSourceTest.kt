package com.rafael.ordnung.data.datasource

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.rafael.ordnung.domain.model.AuthResult
import com.rafael.ordnung.domain.model.AuthSession
import com.rafael.ordnung.domain.model.AuthToken
import com.rafael.ordnung.domain.model.User
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GoogleAuthDataSourceTest {
    
    private lateinit var context: Context
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var dataSource: GoogleAuthDataSource
    
    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        googleSignInClient = mockk(relaxed = true)
        
        // Mock static GoogleSignIn.getClient method
        mockkStatic(GoogleSignIn::class)
        every { GoogleSignIn.getClient(any(), any()) } returns googleSignInClient
        
        dataSource = GoogleAuthDataSource(context)
    }
    
    @Test
    fun `isSignedIn returns true when account exists`() {
        // Given
        val account = mockk<GoogleSignInAccount>()
        mockkStatic(GoogleSignIn::class)
        every { GoogleSignIn.getLastSignedInAccount(context) } returns account
        
        // When
        val result = dataSource.isSignedIn()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isSignedIn returns false when no account exists`() {
        // Given
        mockkStatic(GoogleSignIn::class)
        every { GoogleSignIn.getLastSignedInAccount(context) } returns null
        
        // When
        val result = dataSource.isSignedIn()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `handleSignInResult returns success when ID token is valid`() = runTest {
        // Given
        val idToken = "valid_id_token"
        val account = mockk<GoogleSignInAccount> {
            every { id } returns "user_id"
            every { email } returns "test@example.com"
            every { displayName } returns "Test User"
            every { photoUrl } returns null
            every { isEmailVerified } returns true
            every { account } returns mockk()
        }
        
        mockkStatic(GoogleSignIn::class)
        every { GoogleSignIn.getLastSignedInAccount(context) } returns account
        
        // Mock GoogleAccountCredential
        mockkStatic(com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential::class)
        val credential = mockk<com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential>()
        every { 
            com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential.usingOAuth2(
                any(), 
                any()
            ) 
        } returns credential
        every { credential.selectedAccount } returns mockk()
        every { credential.token } returns "access_token"
        every { credential.scope } returns "calendar drive"
        
        // When
        val result = dataSource.handleSignInResult(idToken)
        
        // Then
        assertTrue(result.success)
        assertNotNull(result.session)
        assertEquals("test@example.com", result.session?.user?.email)
        assertEquals("Test User", result.session?.user?.displayName)
        assertEquals("access_token", result.session?.token?.accessToken)
    }
    
    @Test
    fun `handleSignInResult returns failure when ID token is null`() = runTest {
        // Given
        val idToken = null
        
        // When
        val result = dataSource.handleSignInResult(idToken)
        
        // Then
        assertFalse(result.success)
        assertEquals("No ID token received", result.error)
    }
    
    @Test
    fun `handleSignInResult returns failure when no account found`() = runTest {
        // Given
        val idToken = "valid_id_token"
        
        mockkStatic(GoogleSignIn::class)
        every { GoogleSignIn.getLastSignedInAccount(context) } returns null
        
        // When
        val result = dataSource.handleSignInResult(idToken)
        
        // Then
        assertFalse(result.success)
        assertEquals("No signed-in account found", result.error)
    }
    
    @Test
    fun `signOut calls GoogleSignInClient signOut`() = runTest {
        // Given
        coEvery { googleSignInClient.signOut() } returns mockk()
        
        // When
        val result = dataSource.signOut()
        
        // Then
        coVerify { googleSignInClient.signOut() }
        assertTrue(result.success)
    }
    
    @Test
    fun `signOut returns failure on exception`() = runTest {
        // Given
        val exception = Exception("Sign out failed")
        coEvery { googleSignInClient.signOut() } throws exception
        
        // When
        val result = dataSource.signOut()
        
        // Then
        assertFalse(result.success)
        assertEquals("Sign-out failed: Sign out failed", result.error)
    }
    
    @Test
    fun `revokeAccess calls GoogleSignInClient revokeAccess`() = runTest {
        // Given
        coEvery { googleSignInClient.revokeAccess() } returns mockk()
        
        // When
        val result = dataSource.revokeAccess()
        
        // Then
        coVerify { googleSignInClient.revokeAccess() }
        assertTrue(result.success)
    }
    
    @Test
    fun `refreshToken returns new token when account exists`() = runTest {
        // Given
        val account = mockk<GoogleSignInAccount> {
            every { id } returns "user_id"
            every { email } returns "test@example.com"
            every { displayName } returns "Test User"
            every { photoUrl } returns null
            every { isEmailVerified } returns true
            every { account } returns mockk()
        }
        
        mockkStatic(GoogleSignIn::class)
        every { GoogleSignIn.getLastSignedInAccount(context) } returns account
        
        // Mock GoogleAccountCredential
        mockkStatic(com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential::class)
        val credential = mockk<com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential>()
        every { 
            com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential.usingOAuth2(
                any(), 
                any()
            ) 
        } returns credential
        every { credential.selectedAccount } returns mockk()
        every { credential.token } returns "new_access_token"
        every { credential.scope } returns "calendar drive"
        
        // When
        val result = dataSource.refreshToken()
        
        // Then
        assertTrue(result.success)
        assertNotNull(result.session)
        assertEquals("new_access_token", result.session?.token?.accessToken)
    }
    
    @Test
    fun `refreshToken returns failure when no account exists`() = runTest {
        // Given
        mockkStatic(GoogleSignIn::class)
        every { GoogleSignIn.getLastSignedInAccount(context) } returns null
        
        // When
        val result = dataSource.refreshToken()
        
        // Then
        assertFalse(result.success)
        assertEquals("No signed-in account found", result.error)
    }
}