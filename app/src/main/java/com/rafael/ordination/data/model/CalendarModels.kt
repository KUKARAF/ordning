package com.rafael.ordnung.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rafael.ordnung.domain.model.*

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey
    val id: String,
    val ticketId: Long,
    val title: String,
    val description: String?,
    val startTime: java.time.LocalDateTime,
    val endTime: java.time.LocalDateTime,
    val location: String?,
    val timezone: String,
    val isAllDay: Boolean = false,
    val reminderMinutes: String, // JSON array as string
    val attendees: String, // JSON array as string
    val attachments: String, // JSON array as string
    val color: String?,
    val transparency: String,
    val visibility: String,
    val googleEventId: String? = null,
    val googleEventUrl: String? = null,
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    val updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
)

@Entity(tableName = "calendar_info")
data class CalendarInfoEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val timezone: String,
    val isPrimary: Boolean = false,
    val accessRole: String,
    val color: String?,
    val isSelected: Boolean = false
)

// Extension functions for mapping
fun CalendarEventEntity.toDomainModel(): CalendarEvent {
    return CalendarEvent(
        id = id,
        title = title,
        description = description,
        startTime = startTime,
        endTime = endTime,
        location = location,
        timezone = timezone,
        isAllDay = isAllDay,
        reminderMinutes = emptyList(), // Parse JSON in real implementation
        attendees = emptyList(), // Parse JSON in real implementation
        attachments = emptyList(), // Parse JSON in real implementation
        color = color,
        transparency = Transparency.valueOf(transparency),
        visibility = Visibility.valueOf(visibility)
    )
}

fun CalendarEvent.toEntity(ticketId: Long): CalendarEventEntity {
    return CalendarEventEntity(
        id = id ?: java.util.UUID.randomUUID().toString(),
        ticketId = ticketId,
        title = title,
        description = description,
        startTime = startTime,
        endTime = endTime,
        location = location,
        timezone = timezone,
        isAllDay = isAllDay,
        reminderMinutes = "[]", // Convert to JSON in real implementation
        attendees = "[]", // Convert to JSON in real implementation
        attachments = "[]", // Convert to JSON in real implementation
        color = color,
        transparency = transparency.name,
        visibility = visibility.name
    )
}

fun CalendarInfoEntity.toDomainModel(): CalendarInfo {
    return CalendarInfo(
        id = id,
        name = name,
        description = description,
        timezone = timezone,
        isPrimary = isPrimary,
        accessRole = accessRole,
        color = color
    )
}

fun CalendarInfo.toEntity(): CalendarInfoEntity {
    return CalendarInfoEntity(
        id = id,
        name = name,
        description = description,
        timezone = timezone,
        isPrimary = isPrimary,
        accessRole = accessRole,
        color = color
    )
}