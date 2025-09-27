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
import com.app.billing.instabillz.viewholder.ProductViewHolder;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;


public class ProductViewAdapter extends RecyclerView.Adapter<ProductViewHolder> {

    private Context context;
    private List<ProductModel> productList;
    private BillingClickListener clickListener;

    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public ProductViewAdapter(Context context, List<ProductModel> productList, BillingClickListener clickListener) {
        this.context = context;
        this.productList = productList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_item_design, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel product = productList.get(position);
        holder.productName.setText(product.getName());
        holder.productPrice.setText(numberFormat.format(product.getPrice()).replace("\u00A0", ""));

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(position, "DELETE");
            }
        });
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(position, "EDIT");
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
