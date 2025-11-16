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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.app.billing.instabillz.model.PrinterDataModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.SharedPrefHelper;
import com.google.firebase.firestore.DocumentSnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MerchantActivity extends AppCompatActivity {

    private EditText etMerchantName;
    private Button btnProceed;

    Context context;
    Activity activity;
    SharedPrefHelper sharedPrefHelper;

    PrinterDataModel printerDataModel = new PrinterDataModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant);
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
        context = MerchantActivity.this;
        activity = MerchantActivity.this;

        etMerchantName = findViewById(R.id.etMerchantName);
        btnProceed = findViewById(R.id.btnProceed);
        sharedPrefHelper = new SharedPrefHelper(context);

        ImageView tvCall = findViewById(R.id.tvMerchantOnboarding);

        tvCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+919585905176"));
            startActivity(intent);
        });

        ImageView btnWhatsApp = findViewById(R.id.btnWhatsApp);

        btnWhatsApp.setOnClickListener(v -> {
            String phoneNumber = "+919585905176"; // your number
            String message = "Hello, I would like to connect regarding onboarding InstaBillz.";

            try {
                Uri uri = Uri.parse(
                        "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + Uri.encode(message)
                );
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp not installed!", Toast.LENGTH_SHORT).show();
            }
        });


        btnProceed.setOnClickListener(v -> {
            String name = etMerchantName.getText().toString().trim();

            if (name.isEmpty()) {
                etMerchantName.setError("Please enter merchant name");
                return;
            }

            validateMerchant(name);
        });

    }

    private void validateMerchant(String name) {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getDetailsByDocumentId(AppConstants.SHOP_COLLECTION, name, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                DocumentSnapshot doc = (DocumentSnapshot) data;
                if (doc.exists()) {
                    printerDataModel = doc.toObject(PrinterDataModel.class);

                    if (printerDataModel != null && printerDataModel.getActive()) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate subscriptionDate = LocalDate.parse(printerDataModel.getSubscriptionDate(), formatter);
                        LocalDate today = LocalDate.now();

                        // ✅ Allow only if today is on or before subscription date
                        if (!today.isAfter(subscriptionDate)) {
                            // Subscription still valid
                            sharedPrefHelper.setPrinterDetails(printerDataModel);
                            sharedPrefHelper.setAppName(name);
                            Intent intent = new Intent(context, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Subscription expired
                            showSubscriptionErrorDialog();
                        }

                    } else {
                        showSubscriptionErrorDialog();
                    }
                }else{
                    Toast.makeText(context, "No Merchant Information Found.!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });
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