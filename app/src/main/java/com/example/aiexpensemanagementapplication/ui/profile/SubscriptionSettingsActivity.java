package com.example.aiexpensemanagementapplication.ui.profile;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.model.NotificationPreferences;
import com.example.aiexpensemanagementapplication.notification.ReminderScheduler;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Locale;

public class SubscriptionSettingsActivity extends AppCompatActivity {

    // =====================================================
    // VIEWS
    // =====================================================

    private ImageButton btnBack;

    private SwitchMaterial switchSubscriptionReminder;
    private SwitchMaterial switchRenewalReminder;

    private TextView tvReminderTime;

    private MaterialButton btnSaveSettings;

    // =====================================================
    // DATABASE / AUTH
    // =====================================================

    private DatabaseHelper databaseHelper;

    private FirebaseAuth firebaseAuth;

    private int userId = -1;

    // =====================================================
    // REMINDER TIME
    // =====================================================

    private int reminderHour = 9;
    private int reminderMinute = 0;

    // =====================================================
    // LIFECYCLE
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_subscription_settings
        );

        initializeViews();

        initializeDatabase();

        resolveUser();

        setupListeners();

        loadSettings();
    }

    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeViews() {

        btnBack =
                findViewById(R.id.btnBack);

        switchSubscriptionReminder =
                findViewById(
                        R.id.switchSubscriptionReminder
                );

        switchRenewalReminder =
                findViewById(
                        R.id.switchRenewalReminder
                );

        tvReminderTime =
                findViewById(
                        R.id.tvReminderTime
                );

        btnSaveSettings =
                findViewById(
                        R.id.btnSaveSettings
                );
    }

    // =====================================================
    // INITIALIZE DATABASE
    // =====================================================

    private void initializeDatabase() {

        databaseHelper =
                new DatabaseHelper(this);

        firebaseAuth =
                FirebaseAuth.getInstance();
    }

    // =====================================================
    // RESOLVE CURRENT USER
    // =====================================================

    private void resolveUser() {

        FirebaseUser firebaseUser =
                firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {

            Toast.makeText(
                    this,
                    "User session not found.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        userId =
                databaseHelper.getUserIdByFirebaseUid(
                        firebaseUser.getUid()
                );

        if (userId <= 0) {

            Toast.makeText(
                    this,
                    "Unable to load user information.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }

    // =====================================================
    // LISTENERS
    // =====================================================

    private void setupListeners() {

        // Back
        btnBack.setOnClickListener(v -> finish());

        // Reminder time
        tvReminderTime.setOnClickListener(
                v -> showTimePicker()
        );

        // Save
        btnSaveSettings.setOnClickListener(
                v -> saveSettings()
        );
    }

    // =====================================================
    // LOAD SETTINGS
    // =====================================================

    private void loadSettings() {

        if (userId <= 0) {
            return;
        }

        NotificationPreferences preferences =
                databaseHelper.getNotificationPreferences(
                        String.valueOf(userId)
                );

        // -------------------------------------------------
        // No saved preferences yet
        // -------------------------------------------------

        if (preferences == null) {

            switchSubscriptionReminder.setChecked(true);

            switchRenewalReminder.setChecked(true);

            reminderHour = 9;
            reminderMinute = 0;

            updateReminderTimeText();

            return;
        }

        // -------------------------------------------------
        // Existing preferences
        // -------------------------------------------------

        switchSubscriptionReminder.setChecked(
                preferences.isSubscriptionReminder()
        );

        switchRenewalReminder.setChecked(
                preferences.isRenewalReminder()
        );

        reminderHour =
                preferences.getReminderHour();

        reminderMinute =
                preferences.getReminderMinute();

        updateReminderTimeText();
    }

    // =====================================================
    // TIME PICKER
    // =====================================================

    private void showTimePicker() {

        TimePickerDialog dialog =
                new TimePickerDialog(
                        this,
                        (view, hourOfDay, minute) -> {

                            reminderHour =
                                    hourOfDay;

                            reminderMinute =
                                    minute;

                            updateReminderTimeText();
                        },
                        reminderHour,
                        reminderMinute,
                        false
                );

        dialog.show();
    }

    // =====================================================
    // UPDATE TIME TEXT
    // =====================================================

    private void updateReminderTimeText() {

        Calendar calendar =
                Calendar.getInstance();

        calendar.set(
                Calendar.HOUR_OF_DAY,
                reminderHour
        );

        calendar.set(
                Calendar.MINUTE,
                reminderMinute
        );

        String formattedTime =
                new java.text.SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                ).format(
                        calendar.getTime()
                );

        tvReminderTime.setText(
                formattedTime
        );
    }

    // =====================================================
    // SAVE SETTINGS
    // =====================================================

    private void saveSettings() {

        if (userId <= 0) {

            Toast.makeText(
                    this,
                    "Unable to identify user.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // -------------------------------------------------
        // Load existing preferences
        // -------------------------------------------------

        NotificationPreferences preferences =
                databaseHelper.getNotificationPreferences(
                        String.valueOf(userId)
                );

        if (preferences == null) {

            preferences =
                    createDefaultPreferences();
        }

        // -------------------------------------------------
        // Update subscription settings
        // -------------------------------------------------

        preferences.setSubscriptionReminder(
                switchSubscriptionReminder.isChecked()
        );

        preferences.setRenewalReminder(
                switchRenewalReminder.isChecked()
        );

        preferences.setReminderHour(
                reminderHour
        );

        preferences.setReminderMinute(
                reminderMinute
        );

        // -------------------------------------------------
        // Save
        // -------------------------------------------------

        boolean success =
                databaseHelper.saveNotificationPreferences(
                        String.valueOf(userId),
                        preferences
                );

        if (success) {

            ReminderScheduler scheduler =
                    new ReminderScheduler(this);

            scheduler.applySubscriptionSettings(
                    databaseHelper,
                    userId
            );

            Toast.makeText(
                    this,
                    "Subscription settings saved.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            Toast.makeText(
                    this,
                    "Failed to save subscription settings.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =====================================================
    // DEFAULT PREFERENCES
    // =====================================================

    private NotificationPreferences createDefaultPreferences() {

        NotificationPreferences preferences =
                new NotificationPreferences();

        preferences.setNotificationsEnabled(true);

        preferences.setExpenseReminder(true);

        preferences.setBudgetAlert(true);

        preferences.setLargeTransactionAlert(true);

        preferences.setSubscriptionReminder(true);

        preferences.setRenewalReminder(true);

        preferences.setWeeklyReport(false);

        preferences.setMonthlyReport(false);

        preferences.setReminderHour(9);

        preferences.setReminderMinute(0);

        return preferences;
    }
}