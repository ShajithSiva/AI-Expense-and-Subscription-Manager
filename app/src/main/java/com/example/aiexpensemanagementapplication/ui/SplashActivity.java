package com.example.aiexpensemanagementapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.aiexpensemanagementapplication.ui.auth.CreatePasswordActivity;
import com.example.aiexpensemanagementapplication.ui.auth.LoginActivity;
import com.example.aiexpensemanagementapplication.ui.auth.RegisterActivity;
import com.example.aiexpensemanagementapplication.ui.auth.VerifyEmailActivity;
import com.example.aiexpensemanagementapplication.ui.auth.VerifyMobileActivity;
import com.example.aiexpensemanagementapplication.ui.dashboard.PersonalDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends BaseActivity {

    private static final int SPLASH_DELAY = 2000;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(
                this::checkUserSession,
                SPLASH_DELAY
        );
    }

    private void checkUserSession() {

        FirebaseUser user = mAuth.getCurrentUser();

        // No logged in user
        if (user == null) {

            startActivity(new Intent(
                    SplashActivity.this,
                    RegisterActivity.class));

            finish();
            return;
        }

        user.reload().addOnCompleteListener(task -> {

            if (!user.isEmailVerified()) {

                Intent intent = new Intent(
                        SplashActivity.this,
                        VerifyEmailActivity.class);

                intent.putExtra("email", user.getEmail());

                startActivity(intent);
                finish();
                return;
            }

            firestore.collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(this::checkFirestoreStatus)
                    .addOnFailureListener(e -> {

                        startActivity(new Intent(
                                SplashActivity.this,
                                LoginActivity.class));

                        finish();
                    });

        });
    }

    private void checkFirestoreStatus(@NonNull DocumentSnapshot document) {

        if (!document.exists()) {

            startActivity(new Intent(
                    this,
                    RegisterActivity.class));

            finish();
            return;
        }

        Boolean phoneVerified = document.getBoolean("phoneVerified");
        Boolean accountCompleted = document.getBoolean("accountCompleted");

        if (phoneVerified == null)
            phoneVerified = false;

        if (accountCompleted == null)
            accountCompleted = false;

        // Phone not verified
        if (!phoneVerified) {

            startActivity(new Intent(
                    this,
                    VerifyMobileActivity.class));

            finish();
            return;
        }

        // Password not created
        if (!accountCompleted) {

            startActivity(new Intent(
                    this,
                    CreatePasswordActivity.class));

            finish();
            return;
        }

        // Everything completed
        startActivity(new Intent(
                this,
                PersonalDashboardActivity.class));

        finish();
    }
}