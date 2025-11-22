package com.rafael.ordnung.data.datasource

import android.content.Context
import android.net.Uri
import com.rafael.ordnung.domain.model.Ticket
import com.rafael.ordnung.domain.model.TravelType
import dagger.hilt.android.qualifiers.ApplicationContext
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfParsingDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val qrCodeDataSource: QrCodeDataSource
) {
    
    private val dateTimePatterns = listOf(
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    )
    
    private val trainNumberPattern = Pattern.compile("(?:ICE|IC|EC|RE|RB|S\\d+)\\s*\\d+|\\b[A-Z]{2,4}\\s*\\d+\\b")
    private val seatPattern = Pattern.compile("(?:Sitzplatz|Platz|Seat)\\s*[:\\s]*([A-Z]\\d+|\\d+[A-Z]|\\d+)")
    private val carriagePattern = Pattern.compile("(?:Wagen|Carriage|Coach)\\s*[:\\s]*(\\d+)")
    private val locationPattern = Pattern.compile("[A-ZÄÖÜ][a-zäöüß]+(?:\\s+[A-ZÄÖÜ][a-zäöüß]+)*")
    
    suspend fun parsePdfFile(uri: Uri): Ticket {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileBytes = inputStream?.readBytes()
            inputStream?.close()
            
            if (fileBytes == null) {
                throw IllegalArgumentException("Could not read file")
            }
            
            val fileName = getFileName(uri)
            val filePath = uri.toString()
            val fileHash = calculateFileHash(fileBytes)
            
            // Parse PDF text
            val rawText = extractTextFromPdfBytes(fileBytes)
            
            // Extract information
            val qrCodeData = extractQrCodeData(fileBytes)
            val passengerName = extractPassengerName(rawText)
            val travelType = determineTravelType(rawText)
            val departureLocation = extractDepartureLocation(rawText)
            val arrivalLocation = extractArrivalLocation(rawText)
            val departureTime = extractDepartureTime(rawText)
            val arrivalTime = extractArrivalTime(rawText)
            val trainNumber = extractTrainNumber(rawText)
            val seatNumber = extractSeatNumber(rawText)
            val carriageNumber = extractCarriageNumber(rawText)
            
            Ticket(
                fileName = fileName,
                filePath = filePath,
                fileHash = fileHash,
                passengerName = passengerName,
                travelType = travelType,
                departureLocation = departureLocation,
                arrivalLocation = arrivalLocation,
                departureTime = departureTime,
                arrivalTime = arrivalTime,
                trainNumber = trainNumber,
                seatNumber = seatNumber,
                carriageNumber = carriageNumber,
                qrCodeData = qrCodeData,
                rawText = rawText,
                isProcessed = true
            )
            
        } catch (e: Exception) {
            val fileName = getFileName(uri)
            Ticket(
                fileName = fileName,
                filePath = uri.toString(),
                fileHash = "",
                passengerName = null,
                travelType = TravelType.UNKNOWN,
                departureLocation = null,
                arrivalLocation = null,
                departureTime = null,
                arrivalTime = null,
                trainNumber = null,
                seatNumber = null,
                carriageNumber = null,
                qrCodeData = null,
                rawText = null,
                isProcessed = false,
                errorMessage = e.message
            )
        }
    }
    
    private fun extractTextFromPdfBytes(fileBytes: ByteArray): String {
        return try {
            val document = PDDocument.load(fileBytes)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            document.close()
            text
        } catch (e: Exception) {
            ""
        }
    }
    
    private suspend fun extractQrCodeData(fileBytes: ByteArray): String? {
        val qrCodes = qrCodeDataSource.extractQrCodesFromPdf(fileBytes)
        return qrCodes.firstOrNull { qrCodeDataSource.isValidQrCode(it) }
    }
    
    private fun extractPassengerName(text: String): String? {
        val patterns = listOf(
            Pattern.compile("(?:Passagier|Passenger|Name)\\s*[:\\s]*([A-Z][a-z]+\\s+[A-Z][a-z]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:Fahrgast)\\s*[:\\s]*([A-Z][a-z]+\\s+[A-Z][a-z]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b([A-Z][a-z]+\\s+[A-Z][a-z]+)\\b")
        )
        
        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                return matcher.group(1)
            }
        }
        return null
    }
    
    private fun determineTravelType(text: String): TravelType {
        val lowerText = text.lowercase()
        return when {
            lowerText.contains("zug") || lowerText.contains("train") || lowerText.contains("bahn") -> TravelType.TRAIN
            lowerText.contains("bus") -> TravelType.BUS
            lowerText.contains("flug") || lowerText.contains("flight") -> TravelType.FLIGHT
            lowerText.contains("fähre") || lowerText.contains("ferry") -> TravelType.FERRY
            else -> TravelType.UNKNOWN
        }
    }
    
    private fun extractDepartureLocation(text: String): String? {
        // Look for patterns like "von Berlin", "from Munich", "Berlin Hbf"
        val patterns = listOf(
            Pattern.compile("(?:von|from|ab)\\s+([A-ZÄÖÜ][a-zäöüß]+(?:\\s+[A-ZÄÖÜ][a-zäöüß]+)*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("([A-ZÄÖÜ][a-zäöüß]+(?:\\s+[A-ZÄÖÜ][a-zäöüß]+)*)\\s*(?:Hbf|Hauptbahnhof|Airport|Flughafen)")
        )
        
        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                return matcher.group(1)
            }
        }
        return null
    }
    
    private fun extractArrivalLocation(text: String): String? {
        val patterns = listOf(
            Pattern.compile("(?:nach|to)\\s+([A-ZÄÖÜ][a-zäöüß]+(?:\\s+[A-ZÄÖÜ][a-zäöüß]+)*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("→\\s*([A-ZÄÖÜ][a-zäöüß]+(?:\\s+[A-ZÄÖÜ][a-zäöüß]+)*)")
        )
        
        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                return matcher.group(1)
            }
        }
        return null
    }
    
    private fun extractDepartureTime(text: String): LocalDateTime? {
        return extractDateTime(text, listOf("ab", "departure", "von"))
    }
    
    private fun extractArrivalTime(text: String): LocalDateTime? {
        return extractDateTime(text, listOf("an", "arrival", "nach"))
    }
    
    private fun extractDateTime(text: String, keywords: List<String>): LocalDateTime? {
        for (keyword in keywords) {
            val pattern = Pattern.compile("$keyword\\s*[:\\s]*([0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{2,4}(?:\\s+[0-9]{1,2}:[0-9]{2}(?::[0-9]{2})?)?)", Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val dateStr = matcher.group(1)
                for (formatter in dateTimePatterns) {
                    try {
                        return LocalDateTime.parse(dateStr, formatter)
                    } catch (e: Exception) {
                        // Try next formatter
                    }
                }
            }
        }
        return null
    }
    
    private fun extractTrainNumber(text: String): String? {
        val matcher = trainNumberPattern.matcher(text)
        return if (matcher.find()) matcher.group() else null
    }
    
    private fun extractSeatNumber(text: String): String? {
        val matcher = seatPattern.matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }
    
    private fun extractCarriageNumber(text: String): String? {
        val matcher = carriagePattern.matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }
    
    private fun getFileName(uri: Uri): String {
        return uri.lastPathSegment ?: "unknown_file.pdf"
    }
    
    private fun calculateFileHash(fileBytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(fileBytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}