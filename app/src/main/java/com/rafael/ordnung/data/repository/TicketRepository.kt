package com.rafael.ordnung.data.repository

import com.rafael.ordnung.data.database.TicketDao
import com.rafael.ordnung.data.model.TicketEntity
import com.rafael.ordnung.data.model.toDomainModel
import com.rafael.ordnung.data.model.toEntity
import com.rafael.ordnung.domain.model.Ticket
import com.rafael.ordnung.domain.model.TravelType
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