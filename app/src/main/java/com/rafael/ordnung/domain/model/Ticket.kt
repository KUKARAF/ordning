package com.rafael.ordnung.domain.model

import java.time.LocalDateTime

data class Ticket(
    val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val fileHash: String,
    val passengerName: String?,
    val travelType: TravelType,
    val departureLocation: String?,
    val arrivalLocation: String?,
    val departureTime: LocalDateTime?,
    val arrivalTime: LocalDateTime?,
    val trainNumber: String?,
    val seatNumber: String?,
    val carriageNumber: String?,
    val qrCodeData: String?,
    val rawText: String?,
    val processedAt: LocalDateTime = LocalDateTime.now(),
    val isProcessed: Boolean = false,
    val errorMessage: String? = null
)

enum class TravelType {
    TRAIN,
    BUS,
    FLIGHT,
    FERRY,
    UNKNOWN
}