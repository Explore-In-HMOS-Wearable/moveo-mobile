package com.hmosdemos.moveo.wearengine

import android.content.Context
import android.util.Log
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.wearengine.HiWear
import com.huawei.wearengine.device.Device
import com.huawei.wearengine.p2p.P2pClient
import com.huawei.wearengine.p2p.Receiver


class P2pManager(context: Context) {
    private val p2pClient: P2pClient = HiWear.getP2pClient(context)
    private var messageReceiver: Receiver? = null
    private var messageListener: MessageListener? = null

    interface MessageListener {

        fun onMessageSent(message: String?)
    }

    fun unregisterReceiver() {
        if (messageReceiver != null) {
            p2pClient.unregisterReceiver(messageReceiver)
            messageListener = null
        }
    }

    fun pingDevice(
        device: Device?,
        peerPackageName: String?,
        successListener: OnSuccessListener<String?>,
        failureListener: OnFailureListener
    ) {
        p2pClient.ping(device) { result ->
            val message =
                """- Connected Device Name: ${device?.name}
                   - Peer Package Name: $peerPackageName
                   - Ping Result: ${STRING_RESULT}${result}
                """.trimIndent()
            successListener.onSuccess(message)
        }.addOnSuccessListener { aVoid: Void? ->
            successListener.onSuccess("${device?.name}${DEVICE_NAME_OF}${peerPackageName}")
        }.addOnFailureListener { e: Exception? ->
            failureListener.onFailure(Exception("Ping Fail:$FAILURE$e", e))
        }
    }


    companion object {
        private const val DEVICE_NAME_OF = "'s "
        private const val STRING_RESULT = " result:"
        private const val FAILURE = " task failure"
    }
}