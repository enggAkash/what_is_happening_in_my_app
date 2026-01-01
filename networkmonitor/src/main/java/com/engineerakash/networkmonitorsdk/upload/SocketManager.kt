package com.engineerakash.networkmonitorsdk.upload

import android.content.Context
import com.engineerakash.networkmonitorsdk.config.MonitorConfig
import com.engineerakash.networkmonitorsdk.models.NetworkRequest
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URISyntaxException

/**
 * Manager for Socket.IO real-time communication
 * Follows MVVM architecture pattern
 */
class SocketManager(
    private val context: Context,
    private val config: MonitorConfig
) {
    
    private var socket: Socket? = null
    private val socketScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val messageQueue = mutableListOf<NetworkRequest>()
    private var isConnected = false
    
    /**
     * Connect to socket server
     */
    fun connect() {
        if (!config.enableRealtimeUpload) {
            return
        }
        
        socketScope.launch {
            try {
                val options = IO.Options().apply {
                    // TODO: Add authentication if needed (query params, headers, etc.)
                    // query = "apiKey=${config.apiKey}"
                }
                
                socket = IO.socket(config.socketEndpoint, options)
                
                socket?.on(Socket.EVENT_CONNECT) {
                    isConnected = true
                    // Send queued messages
                    flushQueue()
                }
                
                socket?.on(Socket.EVENT_DISCONNECT) {
                    isConnected = false
                }
                
                socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                    // TODO: Handle connection error
                    isConnected = false
                }
                
                socket?.connect()
            } catch (e: URISyntaxException) {
                // TODO: Log error
            }
        }
    }
    
    /**
     * Disconnect from socket server
     */
    fun disconnect() {
        socketScope.launch {
            socket?.disconnect()
            socket?.close()
            socket = null
            isConnected = false
        }
    }
    
    /**
     * Send network request in real-time
     */
    fun sendRequest(request: NetworkRequest) {
        if (!config.enableRealtimeUpload) {
            return
        }
        
        socketScope.launch {
            val jsonObject = requestToJson(request)
            
            if (isConnected && socket != null) {
                try {
                    socket?.emit("network_request", jsonObject)
                } catch (e: Exception) {
                    // If send fails, add to queue
                    messageQueue.add(request)
                }
            } else {
                // Add to queue if not connected
                messageQueue.add(request)
            }
        }
    }
    
    /**
     * Flush queued messages
     */
    private fun flushQueue() {
        socketScope.launch {
            val queueCopy = messageQueue.toList()
            messageQueue.clear()
            
            queueCopy.forEach { request ->
                val jsonObject = requestToJson(request)
                try {
                    socket?.emit("network_request", jsonObject)
                } catch (e: Exception) {
                    // Re-add to queue if send fails
                    messageQueue.add(request)
                }
            }
        }
    }
    
    /**
     * Convert NetworkRequest to JSONObject
     */
    private fun requestToJson(request: NetworkRequest): JSONObject {
        return JSONObject().apply {
            put("timestamp", request.timestamp)
            put("url", request.url)
            put("method", request.method)
            put("requestHeaders", JSONObject(request.requestHeaders))
            put("requestBody", request.requestBody)
            put("responseCode", request.responseCode)
            put("responseHeaders", JSONObject(request.responseHeaders))
            put("responseBody", request.responseBody)
            put("duration", request.duration)
            put("userId", request.userId)
            put("properties", JSONObject(request.properties))
        }
    }
    
    /**
     * Check if socket is connected
     */
    fun isConnected(): Boolean = isConnected
}

