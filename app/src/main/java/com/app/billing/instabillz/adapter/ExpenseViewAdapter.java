package com.app.billing.instabillz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.ExpenseModel;
import com.app.billing.instabillz.viewholder.ExpenseViewHolder;

import java.util.List;


public class ExpenseViewAdapter extends RecyclerView.Adapter<ExpenseViewHolder> {

    private Context context;
    private List<ExpenseModel> expenseList;
    private BillingClickListener clickListener;

    public ExpenseViewAdapter(Context context, List<ExpenseModel> expenseList, BillingClickListener clickListener) {
        this.context = context;
        this.expenseList = expenseList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item_design, parent, false);
        return new ExpenseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder h, int position) {
        ExpenseModel e = expenseList.get(position);
        h.tvType.setText("Type: " + e.getType());
        h.tvDesc.setText("Desc: " + e.getDescription());
        h.tvDate.setText("Date: " + e.getDate());
        h.tvAmount.setText("₹ " + e.getAmount());
        h.btnDelete.setOnClickListener(v -> clickListener.click(position, "DELETE"));
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }
}

