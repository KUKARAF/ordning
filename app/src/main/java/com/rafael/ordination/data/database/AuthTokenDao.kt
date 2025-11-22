package com.rafael.ordnung.data.database

import androidx.room.*
import com.rafael.ordnung.data.model.AuthTokenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthTokenDao {
    
    @Query("SELECT * FROM auth_tokens WHERE userId = :userId")
    suspend fun getTokenByUserId(userId: String): AuthTokenEntity?
    
    @Query("SELECT * FROM auth_tokens")
    fun getAllTokens(): Flow<List<AuthTokenEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(token: AuthTokenEntity)
    
    @Update
    suspend fun updateToken(token: AuthTokenEntity)
    
    @Delete
    suspend fun deleteToken(token: AuthTokenEntity)
    
    @Query("DELETE FROM auth_tokens WHERE userId = :userId")
    suspend fun deleteTokenByUserId(userId: String)
    
    @Query("DELETE FROM auth_tokens")
    suspend fun deleteAllTokens()
    
    @Query("SELECT * FROM auth_tokens WHERE expiresAt < :currentTime")
    suspend fun getExpiredTokens(currentTime: java.time.LocalDateTime): List<AuthTokenEntity>
    
    @Query("DELETE FROM auth_tokens WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredTokens(currentTime: java.time.LocalDateTime)
}