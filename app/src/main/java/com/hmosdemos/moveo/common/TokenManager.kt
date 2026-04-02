package com.hmosdemos.moveo.common

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hmosdemos.moveo.model.TokenResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date

val Context.dataStore by preferencesDataStore(name = "auth_prefs")

object TokenManager {

    private val ACCESS_TOKEN_KEY = stringPreferencesKey("accessToken")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refreshToken")
    private val EXPIRE_AT_KEY = stringPreferencesKey("expireAt")

    suspend fun saveToken(context: Context, token: TokenResponse) {
        val expireAt = System.currentTimeMillis() + token.expiresIn * 1000L
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = token.accessToken
            prefs[REFRESH_TOKEN_KEY] = token.refreshToken
            prefs[EXPIRE_AT_KEY] = expireAt.toString()
        }
    }

    suspend fun getAccessToken(context: Context): String? {
        return context.dataStore.data
            .map { prefs -> prefs[ACCESS_TOKEN_KEY]?.ifEmpty { null } }
            .first()
    }

    suspend fun getRefreshToken(context: Context): String? {
        return context.dataStore.data
            .map { prefs -> prefs[REFRESH_TOKEN_KEY]?.ifEmpty { null } }
            .first()
    }

    suspend fun isAccessTokenExpired(context: Context): Boolean {
        val expireAtStr = context.dataStore.data
            .map { prefs -> prefs[EXPIRE_AT_KEY] }
            .first()

        val expireAt = expireAtStr?.toLongOrNull() ?: return true

        val now = System.currentTimeMillis()
        Log.d("TokenManager", "Check now      : ${Date(now)}")
        Log.d("TokenManager", "Check expireAt : ${Date(expireAt)}")

        return now > expireAt
    }

    suspend fun clear(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = ""
            prefs[REFRESH_TOKEN_KEY] = ""
            prefs[EXPIRE_AT_KEY] = ""
        }
    }
}