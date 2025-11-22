package com.rafael.ordnung.data.datasource

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory

import com.rafael.ordnung.domain.model.AuthResult
import com.rafael.ordnung.domain.model.AuthSession
import com.rafael.ordnung.domain.model.AuthToken
import com.rafael.ordnung.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.android.gms.tasks.await
import com.google.android.gms.common.Scopes
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.rafael.ordnung.R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .requestScopes(
                com.google.api.services.calendar.CalendarScopes.CALENDAR,
                com.google.api.services.calendar.CalendarScopes.CALENDAR_EVENTS,
                com.google.api.services.drive.DriveScopes.DRIVE_FILE,
                com.google.api.services.drive.DriveScopes.DRIVE_APPDATA
            )
            .build()
        
        GoogleSignIn.getClient(context, gso)
    }
    
    suspend fun signIn(): AuthResult {
        return try {
            // Note: This would typically be called from UI with Activity result
            // For now, we'll return the intent that should be launched
            val signInIntent = googleSignInClient.signInIntent
            AuthResult(
                success = false,
                error = "UI flow required. Use signInIntent: $signInIntent"
            )
        } catch (e: Exception) {
            AuthResult(
                success = false,
                error = "Sign-in failed: ${e.message}"
            )
        }
    }
    
    suspend fun handleSignInResult(idToken: String?): AuthResult {
        return try {
            if (idToken == null) {
                return AuthResult(
                    success = false,
                    error = "No ID token received"
                )
            }
            
            // Get GoogleSignInAccount from the signed-in account
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                return AuthResult(
                    success = false,
                    error = "No signed-in account found"
                )
            }
            
            val user = User(
                id = account.id ?: "",
                email = account.email ?: "",
                displayName = account.displayName,
                photoUrl = account.photoUrl?.toString(),
                        isEmailVerified = account.email != null // GoogleSignInAccount doesn't have isEmailVerified
            )
            
            // Get OAuth token for API calls
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(
                    com.google.api.services.calendar.CalendarScopes.CALENDAR,
                    com.google.api.services.calendar.CalendarScopes.CALENDAR_EVENTS,
                    com.google.api.services.drive.DriveScopes.DRIVE_FILE,
                    com.google.api.services.drive.DriveScopes.DRIVE_APPDATA
                )
            )
            credential.selectedAccount = account.account
            
            // Get access token
            val token = credential.token
            val expiresAt = LocalDateTime.now().plusHours(1) // Tokens typically expire in 1 hour
            
            val authToken = AuthToken(
                accessToken = token,
                refreshToken = null, // Google handles refresh automatically
                idToken = idToken,
                expiresAt = expiresAt,
                tokenType = "Bearer",
                scope = credential.scope
            )
            
            val session = AuthSession(
                user = user,
                token = authToken,
                isActive = true
            )
            
            AuthResult(
                success = true,
                session = session
            )
            
        } catch (e: Exception) {
            AuthResult(
                success = false,
                error = "Failed to process sign-in result: ${e.message}"
            )
        }
    }
    
    suspend fun signOut(): AuthResult {
        return try {
            googleSignInClient.signOut().await()
            AuthResult(success = true)
        } catch (e: Exception) {
            AuthResult(
                success = false,
                error = "Sign-out failed: ${e.message}"
            )
        }
    }
    
    suspend fun revokeAccess(): AuthResult {
        return try {
            googleSignInClient.revokeAccess().await()
            AuthResult(success = true)
        } catch (e: Exception) {
            AuthResult(
                success = false,
                error = "Revoke access failed: ${e.message}"
            )
        }
    }
    
    fun getSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
    
    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }
    
    suspend fun refreshToken(): AuthResult {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                return AuthResult(
                    success = false,
                    error = "No signed-in account found"
                )
            }
            
            // Get fresh token
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(
                    com.google.api.services.calendar.CalendarScopes.CALENDAR,
                    com.google.api.services.calendar.CalendarScopes.CALENDAR_EVENTS,
                    com.google.api.services.drive.DriveScopes.DRIVE_FILE,
                    com.google.api.services.drive.DriveScopes.DRIVE_APPDATA
                )
            )
            credential.selectedAccount = account.account
            
            val token = credential.token
            val expiresAt = LocalDateTime.now().plusHours(1)
            
            val authToken = AuthToken(
                accessToken = token,
                refreshToken = null,
                idToken = null, // ID token doesn't need refresh
                expiresAt = expiresAt,
                tokenType = "Bearer",
                scope = credential.scope
            )
            
            AuthResult(
                success = true,
                session = AuthSession(
                    user = User(
                        id = account.id ?: "",
                        email = account.email ?: "",
                        displayName = account.displayName,
                        photoUrl = account.photoUrl?.toString(),
                isEmailVerified = account.email != null // GoogleSignInAccount doesn't have isEmailVerified
                    ),
                    token = authToken,
                    isActive = true
                )
            )
            
        } catch (e: Exception) {
            AuthResult(
                success = false,
                error = "Token refresh failed: ${e.message}"
            )
        }
    }
    
    fun getSignInIntent(): android.content.Intent {
        return googleSignInClient.signInIntent
    }
}