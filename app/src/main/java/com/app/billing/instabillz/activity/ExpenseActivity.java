package com.app.billing.instabillz.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.ExpenseViewAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.AttendanceModel;
import com.app.billing.instabillz.model.ExpenseModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.SingleTon;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseActivity extends AppCompatActivity {

    Context context;
    Activity activity;

    TextView back;
    RecyclerView recyclerView;
    FloatingActionButton add_fab;

    Spinner typeSpinner, dateRangeSpinner;
    Button btnSearch;

    String selectedType="", selectedRange="";

    List<ExpenseModel> expenseModelList;
    ExpenseViewAdapter adapter;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
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
        context = ExpenseActivity.this;
        activity = ExpenseActivity.this;
        db = FirebaseFirestore.getInstance();

        back = findViewById(R.id.expense_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        add_fab = (FloatingActionButton) findViewById(R.id.expense_add_fab);
        add_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateExpenseDialog();
            }
        });

        typeSpinner = findViewById(R.id.expense_type_spinner);
        dateRangeSpinner = findViewById(R.id.expense_date_range);
        btnSearch = findViewById(R.id.expense_search);

        BillingClickListener clickListener = new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if(type.equalsIgnoreCase("DELETE")){
                    deleteConfirmationPopUp(index);
                }
            }
        };

        expenseModelList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.expense_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new ExpenseViewAdapter(context, expenseModelList, clickListener);
        recyclerView.setAdapter(adapter);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.expense_type,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> dateRangeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.expense_range,
                android.R.layout.simple_spinner_item
        );
        dateRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateRangeSpinner.setAdapter(dateRangeAdapter);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedType = typeSpinner.getSelectedItem().toString();
                selectedRange = dateRangeSpinner.getSelectedItem().toString();

                // Call your custom method here
                fetchExpense(selectedType, selectedRange);
            }
        });
        typeSpinner.setSelection(0);
        dateRangeSpinner.setSelection(0);
        selectedType = typeSpinner.getSelectedItem().toString();
        selectedRange = dateRangeSpinner.getSelectedItem().toString();
        fetchExpense(selectedType, selectedRange);

    }


    private void deleteConfirmationPopUp(int position) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        String message = "Do you want to remove the Expense?\n\n" +
                "Expense Type: " + expenseModelList.get(position).getType() + "\n" +
                "Date: " + expenseModelList.get(position).getDate();
        builder.setMessage(message);
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            try {
                removeExpenseItem(position);
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

    private void removeExpenseItem(int position) {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        String docId = expenseModelList.get(position).getId();
        InstaFirebaseRepository.getInstance().deleteData(AppConstants.APP_NAME + AppConstants.EXPENSE_COLLECTION, docId, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(Object data) {
                expenseModelList.remove(position);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchExpense(String expenseType, String selectedDateRange) {
        expenseModelList.clear();
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

        System.out.println("ExpenseFilter " + expenseType + " | " + startDate + " → " + endDate);

        // 🔹 Build the query
        Query query = db.collection(AppConstants.APP_NAME + AppConstants.EXPENSE_COLLECTION);

        if (!expenseType.equals("ALL")) {
            query = query.whereEqualTo("type", expenseType);
        }

        query = query
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", Query.Direction.DESCENDING);

        // 🔹 Execute query
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ExpenseModel model = doc.toObject(ExpenseModel.class);
                        expenseModelList.add(model);
                    }
                    adapter.notifyDataSetChanged();
                    // 🔹 Update RecyclerView or show message
                    if (expenseModelList.isEmpty()) {
                        Toast.makeText(this, "No attendance records found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showCreateExpenseDialog() {
        Dialog dialog = new Dialog(context);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_expense_create, null);
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

        Spinner spinnerType = dialog.findViewById(R.id.expense_create_type_spinner);
        TextView selectDateTV = dialog.findViewById(R.id.expense_create_date); // update id to your TextView
        EditText descET = dialog.findViewById(R.id.expense_create_desc);
        EditText amountET = dialog.findViewById(R.id.expense_create_price);
        Button submitBtn = dialog.findViewById(R.id.expense_create_submit);
        ImageView closeBtn = dialog.findViewById(R.id.expense_create_close);

        // 1️⃣ Setup Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.expense_create_type, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        // 2️⃣ Date picker
        final Calendar calendar = Calendar.getInstance();
        final String[] selectedDate = {""};

        selectDateTV.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (view, y, m, d) -> {
                        calendar.set(y, m, d);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        selectedDate[0] = sdf.format(calendar.getTime());
                        selectDateTV.setText(selectedDate[0]);
                    }, year, month, day);
            datePicker.show();
        });

        // 3️⃣ Close dialog
        closeBtn.setOnClickListener(v -> dialog.dismiss());

        // 4️⃣ Submit button
        submitBtn.setOnClickListener(v -> {
            SingleTon.hideKeyboard(context, activity);
            String type = spinnerType.getSelectedItem().toString();
            String desc = descET.getText().toString().trim();
            String amountStr = amountET.getText().toString().trim();

            // ✅ Validations
            if (type.isEmpty() || type.equals("Select Type")) {
                Toast.makeText(this, "Please select expense type", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedDate[0].isEmpty()) {
                Toast.makeText(this, "Please select date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Create ExpenseModel object
            ExpenseModel expense = new ExpenseModel();
            expense.setId(SingleTon.generateExpenseDocument());
            expense.setType(type);
            expense.setDate(selectedDate[0]);
            expense.setAmount(amount);
            expense.setDescription(desc);

            InstaFirebaseRepository.getInstance().addDataBase(AppConstants.APP_NAME + AppConstants.EXPENSE_COLLECTION, expense.getId(), expense, new InstaFirebaseRepository.OnFirebaseWriteListener() {
                @Override
                public void onSuccess(Object data) {
                    dialog.dismiss();
                    Toast.makeText(context, "New Expense Added", Toast.LENGTH_LONG).show();
                    fetchExpense(selectedType, selectedRange);
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Exception e) {
                    dialog.dismiss();
                    e.printStackTrace();
                    Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
                }
            });

        });

        dialog.show();
    }

}