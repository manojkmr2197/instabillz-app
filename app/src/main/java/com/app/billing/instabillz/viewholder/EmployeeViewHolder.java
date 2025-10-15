package com.app.billing.instabillz.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;

public class EmployeeViewHolder extends RecyclerView.ViewHolder {

    public ImageView btnClose, btnEdit,  btnAttendance;
    public TextView tvName, tvRole;
    public View statusIndicator;

    public EmployeeViewHolder(@NonNull View itemView) {
        super(itemView);

        btnClose = itemView.findViewById(R.id.employee_item_close);
        btnEdit = itemView.findViewById(R.id.employee_item_edit);
        btnAttendance = itemView.findViewById(R.id.employee_item_attendance);
        tvName = itemView.findViewById(R.id.employee_item_name);
        tvRole = itemView.findViewById(R.id.employee_item_role);
        statusIndicator = itemView.findViewById(R.id.employee_item_status);
    }
}
