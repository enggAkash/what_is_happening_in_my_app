package com.engineerakash.networkmonitorsdk.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.engineerakash.networkmonitorsdk.models.NetworkRequest

/**
 * Room Entity for NetworkRequest
 */
@Entity(tableName = "network_requests")
data class NetworkRequestEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "url")
    val url: String,
    
    @ColumnInfo(name = "method")
    val method: String,
    
    @ColumnInfo(name = "request_headers")
    val requestHeaders: Map<String, String>,
    
    @ColumnInfo(name = "request_body")
    val requestBody: String?,
    
    @ColumnInfo(name = "response_code")
    val responseCode: Int?,
    
    @ColumnInfo(name = "response_headers")
    val responseHeaders: Map<String, String>,
    
    @ColumnInfo(name = "response_body")
    val responseBody: String?,
    
    @ColumnInfo(name = "duration")
    val duration: Long,
    
    @ColumnInfo(name = "user_id")
    val userId: String?,
    
    @ColumnInfo(name = "properties")
    val properties: Map<String, String>,
    
    @ColumnInfo(name = "uploaded")
    val uploaded: Boolean = false
) {
    /**
     * Convert to domain model
     */
    fun toNetworkRequest(): NetworkRequest {
        return NetworkRequest(
            id = id,
            timestamp = timestamp,
            url = url,
            method = method,
            requestHeaders = requestHeaders,
            requestBody = requestBody,
            responseCode = responseCode,
            responseHeaders = responseHeaders,
            responseBody = responseBody,
            duration = duration,
            userId = userId,
            properties = properties
        )
    }
    
    companion object {
        /**
         * Convert from domain model
         */
        fun fromNetworkRequest(networkRequest: NetworkRequest, uploaded: Boolean = false): NetworkRequestEntity {
            return NetworkRequestEntity(
                id = networkRequest.id ?: 0,
                timestamp = networkRequest.timestamp,
                url = networkRequest.url,
                method = networkRequest.method,
                requestHeaders = networkRequest.requestHeaders,
                requestBody = networkRequest.requestBody,
                responseCode = networkRequest.responseCode,
                responseHeaders = networkRequest.responseHeaders,
                responseBody = networkRequest.responseBody,
                duration = networkRequest.duration,
                userId = networkRequest.userId,
                properties = networkRequest.properties,
                uploaded = uploaded
            )
        }
    }
}

