package com.smart.csoft.dto;

import java.io.Serializable;

/**
 * Created by umprasad on 1/7/2018.
 */

public class WifiDevice implements Serializable {

    private String SSID;

    private Integer netWorkId;

    private String location;

    private String ipAddress;

    private boolean isEnabled;

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public Integer getNetWorkId() {
        return netWorkId;
    }

    public void setNetWorkId(Integer netWorkId) {
        this.netWorkId = netWorkId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
