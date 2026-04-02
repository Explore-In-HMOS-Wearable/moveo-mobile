package com.hmosdemos.moveo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hmosdemos.moveo.pages.AuthenticationScreen
import com.hmosdemos.moveo.pages.LoginScreen
import com.hmosdemos.moveo.pages.MainScreen
import com.hmosdemos.moveo.wearengine.AuthManager
import com.hmosdemos.moveo.wearengine.DeviceManager
import com.hmosdemos.moveo.wearengine.P2pManager

class MainActivity : ComponentActivity() {

    private lateinit var deviceManager: DeviceManager
    private lateinit var authManager: AuthManager
    private lateinit var p2pManager: P2pManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initializeManagers()

        setContent {
            AppNavGraph()
        }
    }

    private fun initializeManagers() {
        deviceManager = DeviceManager(this)
        authManager = AuthManager(this)
        p2pManager = P2pManager(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        p2pManager.unregisterReceiver()
    }
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                onNavigateToAuthentication = {
                    navController.navigate("authentication")
                }
            )
        }

        composable("authentication") {
            AuthenticationScreen(
                onNavigateToMain = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}