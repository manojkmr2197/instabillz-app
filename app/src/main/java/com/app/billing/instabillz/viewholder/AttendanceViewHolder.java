package com.app.billing.instabillz.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;

public class AttendanceViewHolder extends RecyclerView.ViewHolder{

    public TextView date,loginTime,logoutTime,hours;
    public Button delete;

    public AttendanceViewHolder(@NonNull View itemView) {
        super(itemView);
        date = (TextView) itemView.findViewById(R.id.attendance_emp_date);
        loginTime = (TextView) itemView.findViewById(R.id.attendance_emp_login);
        logoutTime = (TextView) itemView.findViewById(R.id.attendance_emp_logout);
        hours = (TextView) itemView.findViewById(R.id.attendance_emp_hours);
        delete = (Button) itemView.findViewById(R.id.attendance_emp_delete);
    }
}
