package com.rafael.ordnung.domain.model

import java.time.LocalDateTime

data class CalendarEvent(
    val id: String? = null,
    val title: String,
    val description: String? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val location: String? = null,
    val timezone: String,
    val isAllDay: Boolean = false,
    val reminderMinutes: List<Int> = emptyList(),
    val attendees: List<Attendee> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val color: String? = null,
    val transparency: Transparency = Transparency.OPAQUE,
    val visibility: Visibility = Visibility.DEFAULT
)

data class Attendee(
    val email: String,
    val displayName: String? = null,
    val isOptional: Boolean = false,
    val isOrganizer: Boolean = false,
    val responseStatus: ResponseStatus = ResponseStatus.NEEDS_ACTION
)

data class Attachment(
    val title: String,
    val fileUrl: String? = null,
    val mimeType: String? = null,
    val size: Long? = null
)

enum class Transparency {
    OPAQUE,
    TRANSPARENT
}

enum class Visibility {
    DEFAULT,
    PUBLIC,
    PRIVATE,
    CONFIDENTIAL
}

enum class ResponseStatus {
    NEEDS_ACTION,
    ACCEPTED,
    DECLINED,
    TENTATIVE,
    DELEGATED
}

data class CalendarInfo(
    val id: String,
    val name: String,
    val description: String? = null,
    val timezone: String,
    val isPrimary: Boolean = false,
    val accessRole: String,
    val color: String? = null
)

data class EventCreationResult(
    val success: Boolean,
    val eventId: String? = null,
    val eventUrl: String? = null,
    val error: String? = null
)