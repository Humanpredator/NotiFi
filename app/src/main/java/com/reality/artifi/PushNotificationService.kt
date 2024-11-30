package com.reality.artifi

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.reality.artifi.LogHelper.logMessage

class PushNotificationService : Service() {

    override fun onCreate() {
        super.onCreate()
        logMessage("PushNotificationService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Perform background tasks here
        logMessage("PushNotificationService", "Service started")

        // If the service gets killed by the system, restart it
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // This is used for binding services if required
        return null
    }
}
