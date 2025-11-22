package com.rafael.ordnung.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rafael.ordnung.domain.model.AuthToken
import java.time.LocalDateTime



@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)



fun UserEntity.toDomainModel(): com.rafael.ordnung.domain.model.User {
    return com.rafael.ordnung.domain.model.User(
        id = id,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
        isEmailVerified = isEmailVerified
    )
}

fun com.rafael.ordnung.domain.model.User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
        isEmailVerified = isEmailVerified
    )
}