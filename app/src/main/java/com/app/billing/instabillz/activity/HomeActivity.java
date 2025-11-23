package com.app.billing.instabillz.activity;

import static com.app.billing.instabillz.utils.SingleTon.getStartOfTodayEpoch;
import static com.app.billing.instabillz.utils.SingleTon.hideKeyboard;
import static com.app.billing.instabillz.utils.SingleTon.isSameDay;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Size;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.BillingViewAdapter;
import com.app.billing.instabillz.adapter.ProductDropDownAdapter;
import com.app.billing.instabillz.adapter.ScanCartAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.listener.ApiService;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.CategoryModel;
import com.app.billing.instabillz.model.InvoiceModel;
import com.app.billing.instabillz.model.ProductApiResponse;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.model.ShopsModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.ApiClient;
import com.app.billing.instabillz.utils.BluetoothPrinterHelper;
import com.app.billing.instabillz.utils.SharedPrefHelper;
import com.app.billing.instabillz.utils.SingleTon;
import com.google.android.material.navigation.NavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    Context context;
    Activity activity;

    DrawerLayout mDrawerLayout;
    NavigationView navigationView;

    LinearLayout layoutCategories;

    EditText productSearch;
    List<ProductModel> filteredList;
    List<ProductModel> products;
    BillingViewAdapter gridLayoutAdapter;

    Button submit, preview;

    String selectedCategory = "", searchString = "";
    List<String> categoryModelList = new ArrayList<>();

    LinearLayout gridLayout, scanLayout, emptyLayout,contentLayout;

    List<ProductModel> scanCartItems = new ArrayList<>();
    BillingClickListener scanCartListener;
    ScanCartAdapter scanLayoutAdapter;
    RecyclerView scanCartRecyclerView;
    AutoCompleteTextView autoSearch;
    ProductDropDownAdapter dropDownAdapter;

    PreviewView previewScanView;
    private boolean isProcessingScan = false;
    private Executor executor = Executors.newSingleThreadExecutor();
    private ProcessCameraProvider cameraProvider;
    private boolean isCameraRunning = false;
    private Button btnToggleCamera;


    SharedPrefHelper sharedPrefHelper;

    BluetoothAdapter bluetoothAdapter;
    public BluetoothPrinterHelper bluetoothPrinterHelper;
    ShopsModel shopsModel = new ShopsModel();

    private static final int REQUEST_WRITE_PERMISSION = 786;
    private static final int REQUEST_ENABLE_BT = 10;
    private static final int PERMISSION_BLUETOOTH = 1;
    private static final int PERMISSION_BLUETOOTH_ADMIN = 2;
    private static final int PERMISSION_BLUETOOTH_CONNECT = 3;
    private static final int PERMISSION_BLUETOOTH_SCAN = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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
        context = HomeActivity.this;
        activity = HomeActivity.this;
        sharedPrefHelper = new SharedPrefHelper(context);
        bluetoothPrinterHelper = new BluetoothPrinterHelper(context, activity);
        shopsModel = sharedPrefHelper.getPrinterDetails();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        MenuItem adminGroup = menu.findItem(R.id.admin_group);
        String loginPhone = sharedPrefHelper.getSystemUserPhone();
        if (loginPhone.equals("9585905176")) {
            adminGroup.setVisible(true);   // show admin menu
        } else {
            adminGroup.setVisible(false);  // hide admin menu
        }


        TextView textView = (TextView) findViewById(R.id.home_nav_text_view);
        TextView attendance = (TextView) findViewById(R.id.home_attendance_tv);
        TextView todayInvoice = (TextView) findViewById(R.id.home_today_invoice_tv);

        attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AttendanceActivity.class);
                intent.putExtra("employee_name", sharedPrefHelper.getSystemUserName());
                startActivity(intent);
            }
        });

        todayInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, InvoiceActivity.class);
                intent.putExtra("employee_name", sharedPrefHelper.getSystemUserName());
                startActivity(intent);

            }
        });

        String userName = sharedPrefHelper.getSystemUserName();
        if ("ADMIN".equalsIgnoreCase(sharedPrefHelper.getSystemUserRole())) {
            // Show full name and enable drawer toggle
            //textView.setText("Menu");
            textView.setOnClickListener(v -> {
                if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
            });

            // Optional styling for admin
//            textView.setBackground(null);
//            textView.setTextColor(Color.parseColor("#000000"));
            textView.setClickable(true);

        } else {
            // Disable click
            //textView.setOnClickListener(null);
            textView.setClickable(false);

            // Get first character of name
            String firstChar = userName != null && !userName.isEmpty()
                    ? userName.substring(0, 1).toUpperCase()
                    : "?";

            textView.setText(firstChar);
            textView.setTextColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);

            // Create circular background like GPay avatar
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(Color.parseColor("#1976D2")); // Blue shade, change if needed
            textView.setBackground(circle);

            // Set size for avatar style
            int sizeInDp = 40;
            float scale = textView.getResources().getDisplayMetrics().density;
            int sizeInPx = (int) (sizeInDp * scale + 0.5f);
            textView.setWidth(sizeInPx);
            textView.setHeight(sizeInPx);
            textView.setTextSize(18);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }

        gridLayout = findViewById(R.id.home_bill_grid_layout);
        scanLayout = findViewById(R.id.home_bill_scan_layout);
        emptyLayout = findViewById(R.id.home_bill_scan_empty_view);
        contentLayout = findViewById(R.id.home_bill_scan_content_view);

        //GRID Layout Declarations / Functionalities
        layoutCategories = findViewById(R.id.home_layout_categories);
        productSearch = (EditText) findViewById(R.id.home_etSearch);
        submit = (Button) findViewById(R.id.home_submit_invoice);
        preview = (Button) findViewById(R.id.home_preview);
        submit.setOnClickListener(this);
        preview.setOnClickListener(this);
        RecyclerView gridRecyclerView = findViewById(R.id.home_bill_grid_recyclerView);
        gridRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        products = new ArrayList<>();
        filteredList = new ArrayList<>();
        loadProductList();
        loadCategoryList();
        gridLayoutAdapter = new BillingViewAdapter(context, filteredList, new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if ("ADD".equalsIgnoreCase(type)) {
                    //No Use Here
                } else if ("REMOVE".equalsIgnoreCase(type)) {
                    //No Use Here
                }
            }
        });
        gridRecyclerView.setAdapter(gridLayoutAdapter);
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
                homePageFilter(searchString);
            }
        });

        //Scan Layout Functionalities
        autoSearch = (AutoCompleteTextView) findViewById(R.id.home_bill_scan_autoCompleteSearch);
        autoSearch.setThreshold(1); // show results after 1 character typed
        autoSearch.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        autoSearch.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);



        // When user selects a product
        autoSearch.setOnItemClickListener((parent, view, position, id) -> {
            ProductModel selected = (ProductModel) parent.getItemAtPosition(position);
            hideKeyboard(context,activity);
            showQtyDialog(selected);
        });

        scanCartListener = new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if ("DELETE".equalsIgnoreCase(type)) {
                    showDeleteConfirmDialog(index);
                }
            }
        };
        btnToggleCamera = findViewById(R.id.btnToggleCamera);

        btnToggleCamera.setOnClickListener(v -> {
            if (isCameraRunning) {
                stopCamera();
            } else {
                askCameraPermission(); // this will call startCamera()
            }
        });

        previewScanView = (PreviewView) findViewById(R.id.home_bill_scan_preview_view);
        scanCartRecyclerView = (RecyclerView) findViewById(R.id.home_bill_scan_recyclerview);
        scanLayoutAdapter = new ScanCartAdapter(context, scanCartItems, scanCartListener);
        scanCartRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        scanCartRecyclerView.setAdapter(scanLayoutAdapter);

        if ("Quick Billing".equalsIgnoreCase(shopsModel.getBillingType())) {
            gridLayout.setVisibility(View.VISIBLE);
            scanLayout.setVisibility(View.GONE);
        } else if ("Product QR Billing".equalsIgnoreCase(shopsModel.getBillingType())) {
            gridLayout.setVisibility(View.GONE);
            scanLayout.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.VISIBLE);
            //askCameraPermission();
        }

        enableBluetooth();
    }

    private void showDeleteConfirmDialog(int index) {

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_confirmation);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Zomato bottom slide animation
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;

        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        TextView tvSubMessage = dialog.findViewById(R.id.tvSubMessage);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        tvMessage.setText("Remove item?");
        tvSubMessage.setText("This action cannot be undone.");

        btnDelete.setOnClickListener(v -> {
            scanCartItems.remove(index);
            scanLayoutAdapter.notifyDataSetChanged();
            dialog.dismiss();

            if (scanCartItems.isEmpty()) {
                emptyLayout.setVisibility(View.VISIBLE);
                contentLayout.setVisibility(View.GONE);
            } else {
                emptyLayout.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);
                scanCartRecyclerView.smoothScrollToPosition(scanCartItems.size() - 1);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private void showQtyDialog(ProductModel product) {

        Dialog dialog = new Dialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.cart_dialog_qty, null);
        dialog.setContentView(sheetView);
        dialog.setCanceledOnTouchOutside(false);

        // Transparent background for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        }

        TextView tvName = dialog.findViewById(R.id.tvProductName);
        TextView btnMinus = dialog.findViewById(R.id.btnMinus);
        TextView btnPlus = dialog.findViewById(R.id.btnPlus);
        EditText etQty = dialog.findViewById(R.id.etQty);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

        tvName.setText("Enter " + product.getName() + " Quantity");

        // Minus button
        btnMinus.setOnClickListener(v -> {
            int qty = Integer.parseInt(etQty.getText().toString());
            if (qty > 1) {
                etQty.setText(String.valueOf(qty - 1));
            }
        });

        // Plus button
        btnPlus.setOnClickListener(v -> {
            int qty = Integer.parseInt(etQty.getText().toString());
            etQty.setText(String.valueOf(qty + 1));
        });

        btnSubmit.setOnClickListener(v -> {
            String qtyStr = etQty.getText().toString().trim();
            if (!qtyStr.isEmpty() && Integer.parseInt(qtyStr) > 0) {
                syncScanProducts(product, Integer.parseInt(qtyStr));
                dialog.dismiss();
            } else {
                Toast.makeText(activity, "Enter valid quantity", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }



    private void syncScanProducts(ProductModel selected,int qty) {
        boolean found = false;

        for (ProductModel item : scanCartItems) {
            if (item.getName().equals(selected.getName())) {   // or use ID if available
                item.setQty(item.getQty() + qty);  // Increase quantity
                found = true;
                break;
            }
        }

        if (!found) {
            selected.setQty(qty);   // default qty for new item
            scanCartItems.add(selected);
        }

        scanLayoutAdapter.notifyDataSetChanged();
        scanCartRecyclerView.smoothScrollToPosition(scanCartItems.size() - 1);
        if (scanCartItems.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
        }else{
            emptyLayout.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
        }
        autoSearch.setText(""); // clear search
    }

    private void enableBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bluetoothAdapter.isEnabled()) {
            //Toast.makeText(this, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show();
            return;
        }
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, PERMISSION_BLUETOOTH);
                return;
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, PERMISSION_BLUETOOTH_ADMIN);
                return;
            } else {
                // Your Bluetooth logic here
            }
        } else {
            // For Android 12 (S) and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_BLUETOOTH_CONNECT);
                return;
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, PERMISSION_BLUETOOTH_SCAN);
                return;
            } else {
                // Your Bluetooth logic here
            }
        }

        // Permission granted, request to enable Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableBluetooth(); // Retry enabling Bluetooth
            } else {
                Toast.makeText(this, "Bluetooth permission is required", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }


    private void homePageFilter(CharSequence constraint) {
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

        gridLayoutAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent i = null;
        mDrawerLayout.closeDrawer(GravityCompat.START);
        if (item.getItemId() == R.id.nav_products) {
            i = new Intent(HomeActivity.this, ProductActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_category) {
            i = new Intent(HomeActivity.this, CategoryActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_stocks) {
            i = new Intent(HomeActivity.this, StockActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_vendors) {
            i = new Intent(HomeActivity.this, VendorActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_expense) {
            i = new Intent(HomeActivity.this, ExpenseActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_employee) {
            i = new Intent(HomeActivity.this, EmployeeActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_attendance) {
            i = new Intent(HomeActivity.this, AttendanceActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_employee_report) {
            i = new Intent(HomeActivity.this, EmployeeReportActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_invoice) {
            i = new Intent(HomeActivity.this, InvoiceActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_report) {
            i = new Intent(HomeActivity.this, ReportActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_consolidate_report) {
            i = new Intent(HomeActivity.this, ConsolidateReportActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_onboarding) {
            i = new Intent(HomeActivity.this, OnboardingActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_shops) {
            i = new Intent(HomeActivity.this, ShopManagementActivity.class);
            startActivity(i);
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        if (R.id.home_submit_invoice == view.getId()) {
            submitCartItems();
        } else if (R.id.home_preview == view.getId()) {
            showCartPreview();
        }
    }

    private void submitCartItems() {

        StringBuilder sb = new StringBuilder();
        final double[] total = {0};
        List<ProductModel> billItems = new ArrayList<>();
        if ("Quick Billing".equalsIgnoreCase(shopsModel.getBillingType())) {
            for (ProductModel product : products) {
                if (product.getQty() > 0) {
                    billItems.add(product);
                    sb.append(product.getName())
                            .append(" x ")
                            .append(product.getQty())
                            .append(" = ₹")
                            .append(product.getQty() * product.getPrice())
                            .append("\n");
                    total[0] += product.getQty() * product.getPrice();
                }
            }
        } else if ("Product QR Billing".equalsIgnoreCase(shopsModel.getBillingType())) {
            for (ProductModel product : scanCartItems) {
                if (product.getQty() > 0) {
                    billItems.add(product);
                    sb.append(product.getName())
                            .append(" x ")
                            .append(product.getQty())
                            .append(" = ₹")
                            .append(product.getQty() * product.getPrice())
                            .append("\n");
                    total[0] += product.getQty() * product.getPrice();
                }
            }
        }

        if (sb.length() > 0) {
            Dialog dialog = new Dialog(this);
            View sheetView = getLayoutInflater().inflate(R.layout.home_cart_submit, null);
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

            ImageView close = sheetView.findViewById(R.id.cart_close);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SingleTon.hideKeyboard(context, activity);
                    dialog.dismiss();
                }
            });

            TextView cartItems = sheetView.findViewById(R.id.cartItems);
            TextView cartTotal = sheetView.findViewById(R.id.cartTotal);
            Button btnCheckoutSave = sheetView.findViewById(R.id.btnCheckoutSave);
            Button btnCheckoutPrint = sheetView.findViewById(R.id.btnCheckoutPrint);
            EditText parcelAmount = sheetView.findViewById(R.id.bill_parcel_amount);

            RadioGroup paymentGroup = (RadioGroup) sheetView.findViewById(R.id.new_bill_payment_radio_group);
            RadioButton cashRadioBt = (RadioButton) sheetView.findViewById(R.id.new_billing_payment_cash);
            RadioButton upiRadioBt = (RadioButton) sheetView.findViewById(R.id.new_billing_payment_upi);

            cashRadioBt.setChecked(true);
            String[] paymentMode = {"CASH"};
            paymentGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    // Find which radio button is selected
                    if (R.id.new_billing_payment_cash == checkedId) {
                        paymentMode[0] = "CASH";
                    } else if (R.id.new_billing_payment_upi == checkedId) {
                        paymentMode[0] = "UPI";
                    }
                }
            });

            cartItems.setText(sb.toString());
            cartTotal.setText("Total: ₹" + total[0]);
            Double[] parcelAmountValue = {0.0};

            parcelAmount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.toString().isEmpty()) {
                        parcelAmountValue[0] = 0.0;
                    } else {
                        parcelAmountValue[0] = Double.parseDouble(s.toString());
                    }
                    cartTotal.setText("Total: ₹" + (total[0] + parcelAmountValue[0]));
                }
            });
            InvoiceModel newInvoice = new InvoiceModel();
            // IST ZoneOffset is +5:30
            ZoneOffset istOffset = ZoneOffset.ofHoursMinutes(5, 30);
            newInvoice.setBillingDate(OffsetDateTime.now(istOffset).toEpochSecond());
            newInvoice.setEmployeeName(sharedPrefHelper.getSystemUserName());
            newInvoice.setEmployeePhone(sharedPrefHelper.getSystemUserPhone());
            newInvoice.setPrint(false);


            btnCheckoutSave.setOnClickListener(b -> {
                newInvoice.setPaymentMode(paymentMode[0]);
                newInvoice.setUpiPaymentStatus("SUCCESS");
                newInvoice.setParcelCost(parcelAmountValue[0]);
                newInvoice.setTotalCost(total[0]);
                newInvoice.setSellingCost(total[0] + parcelAmountValue[0]);
                newInvoice.setProductModelList(billItems);
                SingleTon.hideKeyboard(context, activity);
                saveNewBill(newInvoice, false);
                dialog.dismiss();
            });

            btnCheckoutPrint.setOnClickListener(b -> {
                if (shopsModel == null) {
                    Toast.makeText(this, "Printer data not loaded", Toast.LENGTH_SHORT).show();
                    return;
                }
                newInvoice.setPaymentMode(paymentMode[0]);
                newInvoice.setUpiPaymentStatus("SUCCESS");
                newInvoice.setParcelCost(parcelAmountValue[0]);
                newInvoice.setTotalCost(total[0]);
                newInvoice.setSellingCost(total[0] + parcelAmountValue[0]);
                newInvoice.setProductModelList(billItems);
                SingleTon.hideKeyboard(context, activity);
                saveNewBill(newInvoice, true);
                dialog.dismiss();
            });

            dialog.show();
        } else {
            Toast.makeText(this, "No items selected!", Toast.LENGTH_SHORT).show();
        }


    }

    private void saveNewBill(InvoiceModel newInvoice, boolean isPrintNeeded) {
        Toast.makeText(this, "Loading!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getLatestToken(new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                QuerySnapshot doc = (QuerySnapshot) data;

                if (!doc.isEmpty() && doc.getDocuments().get(0).exists()) {
                    InvoiceModel model = doc.getDocuments().get(0).toObject(InvoiceModel.class);
                    if (model != null && isSameDay(model.getBillingDate(), getStartOfTodayEpoch())) {
                        newInvoice.setToken(model.getToken() + 1);
                    } else {
                        newInvoice.setToken(1);
                    }
                } else {
                    newInvoice.setToken(1);
                }
                InstaFirebaseRepository.getInstance().addDataBase(AppConstants.APP_NAME + AppConstants.SALES_COLLECTION, String.valueOf(newInvoice.getBillingDate()), newInvoice, new InstaFirebaseRepository.OnFirebaseWriteListener() {
                    @Override
                    public void onSuccess(Object data) {
                        Toast.makeText(context, "🧾 New bill generated — Token #" + newInvoice.getToken(), Toast.LENGTH_LONG).show();

                        if (isPrintNeeded) {
                            generateHardCopyBill(newInvoice);
                        } else {
                            loadProductList();
                        }

                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void generateHardCopyBill(InvoiceModel newInvoice) {

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(context, "Please turn ON Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, PERMISSION_BLUETOOTH);
                    return;
                } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, PERMISSION_BLUETOOTH_ADMIN);
                    return;
                } else {
                    // Your Bluetooth logic here
                }
            } else {
                // For Android 12 (S) and above
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_BLUETOOTH_CONNECT);
                    return;
                } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, PERMISSION_BLUETOOTH_SCAN);
                    return;
                } else {
                    // Your Bluetooth logic here
                }
            }
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (!pairedDevices.isEmpty()) {
                boolean state = false;
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().contains(shopsModel.getPrinterName()) && !state) {
                        printConfirmationPopup(newInvoice, device.getName());
                        state = true;
                    }
                }
                if (!state) {
                    Toast.makeText(context, "Printer not connected properly.!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "No paired devices found", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void printConfirmationPopup(InvoiceModel billData, String printer) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation (" + printer + ")");
        builder.setMessage("Do you want to print the bill?");
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();

            Toast.makeText(context, "Printing.!", Toast.LENGTH_LONG).show();
            try {
                bluetoothPrinterHelper.printSmallFontReceipt(billData, shopsModel);
                loadProductList();

            } catch (Exception e) {
                Toast.makeText(context, "Printer not available. Please restart the printer.!", Toast.LENGTH_LONG).show();

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

    private void showCartPreview() {

        StringBuilder sb = new StringBuilder();
        double total = 0;

        if ("Quick Billing".equalsIgnoreCase(shopsModel.getBillingType())) {
            for (ProductModel product : products) {
                if (product.getQty() > 0) {
                    sb.append(product.getName())
                            .append(" x ")
                            .append(product.getQty())
                            .append(" = ₹")
                            .append(product.getQty() * product.getPrice())
                            .append("\n");
                    total += product.getQty() * product.getPrice();
                }
            }
        } else if ("Product QR Billing".equalsIgnoreCase(shopsModel.getBillingType())) {
            for (ProductModel product : scanCartItems) {
                if (product.getQty() > 0) {
                    sb.append(product.getName())
                            .append(" x ")
                            .append(product.getQty())
                            .append(" = ₹")
                            .append(product.getQty() * product.getPrice())
                            .append("\n");
                    total += product.getQty() * product.getPrice();
                }
            }
        }

        if (sb.length() > 0) {
            Dialog dialog = new Dialog(this);
            View sheetView = getLayoutInflater().inflate(R.layout.home_cart_preview, null);
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

            TextView cartItems = sheetView.findViewById(R.id.cartItems);
            TextView cartTotal = sheetView.findViewById(R.id.cartTotal);
            Button btnCheckoutSave = sheetView.findViewById(R.id.btnCheckoutSave);
            Button btnCheckoutPrint = sheetView.findViewById(R.id.btnCheckoutPrint);
            EditText parcelAmount = sheetView.findViewById(R.id.bill_parcel_amount);

            cartItems.setText(sb.toString());
            cartTotal.setText("Total: ₹" + total);

            btnCheckoutSave.setText("Close");
            btnCheckoutPrint.setVisibility(View.GONE);
            parcelAmount.setVisibility(View.GONE);
            btnCheckoutSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SingleTon.hideKeyboard(context, activity);
                    dialog.dismiss();
                }
            });

            dialog.show();
        } else {
            Toast.makeText(this, "No items selected!", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProductList();
    }

    private void loadProductList() {
        //Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getAllDetails(AppConstants.APP_NAME + AppConstants.PRODUCTS_COLLECTION, "name", Query.Direction.ASCENDING, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                products.clear();
                filteredList.clear();
                scanCartItems.clear();
                QuerySnapshot documentSnapshotList = (QuerySnapshot) data;
                for (DocumentSnapshot doc : documentSnapshotList) {
                    products.add(doc.toObject(ProductModel.class));
                }
                filteredList.addAll(products);
                gridLayoutAdapter.notifyDataSetChanged();
                if (scanCartItems.isEmpty()) {
                    emptyLayout.setVisibility(View.VISIBLE);
                    contentLayout.setVisibility(View.GONE);
                }else{
                    emptyLayout.setVisibility(View.GONE);
                    contentLayout.setVisibility(View.VISIBLE);
                }
                scanLayoutAdapter.notifyDataSetChanged();
                dropDownAdapter = new ProductDropDownAdapter(context, products);
                autoSearch.setAdapter(dropDownAdapter);
                dropDownAdapter.notifyDataSetChanged();
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
        InstaFirebaseRepository.getInstance().getAllDetails(AppConstants.APP_NAME + AppConstants.CATEGORIES_COLLECTION, "name", Query.Direction.ASCENDING, new InstaFirebaseRepository.OnFirebaseWriteListener() {
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
                    tv.setTextSize(14);
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
                        homePageFilter(searchString);
                    });

                    layoutCategories.addView(tv);
                    if (category.equalsIgnoreCase("All")) {
                        highlightSelectedCategory(tv);
                        selectedCategory = tv.getText().toString();
                        homePageFilter(searchString);
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

    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    private void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            isCameraRunning = false;
            btnToggleCamera.setText("Start Camera");
        }
    }


    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();  // save provider globally
                bindCamera(cameraProvider);
                isCameraRunning = true;
                btnToggleCamera.setText("Stop Camera");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCamera(ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewScanView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(executor, imageProxy -> {
            processImage(imageProxy);
        });

        CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;

        cameraProvider.bindToLifecycle(this, selector, preview, imageAnalysis);
    }

    @OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    private void processImage(ImageProxy imageProxy) {
        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        BarcodeScanner scanner = BarcodeScanning.getClient();

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (isProcessingScan) return;

                    for (Barcode barcode : barcodes) {
                        String code = barcode.getRawValue();
                        if (code != null) {
                            isProcessingScan = true;
                            // ⬇️ ADD THIS LINE
                            playBeepAndVibrate();
                            runOnUiThread(() -> showProductDetails(code));
                        }
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void showProductDetails(String barcode) {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getDetailsByDocumentId(AppConstants.APP_NAME + AppConstants.PRODUCTS_COLLECTION, barcode, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                DocumentSnapshot doc = (DocumentSnapshot) data;
                if (doc.exists()) {
                    ProductModel scannedProduct = doc.toObject(ProductModel.class);
                    if (scannedProduct != null) {
                        showQtyDialog(scannedProduct);
                        unlockScannerAfterDelay();
                    } else {
                        Toast.makeText(context, "Product not found in inventory. Add details to continue.", Toast.LENGTH_SHORT).show();
                        fetchFromAPI(barcode);
                    }
                }else{
                    Toast.makeText(context, "Product not found in inventory. Add details to continue.", Toast.LENGTH_SHORT).show();
                    fetchFromAPI(barcode);
                }
            }
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
                unlockScannerAfterDelay();
            }
        });

    }

    private void playBeepAndVibrate() {

        // Vibrate
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(150);
            }
        }

        // Beep sound
        ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
    }


    private void fetchFromAPI(String barcode) {

        ApiService service = ApiClient.getClient().create(ApiService.class);

        service.getProduct(barcode).enqueue(new Callback<ProductApiResponse>() {
            @Override
            public void onResponse(Call<ProductApiResponse> call, Response<ProductApiResponse> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    showAddProductDialog(barcode, null, null);
                    return;
                }

                ProductApiResponse data = response.body();

                if (data.status == 1) {
                    String name = data.product.product_name;
                    String category = data.product.categories;

                    showAddProductDialog(barcode, name, category);
                } else {
                    showAddProductDialog(barcode, null, null);
                }
            }

            @Override
            public void onFailure(Call<ProductApiResponse> call, Throwable t) {
                showAddProductDialog(barcode, null, null);
            }
        });
    }

    private void showAddProductDialog(String barcode, String name, String category)  {
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
        delete.setVisibility(View.GONE);
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
                unlockScannerAfterDelay();
            }
        });

        // Prefill from API if available
        if (name != null) productName.setText(name);
        if (category != null && !category.isEmpty()) {
            int indexToSelect = categoryModelList.indexOf(category);

            if (indexToSelect >= 0) {
                categorySpinner.setSelection(indexToSelect);
            }
        }

        submit.setOnClickListener(b -> {
            ProductModel newProductModel = new ProductModel();
            newProductModel.setId(barcode);
            newProductModel.setName(productName.getText().toString().toUpperCase());
            newProductModel.setPrice(Double.parseDouble(productPrice.getText().toString()));
            newProductModel.setCategoryName(categorySpinner.getSelectedItem().toString());

            InstaFirebaseRepository.getInstance().addDataBase(AppConstants.APP_NAME + AppConstants.PRODUCTS_COLLECTION, newProductModel.getId(), newProductModel, new InstaFirebaseRepository.OnFirebaseWriteListener() {
                @Override
                public void onSuccess(Object orderId) {
                    Toast.makeText(context, "Products Updated", Toast.LENGTH_LONG).show();
                    products.add(newProductModel);
                    showQtyDialog(newProductModel);
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
                }
            });
            unlockScannerAfterDelay();
            dialog.dismiss();
        });

        dialog.show();

    }

    private void unlockScannerAfterDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isProcessingScan = false;   // 🔥 unlock scanner
        }, 1500);
    }
}