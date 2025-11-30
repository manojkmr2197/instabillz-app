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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.ProductViewAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.CategoryModel;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.ReportGenerator;
import com.app.billing.instabillz.utils.SharedPrefHelper;
import com.app.billing.instabillz.utils.SingleTon;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductActivity extends AppCompatActivity implements View.OnClickListener {

    TextView back, download;
    RecyclerView recyclerView;
    FloatingActionButton add_fab, bulk_add_fab;

    Context context;
    Activity activity;
    SharedPrefHelper sharedPrefHelper;

    LinearLayout layoutCategories;
    EditText productSearch;
    List<ProductModel> filteredList;
    List<ProductModel> products;
    ProductViewAdapter adapter;
    BillingClickListener listener;

    String selectedCategory = "", searchString = "";
    List<String> categoryModelList = new ArrayList<>();

    private static final int PICK_EXCEL = 101;
    BottomSheetDialog uploadSheet;
    List<ProductModel> excelRecords = new ArrayList<>();
    Set<String> categorySet = new HashSet<>();

    private AlertDialog loaderDialog;


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
            window.setStatusBarColor(getResources().getColor(R.color.status_bar_color, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        context = ProductActivity.this;
        activity = ProductActivity.this;
        sharedPrefHelper = new SharedPrefHelper(this);

        productSearch = (EditText) findViewById(R.id.product_etSearch);
        layoutCategories = findViewById(R.id.layoutCategories);
        recyclerView = (RecyclerView) findViewById(R.id.product_recyclerView);
        back = (TextView) findViewById(R.id.product_back);
        back.setOnClickListener(this);

        download = (TextView) findViewById(R.id.product_download);
        download.setOnClickListener(this);

        add_fab = (FloatingActionButton) findViewById(R.id.product_add_fab);
        add_fab.setOnClickListener(this);

        bulk_add_fab = (FloatingActionButton) findViewById(R.id.product_bulk_add_fab);
        bulk_add_fab.setOnClickListener(this);

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
                searchString = s.toString();
                productPageFilter(searchString);
            }
        });
        loadProductList();
        loadCategoryList();
    }

    private void productDeleteConfirmationPopUp(ProductModel productModel) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to Delete the Product [" + productModel.getName() + "]");
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

        if (StringUtils.isNotBlank(selectedCategory) && !selectedCategory.equalsIgnoreCase("All")) {
            for (int i = filteredList.size() - 1; i >= 0; i--) {
                if (!selectedCategory.equalsIgnoreCase(filteredList.get(i).getCategoryName())) {
                    filteredList.remove(i);
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
        } else if (view.getId() == R.id.product_bulk_add_fab) {
            askWarningPopup();
        }

    }

    private void askWarningPopup() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(context);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_bulk_upload_warning, null);
        bottomSheet.setContentView(sheetView);

        Button btnContinue = sheetView.findViewById(R.id.btnContinue);
        Button btnCancel = sheetView.findViewById(R.id.btnCancel);

        btnContinue.setOnClickListener(view -> {
            bottomSheet.dismiss();
            showUploadBottomSheet();

        });

        btnCancel.setOnClickListener(view -> bottomSheet.dismiss());

        bottomSheet.show();
    }


    private void showUploadBottomSheet() {
        uploadSheet = new BottomSheetDialog(context);
        View view = getLayoutInflater().inflate(R.layout.dialog_bulk_upload_submit, null);
        uploadSheet.setContentView(view);

        TextView tvFileName = view.findViewById(R.id.tvFileName);
        TextView tvRowCount = view.findViewById(R.id.tvRowCount);
        Button btnChooseFile = view.findViewById(R.id.btnChooseFile);
        Button btnSync = view.findViewById(R.id.btnSync);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnChooseFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            startActivityForResult(intent, PICK_EXCEL);
        });

        btnCancel.setOnClickListener(v -> uploadSheet.dismiss());

        uploadSheet.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_EXCEL && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            parseExcel(uri);
        }
    }

    private void parseExcel(Uri uri) {
        excelRecords.clear();
        List<String> errorList = new ArrayList<>();

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    errorList.add("Row " + (i + 1) + " → Empty row");
                    continue;
                }

                try {
                    String name = row.getCell(0).getStringCellValue();
                    String price = row.getCell(1).toString();
                    String category = row.getCell(2).getStringCellValue();

                    if (name == null || name.trim().isEmpty()) {
                        throw new Exception("Product name missing");
                    }
                    if (price == null || price.trim().isEmpty()) {
                        throw new Exception("Product price missing");
                    }
                    if (category == null || category.trim().isEmpty()) {
                        throw new Exception("Product price missing");
                    }
                    categorySet.add(category.toUpperCase());
                    excelRecords.add(new ProductModel(name.toUpperCase(), Double.valueOf(price), category.toUpperCase()));

                } catch (Exception e) {
                    errorList.add("Row " + (i + 1) + " → " + e.getMessage());
                }
            }

            // Update UI in Bottom Sheet
            TextView tvFileName = uploadSheet.findViewById(R.id.tvFileName);
            TextView tvRowCount = uploadSheet.findViewById(R.id.tvRowCount);
            TextView tvErrors = uploadSheet.findViewById(R.id.tvErrors);
            Button btnSync = uploadSheet.findViewById(R.id.btnSync);

            tvFileName.setText("File Loaded Successfully");

            // Show errors if any
            if (!errorList.isEmpty()) {
                tvErrors.setVisibility(View.VISIBLE);
                tvRowCount.setVisibility(View.GONE);

                StringBuilder sb = new StringBuilder();
                for (String err : errorList) {
                    sb.append(err).append("\n");
                }
                tvErrors.setText(sb.toString());
            } else {
                tvErrors.setVisibility(View.GONE);
                tvRowCount.setText("Valid Rows: " + excelRecords.size());
            }

            // Sync only if at least 1 valid row exists
            if (!excelRecords.isEmpty() && errorList.isEmpty()) {
                btnSync.setVisibility(View.VISIBLE);

                btnSync.setOnClickListener(v -> {
                    bulkUploadProducts();
                    uploadSheet.dismiss();
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading Excel file", Toast.LENGTH_SHORT).show();
        }
    }

    private Task<Void> deleteCollection(String collectionName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference colRef = db.collection(collectionName);

        return colRef.get().continueWithTask(task -> {
            WriteBatch batch = db.batch();
            for (DocumentSnapshot doc : task.getResult()) {
                batch.delete(doc.getReference());
            }
            return batch.commit();
        });
    }


    private Task<Void> bulkInsert(String collectionName, List<?> list) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Task<Void>> taskList = new ArrayList<>();

        int batchSize = 500;
        WriteBatch batch = db.batch();
        int counter = 0;

        for (Object obj : list) {

            DocumentReference ref = db.collection(collectionName).document();
            batch.set(ref, obj);
            counter++;

            if (counter == batchSize) {
                taskList.add(batch.commit());
                batch = db.batch();
                counter = 0;
            }
        }

        // Commit last batch
        if (counter > 0) {
            taskList.add(batch.commit());
        }

        return Tasks.whenAll(taskList);
    }

    private void showLoader() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(R.layout.loader_layout); // Your custom loader layout with ProgressBar
        loaderDialog = builder.create();
        loaderDialog.show();
    }

    private void hideLoader() {
        if (loaderDialog != null && loaderDialog.isShowing()) {
            loaderDialog.dismiss();
        }
    }


    private void bulkUploadProducts() {

        showLoader();
        List<CategoryModel> categoryList = new ArrayList<>();
        for (String cat : categorySet) {
            categoryList.add(new CategoryModel(SingleTon.generateCategoryDocument(), cat));
        }

        for (ProductModel productModel : excelRecords) {
            productModel.setId(SingleTon.generateProductDocument());
        }

        // STEP 1: Delete categories
        deleteCollection(sharedPrefHelper.getAppName() + AppConstants.CATEGORIES_COLLECTION)
                .addOnSuccessListener(a -> {

                    // STEP 2: Delete products
                    deleteCollection(sharedPrefHelper.getAppName() + AppConstants.PRODUCTS_COLLECTION)
                            .addOnSuccessListener(b -> {

                                // STEP 3: Insert categories
                                bulkInsert(sharedPrefHelper.getAppName() + AppConstants.CATEGORIES_COLLECTION, categoryList)
                                        .addOnSuccessListener(c -> {

                                            // STEP 4: Insert products
                                            bulkInsert(sharedPrefHelper.getAppName() + AppConstants.PRODUCTS_COLLECTION, excelRecords)
                                                    .addOnSuccessListener(d -> {
                                                        hideLoader();
                                                        Toast.makeText(
                                                                this,
                                                                "Sync Completed!\n" +
                                                                        "Categories: " + categoryList.size() + "\n" +
                                                                        "Products: " + excelRecords.size(),
                                                                Toast.LENGTH_LONG
                                                        ).show();

                                                        uploadSheet.dismiss();
                                                    });
                                        });
                            });
                });

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

        Spinner categorySpinner = sheetView.findViewById(R.id.product_create_category_spinner);
        ArrayAdapter<String> arrayadapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, categoryModelList.subList(1, categoryModelList.size()));
        categorySpinner.setAdapter(arrayadapter);
        categorySpinner.setSelection(0);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SingleTon.hideKeyboard(context, activity);
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

            if (StringUtils.isNotBlank(productModel.getCategoryName())) {
                categorySpinner.setSelection(arrayadapter.getPosition(productModel.getCategoryName()));
            }
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
            newProductModel.setCategoryName(categorySpinner.getSelectedItem().toString());

            InstaFirebaseRepository.getInstance().addDataBase(sharedPrefHelper.getAppName() + AppConstants.PRODUCTS_COLLECTION, newProductModel.getId(), newProductModel, new InstaFirebaseRepository.OnFirebaseWriteListener() {
                @Override
                public void onSuccess(Object orderId) {
                    Toast.makeText(context, "Products Updated", Toast.LENGTH_LONG).show();
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

    private void deleteProductItem(String id) {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().deleteData(sharedPrefHelper.getAppName() + AppConstants.PRODUCTS_COLLECTION, id, new InstaFirebaseRepository.OnFirebaseWriteListener() {
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
        InstaFirebaseRepository.getInstance().getAllDetails(sharedPrefHelper.getAppName() + AppConstants.PRODUCTS_COLLECTION, "name", Query.Direction.ASCENDING, new InstaFirebaseRepository.OnFirebaseWriteListener() {
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

    private void loadCategoryList() {

        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getAllDetails(sharedPrefHelper.getAppName() + AppConstants.CATEGORIES_COLLECTION, "name", Query.Direction.ASCENDING, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                categoryModelList.clear();
                categoryModelList.add("All");
                QuerySnapshot documentSnapshotList = (QuerySnapshot) data;
                for (DocumentSnapshot doc : documentSnapshotList) {
                    categoryModelList.add(doc.toObject(CategoryModel.class).getName());
                }
                // Inflate category chips
                for (String category : categoryModelList) {
                    TextView tv = new TextView(context);
                    tv.setText(category);
                    tv.setTextSize(16);
                    tv.setPadding(24, 12, 24, 12);
                    tv.setBackground(ContextCompat.getDrawable(context, R.drawable.category_chip_bg));
                    tv.setTextColor(ContextCompat.getColor(context, android.R.color.black));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(10, 8, 10, 8);
                    tv.setLayoutParams(params);

                    // Optional: handle click
                    tv.setOnClickListener(v -> {
                        // You can handle category click here (e.g. filter product list)
                        // Example: highlight the selected one
                        highlightSelectedCategory(tv);
                        selectedCategory = tv.getText().toString();
                        productPageFilter(searchString);
                    });

                    layoutCategories.addView(tv);
                    if (category.equalsIgnoreCase("All")) {
                        highlightSelectedCategory(tv);
                        selectedCategory = tv.getText().toString();
                        productPageFilter(searchString);
                    }
                }

            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void highlightSelectedCategory(TextView selectedView) {
        // Reset all first
        for (int i = 0; i < layoutCategories.getChildCount(); i++) {
            TextView tv = (TextView) layoutCategories.getChildAt(i);
            tv.setBackground(ContextCompat.getDrawable(this, R.drawable.category_chip_bg));
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        }
        // Highlight selected
        selectedView.setBackground(ContextCompat.getDrawable(this, R.drawable.category_chip_selected_bg));
        selectedView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

}