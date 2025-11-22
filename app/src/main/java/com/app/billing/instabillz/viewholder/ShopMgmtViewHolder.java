package com.app.billing.instabillz.viewholder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;

public class ShopMgmtViewHolder extends RecyclerView.ViewHolder{

    public TextView name,phone,address,expiryDate,onboardDate;
    public ImageButton edit,delete;

    public ShopMgmtViewHolder(@NonNull View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.shop_item_name);
        phone = (TextView) itemView.findViewById(R.id.shop_item_phone);
        address = (TextView) itemView.findViewById(R.id.shop_item_address);
        expiryDate = (TextView) itemView.findViewById(R.id.shop_item_expiry_date);
        onboardDate = (TextView) itemView.findViewById(R.id.shop_item_onboard_date);
        edit = (ImageButton) itemView.findViewById(R.id.shop_item_edit);
        delete = (ImageButton) itemView.findViewById(R.id.shop_item_delete);
    }
}
