package com.app.billing.instabillz.activity;

import static com.app.billing.instabillz.utils.SingleTon.getStartOfTodayEpoch;
import static com.app.billing.instabillz.utils.SingleTon.isSameDay;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.BillingViewAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.EmployeeModel;
import com.app.billing.instabillz.model.InvoiceModel;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        TextView textView = (TextView) findViewById(R.id.home_nav_text_view);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }

            }
        });


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
                saveNewBill(newInvoice,false);
                dialog.dismiss();
            });

            btnCheckoutPrint.setOnClickListener(b -> {
                newInvoice.setPaymentMode(paymentMode[0]);
                newInvoice.setUpiPaymentStatus("SUCCESS");
                newInvoice.setParcelCost(parcelAmountValue[0]);
                newInvoice.setTotalCost(total[0]);
                newInvoice.setSellingCost(total[0] + parcelAmountValue[0]);
                newInvoice.setProductModelList(billItems);
                SingleTon.hideKeyboard(context, activity);
                saveNewBill(newInvoice,true);
                dialog.dismiss();
            });

            dialog.show();
        } else {
            Toast.makeText(this, "No items selected!", Toast.LENGTH_SHORT).show();
        }


    }

    private void saveNewBill(InvoiceModel newInvoice,boolean isPrintNeeded) {
        Toast.makeText(this, "Loading!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getLatestToken(new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                QuerySnapshot doc = (QuerySnapshot) data;

                if(!doc.isEmpty() && doc.getDocuments().get(0).exists()){
                    InvoiceModel model = doc.getDocuments().get(0).toObject(InvoiceModel.class);
                    if (model != null && isSameDay(model.getBillingDate(), getStartOfTodayEpoch())) {
                        newInvoice.setToken(model.getToken() + 1);
                    }else{
                        newInvoice.setToken(1);
                    }
                }else{
                    newInvoice.setToken(1);
                }
                InstaFirebaseRepository.getInstance().addDataBase(AppConstants.APP_NAME + AppConstants.SALES_COLLECTION, String.valueOf(newInvoice.getBillingDate()), newInvoice, new InstaFirebaseRepository.OnFirebaseWriteListener() {
                    @Override
                    public void onSuccess(Object data) {
                        Toast.makeText(context, "🧾 New bill generated — Token #" + newInvoice.getToken(), Toast.LENGTH_LONG).show();

                        if(isPrintNeeded){
                            generateHardCopyBill(newInvoice);
                        }
                        loadProductList();
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