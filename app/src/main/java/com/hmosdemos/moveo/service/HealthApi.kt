package com.hmosdemos.moveo.service

import com.hmosdemos.moveo.model.HealthResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class HealthRequestBody(
    val polymerizeWith: List<Map<String, String>>,
    val startTime: Long,
    val endTime: Long
)

interface HealthApi {
    @POST("healthkit/v2/sampleSet:polymerize")
    suspend fun getSampleSet(
        @Header("Authorization") authorization: String,
        @Body body: HealthRequestBody
    ): HealthResponse
}