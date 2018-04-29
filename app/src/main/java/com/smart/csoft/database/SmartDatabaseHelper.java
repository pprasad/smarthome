package com.smart.csoft.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.smart.csoft.dto.WifiDevice;

import java.util.List;

/**
 * Created by umprasad on 1/7/2018.
 */

public class SmartDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SMARTHOMEAPP";

    private static final Integer DATABASE_VERSION = 3;

    public SmartDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        WifiTable.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        WifiTable.onUpgrade(sqLiteDatabase);
    }

    public Long addWifi(WifiDevice wifiDevice) {
        Long flag = WifiTable.insert(wifiDevice, this.getWritableDatabase());
        return flag;
    }

    public List<WifiDevice> getWifiDevices() {
        return WifiTable.getWifiDevices(this.getReadableDatabase());
    }

    public Integer updateWifiDevice(WifiDevice wifiDevice) {
        return WifiTable.update(wifiDevice, this.getWritableDatabase());
    }

    public Integer deleteWifiDevice(WifiDevice wifiDevice) {
        return WifiTable.delete(wifiDevice, this.getWritableDatabase());
    }
}
