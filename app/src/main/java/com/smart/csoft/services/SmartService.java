package com.smart.csoft.services;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.smart.csoft.R;
import com.smart.csoft.dto.Device;

import java.security.Permission;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by umprasad on 12/23/2017.
 */

public class SmartService {
    private static SmartService _INSTACE;
    private Context context;
    private FragmentManager fragmentManager;
    private List<Device> devices;
    private List<Device> viewDeviceList;
    private ProgressDialog progressBar = null;
    private String netWorkId;
    private boolean isAutoClose=true;
    private int wifiIndex=-1;
    private String ssid;
    private Handler handler;
    private Runnable runnable;

    private SmartService() {
    }

    public static SmartService getInstace() {
        if (_INSTACE == null) {
            _INSTACE = new SmartService();
        }
        return _INSTACE;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void showMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public WifiManager getWifiManager() {
        return (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public void showFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = this.fragmentManager.beginTransaction();
            transaction.replace(R.id.body_content, fragment);
            transaction.commit();
        }
    }

    public FragmentManager getFragmentManager() {
        return this.fragmentManager;
    }

    public List<WifiConfiguration> getConfiguareList() {
        return this.getWifiManager().getConfiguredNetworks();
    }

    public List<ScanResult> getScanResults() {
        return this.getWifiManager().getScanResults();
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public List<Device> getViewDeviceList() {
        return viewDeviceList != null ? viewDeviceList : new ArrayList<Device>();
    }

    public void setViewDeviceList(List<Device> viewDeviceList) {
        this.viewDeviceList = viewDeviceList;
    }

    public void addViewDevice(Device device) {
        if (this.viewDeviceList == null) {
            this.viewDeviceList = new ArrayList<>();
        }else if(!this.viewDeviceList.contains(device)) {
            this.viewDeviceList.add(device);
        }
    }

    public String getProperty(Integer id) {
        String resoureId=null;
        if(this.context!=null && this.context.getResources()!=null){
            resoureId=this.context.getResources().getString(id);
        }
        return resoureId;
    }

    public void show(Context context) {
        if(progressBar==null) {
            progressBar = new ProgressDialog(context);
            progressBar.setMessage(this.getProperty(R.string.msg_progress_bar));
            progressBar.setCancelable(false);
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.show();
        }
    }

    public void close() {
        Log.i("SmartServices","Progress "+progressBar);
        if(progressBar!=null &&  progressBar.isShowing()) {
             progressBar.dismiss();
             this.progressBar=null;
        }
    }

    public AlertDialog getDialogBox(Context context, View view) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle(R.string.time_label);
        dialogBuilder.setIcon(R.drawable.icons_timer);
        dialogBuilder.setView(view);
        dialogBuilder.setPositiveButton("Save", null);
        dialogBuilder.setNegativeButton("Close", null);
        return dialogBuilder.create();
    }
    public void logOut(){
         _INSTACE=null;
        devices=null;
        viewDeviceList=null;
        context=null;
        fragmentManager=null;
        this.close();
    }

    public String getNetWorkId() {
        return netWorkId;
    }

    public void setNetWorkId(String netWorkId) {
        this.netWorkId = netWorkId;
    }

    public void setAutoClose(){
        final Timer taskTimer=new Timer();
        taskTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(isAutoClose){
                    if(progressBar!=null && progressBar.isShowing()){
                        progressBar.dismiss();
                    }
                }
            }
        },2000);
    }

    public boolean isAutoClose() {
        return isAutoClose;
    }

    public void setAutoClose(boolean autoClose) {
        isAutoClose = autoClose;
    }

    public int getWifiIndex() {
        return wifiIndex;
    }

    public void setWifiIndex(int wifiIndex) {
        this.wifiIndex = wifiIndex;
    }

    public void setProgressBar(String message) {
        if(this.progressBar!=null){
            this.progressBar.setMessage(message);
        }
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public void setHandler(Handler handler,Runnable runnable){
        this.handler=handler;
        this.runnable=runnable;
    }
}
