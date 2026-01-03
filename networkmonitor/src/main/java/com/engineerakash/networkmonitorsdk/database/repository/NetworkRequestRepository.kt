package com.engineerakash.networkmonitorsdk.database.repository

import android.util.Log
import com.engineerakash.networkmonitorsdk.database.dao.NetworkRequestDao
import com.engineerakash.networkmonitorsdk.database.entity.NetworkRequestEntity
import com.engineerakash.networkmonitorsdk.models.NetworkRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository layer for network request data operations
 * Follows MVVM architecture pattern
 */
class NetworkRequestRepository(
    private val networkRequestDao: NetworkRequestDao
) {
    
    companion object {
        private const val TAG = "WhatsIsHappeningInMyApp"
    }
    
    /**
     * Insert a network request asynchronously
     */
    suspend fun insertRequest(networkRequest: NetworkRequest): Long {
        val entity = NetworkRequestEntity.fromNetworkRequest(networkRequest)
        val id = networkRequestDao.insert(entity)
        Log.d(TAG, "💾 DB INSERT → ID: $id | ${networkRequest.method} ${networkRequest.url} | Status: ${networkRequest.responseCode ?: "N/A"} | Duration: ${networkRequest.duration}ms")
        return id
    }
    
    /**
     * Get pending (not uploaded) requests
     */
    suspend fun getPendingRequests(limit: Int = 100): List<NetworkRequest> {
        val requests = networkRequestDao.getPendingRequests(limit).map { it.toNetworkRequest() }
        Log.d(TAG, "📤 DB READ → Retrieved ${requests.size} pending requests (limit: $limit)")
        return requests
    }
    
    /**
     * Mark requests as uploaded
     */
    suspend fun markAsUploaded(ids: List<Long>) {
        networkRequestDao.markAsUploaded(ids)
        Log.d(TAG, "✅ DB UPDATE → Marked ${ids.size} requests as uploaded: $ids")
    }
    
    /**
     * Delete uploaded requests
     */
    suspend fun deleteUploadedBefore(timestamp: Long) {
        networkRequestDao.deleteUploadedBefore(timestamp)
        Log.d(TAG, "🗑️ DB DELETE → Deleted uploaded requests before timestamp: $timestamp")
    }
    
    /**
     * Delete requests by IDs
     */
    suspend fun deleteByIds(ids: List<Long>) {
        networkRequestDao.deleteByIds(ids)
        Log.d(TAG, "🗑️ DB DELETE → Deleted ${ids.size} requests by IDs: $ids")
    }
    
    /**
     * Get count of pending requests
     */
    suspend fun getPendingCount(): Int {
        val count = networkRequestDao.getPendingCount()
        Log.d(TAG, "📊 DB READ → Pending requests count: $count")
        return count
    }
    
    /**
     * Get all requests as Flow (for observing changes)
     */
    fun getAllRequests(): Flow<List<NetworkRequest>> {
        return networkRequestDao.getAllRequests().map { entities ->
            entities.map { it.toNetworkRequest() }
        }
    }
}

