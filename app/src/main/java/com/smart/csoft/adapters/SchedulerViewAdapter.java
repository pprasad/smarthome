package com.smart.csoft.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.smart.csoft.R;
import com.smart.csoft.dto.Scheduler;
import com.smart.csoft.services.RestClient;
import com.smart.csoft.services.SmartHomeUtils;
import com.smart.csoft.services.SmartService;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by umprasad on 12/31/2017.
 */

public class SchedulerViewAdapter extends RecyclerView.Adapter<SchedulerViewAdapter.SchedulerViewHolder> {

    private List<Scheduler> schedulers;
    private Context context;
    private LayoutInflater inflater;
    private RestClient restClient = RestClient.getInstance();
    private SmartService smartService = SmartService.getInstace();


    public SchedulerViewAdapter(List<Scheduler> schedulers, Context context) {
        this.schedulers = schedulers;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public SchedulerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.adapter_scheduler_view, parent, false);
        return new SchedulerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SchedulerViewHolder holder, int position) {
        final Scheduler scheduler = schedulers.get(position);
        holder.startTimeLabel.setText(scheduler.getStartTime());
        holder.endTimeLabel.setText(scheduler.getEndTime());
        holder.status.setChecked(scheduler.getStatus().intValue() == 1);
        holder.status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean flag) {
                smartService.show(context);
                final boolean isFlag = flag;
                Gson gson = new Gson();
                scheduler.setStatus(isFlag ? 1 : 0);
                if (scheduler.getIsRunning() == null) {
                    scheduler.setIsRunning(0);
                }
                String request = gson.toJson(scheduler);
                //RequestParams params = new RequestParams();
                //params.put("scheduler", request);
                restClient.postJsonCall(context,SmartHomeUtils.CREATE_SCHEDULER,request, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        scheduler.setStatus(isFlag ? 1 : 0);
                        smartService.close();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        smartService.close();
                        scheduler.setStatus(0);
                        holder.status.setChecked(false);
                        smartService.showMessage(smartService.getProperty(R.string.device_communication_msg));
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return schedulers.size();
    }

    public class SchedulerViewHolder extends RecyclerView.ViewHolder {
        public TextView startTimeLabel, endTimeLabel;
        public Switch status;

        public SchedulerViewHolder(View itemView) {
            super(itemView);
            startTimeLabel = itemView.findViewById(R.id.time_start_Txt);
            endTimeLabel = itemView.findViewById(R.id.time_end_Txt);
            status = itemView.findViewById(R.id.scheduler_status);
        }
    }

}
