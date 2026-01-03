package com.engineerakash.networkmonitorsdk

import android.content.Context
import com.engineerakash.networkmonitorsdk.config.MonitorConfig
import com.engineerakash.networkmonitorsdk.database.NetworkMonitorDatabase
import com.engineerakash.networkmonitorsdk.database.repository.NetworkRequestRepository
import com.engineerakash.networkmonitorsdk.interceptor.NetworkMonitorInterceptor
import okhttp3.Interceptor

/**
 * Main SDK class for Network Monitoring
 * Initialize this in your Application class
 */
object NetworkMonitor {
    
    @Volatile
    private var isInitialized = false
    
    private var config: MonitorConfig? = null
    private var interceptor: NetworkMonitorInterceptor? = null
    private var repository: NetworkRequestRepository? = null
    
    @Volatile
    private var userId: String? = null
    
    private val properties = mutableMapOf<String, String>()
    private val propertiesLock = Any()
    
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
                val appContext = context.applicationContext
                this.config = config
                
                // Initialize database and repository
                val database = NetworkMonitorDatabase.getInstance(appContext)
                this.repository = NetworkRequestRepository(database.networkRequestDao())
                
                // Initialize interceptor with dependencies
                this.interceptor = NetworkMonitorInterceptor(
                    repository = repository!!,
                    config = config,
                    getUserId = { userId },
                    getProperties = {
                        synchronized(propertiesLock) {
                            properties.toMap()
                        }
                    }
                )
                
                // TODO: Initialize upload manager, socket manager
                isInitialized = true
            }
        }
    }
    
    /**
     * Get the interceptor instance to add to OkHttpClient
     */
    @JvmStatic
    fun getInterceptor(): Interceptor? {
        return interceptor
    }
    
    /**
     * Set or update user ID
     */
    @JvmStatic
    fun setUserId(userId: String) {
        check(isInitialized) { "NetworkMonitor must be initialized first. Call NetworkMonitor.init()" }
        this.userId = userId
    }
    
    /**
     * Reset user ID
     */
    @JvmStatic
    fun resetUserId() {
        check(isInitialized) { "NetworkMonitor must be initialized first. Call NetworkMonitor.init()" }
        this.userId = null
    }
    
    /**
     * Set a property that will be attached to all network requests
     */
    @JvmStatic
    fun setProperty(key: String, value: String) {
        check(isInitialized) { "NetworkMonitor must be initialized first. Call NetworkMonitor.init()" }
        synchronized(propertiesLock) {
            properties[key] = value
        }
    }
    
    /**
     * Reset all properties
     */
    @JvmStatic
    fun resetProperties() {
        check(isInitialized) { "NetworkMonitor must be initialized first. Call NetworkMonitor.init()" }
        synchronized(propertiesLock) {
            properties.clear()
        }
    }
    
    /**
     * Check if SDK is initialized
     */
    @JvmStatic
    fun isInitialized(): Boolean = isInitialized
    
    /**
     * Get the database file path (useful for Database Inspector)
     * @param context Application context (use applicationContext to avoid leaks)
     * @return Database file path, or null if SDK is not initialized
     */
    @JvmStatic
    fun getDatabasePath(context: Context): String? {
        check(isInitialized) { "NetworkMonitor must be initialized first. Call NetworkMonitor.init()" }
        return context.applicationContext.getDatabasePath("network_monitor_db")?.absolutePath
    }
}

