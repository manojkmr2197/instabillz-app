package com.app.billing.instabillz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.model.ProductModel;

import java.util.List;

public class ProductDropDownAdapter extends ArrayAdapter<ProductModel> {

    private List<ProductModel> productList;

    public ProductDropDownAdapter(Context context, List<ProductModel> list) {
        super(context, 0, list);
        this.productList = list;
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
        details.setText("₹" + model.getPrice() + " | Qty: " + model.getQty());

        return convertView;
    }
}

