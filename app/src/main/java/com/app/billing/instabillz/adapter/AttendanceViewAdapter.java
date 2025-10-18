package com.app.billing.instabillz.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.AttendanceModel;
import com.app.billing.instabillz.viewholder.AttendanceViewHolder;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class AttendanceViewAdapter extends RecyclerView.Adapter<AttendanceViewHolder>{

    Context context;
    List<AttendanceModel> attendanceModelList;
    BillingClickListener listener;
    Boolean isAdmin;

    public AttendanceViewAdapter(Context context, List<AttendanceModel> attendanceModelList, BillingClickListener listener,Boolean isAdmin) {
        this.context = context;
        this.attendanceModelList = attendanceModelList;
        this.listener = listener;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.attandence_item_design, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, @SuppressLint("RecyclerView") int position) {

        AttendanceModel attendanceModel = attendanceModelList.get(position);

        holder.date.setText(attendanceModel.getDate());
        holder.loginTime.setText(StringUtils.isNotBlank(attendanceModel.getLoginTime())?attendanceModel.getLoginTime():"--:--");
        holder.logoutTime.setText(StringUtils.isNotBlank(attendanceModel.getLogoutTime())?attendanceModel.getLogoutTime():"--:--");
        holder.hours.setText(StringUtils.isNotBlank(attendanceModel.getWorkingHours())?attendanceModel.getWorkingHours():"--:--");

        if(isAdmin){
            holder.delete.setVisibility(View.VISIBLE);
        }else{
            holder.delete.setVisibility(View.GONE);
        }

        if(StringUtils.isNotBlank(attendanceModel.getEmployeeName())){
            holder.delete.setVisibility(View.VISIBLE);
            holder.name.setText(attendanceModel.getEmployeeName());
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.click(position,"DELETE");
                }
            });
        }else{
            holder.delete.setVisibility(View.INVISIBLE);
            holder.delete.setClickable(false);
            holder.name.setText("Name");
        }
    }

    @Override
    public int getItemCount() {
        return attendanceModelList.size();
    }
}
