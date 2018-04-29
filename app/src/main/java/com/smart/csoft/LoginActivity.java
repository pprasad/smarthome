package com.smart.csoft;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.smart.csoft.adapters.DropDownAdapter;
import com.smart.csoft.adapters.WifiViewAdapter;
import com.smart.csoft.database.SmartDatabaseHelper;
import com.smart.csoft.dto.WifiDevice;
import com.smart.csoft.services.AsyncSmartHandler;
import com.smart.csoft.services.RestClient;
import com.smart.csoft.services.SmartHomeUtils;
import com.smart.csoft.services.SmartService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;


public class LoginActivity extends AppCompatActivity {

    private SmartService smartService;
    private Button loginBtn;
    private DropDownAdapter<WifiConfiguration> dropDownAdapter = null;
    private List<ScanResult> scanResults = null;
    private List<WifiConfiguration> configurations;
    private ProgressDialog progressBar;
    private final RestClient restClient = RestClient.getInstance();
    private GridView gridView;
    private WifiViewAdapter wifiViewAdapter;
    private SmartDatabaseHelper helper;
    private AlertDialog wifiConfigPrompt;
    private Spinner dropDownValues;
    private List<WifiDevice> wifiDevices;
    private EditText locationNameTxt, deviceIpTxt;
    private Boolean isUpdate = false;
    private WifiDevice selectedWifiDevice = null;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        smartService = SmartService.getInstace();
        setContentView(R.layout.activity_login);
        helper = new SmartDatabaseHelper(this);
        gridView = findViewById(R.id.wifi_gridView);
        this.smartService.setContext(this);
        progressBar = new ProgressDialog(this);
        progressBar.setMessage("Please Wait");
        if (smartService.getWifiManager().isWifiEnabled()) {
            prepareDialog();
            progressBar.show();
            scanResults = new ArrayList<>();
            wifiDevices = helper.getWifiDevices();
            wifiViewAdapter = new WifiViewAdapter(wifiDevices, this);
            wifiViewAdapter.setHandlerInterface(handler);
            wifiViewAdapter.setSmartHandler(smartHandler);
            gridView.setAdapter(wifiViewAdapter);
            progressBar.dismiss();
        } else {
            String errorName = getResources().getString(R.string.wifi_error_msg);
            this.smartService.showMessage(errorName);
            this.smartService.logOut();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        toolbar = findViewById(R.id.menu_toolbar);
        toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Integer id = item.getItemId();
        if (id == R.id.nav_add_wifi) {
            if(dropDownValues!=null) {
                dropDownValues.setVisibility(View.VISIBLE);
                wifiConfigPrompt.show();
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    public void prepareDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.wifi_config_prompt, null);
        dropDownValues = view.findViewById(R.id.spinner_device_name);
        locationNameTxt = view.findViewById(R.id.location_name_txt);
        deviceIpTxt = view.findViewById(R.id.device_ip_txt);
        deviceIpTxt.setText(SmartHomeUtils.IP);
        configurations = smartService.getConfiguareList();
        dropDownAdapter = new DropDownAdapter<WifiConfiguration>(configurations, this);
        dropDownValues.setAdapter(dropDownAdapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setNegativeButton("Close", null);
        builder.setPositiveButton("Save", null);
        wifiConfigPrompt = builder.create();
        wifiConfigPrompt.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = wifiConfigPrompt.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int flag = saveWifiDetails();
                        if (flag != -1 && flag != 0) {
                            wifiConfigPrompt.dismiss();
                            clearFields();
                            smartService.showMessage(smartService.getProperty(R.string.sucess_mg));
                        } else if (flag == 0) {
                            smartService.showMessage(smartService.getProperty(R.string.mandatory_msg));
                        } else {
                            smartService.showMessage(smartService.getProperty(R.string.device_error_msg));
                        }
                    }
                });
                Button cancelBtn = wifiConfigPrompt.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clearFields();
                        wifiConfigPrompt.dismiss();
                    }
                });
            }

        });
    }

    private NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager connManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    }
    public void clearFields() {
        isUpdate = false;
        selectedWifiDevice = null;
        locationNameTxt.setText("");
        deviceIpTxt.setText(SmartHomeUtils.IP);
    }
    public int saveWifiDetails() {
        int flag = -1;
        WifiDevice wifiDevice = selectedWifiDevice;
        if (!isUpdate) {
            wifiDevice = new WifiDevice();
            WifiConfiguration configuration = (WifiConfiguration) dropDownValues.getSelectedItem();
            wifiDevice.setSSID(configuration.SSID.replaceAll("\"", ""));
            wifiDevice.setNetWorkId(configuration.networkId);
        }
        if (locationNameTxt.getText().length() != 0 && deviceIpTxt.getText().length() != 0) {
            wifiDevice.setLocation(locationNameTxt.getText().toString());
            wifiDevice.setIpAddress(deviceIpTxt.getText().toString());
            if (helper.addWifi(wifiDevice) != -1 && !isUpdate) {
                wifiDevices.add(wifiDevice);
                wifiViewAdapter.notifyDataSetChanged();
                flag = 1;
            } else if (helper.updateWifiDevice(wifiDevice) != -1 && isUpdate) {
                wifiViewAdapter.notifyDataSetChanged();
                isUpdate = false;
                flag = 1;
            }
        } else {
            flag = 0;
        }
        return flag;
    }

    AsyncSmartHandler smartHandler = new AsyncSmartHandler() {
        @Override
        public void openWifiPrompt(Object object) {
            isUpdate = true;
            selectedWifiDevice = (WifiDevice) object;
            locationNameTxt.setText(selectedWifiDevice.getLocation());
            deviceIpTxt.setText(selectedWifiDevice.getIpAddress());
            dropDownValues.setVisibility(View.GONE);
            wifiConfigPrompt.show();
        }
    };
    AsyncHttpResponseHandler handler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            smartService.close();
            finish();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            smartService.close();
            String erroMsg = getResources().getString(R.string.wifi_connection_failure);
            smartService.showMessage(erroMsg);
        }
    };
}