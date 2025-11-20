package com.rafael.ordination.domain.usecase

import android.net.Uri
import com.rafael.ordination.data.datasource.PdfParsingDataSource
import com.rafael.ordination.data.repository.TicketRepository
import com.rafael.ordination.domain.model.Ticket
import com.rafael.ordination.domain.model.TravelType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ProcessTicketUseCaseTest {
    
    private lateinit var pdfParsingDataSource: PdfParsingDataSource
    private lateinit var ticketRepository: TicketRepository
    private lateinit var processTicketUseCase: ProcessTicketUseCase
    
    @BeforeEach
    fun setup() {
        pdfParsingDataSource = mockk()
        ticketRepository = mockk()
        processTicketUseCase = ProcessTicketUseCase(pdfParsingDataSource, ticketRepository)
    }
    
    @Test
    fun `processTicket should successfully process new ticket`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val expectedTicket = Ticket(
            fileName = "ticket.pdf",
            filePath = "file:///ticket.pdf",
            fileHash = "abc123",
            travelType = TravelType.TRAIN,
            isProcessed = true
        )
        
        coEvery { pdfParsingDataSource.parsePdfFile(uri) } returns expectedTicket
        coEvery { ticketRepository.getTicketByFileHash("abc123") } returns null
        coEvery { ticketRepository.insertTicket(expectedTicket) } returns 1L
        
        // When
        val result = processTicketUseCase.processTicket(uri)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()?.id)
        coVerify { ticketRepository.insertTicket(expectedTicket) }
    }
    
    @Test
    fun `processTicket should reject duplicate ticket`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val existingTicket = Ticket(
            id = 1L,
            fileName = "ticket.pdf",
            filePath = "file:///ticket.pdf",
            fileHash = "abc123",
            travelType = TravelType.TRAIN,
            isProcessed = true
        )
        
        coEvery { pdfParsingDataSource.parsePdfFile(uri) } returns existingTicket
        coEvery { ticketRepository.getTicketByFileHash("abc123") } returns existingTicket
        
        // When
        val result = processTicketUseCase.processTicket(uri)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Ticket already exists", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { ticketRepository.insertTicket(any()) }
    }
    
    @Test
    fun `processTicket should handle parsing errors`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val parsingException = Exception("PDF parsing failed")
        
        coEvery { pdfParsingDataSource.parsePdfFile(uri) } throws parsingException
        
        // When
        val result = processTicketUseCase.processTicket(uri)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(parsingException, result.exceptionOrNull())
    }
    
    @Test
    fun `getAllTickets should return tickets from repository`() = runTest {
        // Given
        val tickets = listOf(
            Ticket(id = 1L, fileName = "ticket1.pdf", filePath = "file1", fileHash = "hash1", travelType = TravelType.TRAIN),
            Ticket(id = 2L, fileName = "ticket2.pdf", filePath = "file2", fileHash = "hash2", travelType = TravelType.BUS)
        )
        
        coEvery { ticketRepository.getAllTickets() } returns flowOf(tickets)
        
        // When
        val result = processTicketUseCase.getAllTickets()
        
        // Then
        result.collect { ticketList ->
            assertEquals(2, ticketList.size)
            assertEquals(tickets, ticketList)
        }
    }
    
    @Test
    fun `getTicketById should return ticket from repository`() = runTest {
        // Given
        val ticket = Ticket(id = 1L, fileName = "ticket.pdf", filePath = "file", fileHash = "hash", travelType = TravelType.TRAIN)
        
        coEvery { ticketRepository.getTicketById(1L) } returns ticket
        
        // When
        val result = processTicketUseCase.getTicketById(1L)
        
        // Then
        assertEquals(ticket, result)
    }
    
    @Test
    fun `deleteTicket should call repository`() = runTest {
        // When
        processTicketUseCase.deleteTicket(1L)
        
        // Then
        coVerify { ticketRepository.deleteTicketById(1L) }
    }
    
    @Test
    fun `reprocessTicket should successfully reprocess existing ticket`() = runTest {
        // Given
        val existingTicket = Ticket(
            id = 1L,
            fileName = "ticket.pdf",
            filePath = "file:///ticket.pdf",
            fileHash = "abc123",
            travelType = TravelType.TRAIN,
            isProcessed = false
        )
        
        val reprocessedTicket = existingTicket.copy(
            isProcessed = true,
            departureLocation = "Berlin",
            arrivalLocation = "Munich"
        )
        
        coEvery { ticketRepository.getTicketById(1L) } returns existingTicket
        coEvery { pdfParsingDataSource.parsePdfFile(Uri.parse("file:///ticket.pdf")) } returns reprocessedTicket
        coEvery { ticketRepository.updateTicket(reprocessedTicket) } returns Unit
        
        // When
        val result = processTicketUseCase.reprocessTicket(1L)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(reprocessedTicket, result.getOrNull())
        coVerify { ticketRepository.updateTicket(reprocessedTicket) }
    }
    
    @Test
    fun `reprocessTicket should handle non-existent ticket`() = runTest {
        // Given
        coEvery { ticketRepository.getTicketById(1L) } returns null
        
        // When
        val result = processTicketUseCase.reprocessTicket(1L)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Ticket not found", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `cleanupFailedTickets should call repository`() = runTest {
        // When
        processTicketUseCase.cleanupFailedTickets()
        
        // Then
        coVerify { ticketRepository.deleteUnprocessedTickets() }
    }
}