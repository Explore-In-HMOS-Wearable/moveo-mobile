package com.hmosdemos.moveo.model

import androidx.compose.runtime.Immutable

@Immutable
data class UiState(
    val deviceState: DeviceState = DeviceState(),
    val messageState: MessageState = MessageState(),
    val fileState: FileState = FileState(),
    val receivedMessages: List<String> = emptyList(),
    val logMessages: List<String> = emptyList()
)