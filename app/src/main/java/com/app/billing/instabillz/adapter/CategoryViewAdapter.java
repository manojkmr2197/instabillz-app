package com.app.billing.instabillz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.CategoryModel;

import java.util.List;

public class CategoryViewAdapter extends RecyclerView.Adapter<CategoryViewAdapter.CategoryViewHolder> {

    private Context context;
    private List<CategoryModel> categoryList;
    private BillingClickListener clickListener;


    public CategoryViewAdapter(Context context, List<CategoryModel> categoryList, BillingClickListener clickListener) {
        this.context = context;
        this.categoryList = categoryList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_item_design, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryModel category = categoryList.get(position);
        holder.tvCategoryName.setText(category.getName());

        holder.ivDelete.setOnClickListener(v -> {
            clickListener.click(position, "DELETE");
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        ImageView ivDelete;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            ivDelete = itemView.findViewById(R.id.ibCategoryDelete);
        }
    }
}

