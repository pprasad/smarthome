package com.smart.csoft.fragement;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.smart.csoft.R;
import com.smart.csoft.adapters.DevicesAdapter;
import com.smart.csoft.adapters.DevicesViewAdapter;
import com.smart.csoft.dto.Device;
import com.smart.csoft.services.RestClient;
import com.smart.csoft.services.SmartHomeUtils;
import com.smart.csoft.services.SmartService;

import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by umprasad on 12/25/2017.
 */

public class ConfigrationFragment extends Fragment {

    private final SmartService smartService = SmartService.getInstace();
    private final RestClient restClient = RestClient.getInstance();
    private ProgressDialog progressDialog = null;
    private AlertDialog dialog = null;
    private DevicesViewAdapter devicesViewAdapter;
    private List<Device> viewDevices;
    private Device device;
    private EditText value;
    private CheckBox isAuto, isManual;
    private final static String TAG="ConfigrationFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this.getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle(smartService.getProperty(R.string.title_device_config));
        View view = inflater.inflate(R.layout.fragment_configration, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        LayoutInflater layoutInflater = getLayoutInflater();
        final RecyclerView recyclerView = view.findViewById(R.id.device_information);
        final View dialogLayout = layoutInflater.inflate(R.layout.layout_custom_dialog, null);
        FloatingActionButton button = getActivity().findViewById(R.id.fab);
        final Spinner deviceSpinner = dialogLayout.findViewById(R.id.spinner_device_name);
        deviceSpinner.setAdapter(new DevicesAdapter(this.getContext(), smartService.getDevices()));
        value = dialogLayout.findViewById(R.id.device_ailas_name_txt);
        isAuto = dialogLayout.findViewById(R.id.auto_type);
        isManual = dialogLayout.findViewById(R.id.manual_type);
        /*View Adapterholders*/
        viewDevices = smartService.getViewDeviceList();
        devicesViewAdapter = new DevicesViewAdapter(viewDevices, view.getContext());
        recyclerView.setAdapter(devicesViewAdapter);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(layoutManager);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
        dialogBuilder.setTitle("Device Configuration");
        dialogBuilder.setIcon(R.drawable.ic_settings_black_48dp);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogLayout);
        dialogBuilder.setPositiveButton("Save", null);
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog = dialogBuilder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            progressDialog.show();
                            Device selectedDevice = (Device) deviceSpinner.getSelectedItem();
                            device = (Device) selectedDevice.clone();
                            device.setLocation(value.getText().toString());
                            device.setAuto(isAuto.isChecked());
                            device.setManual(isManual.isChecked());
                            device.setStatus(0);
                            if (smartService.getViewDeviceList().contains(device)) {
                                smartService.showMessage(smartService.getProperty(R.string.device_error_msg));
                                progressDialog.dismiss();
                            } else {
                                RequestParams params = new RequestParams();
                                params.put("device", new Gson().toJson(device));
                                restClient.getCall(SmartHomeUtils.DEVICE_INFO, params, handler);
                            }
                        } catch (Exception ex) {
                            Log.e("ConfigurationFragement", "TypeCasting", ex);
                        }
                    }
                });
            }

        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                value.setText("");
                isAuto.setChecked(false);
                isManual.setChecked(false);
                dialog.show();
            }
        });
        loadConfigurations();
    }
    public void loadConfigurations(){
        List<Device> devicesList=smartService.getViewDeviceList();
        if(devicesList==null || (devicesList!=null && devicesList.isEmpty())) {
            smartService.show(getContext());
            smartService.setProgressBar("Fetching Device Configurations");
            RequestParams params = new RequestParams();
            params.put("isExisting", true);
            restClient.getJsonCall(SmartHomeUtils.DEVICE_INFO, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Log.i(TAG, "Device List{}" + response.get("devices").toString());
                        if(response!=null) {
                            Gson gson = new Gson();
                            List<Device> devices = gson.fromJson(response.get("devices").toString(), new TypeToken<List<Device>>() {
                            }.getType());
                            smartService.setViewDeviceList(devices);
                            viewDevices.addAll(smartService.getViewDeviceList());
                            devicesViewAdapter.notifyDataSetChanged();
                        }
                        smartService.close();
                    } catch (Exception ex) {
                        Log.e(TAG, "DeviceListError", ex);
                        smartService.close();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    smartService.close();
                }
            });
        }
    }
    AsyncHttpResponseHandler handler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            smartService.addViewDevice(device);
            viewDevices.add(device);
            devicesViewAdapter.notifyDataSetChanged();
            progressDialog.dismiss();
            dialog.dismiss();

        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            progressDialog.dismiss();
            dialog.dismiss();
            smartService.showMessage(smartService.getProperty(R.string.device_communication_msg));
        }
    };

}