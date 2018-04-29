package com.smart.csoft.adapters;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smart.csoft.R;

import java.util.List;

/**
 * Created by umprasad on 12/25/2017.
 */

public class DropDownAdapter<T> extends BaseAdapter {

    private List<T> configurations;
    private Context context;
    private LayoutInflater layoutInflater = null;

    public DropDownAdapter(List<T> configurations, Context context) {
        this.configurations = configurations;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return configurations.size();
    }

    @Override
    public Object getItem(int i) {
        return this.configurations.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        WifiConfiguration configuration = null;
        ScanResult scanResult = null;
        if (this.configurations.get(index) instanceof WifiConfiguration) {
            configuration = (WifiConfiguration) this.configurations.get(index);
        } else {
            scanResult = (ScanResult) this.configurations.get(index);
        }
        view = this.layoutInflater.inflate(R.layout.spinner_dropdown, null);
        if (configuration != null || scanResult != null) {
            TextView value = view.findViewById(R.id.dropdown_value);
            value.setText(configuration != null ? configuration.SSID.replaceAll("\"", "") : scanResult.SSID);
        }
        return view;
    }
}
