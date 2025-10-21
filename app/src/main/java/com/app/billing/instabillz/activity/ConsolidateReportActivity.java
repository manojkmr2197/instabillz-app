package com.app.billing.instabillz.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.utils.SingleTon;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConsolidateReportActivity extends AppCompatActivity {
    private Spinner spinnerDateRange;
    private PieChart piePaymentMode;
    private TextView tvUpiAmount, tvCashAmount;

    private Map<String, View> cardViews = new HashMap<>();

    TextView back;
    private AlertDialog loaderDialog;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    Context context;
    Activity activity;

    final long[] productCount = {0};
    final long[] stockCount = {0};
    final long[] nearZeroStockCount = {0};
    final double[] totalExpense = {0};
    final long[] attendanceCount = {0};
    final long[] invoiceCount = {0};
    final double[] totalSales = {0};
    final double[] totalUpi = {0};
    final double[] totalCash = {0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consolidate_report);
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

        context = ConsolidateReportActivity.this;
        activity = ConsolidateReportActivity.this;

        back = findViewById(R.id.consolidate_report_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        spinnerDateRange = findViewById(R.id.spinnerDateRange);
        piePaymentMode = findViewById(R.id.piePaymentMode);
        tvUpiAmount = findViewById(R.id.tvUpiAmount);
        tvCashAmount = findViewById(R.id.tvCashAmount);

        // ✅ Map card IDs
        cardViews.put("Products", findViewById(R.id.cardProducts));
        cardViews.put("Stocks", findViewById(R.id.cardStock));
        cardViews.put("Expense", findViewById(R.id.cardExpense));
        cardViews.put("Attendance", findViewById(R.id.cardAttendance));
        cardViews.put("Invoices", findViewById(R.id.cardInvoice));
        cardViews.put("Sales", findViewById(R.id.cardSales));

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.expense_range,
                        android.R.layout.simple_spinner_item
                );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDateRange.setAdapter(adapter);

        spinnerDateRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                animateDashboard();
                loadDashboard(spinnerDateRange.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //loadDashboard("Today");
    }

    private void loadDashboard(String range) {

        // Define variables to hold results

        Pair<Date, Date> date_range = SingleTon.getStartAndEndDate(range);
        Date startDate = date_range.first;
        Date endDate = date_range.second;

        Pair<Long, Long> long_range_pair = SingleTon.getEpochStartAndEnd(range);
        long startEpoch = long_range_pair.first;
        long endEpoch = long_range_pair.second;

// 1️⃣ Products count
        Task<AggregateQuerySnapshot> productsTask =
                db.collection(AppConstants.APP_NAME + AppConstants.PRODUCTS_COLLECTION)
                        .count().get(AggregateSource.SERVER)
                        .addOnSuccessListener(snap -> productCount[0] = snap.getCount());

// 2️⃣ Stocks count + nearly zero count
        Task<QuerySnapshot> stocksTask =
                db.collection(AppConstants.APP_NAME + AppConstants.STOCKS_COLLECTION).get()
                        .addOnSuccessListener(snap -> {
                            stockCount[0] = snap.size();
                            nearZeroStockCount[0] = snap.getDocuments().stream()
                                    .filter(d -> d.getDouble("quantity") != null && d.getDouble("quantity") <= 3)
                                    .count();
                        });

// 3️⃣ Expense total in date range
        Task<QuerySnapshot> expenseTask =
                db.collection(AppConstants.APP_NAME + AppConstants.EXPENSE_COLLECTION)
                        .whereGreaterThanOrEqualTo("date", sdf.format(startDate))
                        .whereLessThanOrEqualTo("date", sdf.format(endDate))
                        .get()
                        .addOnSuccessListener(snap -> {
                            double total = 0;
                            for (DocumentSnapshot doc : snap) {
                                Double amount = doc.getDouble("amount");
                                if (amount != null) total += amount;
                            }
                            totalExpense[0] = total;
                        });

// 4️⃣ Attendance count
        Task<AggregateQuerySnapshot> attendanceTask =
                db.collection(AppConstants.APP_NAME + AppConstants.ATTENDANCE_COLLECTION)
                        .whereGreaterThanOrEqualTo("date", sdf.format(startDate))
                        .whereLessThanOrEqualTo("date", sdf.format(endDate))
                        .count()
                        .get(AggregateSource.SERVER)
                        .addOnSuccessListener(snap -> attendanceCount[0] = snap.getCount());

// 5️⃣ Invoice count + total sales + UPI vs CASH
        Task<QuerySnapshot> invoiceTask =
                db.collection(AppConstants.APP_NAME + AppConstants.SALES_COLLECTION)
                        .whereGreaterThanOrEqualTo("billingDate", startEpoch)
                        .whereLessThanOrEqualTo("billingDate", endEpoch)
                        .get()
                        .addOnSuccessListener(snap -> {
                            invoiceCount[0] = snap.size();
                            double sales = 0, upi = 0, cash = 0;
                            for (DocumentSnapshot doc : snap) {
                                Double total = doc.getDouble("sellingCost");
                                String mode = doc.getString("paymentMode");
                                if (total != null) {
                                    sales += total;
                                    if ("UPI".equalsIgnoreCase(mode)) upi += total;
                                    else if ("CASH".equalsIgnoreCase(mode)) cash += total;
                                }
                            }
                            totalSales[0] = sales;
                            totalUpi[0] = upi;
                            totalCash[0] = cash;
                        });
        showLoader();
// ✅ Wait for all to finish
        Tasks.whenAllComplete(productsTask, stocksTask, expenseTask, attendanceTask, invoiceTask)
                .addOnCompleteListener(done -> {
                    // Update your dashboard UI
                    hideLoader();
                    setCardData("Products", String.valueOf(productCount[0]));
                    setCardData("Stocks", String.valueOf(stockCount[0]));
                    setCardData("Expense", "₹ " + totalExpense[0]);
                    setCardData("Attendance", String.valueOf(attendanceCount[0]));
                    setCardData("Invoices", String.valueOf(invoiceCount[0]));
                    setCardData("Sales", "₹ " + totalSales[0]);

                    Double cashValue = totalCash[0];
                    Double upiValue = totalUpi[0];

                    tvUpiAmount.setText("UPI: ₹" + String.format("%.2f", upiValue));
                    tvCashAmount.setText("Cash: ₹" + String.format("%.2f", cashValue));

                    loadPieChart(cashValue, upiValue);

                });

    }


    private void setCardData(String key, String value) {
        View card = cardViews.get(key);
        if (card == null) return;
        TextView title = card.findViewById(R.id.tvTitle);
        TextView val = card.findViewById(R.id.tvValue);
        ImageView icon = card.findViewById(R.id.imgIcon);

        title.setText(key);
        val.setText(value);

        int iconRes = R.drawable.baseline_analytics_24;
        if (key.equals("Products")) iconRes = R.drawable.products;
        else if (key.equals("Stocks")) iconRes = R.drawable.accessory;
        else if (key.equals("Expense")) iconRes = R.drawable.baseline_construction_24;
        else if (key.equals("Attendance")) iconRes = R.drawable.baseline_work_history_24;
        else if (key.equals("Invoices")) iconRes = R.drawable.baseline_file_copy_24;
        else if (key.equals("Sales")) iconRes = R.drawable.line;

        icon.setImageResource(iconRes);
    }

    private void loadPieChart(double cash, double upi) {
        List<PieEntry> entries = Arrays.asList(
                new PieEntry((float) cash, "Cash"),
                new PieEntry((float) upi, "UPI")
        );

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(new int[]{R.color.teal_700, R.color.app_color}, this);
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(14f);

        PieData data = new PieData(set);
        piePaymentMode.setData(data);
        piePaymentMode.getDescription().setEnabled(false);
        piePaymentMode.setUsePercentValues(false);
        piePaymentMode.setCenterText("Payments");
        piePaymentMode.setCenterTextSize(14f);
        piePaymentMode.setDrawHoleEnabled(true);
        piePaymentMode.animateY(1000, Easing.EaseInOutQuad);
        piePaymentMode.invalidate();
    }

    private void animateDashboard() {
        for (View card : cardViews.values()) {
            card.setScaleX(0.8f);
            card.setScaleY(0.8f);
            card.animate().scaleX(1f).scaleY(1f).setDuration(400).start();
        }
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
}
