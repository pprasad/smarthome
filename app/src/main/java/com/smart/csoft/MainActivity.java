package com.smart.csoft;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.smart.csoft.dto.Device;
import com.smart.csoft.fragement.ConfigrationFragment;
import com.smart.csoft.fragement.HomeFragment;
import com.smart.csoft.services.DateTimeDialog;
import com.smart.csoft.services.RestClient;
import com.smart.csoft.services.SmartHomeUtils;
import com.smart.csoft.services.SmartService;

import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = "MainActivity";
    private final SmartService smartService = SmartService.getInstace();
    private final RestClient restClient = RestClient.getInstance();
    private ProgressDialog progressDialog = null;
    private final Integer DEFAULT_MENU = R.id.nav_home;
    private NavigationView navigationView = null;
    private AlertDialog timeSetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(smartService.getProperty(R.string.msg_progress_bar));
        smartService.setFragmentManager(getSupportFragmentManager());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        prepareDialog();
        setDefaultComponent();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.time_settings) {
            timeSetDialog.show();
        }
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment fragment = null;
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (id == R.id.nav_config) {
            fragment = new ConfigrationFragment();
        } else if (id == R.id.nav_logout) {
            smartService.logOut();
            Intent loginActivity = new Intent(this, LoginActivity.class);
            startActivity(loginActivity);
            finish();
            return true;
        }
        smartService.showFragment(fragment);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setDefaultComponent() {
        navigationView.setCheckedItem(DEFAULT_MENU);
        navigationView.getMenu().performIdentifierAction(DEFAULT_MENU, 0);
    }

    public void prepareDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.fragement_timer, null);
        final EditText dateText = view.findViewById(R.id.date_Txt);
        final EditText timeText = view.findViewById(R.id.time_Txt);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setNegativeButton("Close", null);
        builder.setPositiveButton("Save", null);
        dateText.setShowSoftInputOnFocus(false);
        timeText.setShowSoftInputOnFocus(false);
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimeDialog dateTimeDialog = new DateTimeDialog();
                dateTimeDialog.setEditText(dateText);
                dateTimeDialog.setDate(true);
                dateTimeDialog.show(smartService.getFragmentManager(), TAG);

            }
        });
        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimeDialog dateTimeDialog = new DateTimeDialog();
                dateTimeDialog.setEditText(timeText);
                dateTimeDialog.setDate(false);
                dateTimeDialog.setCustomeFormat(SmartHomeUtils.TIME_SEC24);
                dateTimeDialog.show(smartService.getFragmentManager(), TAG);
            }
        });
        timeSetDialog = builder.create();
        timeSetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = timeSetDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            RequestParams params = new RequestParams();
                            params.put("date", dateText.getText());
                            params.put("time", timeText.getText());
                            restClient.getCall(SmartHomeUtils.SYNC_TIME, params, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    smartService.showMessage(smartService.getProperty(R.string.time_success_msg));
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    smartService.showMessage(smartService.getProperty(R.string.wifi_connection_failure));
                                }
                            });
                        } catch (Exception ex) {
                            Log.e(TAG, "TimeException", ex);
                        }
                    }
                });
                Button cancelBtn = timeSetDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dateText.setText("");
                        timeText.setText("");
                        timeSetDialog.dismiss();
                    }
                });
            }

        });
    }
}
