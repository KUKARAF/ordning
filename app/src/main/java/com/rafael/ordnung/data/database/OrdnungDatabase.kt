package com.rafael.ordnung.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.rafael.ordnung.data.model.TicketEntity
import com.rafael.ordnung.data.model.Converters
import javax.inject.Inject

@Database(
    entities = [TicketEntity::class, UserEntity::class, AuthTokenEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class OrdnungDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao
    abstract fun userDao(): UserDao
    abstract fun authTokenDao(): AuthTokenDao
}

class DatabaseCallback @Inject constructor() : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Database creation logic if needed
    }
}

// Database migrations for future schema updates
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example migration for future schema changes
        // database.execSQL("ALTER TABLE tickets ADD COLUMN new_column TEXT")
    }
}