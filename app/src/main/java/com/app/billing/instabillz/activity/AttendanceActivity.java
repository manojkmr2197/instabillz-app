package com.app.billing.instabillz.activity;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.model.AttendanceModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendanceActivity extends AppCompatActivity {

    private Button btnLogin, btnLogout;
    private TextView tvLoginTime, tvLogoutTime, tvWorkingHours, back;
    private LinearLayout filterLL;

    private String loginTime = null;
    private String logoutTime = null;

    private Spinner spinnerEmployees;
    private String selectedEmployee = "";

    String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());


    Context context;
    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_attendance);
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
        context = AttendanceActivity.this;
        activity = AttendanceActivity.this;

        back = findViewById(R.id.attendance_back);
        btnLogin = findViewById(R.id.attendance_login_bt);
        btnLogout = findViewById(R.id.attendance_logout_bt);
        tvLoginTime = findViewById(R.id.attendance_login_tv);
        tvLogoutTime = findViewById(R.id.attendance_logout_tv);
        tvWorkingHours = findViewById(R.id.attendance_working_hours);
        filterLL = findViewById(R.id.attendance_filter_ll);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnLogin.setOnClickListener(v -> {
            if(StringUtils.isBlank(selectedEmployee)){
                Toast.makeText(context, "Select Employee Name.!", Toast.LENGTH_LONG).show();
                return;
            }
            showTimePicker(true);
        });
        btnLogout.setOnClickListener(v -> {
            if(StringUtils.isBlank(selectedEmployee)){
                Toast.makeText(context, "Select Employee Name.!", Toast.LENGTH_LONG).show();
                return;
            }

            showTimePicker(false);
        });

        spinnerEmployees = findViewById(R.id.attendance_employees_spinner);


        Intent intent = getIntent();
        String employeeName = intent.getStringExtra("employee_name");
        if (StringUtils.isNotBlank(employeeName)) {
            selectedEmployee = employeeName;
            selectedDate = StringUtils.isNotBlank(intent.getStringExtra("date")) ? intent.getStringExtra("date") : selectedDate;
            filterLL.setVisibility(View.GONE);
            loadEmployeeData();
        }else{
            loadEmployeesFromFirestore();
        }

    }


    private void showTimePicker(boolean isLogin) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hour, minute) -> {
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            String docId = selectedEmployee + "_" + selectedDate;

            if (isLogin) {
                // Create a new login document

                AttendanceModel login_attendance = new AttendanceModel(
                        selectedEmployee,
                        selectedDate,
                        selectedTime, // login time
                        null,
                        null
                );
                Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
                InstaFirebaseRepository.getInstance().addDataBase(AppConstants.APP_NAME + AppConstants.ATTENDANCE_COLLECTION, docId, login_attendance, new InstaFirebaseRepository.OnFirebaseWriteListener() {
                    @Override
                    public void onSuccess(Object data) {
                        Toast.makeText(AttendanceActivity.this, "Login saved!", Toast.LENGTH_SHORT).show();
                        tvLoginTime.setText(selectedTime);
                        btnLogin.setEnabled(false);
                        btnLogout.setEnabled(true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
                    }
                });

            } else {
                // Update existing document with logout and working hours
                String loginTime = tvLoginTime.getText().toString();
                if (loginTime.equals("--:--")) {
                    Toast.makeText(this, "Login first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String workingHours = calculateWorkingHours(loginTime, selectedTime);

                Map<String, Object> data = new HashMap<>();
                data.put("logoutTime", selectedTime);
                data.put("workingHours", workingHours);
                Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
                InstaFirebaseRepository.getInstance().updateData(AppConstants.APP_NAME + AppConstants.ATTENDANCE_COLLECTION, docId, data, new InstaFirebaseRepository.OnFirebaseWriteListener() {
                    @Override
                    public void onSuccess(Object data) {
                        Toast.makeText(AttendanceActivity.this, "Logout updated!", Toast.LENGTH_SHORT).show();
                        tvLogoutTime.setText(selectedTime);
                        tvWorkingHours.setText("Working Hours: " + workingHours);
                        btnLogout.setEnabled(false);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
                    }
                });
            }

        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

        dialog.show();
    }

    private String calculateWorkingHours(String login, String logout) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date loginDate = sdf.parse(login);
            Date logoutDate = sdf.parse(logout);

            long diff = logoutDate.getTime() - loginDate.getTime();
            long hours = diff / (1000 * 60 * 60);
            long minutes = (diff / (1000 * 60)) % 60;

            return hours + "h " + minutes + "m";
        } catch (Exception e) {
            e.printStackTrace();
            return "--";
        }
    }

    private void loadEmployeeData() {
        if(StringUtils.isBlank(selectedEmployee)){
            Toast.makeText(context, "Please choose employee.!", Toast.LENGTH_SHORT).show();
            return;
        }

        String documentId = selectedEmployee + "_" + selectedDate;
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getDetailsByDocumentId(AppConstants.APP_NAME + AppConstants.ATTENDANCE_COLLECTION, documentId, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                DocumentSnapshot doc = (DocumentSnapshot) data;

                if (doc.exists()) {
                    // Map document to AttendanceModel
                    AttendanceModel attendance = doc.toObject(AttendanceModel.class);

                    if (attendance != null) {
                        tvLoginTime.setText(attendance.getLoginTime() != null ? attendance.getLoginTime() : "--:--");
                        tvLogoutTime.setText(attendance.getLogoutTime() != null ? attendance.getLogoutTime() : "--:--");
                        tvWorkingHours.setText("Working Hours: " + (attendance.getWorkingHours() != null ? attendance.getWorkingHours() : "--"));

                        // Enable/disable buttons based on data
                        btnLogin.setEnabled(attendance.getLoginTime() == null);
                        btnLogout.setEnabled(attendance.getLoginTime() != null && attendance.getLogoutTime() == null);
                    } else {
                        resetUI();
                    }

                } else {
                    // 🔹 No document found — reset UI
                    resetUI();
                }

            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
                resetUI();
            }
        });

    }

    private void resetUI() {
        tvLoginTime.setText("");
        tvLogoutTime.setText("");
        tvWorkingHours.setText("");
        btnLogin.setEnabled(true);
        btnLogin.setAlpha(1f);
        btnLogout.setEnabled(true);
        btnLogout.setAlpha(1f);
    }


    // updated one
    private void loadEmployeesFromFirestore() {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getAllDetails(AppConstants.APP_NAME + AppConstants.EMPLOYEE_COLLECTION, "name", Query.Direction.ASCENDING, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                QuerySnapshot querySnapshot = (QuerySnapshot) data;
                List<String> employees = new ArrayList<>();
                employees.add("Select Employee");
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String name = doc.getString("name");
                    if (name != null) employees.add(name);
                }

                if (employees.size() <= 1) {
                    Toast.makeText(context, "No employees found", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, employees);
                spinnerEmployees.setAdapter(adapter);
                spinnerEmployees.setSelection(0);
                resetUI();

                spinnerEmployees.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            selectedEmployee = null;
                            resetUI();
                            return;
                        }

                        selectedEmployee = employees.get(position);
                        loadEmployeeData(); // fetch login/logout data for selected employee
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });
    }

}