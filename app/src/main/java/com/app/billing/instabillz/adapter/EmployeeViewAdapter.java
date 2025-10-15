package com.app.billing.instabillz.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.EmployeeModel;
import com.app.billing.instabillz.viewholder.EmployeeViewHolder;

import java.util.List;

public class EmployeeViewAdapter extends RecyclerView.Adapter<EmployeeViewHolder>{

    private Context context;
    private List<EmployeeModel> employeeList;
    private BillingClickListener clickListener;

    public EmployeeViewAdapter(Context context, List<EmployeeModel> employeeList, BillingClickListener clickListener) {
        this.context = context;
        this.employeeList = employeeList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.employee_item_design, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        EmployeeModel employee = employeeList.get(position);

        holder.tvName.setText(employee.getName());
        holder.tvRole.setText(employee.getRole());

        // ✅ Set Active/Inactive indicator
        int color = employee.getActive() ? Color.parseColor("#4CAF50") : Color.parseColor("#E53935");
        holder.statusIndicator.getBackground().setTint(color);

        // ✅ Click actions
        holder.btnEdit.setOnClickListener(v -> clickListener.click(position,"EDIT"));
        holder.btnAttendance.setOnClickListener(v -> clickListener.click(position,"ATTENDANCE"));
        holder.btnClose.setOnClickListener(v -> clickListener.click(position,"DELETE"));
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }
}
