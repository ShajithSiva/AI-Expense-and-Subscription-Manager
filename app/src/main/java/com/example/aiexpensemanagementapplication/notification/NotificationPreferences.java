package com.example.aiexpensemanagementapplication.notification;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationPreferences {

    private static final String PREF_NAME = "notification_preferences";

    private static final String KEY_BUDGET = "budget_warning";
    private static final String KEY_SUBSCRIPTION = "subscription_reminder";
    private static final String KEY_MONTHLY = "monthly_report";
    private static final String KEY_DAILY = "daily_reminder";

    private final SharedPreferences preferences;

    public NotificationPreferences(Context context) {
        preferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    private String getToday() {
        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());
    }

    /* ===============================
       Budget Warning
       =============================== */

    public boolean isBudgetNotificationShownToday() {
        return getToday().equals(
                preferences.getString(KEY_BUDGET, "")
        );
    }

    public void saveBudgetNotificationDate() {
        preferences.edit()
                .putString(KEY_BUDGET, getToday())
                .apply();
    }

    /* ===============================
       Subscription Reminder
       =============================== */

    public boolean isSubscriptionNotificationShownToday() {
        return getToday().equals(
                preferences.getString(KEY_SUBSCRIPTION, "")
        );
    }

    public void saveSubscriptionNotificationDate() {
        preferences.edit()
                .putString(KEY_SUBSCRIPTION, getToday())
                .apply();
    }

    /* ===============================
       Monthly Report
       =============================== */

    public boolean isMonthlyReportShownToday() {
        return getToday().equals(
                preferences.getString(KEY_MONTHLY, "")
        );
    }

    public void saveMonthlyReportDate() {
        preferences.edit()
                .putString(KEY_MONTHLY, getToday())
                .apply();
    }

    /* ===============================
       Daily Reminder
       =============================== */

    public boolean isDailyReminderShownToday() {
        return getToday().equals(
                preferences.getString(KEY_DAILY, "")
        );
    }

    public void saveDailyReminderDate() {
        preferences.edit()
                .putString(KEY_DAILY, getToday())
                .apply();
    }

    /* ===============================
       Optional Reset
       =============================== */

    public void clearAll() {
        preferences.edit().clear().apply();
    }
}
