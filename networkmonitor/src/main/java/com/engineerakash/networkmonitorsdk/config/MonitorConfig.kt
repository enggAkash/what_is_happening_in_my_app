package com.engineerakash.networkmonitorsdk.config

/**
 * Configuration class for Network Monitor SDK
 */
data class MonitorConfig(
    /**
     * API endpoint URL for batch uploads (internal, not configurable by users)
     */
    internal val uploadEndpoint: String = "https://engineerakash.com/api/network-logs",
    
    /**
     * Socket.IO endpoint URL for real-time uploads (internal, not configurable by users)
     */
    internal val socketEndpoint: String = "https://engineerakash.com",
    
    /**
     * API key/token for authentication
     */
    val apiKey: String? = null,
    
    /**
     * Upload interval in minutes (default: 1 minute)
     */
    val uploadIntervalMinutes: Long = 1L,
    
    /**
     * Enable real-time socket uploads (internal, controlled by server)
     */
    internal val enableRealtimeUpload: Boolean = false,
    
    /**
     * Maximum request body size to capture (in bytes)
     * Default: 1MB
     */
    val maxRequestBodySize: Long = 1024 * 1024,
    
    /**
     * Maximum response body size to capture (in bytes)
     * Default: 1MB
     */
    val maxResponseBodySize: Long = 1024 * 1024,
    
    /**
     * List of URL patterns to include (regex patterns)
     * If empty, all URLs are included
     */
    val includeUrlPatterns: List<String> = emptyList(),
    
    /**
     * List of URL patterns to exclude (regex patterns)
     */
    val excludeUrlPatterns: List<String> = emptyList(),
    
    /**
     * Enable/disable monitoring (default: true)
     */
    val enabled: Boolean = true
) {
    /**
     * Builder class for MonitorConfig
     */
    class Builder {
        private var apiKey: String? = null
        private var uploadIntervalMinutes: Long = 1L
        private var maxRequestBodySize: Long = 1024 * 1024
        private var maxResponseBodySize: Long = 1024 * 1024
        private var includeUrlPatterns: List<String> = emptyList()
        private var excludeUrlPatterns: List<String> = emptyList()
        private var enabled: Boolean = true
        
        fun apiKey(key: String?) = apply { this.apiKey = key }
        fun uploadIntervalMinutes(minutes: Long) = apply { this.uploadIntervalMinutes = minutes }
        fun maxRequestBodySize(size: Long) = apply { this.maxRequestBodySize = size }
        fun maxResponseBodySize(size: Long) = apply { this.maxResponseBodySize = size }
        fun includeUrlPatterns(patterns: List<String>) = apply { this.includeUrlPatterns = patterns }
        fun excludeUrlPatterns(patterns: List<String>) = apply { this.excludeUrlPatterns = patterns }
        fun enabled(enable: Boolean) = apply { this.enabled = enable }
        
        fun build(): MonitorConfig {
            return MonitorConfig(
                apiKey = apiKey,
                uploadIntervalMinutes = uploadIntervalMinutes,
                maxRequestBodySize = maxRequestBodySize,
                maxResponseBodySize = maxResponseBodySize,
                includeUrlPatterns = includeUrlPatterns,
                excludeUrlPatterns = excludeUrlPatterns,
                enabled = enabled
            )
        }
    }
}

