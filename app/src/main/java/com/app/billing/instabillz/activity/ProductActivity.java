package com.app.billing.instabillz.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.ProductViewAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.ReportGenerator;
import com.app.billing.instabillz.utils.SingleTon;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProductActivity extends AppCompatActivity implements View.OnClickListener {

    TextView back, download;
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
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_product);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
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
                if ("EDIT".equalsIgnoreCase(type)) {
                    productAddDialog(filteredList.get(index));
                } else if ("DELETE".equalsIgnoreCase(type)) {
                    productDeleteConfirmationPopUp(filteredList.get(index));
                }
            }
        };

        products = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new ProductViewAdapter(context, filteredList, listener);
        recyclerView.setAdapter(adapter);

        // 🔎 Listen to text changes
        productSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                productPageFilter(s.toString());
            }
        });

        loadProductList();

    }

    private void productDeleteConfirmationPopUp(ProductModel productModel) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to Delete the Product ["+productModel.getName()+"]");
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            try {
                Toast.makeText(context, "Refreshing.!", Toast.LENGTH_LONG).show();
                deleteProductItem(productModel.getId());
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

        if (view.getId() == R.id.product_back) {
            finish();
        } else if (view.getId() == R.id.product_download) {
            downloadProductList();
        } else if (view.getId() == R.id.product_add_fab) {
            productAddDialog(null);
        }

    }
    private void downloadProductList() {
        try {
            saveExcelFile(products);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Report Generation failed ..!", Toast.LENGTH_LONG).show();
        }
    }

    private void saveExcelFile(List<ProductModel> productModelList) throws Exception {
        String fileName = "product-" + System.currentTimeMillis() + ".xlsx";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        ReportGenerator reportGenerator = new ReportGenerator();
        reportGenerator.createProductExcelReport(productModelList, file);

        // Notify the user
        Toast.makeText(this, "Report Generated: " + fileName, Toast.LENGTH_LONG).show();

        // Use FileProvider to get the URI
        Uri fileUri = FileProvider.getUriForFile(this, AppConstants.COM_APP_BILLING_INSTABILLZ_FILEPROVIDER, file);

        // Open the file using a file explorer
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.ms-excel");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void productAddDialog(ProductModel productModel) {
        Dialog dialog = new Dialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_product_create, null);
        dialog.setContentView(sheetView);
        dialog.setCanceledOnTouchOutside(false);

        // Transparent background for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Set dialog position to TOP
            Window window = dialog.getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.TOP;  // 👈 This makes it appear at the top
            params.y = 60; // optional: push it slightly down (in dp)
            window.setAttributes(params);

            // Optional: match width to parent
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // Optional animation (slide from top)
            window.getAttributes().windowAnimations = R.style.TopDialogAnimation;
        }

        EditText productName = sheetView.findViewById(R.id.product_create_name);
        EditText productPrice = sheetView.findViewById(R.id.product_create_price);
        Button submit = sheetView.findViewById(R.id.product_create_submit);
        TextView delete = sheetView.findViewById(R.id.product_create_delete);
        TextView close = sheetView.findViewById(R.id.product_create_close);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SingleTon.hideKeyboard(context,activity);
                dialog.dismiss();
            }
        });

        if (productModel != null) {
            productName.setText(productModel.getName());
            productPrice.setText(String.valueOf(productModel.getPrice()));
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    productDeleteConfirmationPopUp(productModel);
                    dialog.dismiss();
                }
            });
        } else {
            delete.setVisibility(View.GONE);
        }
        submit.setOnClickListener(b -> {
            ProductModel newProductModel = productModel;
            if (newProductModel == null) {
                newProductModel = new ProductModel();
                newProductModel.setId(SingleTon.generateProductDocument());
            }
            newProductModel.setName(productName.getText().toString().toUpperCase());
            newProductModel.setPrice(Double.parseDouble(productPrice.getText().toString()));

            InstaFirebaseRepository.getInstance().addDataBase(AppConstants.APP_NAME + AppConstants.PRODUCTS_COLLECTION, newProductModel.getId(), newProductModel, new InstaFirebaseRepository.OnFirebaseWriteListener() {
                @Override
                public void onSuccess(Object orderId) {
                    Toast.makeText(context, "New product Added", Toast.LENGTH_LONG).show();
                    loadProductList();
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
                }
            });

            dialog.dismiss();
        });

        dialog.show();

    }

    private void deleteProductItem(String id){
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().deleteData(AppConstants.APP_NAME + AppConstants.PRODUCTS_COLLECTION, id, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                Toast.makeText(context, "Product Removed", Toast.LENGTH_LONG).show();
                loadProductList();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadProductList() {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getAllDetails(AppConstants.APP_NAME + AppConstants.PRODUCTS_COLLECTION, "name", Query.Direction.ASCENDING, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                products.clear();
                filteredList.clear();
                QuerySnapshot documentSnapshotList = (QuerySnapshot) data;
                for (DocumentSnapshot doc : documentSnapshotList) {
                    products.add(doc.toObject(ProductModel.class));
                }
                filteredList.addAll(products);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });

    }
}