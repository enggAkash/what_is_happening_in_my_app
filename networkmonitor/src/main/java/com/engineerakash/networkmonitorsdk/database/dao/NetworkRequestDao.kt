package com.engineerakash.networkmonitorsdk.database.dao

import androidx.room.*
import com.engineerakash.networkmonitorsdk.database.entity.NetworkRequestEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) for NetworkRequestEntity
 */
@Dao
interface NetworkRequestDao {
    
    /**
     * Insert a network request
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(networkRequest: NetworkRequestEntity): Long
    
    /**
     * Insert multiple network requests
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(networkRequests: List<NetworkRequestEntity>)
    
    /**
     * Get all pending (not uploaded) requests
     */
    @Query("SELECT * FROM network_requests WHERE uploaded = 0 ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getPendingRequests(limit: Int = 100): List<NetworkRequestEntity>
    
    /**
     * Mark requests as uploaded
     */
    @Query("UPDATE network_requests SET uploaded = 1 WHERE id IN (:ids)")
    suspend fun markAsUploaded(ids: List<Long>)
    
    /**
     * Delete uploaded requests older than specified timestamp
     */
    @Query("DELETE FROM network_requests WHERE uploaded = 1 AND timestamp < :timestamp")
    suspend fun deleteUploadedBefore(timestamp: Long)
    
    /**
     * Delete specific requests by IDs
     */
    @Query("DELETE FROM network_requests WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
    
    /**
     * Get count of pending requests
     */
    @Query("SELECT COUNT(*) FROM network_requests WHERE uploaded = 0")
    suspend fun getPendingCount(): Int
    
    /**
     * Get all requests (for testing/debugging)
     */
    @Query("SELECT * FROM network_requests ORDER BY timestamp DESC")
    fun getAllRequests(): Flow<List<NetworkRequestEntity>>
}

