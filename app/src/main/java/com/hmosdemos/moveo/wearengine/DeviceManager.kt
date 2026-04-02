package com.hmosdemos.moveo.wearengine

import android.content.Context
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.wearengine.HiWear
import com.huawei.wearengine.device.Device
import com.huawei.wearengine.device.DeviceClient

class DeviceManager(context: Context) {
    private val deviceClient: DeviceClient = HiWear.getDeviceClient(context)
    private val devices: MutableList<Device?> = ArrayList<Device?>()


    fun getBondedDevices(
        success: OnSuccessListener<MutableList<Device?>?>,
        failure: OnFailureListener?
    ) {
        deviceClient.bondedDevices
            .addOnSuccessListener(OnSuccessListener { deviceList: MutableList<Device?>? ->
                devices.clear()
                deviceList?.let { devices.addAll(it) }
                success.onSuccess(deviceList)
            })
            .addOnFailureListener(failure)
    }
}