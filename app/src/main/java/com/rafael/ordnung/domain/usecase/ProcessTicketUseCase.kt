package com.rafael.ordnung.domain.usecase

import android.net.Uri
import com.rafael.ordnung.data.datasource.PdfParsingDataSource
import com.rafael.ordnung.data.repository.TicketRepository
import com.rafael.ordnung.domain.model.Ticket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessTicketUseCase @Inject constructor(
    private val pdfParsingDataSource: PdfParsingDataSource,
    private val ticketRepository: TicketRepository
) {
    
    suspend fun processTicket(uri: Uri): Result<Ticket> {
        return try {
            // Check if ticket already exists
            val existingTicket = ticketRepository.getTicketByFileHash("")
            // Note: We need to get the file hash first, but that requires parsing
            // This is a simplified version - in production, you'd want to optimize this
            
            // Parse the PDF
            val ticket = pdfParsingDataSource.parsePdfFile(uri)
            
            // Check for duplicates based on file hash
            val duplicate = ticketRepository.getTicketByFileHash(ticket.fileHash)
            if (duplicate != null) {
                return Result.failure(Exception("Ticket already exists"))
            }
            
            // Save to database
            val ticketId = ticketRepository.insertTicket(ticket)
            val savedTicket = ticket.copy(id = ticketId)
            
            Result.success(savedTicket)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllTickets(): Flow<List<Ticket>> {
        return ticketRepository.getAllTickets()
    }
    
    suspend fun getTicketById(id: Long): Ticket? {
        return ticketRepository.getTicketById(id)
    }
    
    suspend fun deleteTicket(id: Long) {
        ticketRepository.deleteTicketById(id)
    }
    
    suspend fun reprocessTicket(id: Long): Result<Ticket> {
        val ticket = ticketRepository.getTicketById(id)
            ?: return Result.failure(Exception("Ticket not found"))
        
        return try {
            val uri = Uri.parse(ticket.filePath)
            val reprocessedTicket = pdfParsingDataSource.parsePdfFile(uri)
            val updatedTicket = reprocessedTicket.copy(id = id)
            
            ticketRepository.updateTicket(updatedTicket)
            Result.success(updatedTicket)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun cleanupFailedTickets() {
        ticketRepository.deleteUnprocessedTickets()
    }
}