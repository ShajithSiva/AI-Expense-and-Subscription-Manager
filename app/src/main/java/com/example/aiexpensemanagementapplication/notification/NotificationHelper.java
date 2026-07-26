package com.example.aiexpensemanagementapplication.notification;

import com.example.aiexpensemanagementapplication.notification.NotificationConstants;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.aiexpensemanagementapplication.R;

public class NotificationHelper {

    private final Context context;

    private final DatabaseHelper databaseHelper;

    public NotificationHelper(Context context) {

        this.context = context;

        databaseHelper = new DatabaseHelper(context);

        createNotificationChannel();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    NotificationConstants.CHANNEL_ID,
                    NotificationConstants.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription(NotificationConstants.CHANNEL_DESCRIPTION);

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void createChannel(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel channel =
                    new NotificationChannel(

                            NotificationConstants.CHANNEL_ID,

                            NotificationConstants.CHANNEL_NAME,

                            NotificationManager.IMPORTANCE_HIGH

                    );

            channel.setDescription(
                    NotificationConstants.CHANNEL_DESCRIPTION
            );

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            if(manager != null){

                manager.createNotificationChannel(channel);

            }

        }

    }

    public void showNotification(

            int id,

            String title,

            String message

    ){

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        NotificationConstants.CHANNEL_ID
                )
                        .setSmallIcon(R.drawable.notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                return;

            }

        }

        NotificationManagerCompat.from(context)
                .notify(id, builder.build());

        String date = new SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
        ).format(new Date());

        String time = new SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
        ).format(new Date());

        databaseHelper.insertNotification(
                title,
                message,
                getNotificationType(id),
                date,
                time
        );

    }

    private String getNotificationType(int id) {

        switch (id) {

            case NotificationConstants.BUDGET_WARNING_ID:
                return "budget";

            case NotificationConstants.SUBSCRIPTION_REMINDER_ID:
                return "subscription";

            case NotificationConstants.MONTHLY_REPORT_ID:
                return "report";

            case NotificationConstants.DAILY_REMINDER_ID:
                return "daily";

            default:
                return "general";
        }
    }

}