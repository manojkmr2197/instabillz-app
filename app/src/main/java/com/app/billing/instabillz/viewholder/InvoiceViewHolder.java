package com.app.billing.instabillz.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;

public class InvoiceViewHolder extends RecyclerView.ViewHolder{

    public TextView tokenTv, dateTv, totalCostTv, sellingCostTv, parcelCostTv, paymentModeTv, toggleProductsTv;
    public LinearLayout productListLayout;
    public ImageView editBtn, deleteBtn;

    public InvoiceViewHolder(@NonNull View itemView) {
        super(itemView);
        tokenTv = itemView.findViewById(R.id.invoice_token);
        dateTv = itemView.findViewById(R.id.invoice_date);
        totalCostTv = itemView.findViewById(R.id.invoice_total_price);
        sellingCostTv = itemView.findViewById(R.id.invoice_selling_price);
        parcelCostTv = itemView.findViewById(R.id.invoice_parcel_cost);
        paymentModeTv = itemView.findViewById(R.id.invoice_payment_mode);
        toggleProductsTv = itemView.findViewById(R.id.invoice_toggle_products_tv);
        productListLayout = itemView.findViewById(R.id.invoice_products_container);
        editBtn = itemView.findViewById(R.id.invoice_edit);
        deleteBtn = itemView.findViewById(R.id.invoice_delete);
    }


}
