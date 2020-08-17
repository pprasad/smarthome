package com.smart.csoft.fragement;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.smart.csoft.R;
import com.smart.csoft.database.SmartDatabaseHelper;
import com.smart.csoft.database.WifiTable;
import com.smart.csoft.dto.WifiConfig;
import com.smart.csoft.services.RestClient;
import com.smart.csoft.services.SmartHomeUtils;
import com.smart.csoft.services.SmartService;

import org.json.JSONObject;

import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;


public class WifiConfigFragment extends Fragment {

    private final SmartService smartService = SmartService.getInstace();
    private final RestClient restClient = RestClient.getInstance();
    private SmartDatabaseHelper databaseHelper;
    private TextInputEditText wifiSSID;
    private TextInputEditText wifiPWD;
    private TextInputEditText wifiIP;
    private TextInputEditText wifiMac;
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();


    public WifiConfigFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper=new SmartDatabaseHelper(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(smartService.getProperty(R.string.wifi_header_title));
        return inflater.inflate(R.layout.fragment_wifi_config, container, false);
    }
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        wifiSSID=view.findViewById(R.id.wifi_ssid_txt);
        wifiPWD=view.findViewById(R.id.wifi_pwd_txt);
        wifiIP=view.findViewById(R.id.wifi_ip_txt);
        wifiMac=view.findViewById(R.id.wifi_mac_txt);
        final Button saveBtn=view.findViewById(R.id.save_wifi_btn);
        this.loadData();
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smartService.show(getContext());
                WifiConfig wifiConfig=new WifiConfig();
                wifiConfig.setSsid(wifiSSID.getText().toString());
                wifiConfig.setPwd(wifiPWD.getText().toString());
                Log.i("Password",wifiConfig.getPwd());
                String request=gson.toJson(wifiConfig);
                Log.i("Request",request);
                restClient.postJsonCall(view.getContext(),SmartHomeUtils.WIFI_CONFIG,request,handler);
            }
        });
    }
    private void loadData(){
        smartService.show(getContext());
        restClient.getCall(SmartHomeUtils.WIFI_EXIST_CONFIG,null,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                WifiConfig wifiConfig=gson.fromJson(response.toString(),WifiConfig.class);
                smartService.showMessage(wifiConfig.getStatus());
                wifiSSID.setText(wifiConfig.getSsid());
                wifiPWD.setText(wifiConfig.getPwd());
                wifiIP.setText(wifiConfig.getIp());
                wifiMac.setText(wifiConfig.getMac());
                smartService.close();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers,String responseString, Throwable error) {
                smartService.close();
                smartService.showMessage(error.getMessage());
            }
        });
    }
    AsyncHttpResponseHandler handler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            if(responseBody!=null) {
                String response=new String(responseBody);
                WifiConfig wifiConfig = gson.fromJson(response.toString(), WifiConfig.class);
                if ("success".equals(wifiConfig.getStatus())) {
                    wifiIP.setText(wifiConfig.getIp());
                    if (wifiConfig.getIp() != null) {
                        databaseHelper.updateWifiDevice(smartService.getSsid(), wifiConfig.getIp());
                    }
                }
                smartService.close();
                smartService.showMessage(wifiConfig.getStatus());
            }else{
                smartService.close();
                smartService.showMessage(smartService.getProperty(R.string.device_communication_msg));
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            smartService.close();
            smartService.showMessage(error.getMessage());
        }
    };
}
