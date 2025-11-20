package com.rafael.ordnung.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rafael.ordnung.domain.model.TravelType
import java.time.LocalDateTime

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val fileHash: String,
    val passengerName: String?,
    val travelType: String, // TravelType enum as string
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