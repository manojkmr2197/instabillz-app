package com.app.billing.instabillz.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;

public class ExpenseViewHolder  extends RecyclerView.ViewHolder {
    public TextView tvType, tvDesc, tvDate, tvAmount;
    public ImageView btnDelete;

    public ExpenseViewHolder(@NonNull View v) {
        super(v);
        tvType = v.findViewById(R.id.expense_item_type);
        tvDesc = v.findViewById(R.id.expense_item_desc);
        tvDate = v.findViewById(R.id.expense_item_date);
        tvAmount = v.findViewById(R.id.expense_item_amount);
        btnDelete = v.findViewById(R.id.expense_item_close);
    }
}
