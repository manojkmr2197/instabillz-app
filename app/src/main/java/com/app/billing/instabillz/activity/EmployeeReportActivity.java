package com.app.billing.instabillz.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.AttendanceViewAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.AttendanceModel;
import com.app.billing.instabillz.model.EmployeeModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.SharedPrefHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EmployeeReportActivity extends AppCompatActivity {

    TextView back;
    Spinner employeeSpinner, dateRangeSpinner;
    Button btnSearch;

    List<String> employeeList = new ArrayList<>();

    Context context;
    Activity activity;

    ArrayAdapter<String> employeeAdapter;

    AttendanceViewAdapter adapter;
    List<AttendanceModel> attendanceModelList;
    RecyclerView recyclerView;

    FirebaseFirestore db;
    SharedPrefHelper sharedPrefHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_report);
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

        context = EmployeeReportActivity.this;
        activity = EmployeeReportActivity.this;
        sharedPrefHelper = new SharedPrefHelper(context);

        db = FirebaseFirestore.getInstance();

        back = (TextView) findViewById(R.id.attendance_report_back);
        back.setOnClickListener(v -> {
            finish();
        });

        employeeSpinner = findViewById(R.id.attendance_report_employees_spinner);
        dateRangeSpinner = findViewById(R.id.attendance_report_date_range);
        btnSearch = findViewById(R.id.attendance_report_search);
        recyclerView = findViewById(R.id.attendance_report_recyclerView);

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
        ArrayAdapter<CharSequence> dateRangeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.attendance_date_ranges,
                android.R.layout.simple_spinner_item
        );
        dateRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateRangeSpinner.setAdapter(dateRangeAdapter);


        // 🔹 Handle search button click
        btnSearch.setOnClickListener(v -> {
            String selectedEmployee = employeeSpinner.getSelectedItem().toString();
            String selectedRange = dateRangeSpinner.getSelectedItem().toString();

            if (selectedEmployee.equals("Select Employee")) {
                Toast.makeText(this, "Please select an employee", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedRange.equals("Select Date Range")) {
                Toast.makeText(this, "Please select a date range", Toast.LENGTH_SHORT).show();
                return;
            }

            // Call your custom method here
            fetchAttendance(selectedEmployee, selectedRange);

        });

        BillingClickListener listener = new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if(type.equalsIgnoreCase("DELETE")){
                    removeAttendanceItem(index);
                }
            }
        };

        attendanceModelList = new ArrayList<>();
        loadHeaderData(attendanceModelList);
        adapter = new AttendanceViewAdapter(context, attendanceModelList, listener,"ADMIN".equalsIgnoreCase(sharedPrefHelper.getSystemUserRole()));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        String employeeName = intent.getStringExtra("employee_name");
        if (StringUtils.isNotBlank(employeeName)) {
            String dateRange = "Today";
            dateRangeSpinner.setSelection(dateRangeAdapter.getPosition(dateRange));
            employeeSpinner.setSelection(employeeAdapter.getPosition(employeeName));
            fetchAttendance(employeeName, dateRange);
        }
    }

    private void removeAttendanceItem(int position) {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        String docId = attendanceModelList.get(position).getEmployeeName() + "_" + attendanceModelList.get(position).getDate();
        InstaFirebaseRepository.getInstance().deleteData(AppConstants.APP_NAME + AppConstants.ATTENDANCE_COLLECTION, docId, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(Object data) {
                attendanceModelList.remove(position);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadEmployeeList() {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getAllDetails(AppConstants.APP_NAME + AppConstants.EMPLOYEE_COLLECTION, "name", Query.Direction.ASCENDING, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(Object data) {
                employeeList.clear();
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

    private void fetchAttendance(String employeeName, String selectedDateRange) {
        attendanceModelList.clear();
        loadHeaderData(attendanceModelList);
        adapter.notifyDataSetChanged();
        // 🔹 Prepare date formats
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(calendar.getTime());

        String startDate = today;
        String endDate = today;

        // 🔹 Determine startDate & endDate based on selection
        switch (selectedDateRange) {
            case "Today":
                startDate = today;
                endDate = today;
                break;

            case "Yesterday":
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                startDate = sdf.format(calendar.getTime());
                endDate = startDate;
                break;

            case "This Week":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                startDate = sdf.format(calendar.getTime());
                calendar.add(Calendar.DAY_OF_WEEK, 6);
                endDate = sdf.format(calendar.getTime());
                break;

            case "This Month":
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = sdf.format(calendar.getTime());
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = sdf.format(calendar.getTime());
                break;
            case "Last 3 Months":
                // Move calendar 3 months back
                calendar.add(Calendar.MONTH, -3);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = sdf.format(calendar.getTime());
                // End date = today
                calendar = Calendar.getInstance();
                endDate = sdf.format(calendar.getTime());
                break;

            case "Last 6 Months":
                // Move calendar 6 months back
                calendar.add(Calendar.MONTH, -6);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = sdf.format(calendar.getTime());
                // End date = today
                calendar = Calendar.getInstance();
                endDate = sdf.format(calendar.getTime());
                break;
        }

        System.out.println("AttendanceFilter " + employeeName + " | " + startDate + " → " + endDate);

        // 🔹 Build the query
        Query query = db.collection(AppConstants.APP_NAME + AppConstants.ATTENDANCE_COLLECTION);

        if (!employeeName.equals("Select Employee")) {
            query = query.whereEqualTo("employeeName", employeeName);
        }

        query = query
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", Query.Direction.DESCENDING);

        // 🔹 Execute query
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        AttendanceModel model = doc.toObject(AttendanceModel.class);
                        attendanceModelList.add(model);
                    }
                    adapter.notifyDataSetChanged();
                    // 🔹 Update RecyclerView or show message
                    if (attendanceModelList.isEmpty()) {
                        Toast.makeText(this, "No attendance records found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadHeaderData(List<AttendanceModel> attendanceModelList) {
        attendanceModelList.add(new AttendanceModel("","Date","Login","Logout","Working Hours"));
    }

}