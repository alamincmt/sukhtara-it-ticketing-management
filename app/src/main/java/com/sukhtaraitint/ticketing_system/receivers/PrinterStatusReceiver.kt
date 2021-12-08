package com.sukhtaraitint.ticketing_system.receivers


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.json.JSONObject
import java.lang.Exception


class PrinterStatusReceiver : BroadcastReceiver() {
//    private var callbackReceive: CallbackContext? = null
//    private var isReceiving = true
    override fun onReceive(context: Context, data: Intent) {

    val action = data.action
    val type = "PrinterStatus"
    val jsonObj = JSONObject()
    try {
        jsonObj.put("type", type)
        jsonObj.put("action", action)
        Log.i(TAG, "RECEIVED STATUS $action")
//        val result = PluginResult(PluginResult.Status.OK, jsonObj)
//        result.setKeepCallback(true)
//        callbackReceive.sendPluginResult(result)
    } catch (e: Exception) {
        Log.i(TAG, "ERROR: " + e.message)
    }

        /*if (isReceiving && callbackReceive != null) {
            val action = data.action
            val type = "PrinterStatus"
            val jsonObj = JSONObject()
            try {
                jsonObj.put("type", type)
                jsonObj.put("action", action)
                Log.i(TAG, "RECEIVED STATUS $action")
                val result = PluginResult(PluginResult.Status.OK, jsonObj)
                result.setKeepCallback(true)
                callbackReceive.sendPluginResult(result)
            } catch (e: Exception) {
                Log.i(TAG, "ERROR: " + e.message)
            }
        }*/
    }

    /*fun startReceiving(ctx: CallbackContext?) {
        callbackReceive = ctx
        isReceiving = true
        Log.i(TAG, "Start receiving status")
    }

    fun stopReceiving() {
        callbackReceive = null
        isReceiving = false
        Log.i(TAG, "Stop receiving status")
    }*/

    companion object {
        private const val TAG = "SunmiInnerPrinterReceiver"
    }
}