package com.reality.artifi

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.reality.artifi.ui.theme.NotifiTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permissions for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }

        // Request SEND_SMS and RECEIVE_SMS permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.SEND_SMS,
                    android.Manifest.permission.RECEIVE_SMS
                ),
                REQUEST_SMS_PERMISSION
            )
        }

        enableEdgeToEdge()
        setContent {
            NotifiTheme {
                // Track API state
                var isApiCallInProgress by remember { mutableStateOf(false) }
                var isApiCallSuccessful by remember { mutableStateOf(false) }

                MainScreen(
                    isApiCallSuccessful = isApiCallSuccessful,
                    isApiCallInProgress = isApiCallInProgress,
                    onApiRequest = { callback ->
                        isApiCallInProgress = true
                        registerDevice { success ->
                            isApiCallSuccessful = success
                            isApiCallInProgress = false
                            callback(success) // Notify UI of success/failure
                        }
                    }
                )
            }
        }
    }

    private fun registerDevice(onResult: (Boolean) -> Unit) {
        // Log the start of the request to debug duplicate calls
        LogHelper.logMessage("API Request", "RegisterDevice called")

        val firebaseService = MyFirebaseMessagingService()

        firebaseService.generatePayload { payload ->
            val url = BuildConfig.API_URL
            LogHelper.logMessage("API", "URL: $url")
            // Ensure the API request is made only once by adding a flag or logging
            ApiRequestHelper.sendApiRequest(
                payload,
                url,
                onSuccess = {
                    onResult(true) // Notify success
                },
                onFailure = {
                    onResult(false) // Notify failure
                }
            )
        }
    }

    companion object {
        const val REQUEST_NOTIFICATION_PERMISSION = 1
        const val REQUEST_SMS_PERMISSION = 2
    }
}

@Composable
fun MainScreen(
    isApiCallSuccessful: Boolean,
    isApiCallInProgress: Boolean,
    onApiRequest: (onComplete: (Boolean) -> Unit) -> Unit
) {
    // Access the global log state from LogHelper
    val logs = remember { LogHelper.logs }.value
    val scope = rememberCoroutineScope()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Display logs with smaller font size
            Text(
                text = logs,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.height(5.dp))
            // Input area to add logs manually (for testing)
            BasicTextField(
                value = "",
                onValueChange = {},
                keyboardActions = KeyboardActions(
                    onDone = {
                        LogHelper.logMessage("AppLog", "Manual log entry")
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))

            // Buttons for Clear Logs and Re-Auth
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        LogHelper.clearLogs() // Clear logs
                    }
                ) {
                    Text("Clear Logs")
                }

                Button(
                    onClick = {
                        scope.launch {
                            onApiRequest { success ->
                                // Log result
                                LogHelper.logMessage(
                                    "API Button",
                                    if (success) "API Call Successful" else "API Call Failed"
                                )
                            }
                        }
                    },
                    enabled = !isApiCallInProgress // Disable button while waiting for API response
                ) {
                    Text(
                        text = when {
                            isApiCallInProgress -> "Re-Auth (In Progress)"
                            isApiCallSuccessful -> "Re-Auth (Success)"
                            else -> "Re-Auth (Retry)"
                        }
                    )
                }
            }
        }
    }
}
