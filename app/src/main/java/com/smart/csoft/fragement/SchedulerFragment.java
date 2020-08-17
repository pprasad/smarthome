package com.smart.csoft.fragement;

import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.smart.csoft.R;
import com.smart.csoft.adapters.SchedulerViewAdapter;
import com.smart.csoft.dto.Device;
import com.smart.csoft.dto.Scheduler;
import com.smart.csoft.services.DateTimeDialog;
import com.smart.csoft.services.RestClient;
import com.smart.csoft.services.SmartHomeUtils;
import com.smart.csoft.services.SmartService;

import java.util.List;

import cz.msebera.android.httpclient.Header;


/**
 * Created by umprasad on 12/28/2017.
 */

public class SchedulerFragment extends Fragment {

    private final static String TAG = "SchedulerFragment";

    private final SmartService smartService = SmartService.getInstace();

    private final RestClient restClient = RestClient.getInstance();

    private Device device;

    private RecyclerView adapterRecyclerView;

    private List<Scheduler> adapterSchedulers;

    private SchedulerViewAdapter schedulerViewAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.title_scheduler_name);
        View view = inflater.inflate(R.layout.fragement_scheduler, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Integer index = getArguments().getInt(SmartHomeUtils.ARG_INDEX);
        adapterRecyclerView = view.findViewById(R.id.scheduler_recycle_view);
        FloatingActionButton floatingAction = view.findViewById(R.id.fab);
        LayoutInflater dialogPromptLayout = LayoutInflater.from(getContext());
        View dialogPromptView = dialogPromptLayout.inflate(R.layout.dialog_alarm_prompt, null);
        final EditText startTimeText = dialogPromptView.findViewById(R.id.time_start_Txt);
        final EditText endTimeText = dialogPromptView.findViewById(R.id.time_end_Txt);
        startTimeText.setShowSoftInputOnFocus(false);
        endTimeText.setShowSoftInputOnFocus(false);
        TextView locationView = view.findViewById(R.id.location_label_name);
        if (smartService.getViewDeviceList() != null) {
            device = smartService.getViewDeviceList().get(index);
            adapterSchedulers = device.getSchedulers();
            locationView.setText(device.getLocation());
            schedulerViewAdapter = new SchedulerViewAdapter(adapterSchedulers, getContext());
            adapterRecyclerView.setAdapter(schedulerViewAdapter);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
            adapterRecyclerView.setHasFixedSize(true);
            adapterRecyclerView.setLayoutManager(layoutManager);

        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle(R.string.time_label);
        dialogBuilder.setIcon(R.drawable.icons_timer);
        dialogBuilder.setView(dialogPromptView);
        dialogBuilder.setPositiveButton("Save", null);
        dialogBuilder.setNegativeButton("Close", null);
        final AlertDialog dialog = dialogBuilder.create();
        floatingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
        startTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimeDialog dateTimeDialog = new DateTimeDialog();
                dateTimeDialog.setEditText(startTimeText);
                dateTimeDialog.setCustomeFormat(SmartHomeUtils.TIME_MINS);
                dateTimeDialog.setDate(false);
                dateTimeDialog.show(getFragmentManager(), TAG);
            }
        });
        endTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimeDialog dateTimeDialog = new DateTimeDialog();
                dateTimeDialog.setEditText(endTimeText);
                dateTimeDialog.setCustomeFormat(SmartHomeUtils.TIME_MINS);
                dateTimeDialog.setDate(false);
                dateTimeDialog.show(getFragmentManager(), TAG);
            }
        });
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        smartService.show(getContext());
                        final Scheduler scheduler = new Scheduler();
                        scheduler.setStartTime(startTimeText.getText().toString());
                        scheduler.setEndTime(endTimeText.getText().toString());
                        scheduler.setDeviceId(device.getDeviceMode());
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
                        if (!adapterSchedulers.contains(scheduler)) {
                            restClient.postJsonCall(view.getContext(),SmartHomeUtils.CREATE_SCHEDULER,request,new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    device.addscheduler(scheduler);
                                    adapterSchedulers.add(scheduler);
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
                });
            }
        });
    }

}
