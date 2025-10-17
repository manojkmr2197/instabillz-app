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
import com.app.billing.instabillz.viewholder.InvoiceProductViewHolder;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class InvoiceProductViewAdapter extends RecyclerView.Adapter<InvoiceProductViewHolder> {

    private Context context;
    private List<ProductModel> productModelList;
    private BillingClickListener clickListener;

    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public InvoiceProductViewAdapter(Context context, List<ProductModel> productModelList, BillingClickListener clickListener) {
        this.context = context;
        this.productModelList = productModelList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public InvoiceProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.invoice_edit_product_item_design, parent, false);
        return new InvoiceProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceProductViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.productName.setText(productModelList.get(position).getName());
        holder.productQty.setText("x"+productModelList.get(position).getQty());
        holder.productPrice.setText(numberFormat.format(productModelList.get(position).getQty()*productModelList.get(position).getPrice()).replace("\u00A0", ""));


        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(position,"DELETE");
            }
        });
    }

    @Override
    public int getItemCount() {
        return productModelList.size();
    }
}
