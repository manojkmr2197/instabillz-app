package com.app.billing.instabillz.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;

public class ProductViewHolder extends RecyclerView.ViewHolder {

    public Button delete, edit;
    public TextView productName,productPrice;
    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);
        productName = (TextView) itemView.findViewById(R.id.product_item_name);
        productPrice = (TextView) itemView.findViewById(R.id.product_item_price);
        delete = (Button) itemView.findViewById(R.id.product_item_close);
        edit = (Button) itemView.findViewById(R.id.product_item_edit);
    }
}
