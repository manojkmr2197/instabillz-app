package com.app.billing.instabillz.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.utils.SharedPrefHelper;

import org.apache.commons.lang3.StringUtils;

public class SplashActivity extends AppCompatActivity {

    private static int TIME_OUT = 2000;

    SharedPrefHelper sharedPrefHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
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

        ImageView logo = findViewById(R.id.logo);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.logo_pop);
        logo.startAnimation(anim);

        TextView tv = findViewById(R.id.textTagline);
        animateText(tv, "Complete your business with ease", 30); // 60ms per letter

        new Handler().postDelayed(() -> {
            sharedPrefHelper = new SharedPrefHelper(this);
            if (StringUtils.isBlank(sharedPrefHelper.getAppName())) {
                Intent intent = new Intent(SplashActivity.this, MerchantActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }else{
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }, TIME_OUT);

    }

    public void animateText(TextView textView, String text, long delay) {
        textView.setText("");
        for (int i = 0; i <= text.length(); i++) {
            final int index = i;
            textView.postDelayed(() -> {
                textView.setText(text.substring(0, index));
            }, i * delay);
        }
    }

}