package com.app.billing.instabillz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.model.ProductModel;

import java.util.ArrayList;
import java.util.List;

public class ProductDropDownAdapter extends ArrayAdapter<ProductModel> {

    private List<ProductModel> productList;     // filtered list
    private List<ProductModel> fullList;        // original full list

    public ProductDropDownAdapter(Context context, List<ProductModel> list) {
        super(context, 0, list);
        this.productList = list;
        this.fullList = new ArrayList<>(list); // keep backup for filtering
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.product_dropdown_design, parent, false);
        }

        ProductModel model = productList.get(position);

        TextView name = convertView.findViewById(R.id.tvProductName);
        TextView details = convertView.findViewById(R.id.tvProductDetails);

        name.setText(model.getName());
        details.setText("₹ " + model.getPrice());

        return convertView;
    }

    // ---------------------------------------------------
    // 🔥 IMPORTANT: Case-insensitive and contains based FILTER
    // ---------------------------------------------------
    @NonNull
    @Override
    public Filter getFilter() {
        return productFilter;
    }

    private final Filter productFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ProductModel> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(fullList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (ProductModel item : fullList) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = suggestions;
            results.count = suggestions.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            productList.clear();
            productList.addAll((List<ProductModel>) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((ProductModel) resultValue).getName();
        }
    };
}

