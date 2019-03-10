package com.lckiss.adbtools.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiUtils {

    /**
     * 获取手机IP地址
     * 必须连上wifi
     */
    public static String getIpAddress(Context context) {
        int result = 0;
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        result = wifiInfo.getIpAddress();
        return intToIp(result);
    }

    /**
     * 直接获取的IP地址是一个4字节的整数
     * 需要手动处理成常见的192.168.1.155形式
     */
    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }
}


