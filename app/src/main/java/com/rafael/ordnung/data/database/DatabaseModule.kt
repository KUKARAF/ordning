package com.rafael.ordnung.data.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideOrdnungDatabase(
        @ApplicationContext context: Context,
        callback: DatabaseCallback
    ): OrdnungDatabase {
        return Room.databaseBuilder(
            context,
            OrdnungDatabase::class.java,
            "ordnung_database"
        )
        .addCallback(callback)
        .fallbackToDestructiveMigration() // For development - remove in production
        .build()
    }
    
    @Provides
    fun provideTicketDao(database: OrdnungDatabase): TicketDao {
        return database.ticketDao()
    }
    

}