package com.gpssimulator.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.gpssimulator.R
import com.gpssimulator.service.LocationSimulationService

object NotificationHelper {
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                LocationSimulationService.NOTIFICATION_CHANNEL_ID,
                "GPS Simulator",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "GPS simulation service notifications"
                setShowBadge(false)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun createSimulationNotification(
        context: Context,
        title: String,
        content: String,
        isRunning: Boolean = false,
        progress: Int = 0
    ): Notification {
        val stopIntent = Intent(context, LocationSimulationService::class.java).apply {
            action = LocationSimulationService.ACTION_STOP_SIMULATION
        }
        
        val stopPendingIntent = PendingIntent.getService(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, LocationSimulationService.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_walk)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isRunning)
            .setOnlyAlertOnce(true)
        
        if (isRunning) {
            builder.addAction(
                R.drawable.ic_walk,
                "Stop",
                stopPendingIntent
            )
            
            if (progress > 0) {
                builder.setProgress(100, progress, false)
            }
        }
        
        return builder.build()
    }
}
