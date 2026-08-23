package com.nibm.techfix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.techfix.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION_MS = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish(); // remove splash from the back stack so back button doesn't return to it
        }, SPLASH_DURATION_MS);
    }
}
