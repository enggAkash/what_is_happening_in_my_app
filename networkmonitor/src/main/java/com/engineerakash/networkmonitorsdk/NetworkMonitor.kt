package com.engineerakash.networkmonitorsdk

import android.content.Context
import com.engineerakash.networkmonitorsdk.config.MonitorConfig
import com.engineerakash.networkmonitorsdk.interceptor.NetworkMonitorInterceptor

/**
 * Main SDK class for Network Monitoring
 * Initialize this in your Application class
 */
object NetworkMonitor {
    
    @Volatile
    private var isInitialized = false
    
    private var config: MonitorConfig? = null
    private var context: Context? = null
    private var interceptor: NetworkMonitorInterceptor? = null
    
    /**
     * Initialize the Network Monitor SDK
     * @param context Application context
     * @param config Configuration for the SDK
     */
    @JvmStatic
    fun init(context: Context, config: MonitorConfig) {
        if (isInitialized) {
            return
        }
        
        synchronized(this) {
            if (!isInitialized) {
                this.context = context.applicationContext
                this.config = config
                this.interceptor = NetworkMonitorInterceptor()
                // TODO: Initialize database, upload manager, socket manager
                isInitialized = true
            }
        }
    }
    
    /**
     * Set or update user ID
     */
    @JvmStatic
    fun setUserId(userId: String) {
        check(isInitialized) { "NetworkMonitor must be initialized first. Call NetworkMonitor.init()" }
        // TODO: Update user ID
    }
    
    /**
     * Reset user ID
     */
    @JvmStatic
    fun resetUserId() {
        check(isInitialized) { "NetworkMonitor must be initialized first. Call NetworkMonitor.init()" }
        // TODO: Reset user ID
    }
    
    /**
     * Set a property that will be attached to all network requests
     */
    @JvmStatic
    fun setProperty(key: String, value: String) {
        check(isInitialized) { "NetworkMonitor must be initialized first. Call NetworkMonitor.init()" }
        // TODO: Set property
    }
    
    /**
     * Reset all properties
     */
    @JvmStatic
    fun resetProperties() {
        check(isInitialized) { "NetworkMonitor must be initialized first. Call NetworkMonitor.init()" }
        // TODO: Reset properties
    }
    
    /**
     * Check if SDK is initialized
     */
    @JvmStatic
    fun isInitialized(): Boolean = isInitialized
}

