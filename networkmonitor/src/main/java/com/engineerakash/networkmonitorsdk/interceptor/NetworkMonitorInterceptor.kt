package com.engineerakash.networkmonitorsdk.interceptor

import android.util.Base64
import android.util.Log
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
    
    companion object {
        private const val TAG = "WhatsIsHappeningInMyApp"
    }
    
    /**
     * Coroutine scope for asynchronous database operations
     * Uses Dispatchers.IO for I/O operations and SupervisorJob to handle failures independently
     * Thread-safe: Each request is handled independently, no shared mutable state
     */
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
        
        // Log request detected
        Log.d(TAG, "→ Request: $method $url")
        if (requestBody != null) {
            Log.d(TAG, "→ Request Body: $requestBody")
        }
        
        return try {
            val response = chain.proceed(newRequest)
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            // Capture response details
            val responseCode = response.code
            val responseHeaders = response.headers.toMultimap().mapValues { it.value.firstOrNull() ?: "" }
            val responseBody = captureResponseBody(response)
            
            // Log response received
            Log.d(TAG, "← Response: $method $url | Status: $responseCode | Duration: ${duration}ms")
            if (responseBody != null) {
                Log.d(TAG, "← Response Body: $responseBody")
            }
            
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
            
            // Log error
            Log.e(TAG, "✗ Error: $method $url | Duration: ${duration}ms | Error: ${e.message}")
            
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
     * Handles different content types: JSON, XML, binary (base64 encoded)
     */
    private fun cloneRequestBody(request: okhttp3.Request): Pair<okhttp3.Request, String?> {
        val requestBody = request.body ?: return Pair(request, null)
        
        return try {
            val buffer = okio.Buffer()
            requestBody.writeTo(buffer)
            val contentType = requestBody.contentType()
            val contentLength = requestBody.contentLength()
            
            // Clone buffer for reading body content (for logging/storage)
            val clonedBuffer = buffer.clone()
            val bodyString = captureBodyContent(clonedBuffer, contentType, contentLength, config.maxRequestBodySize)
            
            // Create a new request with the original buffer (not consumed yet)
            val bytes = buffer.readByteArray()
            val newBody = bytes.toRequestBody(contentType)
            val newRequest = request.newBuilder()
                .method(request.method, newBody)
                .build()
            
            Pair(newRequest, bodyString)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clone request body: ${e.message}")
            // If cloning fails, return original request and null body
            Pair(request, null)
        }
    }
    
    /**
     * Capture response body content without consuming the original response
     * Handles different content types: JSON, XML, binary (base64 encoded)
     */
    private fun captureResponseBody(response: Response): String? {
        val responseBody = response.body ?: return null
        
        return try {
            // Use source buffer to peek at response without consuming it
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Request the entire body
            val buffer = source.buffer
            
            // Clone the buffer to read without consuming the original
            val clonedBuffer = buffer.clone()
            val contentType = responseBody.contentType()
            val contentLength = responseBody.contentLength()
            
            // Handle different content types
            captureBodyContent(clonedBuffer, contentType, contentLength, config.maxResponseBodySize)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to capture response body: ${e.message}")
            null
        }
    }
    
    /**
     * Capture body content handling different content types
     * @param buffer The buffer containing the body data
     * @param contentType The content type (MediaType) or null
     * @param contentLength The content length in bytes
     * @param maxSize Maximum size to capture
     * @return String representation of the body (text for JSON/XML, base64 for binary)
     */
    private fun captureBodyContent(
        buffer: okio.Buffer,
        contentType: okhttp3.MediaType?,
        contentLength: Long,
        maxSize: Long
    ): String? {
        if (contentLength <= 0) {
            return null
        }
        
        return try {
            val isTextContent = isTextContentType(contentType)
            val maxBytes = maxSize.coerceAtMost(contentLength)
            
            if (isTextContent) {
                // For text content (JSON, XML, plain text, etc.)
                val textContent = buffer.readUtf8(maxBytes)
                if (contentLength > maxBytes) {
                    "$textContent... [truncated, original size: $contentLength bytes]"
                } else {
                    textContent
                }
            } else {
                // For binary content (images, PDFs, etc.) - encode as base64
                val bytes = buffer.readByteArray(maxBytes)
                val base64Content = Base64.encodeToString(bytes, Base64.NO_WRAP)
                if (contentLength > maxBytes) {
                    "[Binary data - base64 encoded, truncated from ${contentLength} bytes]\n$base64Content"
                } else {
                    "[Binary data - base64 encoded, ${bytes.size} bytes]\n$base64Content"
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read body content: ${e.message}")
            null
        }
    }
    
    /**
     * Check if content type is text-based (can be read as UTF-8)
     */
    private fun isTextContentType(contentType: okhttp3.MediaType?): Boolean {
        if (contentType == null) {
            // If content type is unknown, try to read as text
            return true
        }
        
        val type = contentType.type.lowercase()
        val subtype = contentType.subtype.lowercase()
        
        // Text-based content types
        return when {
            type == "text" -> true
            subtype.contains("json") -> true
            subtype.contains("xml") -> true
            subtype.contains("html") -> true
            subtype.contains("javascript") -> true
            subtype.contains("css") -> true
            subtype.contains("csv") -> true
            subtype.contains("plain") -> true
            // Application types that are typically text
            type == "application" && (
                subtype.contains("json") ||
                subtype.contains("xml") ||
                subtype.contains("javascript") ||
                subtype.contains("x-www-form-urlencoded") ||
                subtype.contains("form-data")
            ) -> true
            else -> false
        }
    }
    
    /**
     * Save network request to database asynchronously
     * Thread-safe: Uses coroutines with Dispatchers.IO for background execution
     * Handles concurrent requests: Each request is saved independently
     * Room database operations are inherently thread-safe
     */
    private fun saveToDatabase(networkRequest: NetworkRequest) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                // Room database operations are thread-safe and can handle concurrent inserts
                repository.insertRequest(networkRequest)
            } catch (e: Exception) {
                // Log error silently to avoid breaking the network request
                // In production, you might want to log this to a crash reporting service
                Log.e(TAG, "Failed to save network request to database: ${e.message}", e)
            }
        }
    }
}

