package com.engineerakash.networkmonitorsdk.upload

import android.content.Context
import androidx.work.*
import com.engineerakash.networkmonitorsdk.database.NetworkMonitorDatabase
import com.engineerakash.networkmonitorsdk.database.repository.NetworkRequestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager service for periodic uploads
 * Follows MVVM architecture pattern
 */
class UploadService(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val database = NetworkMonitorDatabase.getInstance(applicationContext)
                val repository = NetworkRequestRepository(database.networkRequestDao())
                // TODO: Get config from shared preferences or injected dependency
                // For now, we'll need to get config from somewhere
                
                // TODO: Create UploadManager and perform upload
                // val uploadManager = UploadManager(applicationContext, config, repository)
                // val success = uploadManager.uploadPendingRequests()
                
                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }
    
    companion object {
        /**
         * Schedule periodic upload work
         */
        fun schedulePeriodicUpload(context: Context, intervalMinutes: Long) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val uploadWork = PeriodicWorkRequestBuilder<UploadService>(
                intervalMinutes,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "network_monitor_upload",
                ExistingPeriodicWorkPolicy.KEEP,
                uploadWork
            )
        }
        
        /**
         * Cancel periodic upload work
         */
        fun cancelPeriodicUpload(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork("network_monitor_upload")
        }
    }
}

