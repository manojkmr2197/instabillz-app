package com.app.billing.instabillz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.StockModel;
import com.app.billing.instabillz.viewholder.StockViewHolder;

import java.util.List;

public class StockViewAdapter extends RecyclerView.Adapter<StockViewHolder>{

    private Context context;
    private List<StockModel> itemList;
    private BillingClickListener clickListener;

    public StockViewAdapter(Context context, List<StockModel> itemList, BillingClickListener clickListener) {
        this.context = context;
        this.itemList = itemList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_item_design, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        holder.name.setText(itemList.get(position).getName());
        holder.qty.setText(itemList.get(position).getQuantity()+" "+itemList.get(position).getUnit());
        holder.load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(position,"LOAD");
            }
        });

        holder.unLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(position,"UNLOAD");
            }
        });

        holder.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(position, "DELETE");
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
