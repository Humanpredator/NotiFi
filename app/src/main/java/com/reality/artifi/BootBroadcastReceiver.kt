package com.reality.artifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.reality.artifi.LogHelper.logMessage

class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            logMessage("BootReceiver", "Device Booted. Starting service...")
            val serviceIntent = Intent(context, PushNotificationService::class.java)
            context.startService(serviceIntent)
        }
    }
}
