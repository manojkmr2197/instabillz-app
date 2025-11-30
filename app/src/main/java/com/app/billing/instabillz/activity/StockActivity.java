package com.app.billing.instabillz.activity;

import android.annotation.SuppressLint;
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
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.StockViewAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.model.StockModel;
import com.app.billing.instabillz.model.VendorModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.ReportGenerator;
import com.app.billing.instabillz.utils.SharedPrefHelper;
import com.app.billing.instabillz.utils.SingleTon;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockActivity extends AppCompatActivity implements View.OnClickListener {
    TextView back, download;
    RecyclerView recyclerView;
    FloatingActionButton add_fab;
    LinearLayout emptyLayout;

    Context context;
    Activity activity;
    SharedPrefHelper sharedPrefHelper;

    EditText stockSearch;
    List<StockModel> filteredList;
    List<StockModel> stocks;
    StockViewAdapter adapter;
    BillingClickListener listener;

    List<VendorModel> vendorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);
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

        context = StockActivity.this;
        activity = StockActivity.this;
        sharedPrefHelper = new SharedPrefHelper(this);

        back = (TextView) findViewById(R.id.stock_back);
        back.setOnClickListener(this);
        download = (TextView) findViewById(R.id.stock_download);

        emptyLayout = (LinearLayout) findViewById(R.id.emptyLayout);
        stockSearch = (EditText) findViewById(R.id.stock_etSearch);
        add_fab = (FloatingActionButton) findViewById(R.id.stock_add_fab);

        add_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewStockItem();
            }
        });

        vendorList = new ArrayList<>();
        loadVendorList();

        recyclerView = (RecyclerView) findViewById(R.id.stock_recyclerView);

        listener = new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if (type.equalsIgnoreCase("LOAD")) {
                    showQuantityDialog(filteredList.get(index), type);
                } else if (type.equalsIgnoreCase("UNLOAD")) {
                    showQuantityDialog(filteredList.get(index), type);
                } else if (type.equalsIgnoreCase("DELETE")) {
                    deleteConfirmationPopUp(filteredList.get(index));
                } else if (type.equalsIgnoreCase("PHONE")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + filteredList.get(index).getVendorModel().getPhone()));
                    context.startActivity(intent);
                }
            }
        };

        stocks = new ArrayList<>();
        filteredList = new ArrayList<>();
        loadStockList();

        adapter = new StockViewAdapter(context, filteredList, listener);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        stockSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                stockPageFilter(s.toString());
            }
        });


    }

    private void showQuantityDialog(StockModel stockModel, String action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(action + " Quantity for " + stockModel.getName());

        // 🔹 Create a container layout for EditText + buttons
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 40, 30, 20);

        // 🔹 Minus Button
        Button btnMinus = new Button(context);
        btnMinus.setText("-");
        btnMinus.setTextSize(20);
        btnMinus.setPadding(20, 10, 20, 10);

        // 🔹 EditText (centered between buttons)
        final EditText input = new EditText(context);
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        etParams.setMargins(30, 0, 30, 0); // 🔹 margin around EditText
        input.setLayoutParams(etParams);
        input.setGravity(Gravity.CENTER);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(""+stockModel.getQuantity()); // default value
        input.setSelectAllOnFocus(true);

        // 🔹 Plus Button
        Button btnPlus = new Button(context);
        btnPlus.setText("+");
        btnPlus.setTextSize(20);
        btnPlus.setPadding(20, 10, 20, 10);

        // Add buttons and EditText to layout
        layout.addView(btnMinus);
        layout.addView(input);
        layout.addView(btnPlus);

        builder.setView(layout);

        // 🔹 Increment/Decrement Logic
        btnPlus.setOnClickListener(v -> {
            String currentText = input.getText().toString().trim();
            int value = currentText.isEmpty() ? 0 : Integer.parseInt(currentText);
            input.setText(String.valueOf(value + 1));
        });

        btnMinus.setOnClickListener(v -> {
            String currentText = input.getText().toString().trim();
            int value = currentText.isEmpty() ? 0 : Integer.parseInt(currentText);
            if (value > 1) input.setText(String.valueOf(value - 1));
        });

        // 🔹 OK Button
        builder.setPositiveButton("OK", (dialog, which) -> {
            String qtyStr = input.getText().toString().trim();
            if (!qtyStr.isEmpty()) {
                int qty = Integer.parseInt(qtyStr);
                if(action.equalsIgnoreCase("LOAD")) {
                    stockModel.setQuantity(stockModel.getQuantity() + qty);
                }else if(action.equalsIgnoreCase("UNLOAD")) {
                    if(stockModel.getQuantity() < qty){
                        Toast.makeText(context, "Stock Quantity is not sufficient", Toast.LENGTH_LONG).show();
                        return;
                    }
                    stockModel.setQuantity(stockModel.getQuantity() - qty);
                }
                InstaFirebaseRepository.getInstance().addDataBase(sharedPrefHelper.getAppName() + AppConstants.STOCKS_COLLECTION, stockModel.getId(), stockModel, new InstaFirebaseRepository.OnFirebaseWriteListener() {
                    @Override
                    public void onSuccess(Object orderId) {
                        Toast.makeText(context, "Stock updated", Toast.LENGTH_LONG).show();
                        loadStockList();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(context, "Please enter a quantity", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }


    private void loadStockList() {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getAllDetails(sharedPrefHelper.getAppName() + AppConstants.STOCKS_COLLECTION, "name", Query.Direction.ASCENDING, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(Object data) {
                stocks.clear();
                filteredList.clear();
                QuerySnapshot documentSnapshotList = (QuerySnapshot) data;
                for (DocumentSnapshot doc : documentSnapshotList) {
                    stocks.add(doc.toObject(StockModel.class));
                }
                filteredList.addAll(stocks);
                adapter.notifyDataSetChanged();
                if(filteredList.isEmpty()){
                    emptyLayout.setVisibility(View.VISIBLE);
                }else {
                    emptyLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void stockPageFilter(String constraint) {
        filteredList.clear();
        if (constraint == null || constraint.isEmpty()) {
            filteredList.addAll(stocks);
        } else {
            String filterPattern = constraint.toString().toLowerCase().trim();
            for (StockModel item : stocks) {
                if (item.getName().toLowerCase().startsWith(filterPattern)) {
                    filteredList.add(item);
                }
            }
        }
        if(filteredList.isEmpty()){
            emptyLayout.setVisibility(View.VISIBLE);
        }else {
            emptyLayout.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    private void deleteConfirmationPopUp(StockModel stockModel) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to Delete the Stock [" + stockModel.getName() + "]");
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            try {
                Toast.makeText(context, "Refreshing.!", Toast.LENGTH_LONG).show();
                deleteItem(stockModel.getId());
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

    private void deleteItem(String id) {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().deleteData(sharedPrefHelper.getAppName() + AppConstants.STOCKS_COLLECTION, id, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                Toast.makeText(context, "Stock Removed", Toast.LENGTH_LONG).show();
                loadStockList();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createNewStockItem() {

        Dialog dialog = new Dialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_stock_create, null);
        dialog.setContentView(sheetView);
        dialog.setCanceledOnTouchOutside(false);
        final VendorModel[] selectedVendor = {null};

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


        EditText name = sheetView.findViewById(R.id.stock_create_name);
        EditText qty = sheetView.findViewById(R.id.stock_create_qty);
        Button submit = sheetView.findViewById(R.id.stock_create_submit);
        TextView close = sheetView.findViewById(R.id.stock_create_close);

        Spinner scaleSpinner = sheetView.findViewById(R.id.stock_create_qty_scale);
        ArrayAdapter<CharSequence> stockAdapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_qty_scale, android.R.layout.simple_spinner_item);

        stockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scaleSpinner.setAdapter(stockAdapter);

        Spinner vendorSpinner = sheetView.findViewById(R.id.stock_create_vendor);


        ArrayAdapter<VendorModel> vendorAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                vendorList);

        vendorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vendorSpinner.setAdapter(vendorAdapter);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SingleTon.hideKeyboard(context,activity);
                dialog.dismiss();
            }
        });

        vendorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedVendor[0] = vendorList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nameValue = name.getText().toString().toUpperCase();
                String qtyValue = qty.getText().toString();
                String scale = scaleSpinner.getSelectedItem().toString();

                if (nameValue.isEmpty()) {
                    name.setError("Enter Product Name");
                    return;
                }
                if (qtyValue.isEmpty()) {
                    qty.setError("Enter Product Qty");
                    return;
                }

                StockModel stockModel = new StockModel();
                stockModel.setId(SingleTon.generateStockDocument());
                stockModel.setName(nameValue);
                stockModel.setUnit(scale);
                stockModel.setQuantity(Integer.parseInt(qtyValue));
                stockModel.setVendorModel(selectedVendor[0]);

                InstaFirebaseRepository.getInstance().addDataBase(sharedPrefHelper.getAppName() + AppConstants.STOCKS_COLLECTION, stockModel.getId(), stockModel, new InstaFirebaseRepository.OnFirebaseWriteListener() {
                    @Override
                    public void onSuccess(Object orderId) {
                        dialog.dismiss();
                        Toast.makeText(context, "New Stock Added", Toast.LENGTH_LONG).show();
                        loadStockList();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        dialog.dismiss();
                        Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        dialog.show();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.stock_back) {
            finish();
        } else if (view.getId() == R.id.stock_download) {
            downloadStockList();
        }
    }

    private void downloadStockList() {
        try {
            saveExcelFile(stocks);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Report Generation failed ..!", Toast.LENGTH_LONG).show();
        }
    }

    private void saveExcelFile(List<StockModel> stocks) throws Exception {
        String fileName = "stocks-" + System.currentTimeMillis() + ".xlsx";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        ReportGenerator reportGenerator = new ReportGenerator();
        reportGenerator.createStockExcelReport(stocks, file);

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

    private void loadVendorList() {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getAllDetails(sharedPrefHelper.getAppName() + AppConstants.VENDOR_COLLECTION, "name", Query.Direction.ASCENDING, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(Object data) {
                vendorList.clear();
                QuerySnapshot documentSnapshotList = (QuerySnapshot) data;
                for (DocumentSnapshot doc : documentSnapshotList) {
                    vendorList.add(doc.toObject(VendorModel.class));
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });
    }
}