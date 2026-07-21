package com.example.aiexpensemanagementapplication.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import android.widget.Toast;

import com.google.firebase.Timestamp;

import com.example.aiexpensemanagementapplication.model.NotificationPreferences;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.activity.OnBackPressedCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;

public class NotificationPreferancesActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;

    private SwitchMaterial switchEnableNotifications;
    private SwitchMaterial switchExpenseReminder;
    private SwitchMaterial switchBudgetAlert;
    private SwitchMaterial switchLargeTransaction;
    private SwitchMaterial switchSubscriptionReminder;
    private SwitchMaterial switchRenewalReminder;
    private SwitchMaterial switchWeeklyReport;
    private SwitchMaterial switchMonthlyReport;

    private TextView tvReminderTime;

    private MaterialButton btnChangeTime;
    private MaterialButton btnSave;

    private CircularProgressIndicator progressIndicator;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private DatabaseHelper databaseHelper;

    private int reminderHour = 9;
    private int reminderMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_preferances);

        initializeViews();
        initializeFirebase();
        setupToolbar();
        setupListeners();

        loadPreferences();
    }
    private void initializeViews() {

        toolbar = findViewById(R.id.toolbar);

        switchEnableNotifications = findViewById(R.id.switchEnableNotifications);
        switchExpenseReminder = findViewById(R.id.switchExpenseReminder);
        switchBudgetAlert = findViewById(R.id.switchBudgetAlert);
        switchLargeTransaction = findViewById(R.id.switchLargeTransaction);

        switchSubscriptionReminder = findViewById(R.id.switchSubscriptionReminder);
        switchRenewalReminder = findViewById(R.id.switchRenewalReminder);

        switchWeeklyReport = findViewById(R.id.switchWeeklyReport);
        switchMonthlyReport = findViewById(R.id.switchMonthlyReport);

        tvReminderTime = findViewById(R.id.tvReminderTime);

        btnChangeTime = findViewById(R.id.btnChangeTime);
        btnSave = findViewById(R.id.btnSave);

        progressIndicator = findViewById(R.id.progressIndicator);

        databaseHelper = new DatabaseHelper(this);
    }

    private void initializeFirebase() {

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            finish();
        }
    }

    private void setupToolbar() {

        toolbar.setNavigationOnClickListener(v -> finish());

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finish();
                    }
                });
    }

    private void setupListeners() {

        switchEnableNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {

            updateControls(isChecked);

        });

        btnChangeTime.setOnClickListener(v -> showTimePicker());

        btnSave.setOnClickListener(v -> savePreferences());
    }

    private void updateControls(boolean enabled) {

        switchExpenseReminder.setEnabled(enabled);
        switchBudgetAlert.setEnabled(enabled);
        switchLargeTransaction.setEnabled(enabled);

        switchSubscriptionReminder.setEnabled(enabled);
        switchRenewalReminder.setEnabled(enabled);

        switchWeeklyReport.setEnabled(enabled);
        switchMonthlyReport.setEnabled(enabled);

        btnChangeTime.setEnabled(enabled);
    }

    private void showTimePicker() {

        MaterialTimePicker picker =
                new MaterialTimePicker.Builder()
                        .setHour(reminderHour)
                        .setMinute(reminderMinute)
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setTitleText("Reminder Time")
                        .build();

        picker.addOnPositiveButtonClickListener(v -> {

            reminderHour = picker.getHour();
            reminderMinute = picker.getMinute();

            updateReminderTime();
        });

        picker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    private void updateReminderTime() {

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, reminderHour);
        calendar.set(Calendar.MINUTE, reminderMinute);

        SimpleDateFormat sdf =
                new SimpleDateFormat("hh:mm a", Locale.getDefault());

        tvReminderTime.setText(sdf.format(calendar.getTime()));
    }

    private void showLoading(boolean loading) {

        progressIndicator.setVisibility(
                loading ? View.VISIBLE : View.GONE);

        btnSave.setEnabled(!loading);

        switchEnableNotifications.setEnabled(!loading);

        updateControls(!loading &&
                switchEnableNotifications.isChecked());
    }

    private void loadPreferences() {

        showLoading(true);

        String uid = currentUser.getUid();

        // 1. Load from SQLite first
        NotificationPreferences localPreferences =
                databaseHelper.getNotificationPreferences(uid);

        if (localPreferences != null) {
            updateUI(localPreferences);
        }

        // 2. Refresh from Firestore
        db.collection("notification_preferences")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        NotificationPreferences preferences =
                                documentSnapshot.toObject(NotificationPreferences.class);

                        if (preferences != null) {

                            updateUI(preferences);

                            databaseHelper.saveNotificationPreferences(
                                    uid,
                                    preferences
                            );
                        }

                    } else {

                        // First time user
                        NotificationPreferences defaultPreferences =
                                createDefaultPreferences();

                        updateUI(defaultPreferences);

                        databaseHelper.saveNotificationPreferences(
                                uid,
                                defaultPreferences
                        );
                    }

                    showLoading(false);

                })
                .addOnFailureListener(e -> {

                    showLoading(false);

                    Toast.makeText(
                            this,
                            "Loaded local preferences",
                            Toast.LENGTH_SHORT
                    ).show();

                });
    }

    private NotificationPreferences createDefaultPreferences() {

        return new NotificationPreferences(
                true,   // notifications enabled
                true,   // expense reminder
                true,   // budget alert
                true,   // large transaction

                true,   // subscription
                true,   // renewal

                true,   // weekly
                true,   // monthly

                9,
                0,

                Timestamp.now()
        );
    }

    private void updateUI(NotificationPreferences preferences) {

        switchEnableNotifications.setChecked(
                preferences.isNotificationsEnabled());

        switchExpenseReminder.setChecked(
                preferences.isExpenseReminder());

        switchBudgetAlert.setChecked(
                preferences.isBudgetAlert());

        switchLargeTransaction.setChecked(
                preferences.isLargeTransactionAlert());

        switchSubscriptionReminder.setChecked(
                preferences.isSubscriptionReminder());

        switchRenewalReminder.setChecked(
                preferences.isRenewalReminder());

        switchWeeklyReport.setChecked(
                preferences.isWeeklyReport());

        switchMonthlyReport.setChecked(
                preferences.isMonthlyReport());

        reminderHour = preferences.getReminderHour();
        reminderMinute = preferences.getReminderMinute();

        updateReminderTime();

        updateControls(
                preferences.isNotificationsEnabled()
        );
    }

    private void savePreferences() {

        showLoading(true);

        NotificationPreferences preferences =
                new NotificationPreferences(

                        switchEnableNotifications.isChecked(),

                        switchExpenseReminder.isChecked(),

                        switchBudgetAlert.isChecked(),

                        switchLargeTransaction.isChecked(),

                        switchSubscriptionReminder.isChecked(),

                        switchRenewalReminder.isChecked(),

                        switchWeeklyReport.isChecked(),

                        switchMonthlyReport.isChecked(),

                        reminderHour,

                        reminderMinute,

                        Timestamp.now()
                );

        String uid = currentUser.getUid();

        // Save locally first
        databaseHelper.saveNotificationPreferences(
                uid,
                preferences
        );

        // Sync to Firestore
        db.collection("notification_preferences")
                .document(uid)
                .set(preferences)
                .addOnSuccessListener(unused -> {

                    showLoading(false);

                    Toast.makeText(
                            this,
                            "Notification preferences updated",
                            Toast.LENGTH_SHORT
                    ).show();

                })
                .addOnFailureListener(e -> {

                    showLoading(false);

                    Toast.makeText(
                            this,
                            "Saved locally. Cloud sync failed.",
                            Toast.LENGTH_LONG
                    ).show();

                });
    }

}