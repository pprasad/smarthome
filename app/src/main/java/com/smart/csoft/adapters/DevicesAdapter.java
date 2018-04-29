package com.smart.csoft.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smart.csoft.R;
import com.smart.csoft.dto.Device;

import java.util.List;

/**
 * Created by umprasad on 12/26/2017.
 */

public class DevicesAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater layoutInflater = null;
    private List<Device> devices;

    public DevicesAdapter(Context context, List<Device> devices) {
        this.context = context;
        this.devices = devices;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int i) {
        return devices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Device device = (Device) getItem(i);
        view = this.layoutInflater.inflate(R.layout.spinner_dropdown, null);
        TextView value = view.findViewById(R.id.dropdown_value);
        value.setText(device.getDeviceId());
        return view;
    }
}
