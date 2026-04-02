package com.hmosdemos.moveo.pages

import android.app.Application
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hmosdemos.moveo.viewmodel.HomeViewModel
import com.hmosdemos.moveo.viewmodel.HomeViewModelFactory

@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit,
) {
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )

    Scaffold { paddingValues ->
        HomeScreen(
            paddingValues = paddingValues,
            onNavigateToLogin = onNavigateToLogin,
            viewModel = homeViewModel,
        )
    }
}