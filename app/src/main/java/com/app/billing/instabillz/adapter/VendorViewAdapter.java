package com.app.billing.instabillz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.VendorModel;
import com.app.billing.instabillz.viewholder.VendorViewHolder;

import java.util.List;

public class VendorViewAdapter extends RecyclerView.Adapter<VendorViewHolder>{

    Context context;
    List<VendorModel> vendorModelList;
    BillingClickListener clickListener;

    public VendorViewAdapter(Context context, List<VendorModel> vendorModelList, BillingClickListener clickListener) {
        this.context = context;
        this.vendorModelList = vendorModelList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public VendorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vendor_item_design, parent, false);
        return new VendorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VendorViewHolder holder, int position) {
        VendorModel vendor = vendorModelList.get(position);

        holder.tvVendorName.setText(vendor.getName());
        holder.tvVendorPhone.setText(vendor.getPhone());
        holder.tvVendorRemarks.setText(vendor.getRemarks());

        holder.btnDelete.setOnClickListener(v -> clickListener.click(position,"DELETE"));
        holder.btnEdit.setOnClickListener(v -> clickListener.click(position,"EDIT"));
    }

    @Override
    public int getItemCount() {
        return vendorModelList.size();
    }
}
