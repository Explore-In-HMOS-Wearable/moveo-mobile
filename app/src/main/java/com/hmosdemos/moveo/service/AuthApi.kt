package com.hmosdemos.moveo.service

import com.hmosdemos.moveo.model.TokenResponse
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi {
    @FormUrlEncoded
    @POST("oauth2/v3/token")
    suspend fun getToken(@FieldMap fields: Map<String, String>): TokenResponse
}