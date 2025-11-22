package com.rafael.ordnung.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rafael.ordnung.data.model.TicketEntity
import com.rafael.ordnung.domain.model.TravelType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class TicketDaoTest {
    
    private lateinit var database: OrdnungDatabase
    private lateinit var ticketDao: TicketDao
    
    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            OrdnungDatabase::class.java
        ).allowMainThreadQueries().build()
        ticketDao = database.ticketDao()
    }
    
    @After
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun insertAndGetTicket() = runTest {
        // Given
        val ticket = createTestTicket()
        
        // When
        val ticketId = ticketDao.insertTicket(ticket)
        val retrievedTicket = ticketDao.getTicketById(ticketId)
        
        // Then
        assertNotNull(retrievedTicket)
        assertEquals(ticket.fileName, retrievedTicket?.fileName)
        assertEquals(ticket.travelType, retrievedTicket?.travelType)
        assertEquals(ticket.departureLocation, retrievedTicket?.departureLocation)
    }
    
    @Test
    fun getAllTickets_returnsOrderedByProcessedAtDesc() = runTest {
        // Given
        val ticket1 = createTestTicket(fileName = "ticket1.pdf", processedAt = LocalDateTime.now().minusHours(2))
        val ticket2 = createTestTicket(fileName = "ticket2.pdf", processedAt = LocalDateTime.now().minusHours(1))
        val ticket3 = createTestTicket(fileName = "ticket3.pdf", processedAt = LocalDateTime.now())
        
        // When
        ticketDao.insertTicket(ticket1)
        ticketDao.insertTicket(ticket2)
        ticketDao.insertTicket(ticket3)
        
        val allTickets = ticketDao.getAllTickets().first()
        
        // Then
        assertEquals(3, allTickets.size)
        assertEquals("ticket3.pdf", allTickets[0].fileName) // Most recent first
        assertEquals("ticket2.pdf", allTickets[1].fileName)
        assertEquals("ticket1.pdf", allTickets[2].fileName)
    }
    
    @Test
    fun getProcessedTickets_returnsOnlyProcessedTickets() = runTest {
        // Given
        val processedTicket = createTestTicket(fileName = "processed.pdf", isProcessed = true)
        val unprocessedTicket = createTestTicket(fileName = "unprocessed.pdf", isProcessed = false)
        
        // When
        ticketDao.insertTicket(processedTicket)
        ticketDao.insertTicket(unprocessedTicket)
        
        val processedTickets = ticketDao.getProcessedTickets().first()
        val unprocessedTickets = ticketDao.getUnprocessedTickets().first()
        
        // Then
        assertEquals(1, processedTickets.size)
        assertEquals("processed.pdf", processedTickets[0].fileName)
        
        assertEquals(1, unprocessedTickets.size)
        assertEquals("unprocessed.pdf", unprocessedTickets[0].fileName)
    }
    
    @Test
    fun getTicketByFileHash_returnsCorrectTicket() = runTest {
        // Given
        val ticket = createTestTicket(fileHash = "unique_hash_123")
        ticketDao.insertTicket(ticket)
        
        // When
        val foundTicket = ticketDao.getTicketByFileHash("unique_hash_123")
        val notFoundTicket = ticketDao.getTicketByFileHash("nonexistent_hash")
        
        // Then
        assertNotNull(foundTicket)
        assertEquals(ticket.fileName, foundTicket?.fileName)
        assertNull(notFoundTicket)
    }
    
    @Test
    fun getTicketsByTravelType_returnsCorrectTickets() = runTest {
        // Given
        val trainTicket = createTestTicket(fileName = "train.pdf", travelType = TravelType.TRAIN.name)
        val busTicket = createTestTicket(fileName = "bus.pdf", travelType = TravelType.BUS.name)
        val anotherTrainTicket = createTestTicket(fileName = "train2.pdf", travelType = TravelType.TRAIN.name)
        
        // When
        ticketDao.insertTicket(trainTicket)
        ticketDao.insertTicket(busTicket)
        ticketDao.insertTicket(anotherTrainTicket)
        
        val trainTickets = ticketDao.getTicketsByTravelType(TravelType.TRAIN.name).first()
        val busTickets = ticketDao.getTicketsByTravelType(TravelType.BUS.name).first()
        
        // Then
        assertEquals(2, trainTickets.size)
        assertTrue(trainTickets.all { it.travelType == TravelType.TRAIN.name })
        
        assertEquals(1, busTickets.size)
        assertEquals(TravelType.BUS.name, busTickets[0].travelType)
    }
    
    @Test
    fun getTicketsByLocation_returnsMatchingTickets() = runTest {
        // Given
        val berlinTicket = createTestTicket(fileName = "berlin.pdf", departureLocation = "Berlin")
        val munichTicket = createTestTicket(fileName = "munich.pdf", departureLocation = "Munich")
        val anotherBerlinTicket = createTestTicket(fileName = "berlin2.pdf", arrivalLocation = "Berlin")
        
        // When
        ticketDao.insertTicket(berlinTicket)
        ticketDao.insertTicket(munichTicket)
        ticketDao.insertTicket(anotherBerlinTicket)
        
        val berlinTickets = ticketDao.getTicketsByLocation("Berlin").first()
        
        // Then
        assertEquals(2, berlinTickets.size)
        assertTrue(berlinTickets.all { ticket ->
            ticket.departureLocation?.contains("Berlin") == true || 
            ticket.arrivalLocation?.contains("Berlin") == true
        })
    }
    
    @Test
    fun updateTicket_modifiesExistingTicket() = runTest {
        // Given
        val originalTicket = createTestTicket(fileName = "original.pdf", isProcessed = false)
        val ticketId = ticketDao.insertTicket(originalTicket)
        
        val updatedTicket = originalTicket.copy(
            id = ticketId,
            isProcessed = true,
            departureLocation = "Updated Berlin"
        )
        
        // When
        ticketDao.updateTicket(updatedTicket)
        val retrievedTicket = ticketDao.getTicketById(ticketId)
        
        // Then
        assertNotNull(retrievedTicket)
        assertTrue(retrievedTicket?.isProcessed == true)
        assertEquals("Updated Berlin", retrievedTicket?.departureLocation)
    }
    
    @Test
    fun deleteTicket_removesTicket() = runTest {
        // Given
        val ticket = createTestTicket()
        val ticketId = ticketDao.insertTicket(ticket)
        
        // Verify ticket exists
        assertNotNull(ticketDao.getTicketById(ticketId))
        
        // When
        ticketDao.deleteTicketById(ticketId)
        
        // Then
        assertNull(ticketDao.getTicketById(ticketId))
    }
    
    @Test
    fun deleteAllTickets_clearsDatabase() = runTest {
        // Given
        ticketDao.insertTicket(createTestTicket(fileName = "ticket1.pdf"))
        ticketDao.insertTicket(createTestTicket(fileName = "ticket2.pdf"))
        assertEquals(2, ticketDao.getTicketCount())
        
        // When
        ticketDao.deleteAllTickets()
        
        // Then
        assertEquals(0, ticketDao.getTicketCount())
    }
    
    @Test
    fun deleteUnprocessedTickets_removesOnlyUnprocessed() = runTest {
        // Given
        val processedTicket = createTestTicket(fileName = "processed.pdf", isProcessed = true)
        val unprocessedTicket = createTestTicket(fileName = "unprocessed.pdf", isProcessed = false)
        
        ticketDao.insertTicket(processedTicket)
        ticketDao.insertTicket(unprocessedTicket)
        
        assertEquals(2, ticketDao.getTicketCount())
        assertEquals(1, ticketDao.getUnprocessedTicketCount())
        
        // When
        ticketDao.deleteUnprocessedTickets()
        
        // Then
        assertEquals(1, ticketDao.getTicketCount())
        assertEquals(0, ticketDao.getUnprocessedTicketCount())
        assertEquals(1, ticketDao.getProcessedTicketCount())
        
        val remainingTicket = ticketDao.getAllTickets().first().first()
        assertEquals("processed.pdf", remainingTicket.fileName)
    }
    
    @Test
    fun getCountMethods_returnCorrectValues() = runTest {
        // Given - empty database
        assertEquals(0, ticketDao.getTicketCount())
        assertEquals(0, ticketDao.getProcessedTicketCount())
        assertEquals(0, ticketDao.getUnprocessedTicketCount())
        
        // When - add tickets
        ticketDao.insertTicket(createTestTicket(fileName = "processed.pdf", isProcessed = true))
        ticketDao.insertTicket(createTestTicket(fileName = "unprocessed.pdf", isProcessed = false))
        ticketDao.insertTicket(createTestTicket(fileName = "unprocessed2.pdf", isProcessed = false))
        
        // Then
        assertEquals(3, ticketDao.getTicketCount())
        assertEquals(1, ticketDao.getProcessedTicketCount())
        assertEquals(2, ticketDao.getUnprocessedTicketCount())
    }
    
    private fun createTestTicket(
        fileName: String = "test_ticket.pdf",
        fileHash: String = "test_hash_123",
        travelType: String = TravelType.TRAIN.name,
        isProcessed: Boolean = true,
        processedAt: LocalDateTime = LocalDateTime.now(),
        departureLocation: String? = "Berlin",
        arrivalLocation: String? = "Munich"
    ): TicketEntity {
        return TicketEntity(
            fileName = fileName,
            filePath = "file:///$fileName",
            fileHash = fileHash,
            passengerName = "Test User",
            travelType = travelType,
            departureLocation = departureLocation,
            arrivalLocation = arrivalLocation,
            departureTime = LocalDateTime.now(),
            arrivalTime = LocalDateTime.now().plusHours(2),
            trainNumber = "ICE 123",
            seatNumber = "23A",
            carriageNumber = "12",
            qrCodeData = "QR_CODE_DATA",
            rawText = "Raw ticket text",
            processedAt = processedAt,
            isProcessed = isProcessed,
            errorMessage = if (isProcessed) null else "Test error"
        )
    }
}