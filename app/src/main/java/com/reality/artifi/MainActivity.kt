package com.reality.artifi

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.reality.artifi.ui.theme.NotifiTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Permission result handlers
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                // Permission granted for notifications
                // Now proceed to request SMS permissions
                requestSmsPermissions()
            } else {
                // Handle permission denial for notifications
            }
        }

    private val requestSmsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val sendSmsGranted = permissions[android.Manifest.permission.SEND_SMS] == true
            val receiveSmsGranted = permissions[android.Manifest.permission.RECEIVE_SMS] == true
            if (sendSmsGranted && receiveSmsGranted) {
                LogHelper.logMessage("PERMISSION","You Have given Access to the SMS")
            } else {
                LogHelper.logMessage("PERMISSION","You Have Denied Access to the SMS")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // If permission is already granted, proceed to SMS permission request
                requestSmsPermissions()
            }
        } else {
            // If not Android 13+, directly request SMS permissions
            requestSmsPermissions()
        }

        enableEdgeToEdge()
        setContent {
            NotifiTheme {
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
                            callback(success)
                        }
                    }
                )
            }
        }
    }

    // Method to request SMS permissions
    private fun requestSmsPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestSmsPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.SEND_SMS,
                    android.Manifest.permission.RECEIVE_SMS
                )
            )
        }
    }

    private fun registerDevice(onResult: (Boolean) -> Unit) {
        LogHelper.logMessage("API Request", "RegisterDevice called")

        val firebaseService = MyFirebaseMessagingService()

        firebaseService.generatePayload { payload ->
            val url = BuildConfig.DEVICE_REGISTER_URL
            LogHelper.logMessage("API", "URL: $url")

            ApiRequestHelper.sendApiRequest(
                payload,
                url,
                onSuccess = { onResult(true) },
                onFailure = { onResult(false) }
            )
        }
    }
}

@Composable
fun MainScreen(
    isApiCallSuccessful: Boolean,
    isApiCallInProgress: Boolean,
    onApiRequest: (onComplete: (Boolean) -> Unit) -> Unit
) {
    val logs = remember { LogHelper.logs }.value
    val scope = rememberCoroutineScope()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = logs,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.height(5.dp))

            BasicTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { LogHelper.clearLogs() }
                ) {
                    Text("Clear Logs")
                }

                Button(
                    onClick = {
                        scope.launch {
                            onApiRequest { success ->
                                LogHelper.logMessage(
                                    "API Button",
                                    if (success) "API Call Successful" else "API Call Failed"
                                )
                            }
                        }
                    },
                    enabled = !isApiCallInProgress
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
