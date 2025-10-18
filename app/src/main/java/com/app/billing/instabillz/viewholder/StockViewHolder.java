package com.app.billing.instabillz.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;

public class StockViewHolder extends RecyclerView.ViewHolder {

    public TextView name,qty,vendorName,vendorPhone;
    public Button close,load,unLoad;

    public StockViewHolder(@NonNull View itemView) {
        super(itemView);
        close = (Button) itemView.findViewById(R.id.stock_item_close);
        load = (Button) itemView.findViewById(R.id.stock_item_load);
        unLoad = (Button) itemView.findViewById(R.id.stock_item_unload);
        name = (TextView) itemView.findViewById(R.id.stock_item_name);
        qty = (TextView) itemView.findViewById(R.id.stock_item_qty);
        vendorName = (TextView) itemView.findViewById(R.id.stock_item_vendor_name);
        vendorPhone = (TextView) itemView.findViewById(R.id.stock_item_vendor_phone);
    }
}
