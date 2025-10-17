package com.app.billing.instabillz.activity;

import static com.app.billing.instabillz.utils.SingleTon.getStartOfTodayEpoch;
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
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.BillingViewAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.AttendanceModel;
import com.app.billing.instabillz.model.EmployeeModel;
import com.app.billing.instabillz.model.InvoiceModel;
import com.app.billing.instabillz.model.PrinterDataModel;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.BluetoothPrinterHelper;
import com.app.billing.instabillz.utils.SharedPrefHelper;
import com.app.billing.instabillz.utils.SingleTon;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    Context context;
    Activity activity;

    DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    BillingClickListener listener;

    EditText productSearch;
    List<ProductModel> filteredList;
    List<ProductModel> products;
    BillingViewAdapter adapter;

    Button submit, preview;

    SharedPrefHelper sharedPrefHelper;

    BluetoothAdapter bluetoothAdapter;
    public BluetoothPrinterHelper bluetoothPrinterHelper;
    PrinterDataModel printerDataModel = new PrinterDataModel();

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
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        context = HomeActivity.this;
        activity = HomeActivity.this;
        sharedPrefHelper = new SharedPrefHelper(context);
        bluetoothPrinterHelper = new BluetoothPrinterHelper(context, activity);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        TextView textView = (TextView) findViewById(R.id.home_nav_text_view);
        TextView attendance = (TextView) findViewById(R.id.home_attendance_tv);
        TextView todayInvoice = (TextView) findViewById(R.id.home_today_invoice_tv);

        loadPrinterData();

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


        listener = new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if ("ADD".equalsIgnoreCase(type)) {

                } else if ("REMOVE".equalsIgnoreCase(type)) {

                }
            }
        };
        RecyclerView recyclerView = findViewById(R.id.home_recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        products = new ArrayList<>();
        filteredList = new ArrayList<>();
        loadProductList();
        adapter = new BillingViewAdapter(context, filteredList, listener);
        recyclerView.setAdapter(adapter);

        productSearch = (EditText) findViewById(R.id.home_etSearch);
        submit = (Button) findViewById(R.id.home_submit_invoice);
        preview = (Button) findViewById(R.id.home_preview);
        submit.setOnClickListener(this);
        preview.setOnClickListener(this);
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
                homePageFilter(s.toString());
            }
        });
        enableBluetooth();
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
        adapter.notifyDataSetChanged();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent i = null;
        mDrawerLayout.closeDrawer(GravityCompat.START);
        if (item.getItemId() == R.id.nav_products) {
            i = new Intent(HomeActivity.this, ProductActivity.class);
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
                        return;
                    }
                    parcelAmountValue[0] = Double.parseDouble(s.toString());
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
                if (printerDataModel == null) {
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
                    if (device.getName().contains(printerDataModel.getPrinterName()) && !state) {
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
                bluetoothPrinterHelper.printSmallFontReceipt(billData,printerDataModel);
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

    private void loadPrinterData() {
        InstaFirebaseRepository.getInstance().getDetailsByDocumentId(AppConstants.SHOP_COLLECTION, AppConstants.APP_NAME, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                DocumentSnapshot doc = (DocumentSnapshot) data;
                if (doc.exists()) {
                    printerDataModel = doc.toObject(PrinterDataModel.class);
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