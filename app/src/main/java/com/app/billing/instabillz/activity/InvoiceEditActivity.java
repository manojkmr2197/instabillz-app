package com.app.billing.instabillz.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.InvoiceProductViewAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.InvoiceModel;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.SharedPrefHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InvoiceEditActivity extends AppCompatActivity implements View.OnClickListener {

    InvoiceModel editInvoiceModel = new InvoiceModel();

    Context context;
    Activity activity;
    SharedPrefHelper sharedPrefHelper;

    String[] paymentMode = {""};

    TextView token, date;
    Button addProductBt, updateInvoiceBt;

    TextView totalPrice, sellingPrice;
    EditText parcelPrice;

    RadioGroup paymentGroup;
    RadioButton cashRadioBt, upiRadioBt;

    RecyclerView productsRecycler;
    List<ProductModel> productModelList = new ArrayList<>();
    InvoiceProductViewAdapter adapter;
    BillingClickListener clickListener;

    List<ProductModel> allProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_edit);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.status_bar_color, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        context = InvoiceEditActivity.this;
        activity = InvoiceEditActivity.this;
        sharedPrefHelper = new SharedPrefHelper(this);

        TextView back = (TextView) findViewById(R.id.invoice_edit_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Get data from intent
        String invoiceJson = getIntent().getStringExtra("invoice");
        if (StringUtils.isBlank(invoiceJson)) {
            Toast.makeText(context, "Invoice Data not loaded.!", Toast.LENGTH_LONG).show();
            finish();
        }
        editInvoiceModel = new Gson().fromJson(invoiceJson, InvoiceModel.class);
        loadProductList();

        token = (TextView) findViewById(R.id.invoice_edit_token);
        date = (TextView) findViewById(R.id.invoice_edit_date);

        addProductBt = (Button) findViewById(R.id.invoice_edit_add_product);
        addProductBt.setOnClickListener(this);
        updateInvoiceBt = (Button) findViewById(R.id.invoice_edit_update);
        updateInvoiceBt.setOnClickListener(this);

        totalPrice = (TextView) findViewById(R.id.invoice_edit_total_price);
        sellingPrice = (TextView) findViewById(R.id.invoice_edit_selling_price);
        parcelPrice = (EditText) findViewById(R.id.invoice_edit_courier_charge);

        parcelPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    return;
                }
                editInvoiceModel.setParcelCost(Double.parseDouble(s.toString()));
                editInvoiceModel.setSellingCost(editInvoiceModel.getTotalCost() + editInvoiceModel.getParcelCost());
                sellingPrice.setText("Final Price: ₹" + (editInvoiceModel.getTotalCost() + editInvoiceModel.getParcelCost()));
            }
        });

        paymentGroup = (RadioGroup) findViewById(R.id.invoice_edit_payment_radio_group);
        cashRadioBt = (RadioButton) findViewById(R.id.invoice_edit_payment_cash);
        upiRadioBt = (RadioButton) findViewById(R.id.invoice_edit_payment_upi);

        paymentGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Find which radio button is selected
                if (R.id.invoice_edit_payment_cash == checkedId) {
                    paymentMode[0] = "CASH";
                } else if (R.id.invoice_edit_payment_upi == checkedId) {
                    paymentMode[0] = "UPI";
                }
                editInvoiceModel.setPaymentMode(paymentMode[0]);
            }
        });

        productsRecycler = (RecyclerView) findViewById(R.id.invoice_edit_products_recycler);
        productsRecycler.setNestedScrollingEnabled(false);
        productsRecycler.setHasFixedSize(false);

        clickListener = new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if (type.equalsIgnoreCase("DELETE")) {
                    deleteProductConfirmation(index);
                }
            }
        };
        productsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvoiceProductViewAdapter(context, productModelList, clickListener);
        productsRecycler.setAdapter(adapter);

        setupUI();
    }


    private void deleteProductConfirmation(int index) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to Delete the Product [" + productModelList.get(index).getName() + "]");
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            try {
                Toast.makeText(context, "Refreshing.!", Toast.LENGTH_LONG).show();
                productModelList.remove(index);
                adapter.notifyDataSetChanged();
                productsRecycler.post(() -> {
                    productsRecycler.invalidate();
                    productsRecycler.requestLayout();
                });
            } catch (Exception e) {
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });

        // Set negative button
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });

        // Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setupUI() {
        token.setText("Token #" + editInvoiceModel.getToken());
        date.setText("Date: " + new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date(editInvoiceModel.getBillingDate() * 1000)));
        switch (editInvoiceModel.getPaymentMode()) {
            case "CASH":
                cashRadioBt.setChecked(true);
                paymentMode[0] = "CASH";
                break;
            case "UPI":
                upiRadioBt.setChecked(true);
                paymentMode[0] = "UPI";
                break;
        }

        totalPrice.setText("Total Price: ₹" + editInvoiceModel.getTotalCost());
        sellingPrice.setText("Final Price: ₹" + editInvoiceModel.getSellingCost());

        if (editInvoiceModel.getParcelCost() != null && editInvoiceModel.getParcelCost() > 0) {
            parcelPrice.setText("" + editInvoiceModel.getParcelCost());
            parcelPrice.setVisibility(View.VISIBLE);
        } else {
            parcelPrice.setVisibility(View.VISIBLE);
            parcelPrice.setText("0");
        }

        // Product List
        productModelList.clear();
        productModelList.addAll(editInvoiceModel.getProductModelList());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.invoice_edit_add_product) {
            showAddProductPopup();
        } else if (view.getId() == R.id.invoice_edit_update) {
            updateInvoice();
        }
    }

    private void updateInvoice() {
        editInvoiceModel.setProductModelList(productModelList);
        InstaFirebaseRepository.getInstance().addDataBase(sharedPrefHelper.getAppName() + AppConstants.SALES_COLLECTION, String.valueOf(editInvoiceModel.getBillingDate()), editInvoiceModel, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                Toast.makeText(context, "🧾 Invoice bill updated — Token #" + editInvoiceModel.getToken(), Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadProductList() {
        //Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getAllDetails(sharedPrefHelper.getAppName() + AppConstants.PRODUCTS_COLLECTION, "name", Query.Direction.ASCENDING, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                allProducts.clear();
                QuerySnapshot documentSnapshotList = (QuerySnapshot) data;
                for (DocumentSnapshot doc : documentSnapshotList) {
                    allProducts.add(doc.toObject(ProductModel.class));
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void showAddProductPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.invoice_edit_product_add_dialog_create, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        AutoCompleteTextView productNameAuto = view.findViewById(R.id.invoice_edit_product_add_name_autocomplete);
        TextView priceText = view.findViewById(R.id.invoice_edit_product_add_price_text);
        EditText qtyEdit = view.findViewById(R.id.invoice_edit_product_add_qty_edittext);
        Button addConfirmBtn = view.findViewById(R.id.invoice_edit_product_add_confirm_button);

        // Setup autocomplete
        List<String> productNames = new ArrayList<>();
        for (ProductModel p : allProducts) {
            productNames.add(p.getName());
        }
        ArrayAdapter<String> productNameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, productNames);
        productNameAuto.setAdapter(productNameAdapter);

        final ProductModel[] selectedProduct = {null};

        productNameAuto.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedName = productNameAdapter.getItem(position);
            for (ProductModel p : allProducts) {
                if (p.getName().equals(selectedName)) {
                    selectedProduct[0] = p;
                    priceText.setText("Price: ₹" + p.getPrice());
                    break;
                }
            }
        });

        addConfirmBtn.setOnClickListener(v -> {
            if (selectedProduct[0] == null) {
                Toast.makeText(this, "Please select a product", Toast.LENGTH_SHORT).show();
                return;
            }
            String qtyStr = qtyEdit.getText().toString().trim();
            if (qtyStr.isEmpty()) {
                Toast.makeText(this, "Enter quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            int qty = Integer.parseInt(qtyStr);

            boolean isExist =false;
            for(int i=0;i<productModelList.size();i++){
                if(productModelList.get(i).getName().equalsIgnoreCase(selectedProduct[0].getName())){
                    productModelList.get(i).setQty(productModelList.get(i).getQty()+qty);
                    isExist = true;
                }
            }
            if(!isExist){
                ProductModel product = selectedProduct[0];
                product.setQty(qty);
                productModelList.add(product);
            }

            adapter.notifyDataSetChanged();
            productsRecycler.post(() -> {
                productsRecycler.invalidate();
                productsRecycler.requestLayout();
            });
            reloadTotalPrice();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void reloadTotalPrice() {
        double total = 0.0;
        for (ProductModel product : productModelList) {
            if (product.getQty() > 0) {
                total += product.getQty() * product.getPrice();
            }
        }
        editInvoiceModel.setTotalCost(total);
        if (editInvoiceModel.getParcelCost() != null && editInvoiceModel.getParcelCost() > 0) {
            editInvoiceModel.setSellingCost(total + editInvoiceModel.getParcelCost());
        } else {
            editInvoiceModel.setSellingCost(total);
        }
        totalPrice.setText("Total Price: ₹" + editInvoiceModel.getTotalCost());
        sellingPrice.setText("Final Price: ₹" + editInvoiceModel.getSellingCost());
    }
}