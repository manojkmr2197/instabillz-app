package com.app.billing.instabillz.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.utils.SingleTon;

public class MainActivity extends AppCompatActivity {


    private static int TIME_OUT = 2500;
    private boolean isNavigated = false;

    ImageView ownerLogo, partnershipIcon, clientLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.status_bar_color, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        ownerLogo = findViewById(R.id.ownerLogo);
        partnershipIcon = findViewById(R.id.partnershipIcon);
        clientLogo = findViewById(R.id.clientLogo);

        if (checkInternet()) {
            startSplashAnimation();
            navigateToLogin();
        } else {
            finish();
        }
    }

    private boolean checkInternet() {
        if (SingleTon.isNetworkConnected(MainActivity.this)) {
            return true;
        } else {
            Toast.makeText(MainActivity.this, "No Internet connection. Please try again .! ", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    private void startSplashAnimation() {
        // Step 1: Fade in owner logo
        ownerLogo.animate()
                .alpha(1f)
                .setDuration(500)
                .withEndAction(() -> {

                    // Step 2: Fade in partnership icon
                    partnershipIcon.animate()
                            .alpha(1f)
                            .setDuration(500)
                            .setStartDelay(200)
                            .withEndAction(() -> {

                                // Step 3: Slide up and fade in client logo
                                clientLogo.setTranslationY(100f);
                                clientLogo.animate()
                                        .translationYBy(-100f)
                                        .alpha(1f)
                                        .setDuration(500)
                                        .withEndAction(this::animationNavigate) // ✅ Only navigate once
                                        .start();

                            }).start();
                }).start();
    }

    private void navigateToLogin() {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        }, TIME_OUT);
    }

    private void animationNavigate() {
        if (isNavigated) return; // ✅ Prevent duplicate navigation
        isNavigated = true;

    }

}