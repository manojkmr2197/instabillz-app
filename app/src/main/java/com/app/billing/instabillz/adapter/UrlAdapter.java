package com.app.billing.instabillz.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;

import java.util.List;

public class UrlAdapter extends RecyclerView.Adapter<UrlAdapter.UrlViewHolder> {

    private List<String> urlList;
    private Context context;

    public UrlAdapter(Context context, List<String> urlList) {
        this.context = context;
        this.urlList = urlList;
    }

    @NonNull
    @Override
    public UrlViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_url, parent, false);
        return new UrlViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UrlViewHolder holder, int position) {
        String url = urlList.get(position);
        // Show only index number (starting from 1)
        holder.tvUrlItem.setText("Index URL - "+String.valueOf(position + 1));

        holder.tvUrlItem.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(browserIntent);
        });
    }

    @Override
    public int getItemCount() {
        return urlList.size();
    }

    static class UrlViewHolder extends RecyclerView.ViewHolder {
        TextView tvUrlItem;

        public UrlViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUrlItem = itemView.findViewById(R.id.tvUrlItem);
        }
    }
}

