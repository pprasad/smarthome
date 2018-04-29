package com.smart.csoft.adapters;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.health.TimerStat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.ResponseHandlerInterface;
import com.smart.csoft.R;
import com.smart.csoft.dto.WifiDevice;
import com.smart.csoft.services.AsyncSmartHandler;
import com.smart.csoft.services.RestClient;
import com.smart.csoft.services.SmartHomeUtils;
import com.smart.csoft.services.SmartService;

import java.util.List;
import java.util.TimerTask;

/**
 * Created by umprasad on 1/6/2018.
 */

public class WifiViewAdapter extends BaseAdapter {

    private List<WifiDevice> WifiDevices;

    private Context context;

    private LayoutInflater layoutInflater;

    private TextView wifiLocation, wifiName;

    private Button wifi_ActionBtn,wifi_check_btn;

    private SmartService smartService = SmartService.getInstace();

    private RestClient restClient = RestClient.getInstance();

    private final String IP = "192.168.4.1";

    private ResponseHandlerInterface handlerInterface;

    private AsyncSmartHandler smartHandler;

    private ImageView wifiSettings;


    public WifiViewAdapter(List<WifiDevice> WifiDevices, Context context) {
        this.WifiDevices = WifiDevices;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        restClient.setIpAddress(IP);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final WifiDevice wifiDevice = WifiDevices.get(i);
        final int index=i;
        view = layoutInflater.inflate(R.layout.activity_wifi_list, null, false);
        wifiLocation = view.findViewById(R.id.wifi_location);
        wifiName = view.findViewById(R.id.wifi_name);
        wifiSettings = view.findViewById(R.id.wifi_setting);
        wifi_ActionBtn = view.findViewById(R.id.wifi_action_btn);
        wifiLocation.setText(wifiDevice.getLocation());
        wifiName.setText(wifiDevice.getSSID());
        wifi_ActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smartService.show(context);
                smartService.setNetWorkId(wifiDevice.getSSID());
                disablewifi(wifiDevice.getNetWorkId(),wifiDevice);
                smartService.getWifiManager().enableNetwork(wifiDevice.getNetWorkId(), true);
                final Handler handler=new Handler();
                Runnable checkNetwork=new Runnable() {
                    int count=0;
                    @Override
                    public void run() {
                        NetworkInfo networkInfo=getNetworkInfo();
                        if(count==2) {
                            if(networkInfo != null && networkInfo.isConnected()) {
                                handler.removeCallbacks(this);
                                smartService.setProgressBar("Connected to "+wifiDevice.getSSID());
                                restClient.setIpAddress(wifiDevice.getIpAddress());
                                restClient.getCall(SmartHomeUtils.URL_VERIFY,handlerInterface);
                            }else {
                                smartService.close();
                                handler.removeCallbacks(this);
                                smartService.showMessage(wifiDevice.getSSID()+" Wifi Offline Mode");
                            }
                        }
                        handler.postDelayed(this,1000);
                        count+=1;
                    }
                };
                handler.postDelayed(checkNetwork,1000);
            }
        });
        wifiSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smartHandler.openWifiPrompt(wifiDevice);
            }
        });
        return view;
    }

    @Override
    public int getCount() {
        return WifiDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return WifiDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public void setHandlerInterface(ResponseHandlerInterface handlerInterface) {
        this.handlerInterface = handlerInterface;
    }

    public void setSmartHandler(AsyncSmartHandler smartHandler) {
        this.smartHandler = smartHandler;
    }
    public void disablewifi(Integer networkId,WifiDevice wifiDevice){
        boolean flag=false;
        Integer currentNetworkId=smartService.getWifiManager().getConnectionInfo().getNetworkId();
        if(currentNetworkId!=-1 && (currentNetworkId!=networkId)) {
            smartService.getWifiManager().disableNetwork(currentNetworkId);
        }
    }
    public NetworkInfo getNetworkInfo() {
        ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    }
}