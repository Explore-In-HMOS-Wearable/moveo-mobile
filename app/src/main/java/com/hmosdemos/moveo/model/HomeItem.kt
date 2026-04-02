package com.hmosdemos.moveo.model
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class HomeItem(
    val icon: ImageVector,
    val iconColor: Color,
    var data: Double = 0.0,
    val unit: String,
    val name: String
)