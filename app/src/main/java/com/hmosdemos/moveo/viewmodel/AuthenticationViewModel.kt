package com.hmosdemos.moveo.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmosdemos.moveo.common.Constants
import com.hmosdemos.moveo.service.RetrofitClient
import com.hmosdemos.moveo.common.TokenManager
import com.hmosdemos.moveo.model.TokenResponse
import kotlinx.coroutines.launch
import java.net.URLDecoder

class AuthenticationViewModel : ViewModel() {


    fun postOAuthToken(code: String, context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val token = sendTokenRequest(
                grantType = "authorization_code",
                code = code,
                context = context
            )
            if (token != null) {
                onSuccess()
            }
        }
    }


    private suspend fun sendTokenRequest(
        grantType: String,
        code: String? = null,
        refreshToken: String? = null,
        context: Context
    ): TokenResponse? {
        return try {
            val bodyMap = mutableMapOf(
                "grant_type" to grantType,
                "client_id" to Constants.CLIENT_ID,
                "client_secret" to Constants.CLIENT_SECRET,
                "redirect_uri" to Constants.REDIRECT_URL
            )
            code?.let { bodyMap["code"] = it }
            refreshToken?.let { bodyMap["refresh_token"] = it }

            val response = RetrofitClient.api.getToken(bodyMap)

            Log.d("AuthViewModel", "Token response: ${response}")
            Log.d("AuthViewModel", "Access token: ${response.accessToken}")
            Log.d("AuthViewModel", "Scope: ${response.scope}")

            if (response.error != 0 || response.accessToken.isEmpty()) {
                Log.d("AuthViewModel", "Token error: ${response.errorDescription}")
                return null
            }

            TokenManager.saveToken(context, response)
            response

        } catch (e: Exception) {
            Log.e("AuthViewModel", "Request failed: ${e.message}")
            null
        }
    }

    fun getQueryParameter(url: String, paramName: String): String? {
        val queryStart = url.indexOf('?')
        if (queryStart == -1) return null

        return url.substring(queryStart + 1)
            .split("&")
            .mapNotNull {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .firstOrNull { it.first == paramName }
            ?.second
            ?.let { URLDecoder.decode(it, "UTF-8") }
    }
}