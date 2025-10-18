package com.app.billing.instabillz.viewholder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;

public class VendorViewHolder extends RecyclerView.ViewHolder {
    public TextView tvVendorName, tvVendorPhone, tvVendorRemarks;
    public ImageButton btnDelete, btnEdit;

    public VendorViewHolder(@NonNull View itemView) {
        super(itemView);
        tvVendorName = itemView.findViewById(R.id.tvVendorName);
        tvVendorPhone = itemView.findViewById(R.id.tvVendorPhone);
        tvVendorRemarks = itemView.findViewById(R.id.tvVendorRemarks);
        btnDelete = itemView.findViewById(R.id.btnDelete);
        btnEdit = itemView.findViewById(R.id.btnEdit);
    }
}
