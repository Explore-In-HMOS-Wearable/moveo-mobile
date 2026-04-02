package com.hmosdemos.moveo.model

data class HealthResponse(
    val group: List<HealthGroup> = emptyList()
)

data class HealthGroup(
    val sampleSet: List<SampleSet> = emptyList()
)

data class SampleSet(
    val samplePoints: List<SamplePoint> = emptyList()
)

data class SamplePoint(
    val startTime: Long,
    val endTime: Long,
    val value: List<HealthValue>
)

data class HealthValue(
    val floatValue: Double = 0.0,
    val integerValue: Int = 0
)