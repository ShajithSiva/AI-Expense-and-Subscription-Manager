package com.example.aiexpensemanagementapplication.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.ui.auth.LoginActivity;
import com.example.aiexpensemanagementapplication.ui.auth.RegisterActivity;
import com.example.aiexpensemanagementapplication.ui.auth.VerifyEmailActivity;
import com.example.aiexpensemanagementapplication.ui.auth.VerifyMobileActivity;
import com.example.aiexpensemanagementapplication.ui.auth.WelcomeActivity;
import com.example.aiexpensemanagementapplication.ui.auth.CreatePasswordActivity;
import com.example.aiexpensemanagementapplication.ui.dashboard.DashboardActivity;
import com.example.aiexpensemanagementapplication.ui.onboarding.OnboardingActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends BaseActivity {

    private static final String PREFS_NAME = "ExpenseVaultPrefs";
    private static final String KEY_ONBOARDING_COMPLETED =
            "onboarding_completed";

    private static final long SPLASH_DELAY = 2000L;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean splashTimeCompleted = false;
    private boolean navigationDecisionReady = false;
    private boolean navigationStarted = false;

    private Intent pendingIntent;

    private final Runnable splashRunnable = () -> {
        splashTimeCompleted = true;
        navigateIfReady();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display your custom splash layout.
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Keep the custom splash visible for at least 2 seconds.
        handler.postDelayed(splashRunnable, SPLASH_DELAY);

        SharedPreferences preferences = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        boolean onboardingCompleted = preferences.getBoolean(
                KEY_ONBOARDING_COMPLETED,
                false
        );

        if (!onboardingCompleted) {
            prepareNavigation(OnboardingActivity.class);
        } else {
            checkUserSession();
        }
    }

    private void checkUserSession() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            prepareNavigation(WelcomeActivity.class);
            return;
        }

        user.reload().addOnCompleteListener(task -> {

            if (isFinishing() || isDestroyed()) {
                return;
            }

            if (!task.isSuccessful()) {
                prepareNavigation(WelcomeActivity.class);
                return;
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser == null) {
                prepareNavigation(WelcomeActivity.class);
                return;
            }

            if (!currentUser.isEmailVerified()) {

                Intent intent = new Intent(
                        SplashActivity.this,
                        VerifyEmailActivity.class
                );

                intent.putExtra("email", currentUser.getEmail());

                prepareNavigation(intent);
                return;
            }

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
                            prepareNavigation(LoginActivity.class);
                        }
                    });
        });
    }

    private void checkFirestoreStatus(
            @NonNull DocumentSnapshot document) {

        if (!document.exists()) {
            prepareNavigation(RegisterActivity.class);
            return;
        }

        Boolean phoneVerified =
                document.getBoolean("phoneVerified");

        Boolean accountCompleted =
                document.getBoolean("accountCompleted");

        if (phoneVerified == null || !phoneVerified) {
            prepareNavigation(VerifyMobileActivity.class);
            return;
        }

        if (accountCompleted == null || !accountCompleted) {
            prepareNavigation(CreatePasswordActivity.class);
            return;
        }

        prepareNavigation(DashboardActivity.class);
    }

    private void prepareNavigation(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        prepareNavigation(intent);
    }

    private void prepareNavigation(Intent intent) {

        if (navigationStarted || isFinishing() || isDestroyed()) {
            return;
        }

        pendingIntent = intent;
        navigationDecisionReady = true;

        navigateIfReady();
    }

    private void navigateIfReady() {

        if (!splashTimeCompleted
                || !navigationDecisionReady
                || navigationStarted
                || pendingIntent == null
                || isFinishing()
                || isDestroyed()) {
            return;
        }

        navigationStarted = true;

        startActivity(pendingIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(splashRunnable);
        super.onDestroy();
    }
}