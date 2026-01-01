package com.engineerakash.networkmonitorsdk.interceptor

import com.engineerakash.networkmonitorsdk.config.MonitorConfig
import com.engineerakash.networkmonitorsdk.database.repository.NetworkRequestRepository
import com.engineerakash.networkmonitorsdk.models.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.util.regex.Pattern

/**
 * OkHttp Interceptor for monitoring network requests and responses
 * Internal class - not exposed to users
 */
internal class NetworkMonitorInterceptor(
    private val repository: NetworkRequestRepository,
    private val config: MonitorConfig,
    private val getUserId: () -> String?,
    private val getProperties: () -> Map<String, String>
) : Interceptor {
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        
        // Check if monitoring is enabled
        if (!config.enabled) {
            return chain.proceed(request)
        }
        
        // Check URL patterns
        if (!shouldMonitorUrl(url)) {
            return chain.proceed(request)
        }
        
        val startTime = System.currentTimeMillis()
        val timestamp = startTime
        
        // Capture request details
        val method = request.method
        val requestHeaders = request.headers.toMultimap().mapValues { it.value.firstOrNull() ?: "" }
        
        // Clone request body to avoid consuming the original
        val (newRequest, requestBody) = cloneRequestBody(request)
        
        return try {
            val response = chain.proceed(newRequest)
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            // Capture response details
            val responseCode = response.code
            val responseHeaders = response.headers.toMultimap().mapValues { it.value.firstOrNull() ?: "" }
            val responseBody = captureResponseBody(response)
            
            // Create NetworkRequest model
            val networkRequest = NetworkRequest(
                timestamp = timestamp,
                url = url,
                method = method,
                requestHeaders = requestHeaders,
                requestBody = requestBody,
                responseCode = responseCode,
                responseHeaders = responseHeaders,
                responseBody = responseBody,
                duration = duration,
                userId = getUserId(),
                properties = getProperties()
            )
            
            // Save to database asynchronously
            saveToDatabase(networkRequest)
            
            response
        } catch (e: IOException) {
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            // Handle error case and log to database
            val networkRequest = NetworkRequest(
                timestamp = timestamp,
                url = url,
                method = method,
                requestHeaders = requestHeaders,
                requestBody = requestBody,
                responseCode = null,
                responseHeaders = emptyMap(),
                responseBody = null,
                duration = duration,
                userId = getUserId(),
                properties = getProperties()
            )
            
            saveToDatabase(networkRequest)
            throw e
        }
    }
    
    /**
     * Check if URL should be monitored based on include/exclude patterns
     */
    private fun shouldMonitorUrl(url: String): Boolean {
        // If include patterns exist, URL must match at least one
        if (config.includeUrlPatterns.isNotEmpty()) {
            val matchesInclude = config.includeUrlPatterns.any { pattern ->
                try {
                    Pattern.matches(pattern, url)
                } catch (e: Exception) {
                    false
                }
            }
            if (!matchesInclude) {
                return false
            }
        }
        
        // URL must not match any exclude pattern
        if (config.excludeUrlPatterns.isNotEmpty()) {
            val matchesExclude = config.excludeUrlPatterns.any { pattern ->
                try {
                    Pattern.matches(pattern, url)
                } catch (e: Exception) {
                    false
                }
            }
            if (matchesExclude) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Clone request body to capture it without consuming the original
     * Returns a new request with cloned body and the captured body string
     */
    private fun cloneRequestBody(request: okhttp3.Request): Pair<okhttp3.Request, String?> {
        val requestBody = request.body ?: return Pair(request, null)
        
        return try {
            val buffer = okio.Buffer()
            requestBody.writeTo(buffer)
            val bodyString = buffer.readUtf8()
            
            // Limit body size based on config for logging
            val truncatedBody = if (bodyString.length > config.maxRequestBodySize) {
                bodyString.take(config.maxRequestBodySize.toInt()) + "... [truncated]"
            } else {
                bodyString
            }
            
            // Create a new request with the cloned body using the original bodyString
            val contentType = requestBody.contentType()
            val newBody = bodyString.toRequestBody(contentType)
            val newRequest = request.newBuilder()
                .method(request.method, newBody)
                .build()
            
            Pair(newRequest, truncatedBody)
        } catch (e: Exception) {
            // If cloning fails, return original request and null body
            Pair(request, null)
        }
    }
    
    /**
     * Capture response body content without consuming the original response
     */
    private fun captureResponseBody(response: Response): String? {
        val responseBody = response.body ?: return null
        
        return try {
            // Use peekBody to read without consuming the response
            val maxSize = config.maxResponseBodySize.coerceAtMost(Long.MAX_VALUE)
            val peekedBody = responseBody.peekBody(maxSize)
            val bodyString = peekedBody.string()
            
            // If body was truncated, add indicator
            if (responseBody.contentLength() > maxSize) {
                bodyString + "... [truncated]"
            } else {
                bodyString
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Save network request to database asynchronously
     */
    private fun saveToDatabase(networkRequest: NetworkRequest) {
        coroutineScope.launch {
            try {
                repository.insertRequest(networkRequest)
            } catch (e: Exception) {
                // Log error silently to avoid breaking the network request
                // In production, you might want to log this to a crash reporting service
            }
        }
    }
}

