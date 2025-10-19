package com.app.billing.instabillz.activity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.billing.instabillz.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsolidateReportActivity extends AppCompatActivity {
    private Spinner spinnerDateRange;
    private PieChart piePaymentMode;
    private TextView tvUpiAmount, tvCashAmount;

    private Map<String, View> cardViews = new HashMap<>();

    private List<String> dateOptions = Arrays.asList("Today", "Yesterday", "Last 7 Days", "This Month");

    TextView back;

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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dateOptions);
        spinnerDateRange.setAdapter(adapter);

        spinnerDateRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                animateDashboard();
                loadDashboard(dateOptions.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadDashboard("Today");
    }

    private void loadDashboard(String range) {
        // Mock values (replace with Firebase queries)
        setCardData("Products", "120");
        setCardData("Stocks", "480");
        setCardData("Expense", "₹2345");
        setCardData("Attendance", "12");
        setCardData("Invoices", "15");
        setCardData("Sales", "₹20,500");

        Double cashValue =12000d;
        Double upiValue = 8500d;

        tvUpiAmount.setText("UPI: ₹" + String.format("%.2f", upiValue));
        tvCashAmount.setText("Cash: ₹" + String.format("%.2f", cashValue));

        loadPieChart(12000, 8500);
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
}
