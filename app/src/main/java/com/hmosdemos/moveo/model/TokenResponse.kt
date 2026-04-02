package com.hmosdemos.moveo.model

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String = "",
    @SerializedName("expires_in") val expiresIn: Long = 0,
    @SerializedName("id_token") val idToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String = "",
    @SerializedName("scope") val scope: String = "",
    @SerializedName("token_type") val tokenType: String = "",
    @SerializedName("error") val error: Int = 0,
    @SerializedName("error_description") val errorDescription: String = "",
    @SerializedName("sub_error") val subError: Int = 0
)