package com.hmosdemos.moveo.model

import android.net.Uri

data class FileState(
    val selectedUri: Uri? = null,
    val selectedFileName: String? = null,
    val isSending: Boolean = false
)