package com.app.billing.instabillz.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.InvoiceViewAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.EmployeeModel;
import com.app.billing.instabillz.model.InvoiceModel;
import com.app.billing.instabillz.model.PrinterDataModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.AutoIndexHelper;
import com.app.billing.instabillz.utils.BluetoothPrinterHelper;
import com.app.billing.instabillz.utils.SharedPrefHelper;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class InvoiceActivity extends AppCompatActivity {

    TextView back;
    Spinner employeeSpinner, dateRangeSpinner;
    Button btnSearch;
    LinearLayout filterLL;

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
        setContentView(R.layout.activity_invoice);
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
        context = InvoiceActivity.this;
        activity = InvoiceActivity.this;
        db = FirebaseFirestore.getInstance();
        sharedPrefHelper = new SharedPrefHelper(context);
        bluetoothPrinterHelper = new BluetoothPrinterHelper(context, activity);
        printerDataModel = sharedPrefHelper.getPrinterDetails();

        back = (TextView) findViewById(R.id.invoice_back);
        back.setOnClickListener(v -> {
            finish();
        });

        employeeSpinner = findViewById(R.id.invoice_employees_spinner);
        dateRangeSpinner = findViewById(R.id.invoice_date_range);
        btnSearch = findViewById(R.id.invoice_search);
        filterLL = (LinearLayout) findViewById(R.id.invoice_filter_ll);


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
            Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
            // Call your custom method here
            fetchInvoices(selectedEmployee, selectedRange, selectedDate);

        });

        recyclerView = findViewById(R.id.invoice_recyclerView);

        BillingClickListener clickListener = new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if(type.equalsIgnoreCase("EDIT")){
                    Intent intent = new Intent(context, InvoiceEditActivity.class);
                    intent.putExtra("invoice", new Gson().toJson(invoiceModelList.get(index))); // pass data as JSON
                    context.startActivity(intent);
                }else if(type.equalsIgnoreCase("DELETE")){
                    deleteConfirmationInvoice(index);
                }else if(type.equalsIgnoreCase("PRINT")){
                    generateHardCopyBill(invoiceModelList.get(index));
                }
            }
        };

        invoiceModelList = new ArrayList<>();
        adapter = new InvoiceViewAdapter(context, invoiceModelList, clickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        String employeeName = intent.getStringExtra("employee_name");
        if (StringUtils.isNotBlank(employeeName)) {
            filterLL.setVisibility(View.GONE);
            employeeSpinner.setSelection(employeeAdapter.getPosition(employeeName));
            dateRangeSpinner.setSelection(dateRangeAdapter.getPosition("Today"));
            Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
            fetchInvoices(employeeName, "Today", selectedDate);
        }else{
            filterLL.setVisibility(View.VISIBLE);
        }

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


    @Override
    protected void onResume() {
        super.onResume();
        if(employeeSpinner.getSelectedItem() != null && dateRangeSpinner.getSelectedItem() != null) {
            fetchInvoices(employeeSpinner.getSelectedItem().toString(), dateRangeSpinner.getSelectedItem().toString(), selectedDate);
        }
    }

    private void deleteConfirmationInvoice(int index) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to Delete the Invoice # "+invoiceModelList.get(index).getToken());
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            try {
                deleteProductItem(index);
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

    private void deleteProductItem(int index) {
        Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().deleteData(AppConstants.APP_NAME + AppConstants.SALES_COLLECTION, String.valueOf(invoiceModelList.get(index).getBillingDate()), new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(Object data) {
                invoiceModelList.remove(index);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchInvoices(String selectedEmployee, String selectedRange, String selectedDate) {
        //Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query query = db.collection(AppConstants.APP_NAME + AppConstants.SALES_COLLECTION);

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

        query = query.orderBy("billingDate", Query.Direction.DESCENDING);
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
                if(StringUtils.isNotBlank(getIntent().getStringExtra("employee_name"))) {
                    employeeSpinner.setSelection(employeeAdapter.getPosition(getIntent().getStringExtra("employee_name")));
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