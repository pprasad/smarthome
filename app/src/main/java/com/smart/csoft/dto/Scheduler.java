package com.smart.csoft.dto;

import java.io.Serializable;

/**
 * Created by umprasad on 12/31/2017.
 */

public class Scheduler implements Serializable {
    private String id;
    private String startTime;
    private String endTime;
    private Integer deviceId;
    private Integer status;
    private Integer isRunning;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsRunning() {
        return isRunning;
    }

    public void setIsRunning(Integer isRunning) {
        this.isRunning = isRunning;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Scheduler scheduler = (Scheduler) o;

        if (id != null ? !id.equals(scheduler.id) : scheduler.id != null) return false;
        return deviceId != null ? deviceId.equals(scheduler.deviceId) : scheduler.deviceId == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (deviceId != null ? deviceId.hashCode() : 0);
        return result;
    }
}
