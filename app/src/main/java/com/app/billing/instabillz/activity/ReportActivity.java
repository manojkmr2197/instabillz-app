package com.app.billing.instabillz.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.model.InvoiceModel;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.utils.ReportGenerator;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportActivity extends AppCompatActivity {

    private Spinner spinnerDateRange;
    private TextView tvCustomRange, tvTotalInvoicesValue, tvRevenueValue, tvPaymentSplit, back,download, tvUpiValue, tvCashValue;
    private BarChart barChartTokens;
    private BarChart hbarTopProducts;
    private RecyclerView rvProducts;

    // Data
    private FirebaseFirestore db;
    private List<InvoiceModel> invoices = new ArrayList<>();
    private Map<String, Integer> tokensPerDay = new HashMap<>();
    private Map<String, Integer> productTotals = new HashMap<>();
    private int upiCount = 0, cashCount = 0;
    private double upiValue,cashValue;
    private double totalRevenue = 0;

    // For custom range
    private LocalDate customStart = null;
    private LocalDate customEnd = null;
    private final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private ProductsSummaryAdapter productsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
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
        db = FirebaseFirestore.getInstance();

        spinnerDateRange = findViewById(R.id.spinnerDateRange);
        tvCustomRange = findViewById(R.id.tvCustomRange);
        tvTotalInvoicesValue = findViewById(R.id.tvTotalInvoicesValue);
        tvRevenueValue = findViewById(R.id.tvRevenueValue);
        tvPaymentSplit = findViewById(R.id.tvPaymentSplit);
        tvUpiValue = findViewById(R.id.tvRevenueUpiValue);
        tvCashValue = findViewById(R.id.tvRevenueCashValue);
        tvPaymentSplit = findViewById(R.id.tvPaymentSplit);
        barChartTokens = findViewById(R.id.barChartTokens);
        hbarTopProducts = findViewById(R.id.hbarTopProducts);
        rvProducts = findViewById(R.id.rvProducts);
        back = findViewById(R.id.invoice_report_back);
        download = findViewById(R.id.invoice_report_download);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadReportList();
            }
        });

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.invoice_date_ranges,
                        android.R.layout.simple_spinner_item
                );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDateRange.setAdapter(adapter);

        spinnerDateRange.setSelection(1); // default Today
        spinnerDateRange.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String sel = (String) parent.getItemAtPosition(position);
                if ("Custom Range".equals(sel)) {
                    openCustomRangePicker();
                } else {
                    // reset custom range label
                    tvCustomRange.setVisibility(View.GONE);
                    customStart = null;
                    customEnd = null;
                    fetchForRange(sel);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        productsAdapter = new ProductsSummaryAdapter(new ArrayList<>());
        rvProducts.setAdapter(productsAdapter);
        rvProducts.setNestedScrollingEnabled(false);

        // initial load
        fetchForRange("Today");
    }

    // -------------------- Fetching & computing --------------------
    private void fetchForRange(String range) {
        // compute start/end epoch seconds (IST)
        long startEpoch = 0L, endEpoch = Long.MAX_VALUE;
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.ofHoursMinutes(5, 30));

        switch (range) {
            case "Today":
                LocalDate today = LocalDate.now(IST);
                startEpoch = today.atStartOfDay(IST).toEpochSecond();
                endEpoch = today.atTime(23, 59, 59).atZone(IST).toEpochSecond();
                break;
            case "Yesterday":
                LocalDate y = LocalDate.now(IST).minusDays(1);
                startEpoch = y.atStartOfDay(IST).toEpochSecond();
                endEpoch = y.atTime(23, 59, 59).atZone(IST).toEpochSecond();
                break;
            case "This Week":
                LocalDate first = LocalDate.now(IST).with(java.time.DayOfWeek.MONDAY);
                LocalDate last = first.plusDays(6);
                startEpoch = first.atStartOfDay(IST).toEpochSecond();
                endEpoch = last.atTime(23, 59, 59).atZone(IST).toEpochSecond();
                break;
            case "This Month":
                LocalDate ms = LocalDate.now(IST).withDayOfMonth(1);
                LocalDate me = LocalDate.now(IST).withDayOfMonth(LocalDate.now(IST).lengthOfMonth());
                startEpoch = ms.atStartOfDay(IST).toEpochSecond();
                endEpoch = me.atTime(23, 59, 59).atZone(IST).toEpochSecond();
                break;
            case "Last 3 Months":
                LocalDate from3 = LocalDate.now(IST).minusMonths(3).withDayOfMonth(1);
                startEpoch = from3.atStartOfDay(IST).toEpochSecond();
                endEpoch = LocalDate.now(IST).atTime(23, 59, 59).atZone(IST).toEpochSecond();
                break;
            case "Last 6 Months":
                LocalDate from6 = LocalDate.now(IST).minusMonths(6).withDayOfMonth(1);
                startEpoch = from6.atStartOfDay(IST).toEpochSecond();
                endEpoch = LocalDate.now(IST).atTime(23, 59, 59).atZone(IST).toEpochSecond();
                break;
            default:
                // Select Date Range or unknown -> return
                Toast.makeText(this, "Please select a valid range", Toast.LENGTH_SHORT).show();
                return;
        }

        // if custom range is set override
        if (customStart != null && customEnd != null) {
            startEpoch = customStart.atStartOfDay(IST).toEpochSecond();
            endEpoch = customEnd.atTime(23, 59, 59).atZone(IST).toEpochSecond();
        }

        loadInvoicesFromFirestore(startEpoch, endEpoch);
    }

    private void openCustomRangePicker() {
        // pick start then end
        LocalDate now = LocalDate.now(IST);
        DatePickerDialog startPicker = new DatePickerDialog(this,
                (v, y, m, d) -> {
                    customStart = LocalDate.of(y, m + 1, d);
                    // open end picker
                    DatePickerDialog endPicker = new DatePickerDialog(this,
                            (v2, y2, m2, d2) -> {
                                customEnd = LocalDate.of(y2, m2 + 1, d2);
                                tvCustomRange.setText(customStart.toString() + " → " + customEnd.toString());
                                tvCustomRange.setVisibility(View.VISIBLE);
                                fetchForRange("Custom");
                            }, now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth());
                    endPicker.show();
                }, now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth());
        startPicker.show();
    }

    private void loadInvoicesFromFirestore(long startEpoch, long endEpoch) {
        // Query by billingDate (assumes billingDate stored as epoch seconds)
        db.collection(AppConstants.APP_NAME + AppConstants.SALES_COLLECTION)
                .whereGreaterThanOrEqualTo("billingDate", startEpoch)
                .whereLessThanOrEqualTo("billingDate", endEpoch)
                .orderBy("billingDate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(this::onInvoicesLoaded)
                .addOnFailureListener(e -> Toast.makeText(this, "Fetch failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void onInvoicesLoaded(QuerySnapshot snaps) {
        invoices.clear();
        for (DocumentSnapshot doc : snaps.getDocuments()) {
            InvoiceModel inv = doc.toObject(InvoiceModel.class);
            if (inv != null) invoices.add(inv);
        }
        if (invoices.isEmpty()) {
            Toast.makeText(this, "No Invoices Found", Toast.LENGTH_SHORT).show();
        }
        computeMetricsAndRender();
    }

    private void computeMetricsAndRender() {
        tokensPerDay.clear();
        productTotals.clear();
        upiCount = 0;
        cashCount = 0;
        totalRevenue = 0;
        upiValue=0;
        cashValue=0;


        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (InvoiceModel inv : invoices) {
            // date key (IST)
            LocalDate date = Instant.ofEpochSecond(inv.getBillingDate()).atZone(IST).toLocalDate();
            String dateKey = date.format(df);
            tokensPerDay.put(dateKey, tokensPerDay.getOrDefault(dateKey, 0) + 1);

            if ("UPI".equalsIgnoreCase(inv.getPaymentMode())) {
                upiCount++;
                if (inv.getSellingCost() != null)
                    upiValue = upiValue+inv.getSellingCost();
            } else {
                cashCount++;
                if (inv.getSellingCost() != null)
                    cashValue = cashValue+inv.getSellingCost();
            }
            if (inv.getSellingCost() != null) totalRevenue += inv.getSellingCost();

            // accumulate products
            if (inv.getProductModelList() != null) {
                for (ProductModel p : inv.getProductModelList()) {
                    if (p == null || p.getName() == null) continue;
                    int prev = productTotals.getOrDefault(p.getName(), 0);
                    productTotals.put(p.getName(), prev + p.getQty());
                }
            }
        }

        // update UI
        runOnUiThread(() -> {
            tvTotalInvoicesValue.setText(String.valueOf(invoices.size()));
            tvRevenueValue.setText("₹" + String.format(Locale.getDefault(), "%.2f", totalRevenue));
            tvPaymentSplit.setText("UPI: " + upiCount + " \nCash: " + cashCount);
            tvUpiValue.setText("₹" + String.format(Locale.getDefault(), "%.2f", upiValue));
            tvCashValue.setText("₹" + String.format(Locale.getDefault(), "%.2f", cashValue));
            renderTokensBarChart();
            renderTopProductsChartAndList();
        });
    }

    // -------------------- Charts & list render --------------------
    private void renderTokensBarChart() {
        List<String> labels = new ArrayList<>(tokensPerDay.keySet()).stream()
                .sorted()
                .collect(Collectors.toList());

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            entries.add(new BarEntry(i, tokensPerDay.get(labels.get(i))));
        }

        BarDataSet set = new BarDataSet(entries, "Tokens / Day");
        set.setColor(getResources().getColor(android.R.color.holo_blue_dark));

        BarData data = new BarData(set);
        data.setBarWidth(0.9f);
        data.setValueTextSize(12f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // ✅ Show whole numbers only
            }
        });

        barChartTokens.setData(data);

        // ✅ X Axis setup
        XAxis xAxis = barChartTokens.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // Prevents decimals between bars
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                if (idx >= 0 && idx < labels.size()) {
                    return labels.get(idx);
                }
                return "";
            }
        });

        // ✅ Y Axis setup (round off Y labels)
        YAxis leftAxis = barChartTokens.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        barChartTokens.getAxisRight().setEnabled(false);
        barChartTokens.getDescription().setEnabled(false);
        barChartTokens.getLegend().setTextSize(12f);
        barChartTokens.invalidate(); // Refresh chart
    }


    private void renderTopProductsChartAndList() {
        // ✅ Sort products by quantity descending
        List<Map.Entry<String, Integer>> list = new ArrayList<>(productTotals.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // ✅ Top 20 products
        List<Map.Entry<String, Integer>> top = list.stream()
                .limit(20)
                .collect(Collectors.toList());

        // ✅ Build entries for chart
        List<BarEntry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        for (int i = 0; i < top.size(); i++) {
            entries.add(new BarEntry(i, top.get(i).getValue())); // X=index, Y=qty
            labels.add(top.get(i).getKey()); // product name
        }

        // ✅ Create dataset
        BarDataSet set = new BarDataSet(entries, "Top Products");
        set.setColor(getResources().getColor(android.R.color.holo_orange_dark));
        set.setValueTextSize(12f);
        set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // show whole numbers
            }
        });

        // ✅ Prepare data and assign to chart
        BarData data = new BarData(set);
        data.setBarWidth(0.7f);
        hbarTopProducts.setData(data);

        // ✅ Configure X Axis (Product names)
        XAxis xAxis = hbarTopProducts.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size());
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45); // rotate labels for visibility
        xAxis.setTextSize(10f);
        xAxis.setAvoidFirstLastClipping(true);

        // ✅ Configure Y Axis (Quantities)
        YAxis leftAxis = hbarTopProducts.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        leftAxis.setDrawGridLines(true);

        // ✅ Disable right Y Axis
        hbarTopProducts.getAxisRight().setEnabled(false);

        // ✅ Disable chart description
        hbarTopProducts.getDescription().setEnabled(false);

        // ✅ Legend & animation
        hbarTopProducts.getLegend().setTextSize(12f);
        hbarTopProducts.animateY(1000);

        // ✅ Refresh chart
        hbarTopProducts.invalidate();

        // ✅ Update RecyclerView list
        List<ProductSummary> summary = top.stream()
                .map(e -> new ProductSummary(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        productsAdapter.updateData(summary);
    }




    // -------------------- Helper classes --------------------
    // Simple adapter for product summary list
    static class ProductSummary {
        String name;
        int qty;

        ProductSummary(String name, int qty) {
            this.name = name;
            this.qty = qty;
        }
    }

    class ProductsSummaryAdapter extends RecyclerView.Adapter<ProductsSummaryAdapter.VH> {
        private List<ProductSummary> list;

        ProductsSummaryAdapter(List<ProductSummary> list) {
            this.list = list;
        }

        void updateData(List<ProductSummary> newList) {
            this.list = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ProductSummary p = list.get(position);
            holder.t1.setText(p.name);
            holder.t2.setText(String.valueOf(p.qty));
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView t1, t2;

            VH(@NonNull View itemView) {
                super(itemView);
                t1 = itemView.findViewById(android.R.id.text1);
                t2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }

    private void downloadReportList() {
        try {
            if (invoices.isEmpty()) {
                Toast.makeText(this, "No Invoices Found", Toast.LENGTH_SHORT).show();
                return;
            }
            saveExcelFile(invoices);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Report Generation failed ..!", Toast.LENGTH_LONG).show();
        }
    }

    private void saveExcelFile(List<InvoiceModel> invoiceModelList) throws Exception {
        String fileName = "invoice-Report-" + System.currentTimeMillis() + ".xlsx";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        ReportGenerator reportGenerator = new ReportGenerator();
        reportGenerator.createInvoiceExcelReport(invoiceModelList, file);

        // Notify the user
        Toast.makeText(this, "Report Generated: " + fileName, Toast.LENGTH_SHORT).show();

        // Use FileProvider to get the URI
        Uri fileUri = FileProvider.getUriForFile(this, AppConstants.COM_APP_BILLING_INSTABILLZ_FILEPROVIDER, file);

        // Open the file using a file explorer
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.ms-excel");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}
