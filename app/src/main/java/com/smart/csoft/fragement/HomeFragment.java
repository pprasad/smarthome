package com.smart.csoft.fragement;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.smart.csoft.R;
import com.smart.csoft.dto.Device;
import com.smart.csoft.services.RestClient;
import com.smart.csoft.services.SmartHomeUtils;
import com.smart.csoft.services.SmartService;

import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by umprasad on 12/28/2017.
 */

public class HomeFragment extends Fragment {

    private final static String TAG="HomeFragment";

    private final RestClient restClient = RestClient.getInstance();

    private final SmartService smartService = SmartService.getInstace();

    private final Handler handler = new Handler();

    private TextView deviceDateTime = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle(smartService.getProperty(R.string.title_home));
        View view = inflater.inflate(R.layout.fragment_home_panel, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        deviceDateTime = view.findViewById(R.id.device_datetime);
        loadDevices();
    }

    @Override
    public void onStart() {
        super.onStart();
        handler.postDelayed(sendUpdatesToUI, 1000);
    }
    public void loadDevices(){
        if (smartService.getDevices() == null) {
            smartService.show(getContext());
            smartService.setProgressBar("Fetching Devices");
            RequestParams params = new RequestParams();
            restClient.getJsonCall(SmartHomeUtils.DEVICE_INFO, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Log.i(TAG,"Configuration List"+response.get("devices").toString());
                        Gson gson = new Gson();
                        List<Device> devices = gson.fromJson(response.get("devices").toString(), new TypeToken<List<Device>>() {
                        }.getType());
                        smartService.setDevices(devices);
                        smartService.close();
                    } catch (Exception ex) {
                        Log.e(TAG,"Device Configuration Error", ex);
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.i(TAG,throwable.getMessage());
                    smartService.close();
                }
            });
        }
    }
    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            restClient.getCall(SmartHomeUtils.SYNC_TIME, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String dateTime = new String(responseBody);
                    if (dateTime != null) {
                        deviceDateTime.setText(dateTime.replaceAll("\"", ""));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
            handler.postDelayed(this, 5000);
        }
    };
}
