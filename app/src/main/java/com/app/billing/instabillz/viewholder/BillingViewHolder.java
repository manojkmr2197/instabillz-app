package com.app.billing.instabillz.viewholder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;

public class BillingViewHolder extends RecyclerView.ViewHolder {
    public TextView ProductName,productPrice, tvCount;
    public ImageButton btnMinus, btnPlus;

    public BillingViewHolder(@NonNull View itemView) {
        super(itemView);
        ProductName = itemView.findViewById(R.id.billing_item_name);
        productPrice = itemView.findViewById(R.id.billing_item_price);
        tvCount = itemView.findViewById(R.id.billing_item_count);
        btnMinus = itemView.findViewById(R.id.billing_item_minus);
        btnPlus = itemView.findViewById(R.id.billing_item_plus);
    }
}
