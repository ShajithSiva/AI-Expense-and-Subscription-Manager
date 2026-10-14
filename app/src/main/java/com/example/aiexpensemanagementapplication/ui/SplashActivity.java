
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
import com.example.aiexpensemanagementapplication.ui.auth.WelcomeActivity;
import com.example.aiexpensemanagementapplication.ui.dashboard.DashboardActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends BaseActivity {

    private static final int SPLASH_DELAY = 2000;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        handler.postDelayed(this::checkUserSession, SPLASH_DELAY);
    }

    private void checkUserSession() {

        FirebaseUser user = mAuth.getCurrentUser();

        // New user: open ExpenseVault Welcome Screen
        if (user == null) {
            navigateTo(WelcomeActivity.class);
            return;
        }

        user.reload().addOnCompleteListener(task -> {

            if (isFinishing() || isDestroyed()) {
                return;
            }

            if (!task.isSuccessful()) {
                navigateTo(WelcomeActivity.class);
                return;
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser == null) {
                navigateTo(WelcomeActivity.class);
                return;
            }

            // Email verification
            if (!currentUser.isEmailVerified()) {

                Intent intent = new Intent(
                        SplashActivity.this,
                        VerifyEmailActivity.class
                );

                intent.putExtra("email", currentUser.getEmail());
                navigate(intent);
                return;
            }

            // Read user account status
            firestore.collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (!isFinishing() && !isDestroyed()) {
                            checkFirestoreStatus(document);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!isFinishing() && !isDestroyed()) {
                            navigateTo(LoginActivity.class);
                        }
                    });
        });
    }

    private void checkFirestoreStatus(
            @NonNull DocumentSnapshot document) {

        if (!document.exists()) {
            navigateTo(RegisterActivity.class);
            return;
        }

        Boolean phoneVerified = document.getBoolean("phoneVerified");
        Boolean accountCompleted = document.getBoolean("accountCompleted");

        if (phoneVerified == null) {
            phoneVerified = false;
        }

        if (accountCompleted == null) {
            accountCompleted = false;
        }

        // Mobile verification required
        if (!phoneVerified) {
            navigateTo(VerifyMobileActivity.class);
            return;
        }

        // Account setup incomplete
        if (!accountCompleted) {
            navigateTo(CreatePasswordActivity.class);
            return;
        }

        // Account completed
        navigateTo(DashboardActivity.class);
    }

    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        navigate(intent);
    }

    private void navigate(Intent intent) {
        handler.removeCallbacksAndMessages(null);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
