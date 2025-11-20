package com.rafael.ordnung.data.database

import androidx.room.*
import com.rafael.ordnung.data.model.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    
    @Query("SELECT * FROM tickets ORDER BY processedAt DESC")
    fun getAllTickets(): Flow<List<TicketEntity>>
    
    @Query("SELECT * FROM tickets WHERE id = :id")
    suspend fun getTicketById(id: Long): TicketEntity?
    
    @Query("SELECT * FROM tickets WHERE fileHash = :fileHash")
    suspend fun getTicketByFileHash(fileHash: String): TicketEntity?
    
    @Query("SELECT * FROM tickets WHERE isProcessed = 1 ORDER BY processedAt DESC")
    fun getProcessedTickets(): Flow<List<TicketEntity>>
    
    @Query("SELECT * FROM tickets WHERE isProcessed = 0 ORDER BY processedAt DESC")
    fun getUnprocessedTickets(): Flow<List<TicketEntity>>
    
    @Query("SELECT * FROM tickets WHERE departureTime >= :startDate AND departureTime <= :endDate ORDER BY departureTime ASC")
    fun getTicketsByDateRange(startDate: String, endDate: String): Flow<List<TicketEntity>>
    
    @Query("SELECT * FROM tickets WHERE travelType = :travelType ORDER BY processedAt DESC")
    fun getTicketsByTravelType(travelType: String): Flow<List<TicketEntity>>
    
    @Query("SELECT * FROM tickets WHERE departureLocation LIKE '%' || :location || '%' OR arrivalLocation LIKE '%' || :location || '%' ORDER BY processedAt DESC")
    fun getTicketsByLocation(location: String): Flow<List<TicketEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>): List<Long>
    
    @Update
    suspend fun updateTicket(ticket: TicketEntity)
    
    @Delete
    suspend fun deleteTicket(ticket: TicketEntity)
    
    @Query("DELETE FROM tickets WHERE id = :id")
    suspend fun deleteTicketById(id: Long)
    
    @Query("DELETE FROM tickets")
    suspend fun deleteAllTickets()
    
    @Query("DELETE FROM tickets WHERE isProcessed = 0")
    suspend fun deleteUnprocessedTickets()
    
    @Query("SELECT COUNT(*) FROM tickets")
    suspend fun getTicketCount(): Int
    
    @Query("SELECT COUNT(*) FROM tickets WHERE isProcessed = 1")
    suspend fun getProcessedTicketCount(): Int
    
    @Query("SELECT COUNT(*) FROM tickets WHERE isProcessed = 0")
    suspend fun getUnprocessedTicketCount(): Int
}