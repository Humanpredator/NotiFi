package com.reality.artifi

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.reality.artifi.LogHelper.logMessage
import org.json.JSONObject

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // This method is called when the app receives a push notification
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Log the incoming message
        logMessage("PushNotificationService", "From: ${remoteMessage.from}")

        remoteMessage.data.let { data ->
            val trigger = data["trigger"]

            if (trigger == "GATEWAY") {
                // Handle SMS Gateway trigger
                val mobileNumber = data["mobileNumber"]
                val message = data["message"]

                if (!mobileNumber.isNullOrEmpty() && !message.isNullOrEmpty()) {
                    val smsGateway = SmsGateway()
                    smsGateway.sendSms(mobileNumber, message)
                } else {
                    logMessage(
                        "PushNotificationService",
                        "Invalid payload for GATEWAY: Missing mobileNumber or message"
                    )
                }
            } else if (trigger == "PUSH") {
                // Handle as a regular push notification
                val notificationTitle = data["title"] ?: "Notification"
                val notificationBody = data["body"] ?: "You have a new message."
                showNotification(notificationTitle, notificationBody)
            } else {
                logMessage("PushNotificationService", "Unknown trigger: $trigger")
            }
        }
    }

    // Called when the FCM token is refreshed or when it's generated for the first time
    @SuppressLint("NewApi")
    fun generatePayload(callback: (JSONObject) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val jsonObject = JSONObject()
            if (task.isSuccessful) {
                // Populate JSON object with device information and the Firebase token
                jsonObject.put("manufacturer", Build.MANUFACTURER)
                jsonObject.put("model", Build.MODEL)
                jsonObject.put("fcmToken", task.result)
                logMessage("FCM", "$jsonObject")
            } else {
                logMessage("FCM", "${task.exception}")
            }

            // Return the jsonObject to the callback
            callback(jsonObject)
        }
    }

    // Helper method to show a simple notification
    private fun showNotification(title: String, body: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel (needed for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default"
            val channelName = "FCM Notifications"
            val channelDescription = "Channel for FCM notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            // Create or update the channel
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Notification ID can be any unique integer
        val notificationId = 0

        // Build the notification
        val notification = NotificationCompat.Builder(this, "default")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Default icon
            .setAutoCancel(true)
            .build()

        // Show the notification
        notificationManager.notify(notificationId, notification)
    }
}
