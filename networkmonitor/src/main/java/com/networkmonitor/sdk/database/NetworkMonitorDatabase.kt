package com.engineerakash.networkmonitorsdk.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.engineerakash.networkmonitorsdk.database.dao.NetworkRequestDao
import com.engineerakash.networkmonitorsdk.database.entity.NetworkRequestEntity

/**
 * Room Database for storing network requests
 */
@Database(
    entities = [NetworkRequestEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NetworkMonitorDatabase : RoomDatabase() {
    
    abstract fun networkRequestDao(): NetworkRequestDao
    
    companion object {
        @Volatile
        private var INSTANCE: NetworkMonitorDatabase? = null
        
        private const val DATABASE_NAME = "network_monitor_db"
        
        fun getInstance(context: Context): NetworkMonitorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NetworkMonitorDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

