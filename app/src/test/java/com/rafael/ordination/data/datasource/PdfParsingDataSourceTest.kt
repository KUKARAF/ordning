package com.rafael.ordination.data.datasource

import android.content.Context
import android.net.Uri
import com.rafael.ordination.domain.model.TravelType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.time.LocalDateTime

class PdfParsingDataSourceTest {
    
    private lateinit var context: Context
    private lateinit var dataSource: PdfParsingDataSource
    private lateinit var qrCodeDataSource: QrCodeDataSource
    
    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        qrCodeDataSource = mockk(relaxed = true)
        dataSource = PdfParsingDataSource(context, qrCodeDataSource)
    }
    
    @Test
    fun `parsePdfFile should extract train ticket information correctly`() = runTest {
        // Given
        val samplePdfText = """
            Deutsche Bahn
            ICE 1234
            von Berlin Hbf
            nach München Hbf
            ab 15.12.2023 10:30
            an 15.12.2023 14:45
            Passagier: Max Mustermann
            Wagen 12
            Sitzplatz 23A
        """.trimIndent()
        
        val mockUri = mockk<Uri>()
        every { mockUri.lastPathSegment } returns "ticket.pdf"
        every { mockUri.toString() } returns "file:///ticket.pdf"
        
        mockkStatic("kotlin.io.ByteStreamsKt")
        every { context.contentResolver.openInputStream(mockUri) } returns 
            ByteArrayInputStream(samplePdfText.toByteArray())
        
        every { qrCodeDataSource.extractQrCodesFromPdf(any()) } returns emptyList()
        
        // When
        val result = dataSource.parsePdfFile(mockUri)
        
        // Then
        assertTrue(result.isProcessed)
        assertEquals("ticket.pdf", result.fileName)
        assertEquals(TravelType.TRAIN, result.travelType)
        assertEquals("Berlin Hbf", result.departureLocation)
        assertEquals("München Hbf", result.arrivalLocation)
        assertEquals("Max Mustermann", result.passengerName)
        assertEquals("ICE 1234", result.trainNumber)
        assertEquals("12", result.carriageNumber)
        assertEquals("23A", result.seatNumber)
    }
    
    @Test
    fun `parsePdfFile should detect bus tickets correctly`() = runTest {
        // Given
        val samplePdfText = """
            FlixBus
            von Berlin
            nach Prague
            ab 15.12.2023 08:00
            Passagier: Anna Schmidt
        """.trimIndent()
        
        val mockUri = mockk<Uri>()
        every { mockUri.lastPathSegment } returns "bus_ticket.pdf"
        every { mockUri.toString() } returns "file:///bus_ticket.pdf"
        
        mockkStatic("kotlin.io.ByteStreamsKt")
        every { context.contentResolver.openInputStream(mockUri) } returns 
            ByteArrayInputStream(samplePdfText.toByteArray())
        
        every { qrCodeDataSource.extractQrCodesFromPdf(any()) } returns emptyList()
        
        // When
        val result = dataSource.parsePdfFile(mockUri)
        
        // Then
        assertTrue(result.isProcessed)
        assertEquals(TravelType.BUS, result.travelType)
        assertEquals("Berlin", result.departureLocation)
        assertEquals("Prague", result.arrivalLocation)
    }
    
    @Test
    fun `parsePdfFile should handle parsing errors gracefully`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        every { mockUri.lastPathSegment } returns "corrupted.pdf"
        every { mockUri.toString() } returns "file:///corrupted.pdf"
        
        every { context.contentResolver.openInputStream(mockUri) } returns null
        
        // When
        val result = dataSource.parsePdfFile(mockUri)
        
        // Then
        assertFalse(result.isProcessed)
        assertEquals("corrupted.pdf", result.fileName)
        assertNotNull(result.errorMessage)
    }
    
    @Test
    fun `determineTravelType should return correct types`() {
        // Test train detection
        assertEquals(TravelType.TRAIN, dataSource.determineTravelType("ICE 1234 nach Berlin"))
        assertEquals(TravelType.TRAIN, dataSource.determineTravelType("Zug nach München"))
        
        // Test bus detection
        assertEquals(TravelType.BUS, dataSource.determineTravelType("Bus von Berlin"))
        assertEquals(TravelType.BUS, dataSource.determineTravelType("FlixBus Ticket"))
        
        // Test flight detection
        assertEquals(TravelType.FLIGHT, dataSource.determineTravelType("Flug LH123"))
        assertEquals(TravelType.FLIGHT, dataSource.determineTravelType("Flight to New York"))
        
        // Test unknown
        assertEquals(TravelType.UNKNOWN, dataSource.determineTravelType("Random document"))
    }
    
    @Test
    fun `extractPassengerName should find names correctly`() {
        // Test German format
        assertEquals("Max Mustermann", dataSource.extractPassengerName("Passagier: Max Mustermann"))
        assertEquals("Anna Schmidt", dataSource.extractPassengerName("Fahrgast: Anna Schmidt"))
        
        // Test English format
        assertEquals("John Doe", dataSource.extractPassengerName("Passenger: John Doe"))
        assertEquals("Jane Smith", dataSource.extractPassengerName("Name: Jane Smith"))
        
        // Test no name found
        assertNull(dataSource.extractPassengerName("No passenger information here"))
    }
    
    @Test
    fun `extractDateTime should parse various date formats`() {
        // Test different date formats
        val text1 = "ab 15.12.2023 10:30"
        val text2 = "departure 2023-12-15 10:30"
        val text3 = "von 15.12.2023"
        
        val result1 = dataSource.extractDepartureTime(text1)
        val result2 = dataSource.extractDepartureTime(text2)
        val result3 = dataSource.extractDepartureTime(text3)
        
        assertNotNull(result1)
        assertNotNull(result2)
        assertNotNull(result3)
        
        // Verify the parsed date is correct
        assertEquals(2023, result1?.year)
        assertEquals(12, result1?.monthValue)
        assertEquals(15, result1?.dayOfMonth)
        assertEquals(10, result1?.hour)
        assertEquals(30, result1?.minute)
    }
    
    @Test
    fun `extractTrainNumber should find various train formats`() {
        assertEquals("ICE 1234", dataSource.extractTrainNumber("ICE 1234 nach Berlin"))
        assertEquals("IC 234", dataSource.extractTrainNumber("IC 234"))
        assertEquals("RE 5678", dataSource.extractTrainNumber("RE 5678"))
        assertEquals("DB 123", dataSource.extractTrainNumber("DB 123"))
        
        assertNull(dataSource.extractTrainNumber("No train number here"))
    }
    
    @Test
    fun `extractSeatNumber should find seat formats`() {
        assertEquals("23A", dataSource.extractSeatNumber("Sitzplatz: 23A"))
        assertEquals("12B", dataSource.extractSeatNumber("Seat 12B"))
        assertEquals("45", dataSource.extractSeatNumber("Platz 45"))
        
        assertNull(dataSource.extractSeatNumber("No seat information"))
    }
    
    @Test
    fun `extractCarriageNumber should find wagon formats`() {
        assertEquals("12", dataSource.extractCarriageNumber("Wagen 12"))
        assertEquals("5", dataSource.extractCarriageNumber("Carriage: 5"))
        assertEquals("8", dataSource.extractCarriageNumber("Coach 8"))
        
        assertNull(dataSource.extractCarriageNumber("No wagon information"))
    }
}