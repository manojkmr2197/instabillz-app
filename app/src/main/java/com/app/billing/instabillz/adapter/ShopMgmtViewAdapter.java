package com.app.billing.instabillz.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.model.ShopsModel;
import com.app.billing.instabillz.viewholder.ProductViewHolder;
import com.app.billing.instabillz.viewholder.ShopMgmtViewHolder;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ShopMgmtViewAdapter extends RecyclerView.Adapter<ShopMgmtViewHolder> {

    private Context context;
    private List<ShopsModel> shopsModelList;
    private BillingClickListener clickListener;

    public ShopMgmtViewAdapter(Context context, List<ShopsModel> shopsModelList, BillingClickListener clickListener) {
        this.context = context;
        this.shopsModelList = shopsModelList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ShopMgmtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shop_item_design, parent, false);
        return new ShopMgmtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopMgmtViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ShopsModel shopsModel = shopsModelList.get(position);

        holder.name.setText(shopsModel.getShopName());
        holder.phone.setText(shopsModel.getPhoneNumber());
        if(StringUtils.isNotBlank(shopsModel.getAddress()))
            holder.address.setText(shopsModel.getAddress());
        else
            holder.address.setVisibility(View.GONE);
        if (shopsModel.getOnboardingDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
            String formattedDate = sdf.format(shopsModel.getOnboardingDate());
            holder.onboardDate.setText("Onboarded: " + formattedDate);
        }

        if(shopsModel.getSubscriptionDate() != null){
            holder.expiryDate.setText("Expiry: "+shopsModel.getSubscriptionDate());
        }

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(position,"EDIT");
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(position,"DELETE");
            }
        });
    }

    @Override
    public int getItemCount() {
        return shopsModelList.size();
    }
}
