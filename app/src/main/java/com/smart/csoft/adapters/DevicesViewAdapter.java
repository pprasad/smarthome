package com.smart.csoft.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.smart.csoft.R;
import com.smart.csoft.dto.Device;
import com.smart.csoft.dto.Scheduler;
import com.smart.csoft.services.DateTimeDialog;
import com.smart.csoft.services.RestClient;
import com.smart.csoft.services.SmartHomeUtils;
import com.smart.csoft.services.SmartService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by umprasad on 12/26/2017.
 */

public class DevicesViewAdapter extends RecyclerView.Adapter<DevicesViewAdapter.DevicesViewHolder> {

    private List<Device> devices;
    private Context context;
    private LayoutInflater layoutInflater;
    private SmartService smartService = SmartService.getInstace();
    private RestClient restClient = RestClient.getInstance();
    private View dialogPromptView;
    private final EditText startTimeText, endTimeText;
    private final AlertDialog alertDialog;
    private final String TAG = "DevicesViewAdapter";
    private Integer position;
    private boolean isOnAndOff=false;
    private boolean isRemove=false;

    public DevicesViewAdapter(List<Device> devices, Context context,boolean isOnAndOff,boolean isRemove) {
        this.devices = devices;
        this.context = context;
        this.isOnAndOff=isOnAndOff;
        this.isRemove=isRemove;
        this.layoutInflater = LayoutInflater.from(context);
        dialogPromptView = this.layoutInflater.inflate(R.layout.dialog_alarm_prompt, null);
        startTimeText = dialogPromptView.findViewById(R.id.time_start_Txt);
        endTimeText = dialogPromptView.findViewById(R.id.time_end_Txt);
        startTimeText.setShowSoftInputOnFocus(false);
        endTimeText.setShowSoftInputOnFocus(false);
        alertDialog = smartService.getDialogBox(context, dialogPromptView);
    }

    @Override
    public DevicesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.view_device_content, parent, false);
        return new DevicesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DevicesViewHolder holder, final int position) {
        final Device device = devices.get(position);
        holder.deviceLabel.setText(device.getLocation());
        holder.imageButton.setVisibility(isOnAndOff?View.VISIBLE:View.INVISIBLE);
        holder.toggleButton.setVisibility(isOnAndOff?View.VISIBLE:View.INVISIBLE);
        holder.removeButton.setVisibility(isRemove?View.VISIBLE:View.INVISIBLE);
        if(isOnAndOff){
            final SchedulerViewAdapter schedulerViewAdapter = new SchedulerViewAdapter(device.getSchedulers(), context);
            holder.recyclerView.setAdapter(schedulerViewAdapter);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
            holder.recyclerView.setHasFixedSize(true);
            holder.recyclerView.setVerticalScrollBarEnabled(true);
            getPinModeStatus(device,holder);
            holder.toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton,final boolean flag) {
                    RequestParams params = new RequestParams();
                    Log.i("Device Mode",device.getDeviceMode().toString());
                    Log.i("Device State","falg{}::"+flag);
                    params.put("state",flag?0:1);
                    params.put("pinmode", device.getDeviceMode());
                    smartService.show(context);
                    restClient.getCall(SmartHomeUtils.MANUAL_CONFIG, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.i(TAG, "schedule status" + new String(responseBody));
                            device.setStatus(flag?1:0);
                            smartService.close();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e(TAG, "schedule error msg", error);
                            device.setStatus(0);
                            holder.toggleButton.setChecked(false);
                            smartService.close();
                            smartService.showMessage(smartService.getProperty(R.string.device_communication_msg));
                        }
                    });
                }
            });
            holder.imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!holder.isOpen) {
                        if (device.getSchedulers().isEmpty()) {
                            getData(device, schedulerViewAdapter);
                        }
                        holder.includeView.setVisibility(View.VISIBLE);
                        init(position, schedulerViewAdapter);
                        holder.isOpen = true;
                    } else {
                        holder.includeView.setVisibility(View.GONE);
                        holder.isOpen = false;
                    }
                }
            });
            holder.timeDialogBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.show();
                }
            });
        }else{
            final RequestParams params=new RequestParams();
            params.put("configType","device");
            params.put("deviceId",device.getDeviceMode());
            holder.removeButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    smartService.show(v.getContext());
                    restClient.getCall(SmartHomeUtils.REMOVE_DEVICE, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                             String response=new String(responseBody);
                             smartService.close();
                             smartService.showMessage(response);
                             devices.remove(device);
                             notifyDataSetChanged();

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            smartService.close();
                            smartService.showMessage("Communication Error");
                        }
                    });
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return devices!=null?devices.size():0;
    }

    public void init(final int position, final SchedulerViewAdapter schedulerViewAdapter) {
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button saveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button cancelBtn = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Device device = devices.get(position);
                        setData(device, schedulerViewAdapter);

                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        });
        startTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimeDialog dateTimeDialog = new DateTimeDialog();
                dateTimeDialog.setEditText(startTimeText);
                dateTimeDialog.setCustomeFormat(SmartHomeUtils.TIME_MINS);
                dateTimeDialog.setDate(false);
                dateTimeDialog.show(smartService.getFragmentManager(), TAG);
            }
        });
        endTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimeDialog dateTimeDialog = new DateTimeDialog();
                dateTimeDialog.setEditText(endTimeText);
                dateTimeDialog.setCustomeFormat(SmartHomeUtils.TIME_MINS);
                dateTimeDialog.setDate(false);
                dateTimeDialog.show(smartService.getFragmentManager(), TAG);
            }
        });
    }

    public void setData(final Device device, final SchedulerViewAdapter schedulerViewAdapter) {
        smartService.show(context);
        final Scheduler scheduler = new Scheduler();
        scheduler.setStartTime(startTimeText.getText().toString());
        scheduler.setEndTime(endTimeText.getText().toString());
        scheduler.setDeviceId(device.getDeviceMode());
        scheduler.setIsRunning(0);
        if (scheduler.getStatus() == null) {
            scheduler.setStatus(1);
        }
        String id = SmartHomeUtils.generateId(scheduler.getDeviceId(), scheduler.getStartTime());
        scheduler.setId(id);
        Gson gson = new Gson();
        String request = gson.toJson(scheduler);
        Log.i(TAG, request);
        RequestParams params = new RequestParams();
        params.put("scheduler", request);
        if (!device.getSchedulers().contains(scheduler)) {
            restClient.postJsonCall(context,SmartHomeUtils.CREATE_SCHEDULER,request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    device.addscheduler(scheduler);
                    schedulerViewAdapter.notifyDataSetChanged();
                    smartService.close();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    smartService.close();
                    smartService.showMessage(error.getMessage());
                }
            });
        } else {
            smartService.close();
            smartService.showMessage("Already Time Configured");
        }
    }

    public void getData(final Device device, final SchedulerViewAdapter schedulerViewAdapter) {
        smartService.show(context);
        RequestParams params = new RequestParams();
        params.put("deviceId", device.getDeviceMode());
        restClient.getJsonCall(SmartHomeUtils.CREATE_SCHEDULER, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Gson gson = new Gson();
                    Log.v("Scheduler List{}",gson.toJson(response));
                    if (response.has("scheduler") && !response.isNull("scheduler")) {
                        List<Scheduler> schedulers = gson.fromJson(response.get("scheduler").toString(), new TypeToken<List<Scheduler>>() {
                        }.getType());
                        device.getSchedulers().addAll(schedulers);
                        schedulerViewAdapter.notifyDataSetChanged();
                    }
                    smartService.close();
                } catch (Exception ex) {
                    Log.e(TAG, "Scheduler Exception", ex);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable error) {
                smartService.close();
                smartService.showMessage(error.getMessage());
            }
        });
    }

    private void getPinModeStatus(final Device device,final DevicesViewHolder holder){
        RequestParams params=new RequestParams();
        params.put("pinmode",device.getDeviceMode());
    /*    if (device.getStatus() != null && device.getStatus().intValue() == 0) {
            holder.toggleButton.setChecked(false);
        } else {
            holder.toggleButton.setChecked(true);
        }
    */    restClient.getCall(SmartHomeUtils.PINMODE_STATUS, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i(TAG, "schedule status" + new String(responseBody));
                String flag=new String(responseBody);
                device.setStatus("false".equals(flag)?1:0);
                holder.toggleButton.setChecked("false".equals(flag)?true:false);
                smartService.close();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(TAG, "schedule error msg", error);
                device.setStatus(0);
                holder.toggleButton.setChecked(false);
                smartService.close();
                smartService.showMessage(smartService.getProperty(R.string.device_communication_msg));
            }
        });
    }

    public class DevicesViewHolder extends RecyclerView.ViewHolder {

        private TextView deviceLabel;
        private ToggleButton toggleButton;
        private RelativeLayout viewDeviceLayout;
        private View includeView;
        private ImageButton imageButton, timeDialogBtn,removeButton;
        private Boolean isOpen = false;
        private RecyclerView recyclerView;

        public DevicesViewHolder(View itemView) {
            super(itemView);
            deviceLabel = itemView.findViewById(R.id.location_label_name);
            toggleButton = itemView.findViewById(R.id.toggleButton);
            viewDeviceLayout = itemView.findViewById(R.id.device_view_layout);
            includeView = itemView.findViewById(R.id.scheduler_include);
            imageButton = itemView.findViewById(R.id.parent_list_item_expand_arrow);
            timeDialogBtn = includeView.findViewById(R.id.time_dialog_btn);
            recyclerView = includeView.findViewById(R.id.scheduler_recycle_view);
            removeButton=itemView.findViewById(R.id.remove_device);
        }
    }
}
