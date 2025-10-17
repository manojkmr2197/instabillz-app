package com.app.billing.instabillz.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;

public class InvoiceProductViewHolder extends RecyclerView.ViewHolder{

    public Button delete;
    public TextView productName,productQty,productPrice;
    public InvoiceProductViewHolder(@NonNull View itemView) {
        super(itemView);
        productName = (TextView) itemView.findViewById(R.id.invoice_product_item_name);
        productQty = (TextView) itemView.findViewById(R.id.invoice_product_item_qty);
        productPrice = (TextView) itemView.findViewById(R.id.invoice_product_item_price);
        delete = (Button) itemView.findViewById(R.id.invoice_product_item_close);
    }
}
