package com.engineerakash.networkmonitorsdk.database.repository

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
    
    /**
     * Insert a network request asynchronously
     */
    suspend fun insertRequest(networkRequest: NetworkRequest): Long {
        val entity = NetworkRequestEntity.fromNetworkRequest(networkRequest)
        return networkRequestDao.insert(entity)
    }
    
    /**
     * Get pending (not uploaded) requests
     */
    suspend fun getPendingRequests(limit: Int = 100): List<NetworkRequest> {
        return networkRequestDao.getPendingRequests(limit).map { it.toNetworkRequest() }
    }
    
    /**
     * Mark requests as uploaded
     */
    suspend fun markAsUploaded(ids: List<Long>) {
        networkRequestDao.markAsUploaded(ids)
    }
    
    /**
     * Delete uploaded requests
     */
    suspend fun deleteUploadedBefore(timestamp: Long) {
        networkRequestDao.deleteUploadedBefore(timestamp)
    }
    
    /**
     * Delete requests by IDs
     */
    suspend fun deleteByIds(ids: List<Long>) {
        networkRequestDao.deleteByIds(ids)
    }
    
    /**
     * Get count of pending requests
     */
    suspend fun getPendingCount(): Int {
        return networkRequestDao.getPendingCount()
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

