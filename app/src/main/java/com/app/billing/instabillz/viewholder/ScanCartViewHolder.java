package com.app.billing.instabillz.viewholder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;

public class ScanCartViewHolder extends RecyclerView.ViewHolder {
    public TextView tvName, tvQty,tvUnitPrice, tvPrice;
    public ImageButton delete;

    public ScanCartViewHolder(@NonNull View v) {
        super(v);
        tvName = v.findViewById(R.id.tvName);
        tvQty = v.findViewById(R.id.tvQty);
        tvUnitPrice = v.findViewById(R.id.tvUnitPrice);
        tvPrice = v.findViewById(R.id.tvPrice);
        delete = v.findViewById(R.id.cart_item_delete);
    }

}
