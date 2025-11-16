package com.app.billing.instabillz.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.model.AttendanceModel;
import com.app.billing.instabillz.model.EmployeeModel;
import com.app.billing.instabillz.model.PrinterDataModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.MoveNextWatcher;
import com.app.billing.instabillz.utils.SharedPrefHelper;
import com.app.billing.instabillz.utils.SingleTon;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LoginActivity extends AppCompatActivity {

    EditText etPhoneNumber, etDigit1, etDigit2, etDigit3, etDigit4;
    Button btnLogin;
    TextView tvDifferentUser;

    Context context;
    Activity activity;
    SharedPrefHelper sharedPrefHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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

        context = LoginActivity.this;
        activity = LoginActivity.this;

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etDigit1 = findViewById(R.id.etDigit1);
        etDigit2 = findViewById(R.id.etDigit2);
        etDigit3 = findViewById(R.id.etDigit3);
        etDigit4 = findViewById(R.id.etDigit4);
        btnLogin = findViewById(R.id.btnLogin);
        tvDifferentUser = findViewById(R.id.tvDifferentUser);

        ImageView logo1 = findViewById(R.id.login_app_logo);
        ImageView logo2 = findViewById(R.id.login_partnership);
        ImageView logo3 = findViewById(R.id.login_client_logo);

        Animation popAnim = AnimationUtils.loadAnimation(this, R.anim.logo_pop);

        logo1.startAnimation(popAnim);
        logo2.startAnimation(popAnim);
        logo3.startAnimation(popAnim);

        sharedPrefHelper = new SharedPrefHelper(context);

        // Load saved phone number
        String savedPhone = sharedPrefHelper.getSystemUserPhone();
        if (StringUtils.isNotBlank(savedPhone)) {
            etPhoneNumber.setText(savedPhone);
            etPhoneNumber.setEnabled(false); // disable editing
        }

        // Move focus automatically between digits
        //setupPasscodeFocus();
        etDigit1.addTextChangedListener(new MoveNextWatcher(etDigit1, etDigit2));
        etDigit2.addTextChangedListener(new MoveNextWatcher(etDigit2, etDigit3));
        etDigit3.addTextChangedListener(new MoveNextWatcher(etDigit3, etDigit4));

        enableBackspaceNavigation(etDigit1, etDigit2, etDigit3, etDigit4);

        etDigit4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    SingleTon.hideKeyboard(context, activity);
                    etDigit4.clearFocus(); // 👈 remove cursor
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnLogin.setOnClickListener(v -> handleLogin());
        tvDifferentUser.setOnClickListener(v -> clearSharedPref());

    }

    private void handleLogin() {
        String phone = etPhoneNumber.getText().toString().trim();
        String passcode = etDigit1.getText().toString() + etDigit2.getText().toString()
                + etDigit3.getText().toString() + etDigit4.getText().toString();

        if (phone.length() != 10) {
            Toast.makeText(this, "Enter valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (passcode.length() != 4) {
            Toast.makeText(this, "Enter 4-digit passcode", Toast.LENGTH_SHORT).show();
            return;
        }

        validateUser(phone, passcode);

    }

    private void validateUser(String phone, String passcode) {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().userLogin(phone, passcode, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                QuerySnapshot doc = (QuerySnapshot) data;

                if (!doc.isEmpty() && doc.getDocuments().get(0).exists()) {
                    EmployeeModel model = doc.getDocuments().get(0).toObject(EmployeeModel.class);

                    if (model == null || !model.getActive()) {
                        Toast.makeText(context, "You are inactive Now. Please contact admin.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(context, "Login Success.!", Toast.LENGTH_SHORT).show();
                    // Save phone number first time
                    sharedPrefHelper.setSystemUserDetails(model);
                    // Go to Home screen

                    Intent intent = new Intent(context, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(context, "⚠️ Invalid passcode. Please try again.", Toast.LENGTH_SHORT).show();
                    etDigit1.setText("");
                    etDigit2.setText("");
                    etDigit3.setText("");
                    etDigit4.setText("");
                    etDigit1.requestFocus();
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    private void clearSharedPref() {
        sharedPrefHelper.clearAllDetails();
        Intent intent = new Intent(LoginActivity.this, MerchantActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void enableBackspaceNavigation(EditText... editTexts) {
        for (int i = 0; i < editTexts.length; i++) {
            EditText current = editTexts[i];
            EditText previous = (i > 0) ? editTexts[i - 1] : null;

            current.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_DEL &&
                        current.getText().toString().isEmpty() &&
                        previous != null) {
                    previous.requestFocus();
                    previous.setSelection(previous.getText().length());
                    return true;
                }
                return false;
            });
        }
    }


    private void showSubscriptionErrorDialog() {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_subscription_error);
        dialog.setCancelable(false);

        // Transparent background with rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvPhoneNumber = dialog.findViewById(R.id.tvPhoneNumber);
        Button btnOk = dialog.findViewById(R.id.btnOk);

        // Clickable phone number -> opens dialer
        tvPhoneNumber.setOnClickListener(v -> {
            String phone = tvPhoneNumber.getText().toString().trim();
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
            startActivity(intent);
        });

        // OK button -> close app
        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            finishAffinity(); // closes all activities
            System.exit(0); // ensures app termination
        });

        dialog.show();
    }

}