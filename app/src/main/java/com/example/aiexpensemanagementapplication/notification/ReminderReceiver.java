package com.example.aiexpensemanagementapplication.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int notificationId = intent.getIntExtra("id", 1000);

        NotificationHelper helper = new NotificationHelper(context);

        helper.showNotification(
                notificationId,
                title,
                message
        );

    }
}