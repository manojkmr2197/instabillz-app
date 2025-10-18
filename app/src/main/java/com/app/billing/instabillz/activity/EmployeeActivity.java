package com.app.billing.instabillz.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.EmployeeViewAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.EmployeeModel;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.model.StockModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.SingleTon;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EmployeeActivity extends AppCompatActivity {

    TextView back;
    RecyclerView recyclerView;
    FloatingActionButton add_fab;

    Context context;
    Activity activity;

    EditText search;
    List<EmployeeModel> filteredList;
    List<EmployeeModel> employees;
    EmployeeViewAdapter adapter;
    BillingClickListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);
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

        context = EmployeeActivity.this;
        activity = EmployeeActivity.this;

        back = (TextView) findViewById(R.id.employee_back);
        back.setOnClickListener(v -> {
            finish();
        });

        add_fab = (FloatingActionButton) findViewById(R.id.employee_add_fab);

        add_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewEmployee(null);
            }
        });

        search = (EditText) findViewById(R.id.employee_etSearch);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchPageFilter(s.toString());
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.employee_recyclerView);

        listener = new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if (type.equalsIgnoreCase("EDIT")) {
                    createNewEmployee(filteredList.get(index));
                } else if (type.equalsIgnoreCase("DELETE")) {
                    deleteConfirmationPopUp(filteredList.get(index));
                } else if (type.equalsIgnoreCase("ATTENDANCE")) {
                    //redirect to attendance report page
                    Intent intent = new Intent(EmployeeActivity.this, EmployeeReportActivity.class);
                    intent.putExtra("employee_name", filteredList.get(index).getName());
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        };
        employees = new ArrayList<>();
        filteredList = new ArrayList<>();
        loadEmployeeList();
        adapter = new EmployeeViewAdapter(context, filteredList, listener);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);


    }

    private void deleteConfirmationPopUp(EmployeeModel employeeModel) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to Delete the Employee [" + employeeModel.getName() + "]");
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            try {
                deleteDBItem(employeeModel.getId());
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

    private void deleteDBItem(String id) {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().deleteData(AppConstants.APP_NAME + AppConstants.EMPLOYEE_COLLECTION, id, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                Toast.makeText(context, "Employee Removed", Toast.LENGTH_LONG).show();
                loadEmployeeList();
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
                employees.clear();
                filteredList.clear();
                search.setText("");
                QuerySnapshot documentSnapshotList = (QuerySnapshot) data;
                for (DocumentSnapshot doc : documentSnapshotList) {
                    employees.add(doc.toObject(EmployeeModel.class));
                }
                filteredList.addAll(employees);
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
    private void searchPageFilter(String constraint) {
        filteredList.clear();
        if (constraint == null || constraint.isEmpty()) {
            filteredList.addAll(employees);
        } else {
            String filterPattern = constraint.toString().toLowerCase().trim();
            for (EmployeeModel item : employees) {
                if (item.getName().toLowerCase().startsWith(filterPattern)) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void createNewEmployee(EmployeeModel employeeModel) {
        Dialog dialog = new Dialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_employee_create, null);
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

        EditText name = (EditText) dialog.findViewById(R.id.employee_add_name);
        EditText phone = (EditText) dialog.findViewById(R.id.employee_add_phone);
        EditText passcode = (EditText) dialog.findViewById(R.id.employee_add_passcode);
        Spinner roleSpinner = (Spinner) dialog.findViewById(R.id.employee_add_role);
        CheckBox availableCheckbox = (CheckBox) dialog.findViewById(R.id.employee_add_checkbox);
        Button submit = (Button) dialog.findViewById(R.id.employee_add_submit);
        TextView close = (TextView) dialog.findViewById(R.id.employee_add_close);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SingleTon.hideKeyboard(context, activity);
                dialog.dismiss();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_role, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        roleSpinner.setAdapter(adapter);

        if (employeeModel != null) {
            name.setText(employeeModel.getName());
            phone.setText(employeeModel.getPhone());
            passcode.setText(employeeModel.getPasscode());
            roleSpinner.setSelection(adapter.getPosition(employeeModel.getRole()));
            availableCheckbox.setChecked(employeeModel.getActive());
        }


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().toString().isEmpty()) {
                    name.setError("Enter employee name");
                    name.requestFocus();
                    return;
                }
                String phoneStr = phone.getText().toString();
                if (phoneStr.isEmpty() || phoneStr.length() != 10 || !phoneStr.matches("\\d{10}")) {
                    phone.setError("Enter valid 10-digit phone number");
                    phone.requestFocus();
                    return;
                }
                if (passcode.getText().toString().isEmpty() || passcode.getText().toString().length() != 4) {
                    passcode.setError("Enter 4-digit passcode");
                    passcode.requestFocus();
                    return;
                }

                EmployeeModel newEmployeeModel = employeeModel;

                if (newEmployeeModel == null) {
                    newEmployeeModel = new EmployeeModel();
                    newEmployeeModel.setId(SingleTon.generateEmployeeDetailDocument());
                }
                newEmployeeModel.setName(name.getText().toString().toUpperCase());
                newEmployeeModel.setPhone(phoneStr);
                newEmployeeModel.setPasscode(passcode.getText().toString());
                newEmployeeModel.setRole(roleSpinner.getSelectedItem().toString());
                newEmployeeModel.setActive(availableCheckbox.isChecked());

                InstaFirebaseRepository.getInstance().addDataBase(AppConstants.APP_NAME + AppConstants.EMPLOYEE_COLLECTION, newEmployeeModel.getId(), newEmployeeModel, new InstaFirebaseRepository.OnFirebaseWriteListener() {
                    @Override
                    public void onSuccess(Object data) {
                        dialog.dismiss();
                        Toast.makeText(context, "New Employee Added", Toast.LENGTH_LONG).show();
                        loadEmployeeList();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });

        dialog.show();

    }
}