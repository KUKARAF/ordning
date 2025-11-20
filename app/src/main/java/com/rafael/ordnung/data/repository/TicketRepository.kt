package com.rafael.ordination.data.repository

import com.rafael.ordination.data.database.TicketDao
import com.rafael.ordination.data.model.TicketEntity
import com.rafael.ordination.domain.model.Ticket
import com.rafael.ordination.domain.model.TravelType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketRepository @Inject constructor(
    private val ticketDao: TicketDao
) {
    
    fun getAllTickets(): Flow<List<Ticket>> {
        return ticketDao.getAllTickets().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getTicketById(id: Long): Ticket? {
        return ticketDao.getTicketById(id)?.toDomainModel()
    }
    
    suspend fun getTicketByFileHash(fileHash: String): Ticket? {
        return ticketDao.getTicketByFileHash(fileHash)?.toDomainModel()
    }
    
    fun getProcessedTickets(): Flow<List<Ticket>> {
        return ticketDao.getProcessedTickets().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getUnprocessedTickets(): Flow<List<Ticket>> {
        return ticketDao.getUnprocessedTickets().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getTicketsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Ticket>> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return ticketDao.getTicketsByDateRange(
            startDate.format(formatter),
            endDate.format(formatter)
        ).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getTicketsByTravelType(travelType: TravelType): Flow<List<Ticket>> {
        return ticketDao.getTicketsByTravelType(travelType.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getTicketsByLocation(location: String): Flow<List<Ticket>> {
        return ticketDao.getTicketsByLocation(location).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun insertTicket(ticket: Ticket): Long {
        return ticketDao.insertTicket(ticket.toEntity())
    }
    
    suspend fun updateTicket(ticket: Ticket) {
        ticketDao.updateTicket(ticket.toEntity())
    }
    
    suspend fun deleteTicket(ticket: Ticket) {
        ticketDao.deleteTicket(ticket.toEntity())
    }
    
    suspend fun deleteTicketById(id: Long) {
        ticketDao.deleteTicketById(id)
    }
    
    suspend fun deleteAllTickets() {
        ticketDao.deleteAllTickets()
    }
    
    suspend fun deleteUnprocessedTickets() {
        ticketDao.deleteUnprocessedTickets()
    }
    
    suspend fun getTicketCount(): Int {
        return ticketDao.getTicketCount()
    }
    
    suspend fun getProcessedTicketCount(): Int {
        return ticketDao.getProcessedTicketCount()
    }
    
    suspend fun getUnprocessedTicketCount(): Int {
        return ticketDao.getUnprocessedTicketCount()
    }
}

// Extension functions for mapping between domain and data models
private fun TicketEntity.toDomainModel(): Ticket {
    return Ticket(
        id = id,
        fileName = fileName,
        filePath = filePath,
        fileHash = fileHash,
        passengerName = passengerName,
        travelType = TravelType.valueOf(travelType),
        departureLocation = departureLocation,
        arrivalLocation = arrivalLocation,
        departureTime = departureTime,
        arrivalTime = arrivalTime,
        trainNumber = trainNumber,
        seatNumber = seatNumber,
        carriageNumber = carriageNumber,
        qrCodeData = qrCodeData,
        rawText = rawText,
        processedAt = processedAt,
        isProcessed = isProcessed,
        errorMessage = errorMessage
    )
}

private fun Ticket.toEntity(): TicketEntity {
    return TicketEntity(
        id = id,
        fileName = fileName,
        filePath = filePath,
        fileHash = fileHash,
        passengerName = passengerName,
        travelType = travelType.name,
        departureLocation = departureLocation,
        arrivalLocation = arrivalLocation,
        departureTime = departureTime,
        arrivalTime = arrivalTime,
        trainNumber = trainNumber,
        seatNumber = seatNumber,
        carriageNumber = carriageNumber,
        qrCodeData = qrCodeData,
        rawText = rawText,
        processedAt = processedAt,
        isProcessed = isProcessed,
        errorMessage = errorMessage
    )
}