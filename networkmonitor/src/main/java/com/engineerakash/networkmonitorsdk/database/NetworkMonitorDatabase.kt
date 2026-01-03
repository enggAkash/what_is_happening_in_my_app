package com.engineerakash.networkmonitorsdk.database

import android.content.Context
import android.util.Log
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
        private const val TAG = "WhatsIsHappeningInMyApp"
        
        @Volatile
        private var INSTANCE: NetworkMonitorDatabase? = null
        
        private const val DATABASE_NAME = "network_monitor_db"
        
        fun getInstance(context: Context): NetworkMonitorDatabase {
            return INSTANCE ?: synchronized(this) {
                if (INSTANCE == null) {
                    Log.d(TAG, "🗄️ Creating database: $DATABASE_NAME")
                    Log.d(TAG, "   Database Path: ${context.applicationContext.getDatabasePath(DATABASE_NAME).absolutePath}")
                    Log.d(TAG, "   Database Version: 1")
                    Log.d(TAG, "   Entities: NetworkRequestEntity")
                    
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        NetworkMonitorDatabase::class.java,
                        DATABASE_NAME
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                    
                    // Open the database to ensure it's created and accessible
                    instance.openHelper.writableDatabase
                    
                    Log.d(TAG, "✅ Database created successfully")
                    Log.d(TAG, "   Full Path: ${context.applicationContext.getDatabasePath(DATABASE_NAME).absolutePath}")
                    Log.d(TAG, "   To view in Database Inspector:")
                    Log.d(TAG, "   1. Run your app and ensure SDK is initialized")
                    Log.d(TAG, "   2. Open Database Inspector in Android Studio")
                    Log.d(TAG, "   3. Click '+' to add database manually")
                    Log.d(TAG, "   4. Enter path: ${context.applicationContext.getDatabasePath(DATABASE_NAME).absolutePath}")
                }
                INSTANCE!!
            }
        }
    }
}

