package com.smart.csoft.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by umprasad on 12/25/2017.
 */

public class Device implements Serializable, Cloneable {

    private Integer id;
    private String deviceId;
    private Integer deviceMode;
    private String location;
    private boolean isAuto;
    private boolean isManual;
    private Integer status;
    private List<Scheduler> schedulers;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getDeviceMode() {
        return deviceMode;
    }

    public void setDeviceMode(Integer deviceMode) {
        this.deviceMode = deviceMode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isAuto() {
        return isAuto;
    }

    public void setAuto(boolean auto) {
        isAuto = auto;
    }

    public boolean isManual() {
        return isManual;
    }

    public void setManual(boolean manual) {
        isManual = manual;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<Scheduler> getSchedulers() {
        if (schedulers == null) {
            schedulers = new ArrayList<>();
        }
        return schedulers;
    }

    public void setSchedulers(List<Scheduler> schedulers) {
        this.schedulers = schedulers;
    }

    public void addscheduler(Scheduler scheduler) {
        if (schedulers == null) {
            schedulers = new ArrayList<>();
        }
        schedulers.add(scheduler);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (deviceId != null ? !deviceId.equals(device.deviceId) : device.deviceId != null)
            return false;
        return deviceMode != null ? deviceMode.equals(device.deviceMode) : device.deviceMode == null;
    }

    @Override
    public int hashCode() {
        int result = deviceId != null ? deviceId.hashCode() : 0;
        result = 31 * result + (deviceMode != null ? deviceMode.hashCode() : 0);
        return result;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
