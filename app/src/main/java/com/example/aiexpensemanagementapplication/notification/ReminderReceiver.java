package com.example.aiexpensemanagementapplication.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String ACTION_DAILY_REMINDER =
            "com.example.aiexpensemanagementapplication.ACTION_DAILY_REMINDER";

    public static final String ACTION_SUBSCRIPTION_REMINDER =
            "com.example.aiexpensemanagementapplication.ACTION_SUBSCRIPTION_REMINDER";

    public static final String EXTRA_USER_ID =
            "user_id";

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {

        if (intent == null) {
            return;
        }

        String action = intent.getAction();

        // =====================================================
        // SUBSCRIPTION REMINDER
        // =====================================================

        if (ACTION_SUBSCRIPTION_REMINDER.equals(action)) {

            int userId =
                    intent.getIntExtra(
                            EXTRA_USER_ID,
                            -1
                    );

            if (userId <= 0) {
                return;
            }

            DatabaseHelper databaseHelper =
                    new DatabaseHelper(context);

            ReminderScheduler scheduler =
                    new ReminderScheduler(context);

            scheduler.checkSubscriptionReminder(
                    databaseHelper,
                    userId
            );

            databaseHelper.close();

            return;
        }

        // =====================================================
        // NORMAL DAILY REMINDER
        // =====================================================

        if (ACTION_DAILY_REMINDER.equals(action)) {

            String title =
                    intent.getStringExtra("title");

            String message =
                    intent.getStringExtra("message");

            String subtitle =
                    intent.getStringExtra("subtitle");

            int notificationId =
                    intent.getIntExtra(
                            "id",
                            1000
                    );

            NotificationHelper helper =
                    new NotificationHelper(context);

            helper.showNotification(
                    notificationId,
                    title,
                    message,
                    subtitle
            );
        }
    }
}