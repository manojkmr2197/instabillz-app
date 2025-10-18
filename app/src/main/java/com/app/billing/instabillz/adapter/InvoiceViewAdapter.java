package com.app.billing.instabillz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.InvoiceModel;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.viewholder.InvoiceViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InvoiceViewAdapter extends RecyclerView.Adapter<InvoiceViewHolder>{

    private Context context;
    private List<InvoiceModel> invoiceList;
    private BillingClickListener clickListener;

    public InvoiceViewAdapter(Context context, List<InvoiceModel> invoiceList, BillingClickListener clickListener) {
        this.context = context;
        this.invoiceList = invoiceList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.invoice_item_design, parent, false);
        return new InvoiceViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        InvoiceModel invoice = invoiceList.get(position);

        holder.tokenTv.setText("Token #" + invoice.getToken());
        holder.dateTv.setText(formatDate(invoice.getBillingDate()));
        holder.totalCostTv.setText("Total: ₹" + invoice.getTotalCost());
        holder.sellingCostTv.setText("Selling: ₹" + invoice.getSellingCost());
        holder.paymentModeTv.setText("Payment: " + invoice.getPaymentMode());

        if (invoice.getParcelCost() != null && invoice.getParcelCost() > 0) {
            holder.parcelCostTv.setVisibility(View.VISIBLE);
            holder.parcelCostTv.setText("Parcel: ₹" + invoice.getParcelCost());
        } else {
            holder.parcelCostTv.setVisibility(View.GONE);
        }

        // Toggle product view
        holder.toggleProductsTv.setOnClickListener(v -> {
            if (holder.productListLayout.getVisibility() == View.VISIBLE) {
                holder.productListLayout.setVisibility(View.GONE);
                holder.toggleProductsTv.setText("Show Products ▼");
            } else {
                holder.productListLayout.setVisibility(View.VISIBLE);
                holder.toggleProductsTv.setText("Hide Products ▲");
            }
        });
        loadProducts(holder.productListLayout, invoice.getProductModelList());

        holder.editBtn.setOnClickListener(v -> clickListener.click(position, "EDIT"));
        holder.deleteBtn.setOnClickListener(v -> clickListener.click(position, "DELETE"));
        holder.printBtn.setOnClickListener(v -> clickListener.click(position, "PRINT"));

    }

    @Override
    public int getItemCount() {
        return invoiceList.size();
    }

    private String formatDate(Long epochSeconds) {
        if (epochSeconds == null) return "";
        Date date = new Date(epochSeconds * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    public void loadProducts(LinearLayout container, List<ProductModel> productList) {
        container.removeAllViews();
        for (ProductModel product : productList) {
            View productView = LayoutInflater.from(context).inflate(R.layout.invoice_item_product_design, container, false);
            ((TextView) productView.findViewById(R.id.product_name)).setText(product.getName());
            ((TextView) productView.findViewById(R.id.product_qty)).setText("x" + product.getQty());
            ((TextView) productView.findViewById(R.id.product_price)).setText("₹" + (product.getQty()*product.getPrice()));
            container.addView(productView);
        }
    }

}
