package com.reality.artifi

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogHelper {

    // Global state to hold the logs
    var logs = mutableStateOf("")

    // Function to log messages with detailed formatting
    fun logMessage(tag: String, message: String) {
        Log.d(tag, message) //Print on Log Cat
        val timestamp = getFormattedTimestamp()
        val formattedMessage = "$timestamp - $tag - $message"
        logs.value += "$formattedMessage\n" // Append the new log message to the UI log state

    }

    // Function to clear all logs
    fun clearLogs() {
        logs.value = "" // Reset the log state
        logMessage("LogHelper", "All logs cleared")
    }

    // Helper function to get the current timestamp
    private fun getFormattedTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        return dateFormat.format(Date())
    }


}
