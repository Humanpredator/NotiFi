package com.reality.artifi

import android.telephony.SmsManager
import com.reality.artifi.LogHelper.logMessage

class SmsGateway {

    // Method to send SMS
    fun sendSms(mobileNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(mobileNumber, null, message, null, null)
            logMessage("SmsGateway", "SMS sent to $mobileNumber: $message")
        } catch (e: Exception) {
            logMessage("SmsGateway", "Failed to send SMS: ${e.message}")
        }
    }
}
