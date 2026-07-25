package com.example.aiexpensemanagementapplication.notification;

import com.example.aiexpensemanagementapplication.notification.NotificationConstants;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.aiexpensemanagementapplication.R;

public class NotificationHelper {

    private final Context context;

    public NotificationHelper(Context context){

        this.context = context;

        createChannel();

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

    }

}