package com.smart.csoft.services;

import android.util.Log;

import java.math.BigInteger;

/**
 * Created by umprasad on 12/25/2017.
 */

public class SmartHomeUtils {

    private static final String TAG = "SmartHomeUtils";
    /*Rest Api's*/
    public static String URL_VERIFY = "/devicestatus";
    public static String DEVICE_INFO = "/deviceinfo";
    public static String MANUAL_CONFIG = "/manualconfig";
    public static String SYNC_TIME = "/synctime";
    public static String CREATE_SCHEDULER = "/schedulerconfig";
    public static String WIFI_CONFIG="/wificonfig";
    public static String WIFI_EXIST_CONFIG="/wifistatus";
    public static String REMOVE_DEVICE="/remove";
    public static String PINMODE_STATUS="/pinmodestatus";
    /*fixed bundle arguments*/
    public static final String ARG_INDEX = "index";
    public static final String CHART_SET = "UTF-8";
    public static final String TIME_SEC = "hh:mm:ss";
    public static final String TIME_SEC24 = "HH:mm:ss";
    public static final String TIME_MINS = "HH:mm";
    public static final String IP = "192.168.4.1";

    public static String generateId(Integer deviceId, String time) {
        String random = null;
        try {
            if (deviceId != null) {
                random = "S" + Integer.toHexString(deviceId);
            }
            if (time != null) {
                random = random + String.format("%1x", new BigInteger(1, time.getBytes(CHART_SET)));
            }
        } catch (Exception ex) {
            Log.e(TAG, "Exception", ex);
        }
        return random;
    }
}
