package com.engineerakash.networkmonitorsdk.models

/**
 * Model representing a network request/response pair
 */
data class NetworkRequest(
    /**
     * Unique identifier (database ID)
     */
    val id: Long? = null,
    
    /**
     * Timestamp when request was initiated (milliseconds since epoch)
     */
    val timestamp: Long,
    
    /**
     * Request URL
     */
    val url: String,
    
    /**
     * HTTP method (GET, POST, PUT, DELETE, etc.)
     */
    val method: String,
    
    /**
     * Request headers as key-value pairs
     */
    val requestHeaders: Map<String, String>,
    
    /**
     * Request body content
     */
    val requestBody: String?,
    
    /**
     * HTTP response status code
     */
    val responseCode: Int?,
    
    /**
     * Response headers as key-value pairs
     */
    val responseHeaders: Map<String, String>,
    
    /**
     * Response body content
     */
    val responseBody: String?,
    
    /**
     * Request duration in milliseconds
     */
    val duration: Long,
    
    /**
     * User ID associated with this request
     */
    val userId: String?,
    
    /**
     * Additional properties attached to this request
     */
    val properties: Map<String, String>
)

