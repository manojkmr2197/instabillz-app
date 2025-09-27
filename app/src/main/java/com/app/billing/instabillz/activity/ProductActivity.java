package com.app.billing.instabillz.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.BillingViewAdapter;
import com.app.billing.instabillz.adapter.ProductViewAdapter;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.ProductModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ProductActivity extends AppCompatActivity implements View.OnClickListener {

    TextView back,download;
    EditText etSearch;
    RecyclerView recyclerView;
    FloatingActionButton add_fab;

    Context context;
    Activity activity;

    EditText productSearch;
    List<ProductModel> filteredList;
    List<ProductModel> products;
    ProductViewAdapter adapter;
    BillingClickListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
        }

        context = ProductActivity.this;
        activity = ProductActivity.this;

        productSearch = (EditText) findViewById(R.id.product_etSearch);
        recyclerView = (RecyclerView) findViewById(R.id.product_recyclerView);
        back = (TextView) findViewById(R.id.product_back);
        back.setOnClickListener(this);

        download = (TextView) findViewById(R.id.product_download);
        download.setOnClickListener(this);

        add_fab = (FloatingActionButton) findViewById(R.id.product_add_fab);
        add_fab.setOnClickListener(this);

        RecyclerView recyclerView = findViewById(R.id.product_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        listener = new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if ("ADD".equalsIgnoreCase(type)) {

                } else if ("REMOVE".equalsIgnoreCase(type)) {

                }
            }
        };

        products = new ArrayList<>();
        products.add(new ProductModel("Regular Tea", 15.0));
        products.add(new ProductModel("Ginger Tea", 20.0));
        products.add(new ProductModel("Masala Tea", 25.0));
        products.add(new ProductModel("Green Tea", 30.0));
        products.add(new ProductModel("Lemon Tea", 25.0));
        products.add(new ProductModel("Black Tea", 20.0));
        products.add(new ProductModel("Coffee", 25.0));
        products.add(new ProductModel("Boost", 30.0));
        products.add(new ProductModel("Horlicks", 30.0));
        products.add(new ProductModel("Milk", 15.0));
        products.add(new ProductModel("Badam Milk", 40.0));
        products.add(new ProductModel("Samosa", 15.0));
        products.add(new ProductModel("Veg Puff", 20.0));
        products.add(new ProductModel("Egg Puff", 25.0));
        products.add(new ProductModel("Chicken Puff", 30.0));
        products.add(new ProductModel("Vada", 10.0));
        products.add(new ProductModel("Bajji", 10.0));
        products.add(new ProductModel("Bonda", 15.0));
        products.add(new ProductModel("Onion Pakoda", 20.0));
        products.add(new ProductModel("Biscuits", 10.0));

        filteredList = new ArrayList<>();
        filteredList.addAll(products);
        adapter = new ProductViewAdapter(context,filteredList,listener);
        recyclerView.setAdapter(adapter);

        // 🔎 Listen to text changes
        productSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                productPageFilter(s.toString());
            }
        });

    }

    private void productPageFilter(CharSequence constraint) {
        filteredList.clear();
        if (constraint == null || constraint.length() == 0) {
            filteredList.addAll(products);
        } else {
            String filterPattern = constraint.toString().toLowerCase().trim();
            for (ProductModel item : products) {
                if (item.getName().toLowerCase().startsWith(filterPattern)) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.product_back){
            finish();
        }else if(view.getId() == R.id.product_download){

        }else if(view.getId() == R.id.product_add_fab){

        }

    }
}