package com.rafael.ordnung.data.datasource

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class QrCodeDataSourceTest {
    
    private lateinit var qrCodeDataSource: QrCodeDataSource
    
    @org.junit.jupiter.api.BeforeEach
    fun setup() {
        qrCodeDataSource = QrCodeDataSource()
    }
    
    @Test
    fun `isValidQrCode should validate Deutsche Bahn format`() {
        assertTrue(qrCodeDataSource.isValidQrCode("OTP123456789"))
        assertTrue(qrCodeDataSource.isValidQrCode("https://bahn.de/ticket/123"))
    }
    
    @Test
    fun `isValidQrCode should validate URL format`() {
        assertTrue(qrCodeDataSource.isValidQrCode("https://example.com/ticket"))
        assertTrue(qrCodeDataSource.isValidQrCode("http://transport.org/check"))
    }
    
    @Test
    fun `isValidQrCode should validate JSON format`() {
        assertTrue(qrCodeDataSource.isValidQrCode("""{"ticket": "123", "valid": true}"""))
        assertTrue(qrCodeDataSource.isValidQrCode("""{"type": "ticket", "data": "..."}"""))
    }
    
    @Test
    fun `isValidQrCode should validate Base64 format`() {
        assertTrue(qrCodeDataSource.isValidQrCode("TGljZW5zZSBkYXRhIGJhc2U2NCBlbmNvZGVk"))
        assertTrue(qrCodeDataSource.isValidQrCode("YW55IGNhcm5hbCBwbGVhc3VyZQ=="))
    }
    
    @Test
    fun `isValidQrCode should reject invalid data`() {
        assertFalse(qrCodeDataSource.isValidQrCode(""))
        assertFalse(qrCodeDataSource.isValidQrCode("short"))
        assertFalse(qrCodeDataSource.isValidQrCode("123"))
        assertFalse(qrCodeDataSource.isValidQrCode("invalid@#$%"))
    }
    
    @Test
    fun `isValidQrCode should accept long text`() {
        assertTrue(qrCodeDataSource.isValidQrCode("This is a longer text that should be considered valid"))
        assertTrue(qrCodeDataSource.isValidQrCode("12345678901234567890"))
    }
}