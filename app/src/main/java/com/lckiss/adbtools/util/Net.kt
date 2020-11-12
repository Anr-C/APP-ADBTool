package com.lckiss.adbtools.util

import android.content.Context
import android.net.wifi.WifiManager

object Net {
    /**
     * 获取手机IP地址
     * 必须连上wifi
     */
    fun getIpAddress(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        return intToIp(wifiInfo.ipAddress)
    }

    /**
     * 直接获取的IP地址是一个4字节的整数
     * 需要手动处理成常见的192.168.1.155形式
     */
    private fun intToIp(i: Int): String {
        return (i and 0xFF).toString() + "." +
                (i shr 8 and 0xFF) + "." +
                (i shr 16 and 0xFF) + "." +
                (i shr 24 and 0xFF)
    }
}