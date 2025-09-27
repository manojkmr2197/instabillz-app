package com.app.billing.instabillz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.viewholder.BillingViewHolder;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BillingViewAdapter extends RecyclerView.Adapter<BillingViewHolder> {

    private List<ProductModel> productList;
    private Context context;
    private BillingClickListener clickListener;

    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public BillingViewAdapter(Context context,List<ProductModel> productList, BillingClickListener clickListener) {
        this.productList = productList;
        this.context = context;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public BillingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.billing_item_design, parent, false);
        return new BillingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillingViewHolder holder, int position) {
        ProductModel product = productList.get(position);
        holder.ProductName.setText(product.getName());
        holder.productPrice.setText(numberFormat.format(product.getPrice()).replace("\u00A0", ""));
        holder.tvCount.setText(String.valueOf(product.getQty()));
        holder.btnMinus.setOnClickListener(v -> {
            if (product.getQty() > 0) {
                product.setQty(product.getQty() - 1);
                notifyItemChanged(position);
                clickListener.click(position,"REMOVE");
            }

        });

        holder.btnPlus.setOnClickListener(v -> {
            product.setQty(product.getQty() + 1);
            notifyItemChanged(position);
            clickListener.click(position,"ADD");
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
