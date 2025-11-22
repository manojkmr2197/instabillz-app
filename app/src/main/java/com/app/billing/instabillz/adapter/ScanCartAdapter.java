package com.app.billing.instabillz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.model.ScanCartModel;
import com.app.billing.instabillz.viewholder.ScanCartViewHolder;

import java.util.List;

public class ScanCartAdapter extends RecyclerView.Adapter<ScanCartViewHolder> {

    Context context;
    List<ProductModel> cartModelsList;
    BillingClickListener billingClickListener;

    public ScanCartAdapter(Context context, List<ProductModel> cartModelsList, BillingClickListener billingClickListener) {
        this.context = context;
        this.cartModelsList = cartModelsList;
        this.billingClickListener = billingClickListener;
    }

    @NonNull
    @Override
    public ScanCartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scan_cart_item, parent, false);
        return new ScanCartViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanCartViewHolder h, int i) {
        ProductModel item = cartModelsList.get(i);

        h.tvName.setText(item.getName());
        h.tvQty.setText("Qty: " + item.getQty());
        h.tvUnitPrice.setText("₹ " + item.getPrice());
        h.tvPrice.setText("₹ " + (item.getQty() * item.getPrice()));
    }

    @Override
    public int getItemCount() {
        return cartModelsList.size();
    }
}

