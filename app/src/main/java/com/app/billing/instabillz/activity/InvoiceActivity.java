package com.app.billing.instabillz.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.InvoiceViewAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.EmployeeModel;
import com.app.billing.instabillz.model.InvoiceModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.SharedPrefHelper;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.commons.lang3.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class InvoiceActivity extends AppCompatActivity {

    TextView back;
    Spinner employeeSpinner, dateRangeSpinner;
    Button btnSearch;

    List<String> employeeList = new ArrayList<>();

    Context context;
    Activity activity;

    ArrayAdapter<String> employeeAdapter;
    ArrayAdapter<CharSequence> dateRangeAdapter;

    RecyclerView recyclerView;
    FirebaseFirestore db;
    SharedPrefHelper sharedPrefHelper;
    String selectedDate = "";

    ImageView calendarIcon;
    TextView selectedDateTv;

    InvoiceViewAdapter adapter;
    List<InvoiceModel> invoiceModelList;

    ZoneOffset istOffset = ZoneOffset.ofHoursMinutes(5, 30);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);
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
        context = InvoiceActivity.this;
        activity = InvoiceActivity.this;
        db = FirebaseFirestore.getInstance();
        sharedPrefHelper = new SharedPrefHelper(context);


        back = (TextView) findViewById(R.id.invoice_back);
        back.setOnClickListener(v -> {
            finish();
        });

        employeeSpinner = findViewById(R.id.invoice_employees_spinner);
        dateRangeSpinner = findViewById(R.id.invoice_date_range);
        btnSearch = findViewById(R.id.invoice_search);


        // 🔹 Load sample employee names

        employeeList.add("Select Employee"); // default option

        employeeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                employeeList
        );
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        employeeSpinner.setAdapter(employeeAdapter);
        loadEmployeeList();

        // 🔹 Load sample date ranges
        dateRangeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.invoice_date_ranges,
                android.R.layout.simple_spinner_item
        );
        dateRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateRangeSpinner.setAdapter(dateRangeAdapter);

        dateRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Hide selected date text
                selectedDateTv.setVisibility(View.GONE);
                selectedDate = "";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        calendarIcon = findViewById(R.id.invoice_calendar_icon);
        selectedDateTv = findViewById(R.id.invoice_selected_date);

        calendarIcon.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Format date as yyyy-MM-dd
                        selectedDate = String.format(Locale.getDefault(),
                                "%04d-%02d-%02d", selectedYear, (selectedMonth + 1), selectedDay);

                        selectedDateTv.setText("Selected Date: " + selectedDate);
                        selectedDateTv.setVisibility(View.VISIBLE);
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });


        // 🔹 Handle search button click
        btnSearch.setOnClickListener(v -> {
            String selectedEmployee = employeeSpinner.getSelectedItem().toString();
            String selectedRange = dateRangeSpinner.getSelectedItem().toString();

            if (selectedEmployee.equals("Select Employee")) {
                Toast.makeText(this, "Please select an employee", Toast.LENGTH_SHORT).show();
                return;
            }

            if (StringUtils.isBlank(selectedDate) && selectedRange.equals("Select Range")) {
                Toast.makeText(this, "Please select a date range", Toast.LENGTH_SHORT).show();
                return;
            }

            // Call your custom method here
            fetchInvoices(selectedEmployee, selectedRange, selectedDate);

        });

        recyclerView = findViewById(R.id.invoice_recyclerView);

        BillingClickListener clickListener = new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if(type.equalsIgnoreCase("EDIT")){

                }else if(type.equalsIgnoreCase("DELETE")){

                }
            }
        };

        invoiceModelList = new ArrayList<>();
        adapter = new InvoiceViewAdapter(context, invoiceModelList, clickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);


    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchInvoices(String selectedEmployee, String selectedRange, String selectedDate) {
        Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference invoiceRef = db.collection(AppConstants.APP_NAME + AppConstants.SALES_COLLECTION);

        Query query = invoiceRef;

        // 1️⃣ Employee filter
        if (!selectedEmployee.equalsIgnoreCase("ALL")) {
            query = query.whereEqualTo("employeeName", selectedEmployee);
        }

        // 2️⃣ Date range filter
        long startEpoch = 0L;
        long endEpoch = Long.MAX_VALUE;

        OffsetDateTime now = OffsetDateTime.now();

        switch (selectedRange) {
            case "Today":
                startEpoch = now.toLocalDate().atStartOfDay().toEpochSecond(istOffset);
                endEpoch = now.toLocalDate().atTime(23, 59, 59).toEpochSecond(istOffset);
                break;

            case "Yesterday":
                OffsetDateTime yesterday = now.minusDays(1);
                startEpoch = yesterday.toLocalDate().atStartOfDay().toEpochSecond(istOffset);
                endEpoch = yesterday.toLocalDate().atTime(23, 59, 59).toEpochSecond(istOffset);
                break;

            case "This Week":
                OffsetDateTime weekStart = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay().atOffset(istOffset);
                OffsetDateTime weekEnd = now.with(DayOfWeek.SUNDAY).toLocalDate().atTime(23, 59, 59).atOffset(istOffset);
                startEpoch = weekStart.toEpochSecond();
                endEpoch = weekEnd.toEpochSecond();
                break;

            case "This Month":
                OffsetDateTime monthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay().atOffset(istOffset);
                OffsetDateTime monthEnd = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).toLocalDate().atTime(23, 59, 59).atOffset(istOffset);
                startEpoch = monthStart.toEpochSecond();
                endEpoch = monthEnd.toEpochSecond();
                break;

            case "Last 3 Months":
                OffsetDateTime last3Months = now.minusMonths(3).withDayOfMonth(1).toLocalDate().atStartOfDay().atOffset(istOffset);
                startEpoch = last3Months.toEpochSecond();
                endEpoch = now.toEpochSecond();
                break;

            case "Last 6 Months":
                OffsetDateTime last6Months = now.minusMonths(6).withDayOfMonth(1).toLocalDate().atStartOfDay().atOffset(istOffset);
                startEpoch = last6Months.toEpochSecond();
                endEpoch = now.toEpochSecond();
                break;
        }


        // 3️⃣ Custom selected date
        if (selectedDate != null && !selectedDate.isEmpty()) {
            LocalDate customDate = LocalDate.parse(selectedDate); // yyyy-MM-dd
            startEpoch = customDate.atStartOfDay().toEpochSecond(istOffset);
            endEpoch = customDate.atTime(23, 59, 59).toEpochSecond(istOffset);
        }


        // Apply billing date filter
        query = query.whereGreaterThanOrEqualTo("billingDate", startEpoch)
                .whereLessThanOrEqualTo("billingDate", endEpoch);

        query = query.orderBy("token", Query.Direction.DESCENDING);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            invoiceModelList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                InvoiceModel invoice = doc.toObject(InvoiceModel.class);
                invoiceModelList.add(invoice);
            }
            adapter.notifyDataSetChanged();

        }).addOnFailureListener(e -> {
            e.printStackTrace();
            Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
        });
    }


    private void loadEmployeeList() {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getAllDetails(AppConstants.APP_NAME + AppConstants.EMPLOYEE_COLLECTION, "name", Query.Direction.ASCENDING, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(Object data) {
                employeeList.clear();
                employeeList.add("ALL");
                List<EmployeeModel> employees = new ArrayList<>();
                QuerySnapshot documentSnapshotList = (QuerySnapshot) data;
                for (DocumentSnapshot doc : documentSnapshotList) {
                    employees.add(doc.toObject(EmployeeModel.class));
                }
                employeeList.addAll(
                        employees.stream()
                                .map(EmployeeModel::getName)
                                .filter(name -> name != null && !name.trim().isEmpty())
                                .collect(Collectors.toList())
                );
                employeeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });
    }
}