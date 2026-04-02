package com.hmosdemos.moveo.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.huawei.wearengine.HiWear
import com.hmosdemos.moveo.common.TokenManager
import com.hmosdemos.moveo.model.*
import com.hmosdemos.moveo.wearengine.AuthManager
import com.hmosdemos.moveo.service.HealthRequestBody
import com.hmosdemos.moveo.service.RetrofitClient
import com.hmosdemos.moveo.wearengine.DeviceManager
import com.hmosdemos.moveo.wearengine.P2pManager
import com.huawei.wearengine.device.Device
import com.huawei.wearengine.p2p.Message
import com.huawei.wearengine.p2p.P2pClient
import com.huawei.wearengine.p2p.SendCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = AuthManager(application)
    private val p2pManager = P2pManager(application)

    private val p2pClient: P2pClient = HiWear.getP2pClient(application)

    private val deviceManager = DeviceManager(application)

    private val endTime   = System.currentTimeMillis()
    private val startTime = endTime - 365L * 24 * 60 * 60 * 1000

    private val _homeItems = MutableStateFlow(initHomeData())
    val homeItems: StateFlow<List<HomeItem>> = _homeItems

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin

    private val _permissionsGranted = MutableStateFlow(false)
    private var connectedDevice: Device? = null
    private var devices: List<Device> = emptyList()

    private var messageListener: P2pManager.MessageListener? = null

    fun checkAndRequestPermissions(onSuccess: () -> Unit) {
        authManager.checkPermissions(object : AuthManager.AuthCheckCallback {
            override fun onResult(allPermissionsGranted: Boolean) {
                if (allPermissionsGranted) {
                    _permissionsGranted.value = true
                    onSuccess()
                } else {
                    requestPermissions(onSuccess)
                }
            }

            override fun onError(e: Exception?) {
                requestPermissions(onSuccess)
            }
        })
    }



    fun sendPing() {
        deviceManager.getBondedDevices({ deviceList ->
        devices = deviceList as List<Device>
            println("device list: " + devices)
        }, { error ->
            println("error devices: $error")
        })
        for(device in devices) {
            if (device.isConnected !== true) continue
            connectedDevice = device
        }

        if (connectedDevice == null) {
            return
        }

        println("connected device: $connectedDevice")
        val peerPackageName: String? = "YOUR_PACKAGE_NAME"
        val peerFingerPrint: String? = "YOUR_FINGER_PRINT"


        p2pClient.setPeerPkgName(peerPackageName)
        p2pClient.setPeerFingerPrint(peerFingerPrint)
        p2pManager.pingDevice(connectedDevice, peerPackageName, { result ->
            println("Send Ping Result: $result")

        }, { error ->
            println("err: ${error}")

        })
    }

    fun sendFile() {
        try {

            val steps = _homeItems.value.find { it.name == "Steps" }?.data ?: 0
            val calories = _homeItems.value.find { it.name == "Calories" }?.data ?: 0
            val distance = _homeItems.value.find { it.name == "Distance" }?.data ?: 0
            val heartRate = _homeItems.value.find { it.name == "Heart Rate" }?.data ?: 0

            val defaultContent = """
        Steps: $steps
        Calories: $calories
        Distance: $distance
        HeartRate: $heartRate
        """.trimIndent()

            val fileName = "default_message.txt"
            val tempFile = File(application.cacheDir, fileName)

            println("connected device: $connectedDevice")

            val peerPackageName: String? = "com.sample.trdtse.payment.lite"
            val peerFingerPrint: String? = "com.sample.trdtse.payment.lite_BLWUj2pedaxMO2aesMfts51a3uDCNT3g1V8NFrrkuKH60gCqaRRFk8sOTcumwfCsQ5t9pd4dDJS3J9wTOablRYs="

            p2pClient.setPeerPkgName(peerPackageName)
            p2pClient.setPeerFingerPrint(peerFingerPrint)

            tempFile.writeText(defaultContent)

            println("File read: " + tempFile.readText())

            val fileMessage = Message.Builder()
                .setPayload(tempFile)
                .build()

            val sendCallback = object : SendCallback {
                override fun onSendResult(resultCode: Int) {
                    if (resultCode == 207) {
                        println("File send successfully")
                        messageListener?.onMessageSent("File send successfully")
                    } else {
                        println("File send failed. Error Code: $resultCode")
                        messageListener?.onMessageSent("File send failed. Error Code: $resultCode")
                    }
                }

                override fun onSendProgress(progress: Long) {
                    Log.d("Send Message", "Progress: $progress bytes")
                }
            }

            println("file message builder " + fileMessage.javaClass)

            p2pClient.send(connectedDevice, fileMessage, sendCallback)

        } catch (e: Exception) {
            Log.e("Send Message:", "Exception sending file: ${e.message}")
        }
    }
    private fun requestPermissions(onSuccess: () -> Unit) {
        authManager.requestPermission(
            {
                _permissionsGranted.value = true
                onSuccess()
            },
            {
                _permissionsGranted.value = false
            }
        )
    }

    fun fetchAllData(context: Context) {
        viewModelScope.launch {
            val accessToken = TokenManager.getAccessToken(context) ?: return@launch
            Log.d("HomeViewModel", "Authorization: Bearer $accessToken")

            try {
                val steps     = fetchData(accessToken, "com.huawei.continuous.steps.delta", isFloat = false)
                val calories  = fetchData(accessToken, "com.huawei.continuous.calories.burnt", isFloat = true)
                val distance  = fetchData(accessToken, "com.huawei.continuous.distance.delta", isFloat = true)
                val heartRate = fetchLatestHeartRate(accessToken)

                val updated = _homeItems.value.map { item ->
                    when (item.name) {
                        "Steps"      -> item.copy(data = steps.total)
                        "Calories" -> item.copy(data = Math.round(calories.total * 100.0) / 100.0)
                        "Distance"   -> item.copy(data = distance.total)
                        "Heart Rate" -> item.copy(data = heartRate)
                        else -> item
                    }
                }

                Log.e("sinan", "fetchAllData: ${updated}")

                _homeItems.value = updated
            } catch (e: Exception) {
                Log.e("HomeViewModel", "fetchAllData error: ${e.message}")
            }
        }
    }

    fun handleGridClick(context: Context) {
        viewModelScope.launch {
            val accessToken = TokenManager.getAccessToken(context)
            val expired = TokenManager.isAccessTokenExpired(context)

            if (accessToken == null || expired) {
                TokenManager.clear(context)
                _navigateToLogin.value = true
            }
        }
    }

    private suspend fun fetchData(
        accessToken: String,
        dataTypeName: String,
        isFloat: Boolean
    ): HealthDataResult {
        val response = RetrofitClient.healthApi.getSampleSet(
            authorization = "Bearer $accessToken",
            body = HealthRequestBody(
                polymerizeWith = listOf(mapOf("dataTypeName" to dataTypeName)),
                startTime = startTime,
                endTime = endTime
            )
        )
        return mapResponse(response, isFloat)
    }

    private suspend fun fetchLatestHeartRate(accessToken: String): Double {
        val result = fetchData(accessToken, "com.huawei.instantaneous.heart_rate", isFloat = true)
        return result.values.lastOrNull() ?: 0.0
    }
    private fun mapResponse(response: HealthResponse, isFloat: Boolean): HealthDataResult {
        val values = mutableListOf<Double>()
        val times  = mutableListOf<String>()

        response.group.forEach { group ->
            group.sampleSet.forEach { set ->
                set.samplePoints.forEach { point ->
                    point.value.forEach { v ->
                        val value = if (isFloat) v.floatValue else v.integerValue.toDouble()
                        values.add(value)
                        times.add(Date(point.startTime / 1_000_000).toString())
                        times.add(Date(point.endTime   / 1_000_000).toString())
                    }
                }
            }
        }

        return HealthDataResult(
            values = values,
            times  = times,
            total  = values.sum()
        )
    }
}

fun initHomeData(): List<HomeItem> = listOf(
    HomeItem(Icons.Filled.Star, Color.Blue,  0.0, "step",  "Steps"),
    HomeItem(Icons.Filled.Favorite,       Color.Red,   0.0, "bpm",   "Heart Rate"),
    HomeItem(Icons.Filled.Check, Color(0xFFFFA500), 0.0, "kcal", "Calories"),
    HomeItem(Icons.Filled.LocationOn,  Color.Green, 0.0, "meter", "Distance"),
)

class HomeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(application) as T
    }
}

