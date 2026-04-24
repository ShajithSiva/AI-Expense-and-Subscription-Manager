package com.example.aiexpensemanagementapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.aiexpensemanagementapplication.ui.dashboard.PersonalDashboardActivity;
import com.example.aiexpensemanagementapplication.ui.auth.VerifyEmailActivity;
import com.example.aiexpensemanagementapplication.ui.auth.LoginActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends BaseActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Delay for splash effect
        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserSession, SPLASH_DELAY);
    }

    private void checkUserSession() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {

            // ✅ User already logged in
            if (user.isEmailVerified()) {

                // 👉 Go to Dashboard
                startActivity(new Intent(this, PersonalDashboardActivity.class));

            } else {

                // 👉 Email not verified
                startActivity(new Intent(this, VerifyEmailActivity.class));
            }

        } else {

            // 👉 No user → go to Login
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish(); // close splash
    }
}