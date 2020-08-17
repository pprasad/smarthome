package com.smart.csoft.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smart.csoft.dto.WifiDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by umprasad on 1/7/2018.
 */

public class WifiTable {

    private static final String TABLE_NAME = "WIFI_INFO";
    private static final String COLUMN_ID = "SSID";
    private static final String COLUMN_NETID = "NET_ID";
    private static final String COLUMN_LOCATION = "SSID_NAME";
    private static final String COLUMN_IP = "DEVICE_IP";

    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_NAME + "(" + COLUMN_ID + " TEXT primary key,"
            + COLUMN_NETID + " INTEGER,"
            + COLUMN_LOCATION + " TEXT not null,"
            + COLUMN_IP + " TEXT not null);";

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS  " + TABLE_NAME;

    private static final String SELECT_QUERY = "SELECT * FROM " + TABLE_NAME;

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database) {
        database.execSQL(DROP_TABLE);
        onCreate(database);
    }

    public static Long insert(WifiDevice wifiDevice, SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, wifiDevice.getSSID());
        values.put(COLUMN_NETID, wifiDevice.getNetWorkId());
        values.put(COLUMN_LOCATION, wifiDevice.getLocation());
        values.put(COLUMN_IP, wifiDevice.getIpAddress());
        return database.insert(TABLE_NAME, null, values);
    }

    public static List<WifiDevice> getWifiDevices(SQLiteDatabase database) {
        Cursor cursor = database.rawQuery(SELECT_QUERY, null);
        List<WifiDevice> wifiDevices = new ArrayList<>();
        while (cursor.moveToNext()) {
            WifiDevice wifiDevice = new WifiDevice();
            wifiDevice.setSSID(cursor.getString(0));
            wifiDevice.setNetWorkId(cursor.getInt(1));
            wifiDevice.setLocation(cursor.getString(2));
            wifiDevice.setIpAddress(cursor.getString(3));
            wifiDevices.add(wifiDevice);
        }
        return wifiDevices;
    }

    public static Integer update(WifiDevice wifiDevice, SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOCATION, wifiDevice.getLocation());
        values.put(COLUMN_IP, wifiDevice.getIpAddress());
        return database.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                new String[]{wifiDevice.getSSID()});
    }
    public static Integer update(String ssid,String ip, SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_IP,ip);
        return database.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                new String[]{ssid});
    }

    public static Integer delete(WifiDevice wifiDevice, SQLiteDatabase database) {
        return database.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{wifiDevice.getSSID()});
    }
}
