package com.reality.artifi

import com.reality.artifi.LogHelper.logMessage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object ApiRequestHelper {

    private val client = OkHttpClient()

    // Method to send JSON data to the server
    fun sendApiRequest(
        jsonObject: JSONObject,
        url: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonObject.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url) // Replace with the actual URL of the API endpoint
            .post(body)
            .build()

        // Make the API call asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logMessage("API Request", "Request failed: ${e.message}")
                onFailure("API request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Handle successful response
                    val responseData = response.body?.string() ?: "No response body"
                    logMessage("API Request", "Success: $responseData")
                    onSuccess(responseData)
                } else {
                    // Handle error response
                    logMessage("API Request", "Error: ${response.code}")
                    onFailure("API request failed: Error code ${response.code}")
                }
            }
        })
    }
}
