package com.example.aiexpensemanagementapplication.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class ReminderScheduler {

    private final Context context;

    public ReminderScheduler(Context context) {

        this.context = context;

    }

    public void scheduleDailyReminder(
            int hour,
            int minute
    ) {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent =
                new Intent(context, ReminderReceiver.class);

        intent.putExtra(
                "title",
                "Daily Expense Reminder"
        );

        intent.putExtra(
                "message",
                "Don't forget to record today's expenses."
        );

        intent.putExtra(
                "id",
                NotificationConstants.DAILY_REMINDER_ID
        );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        NotificationConstants.DAILY_REMINDER_ID,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        Calendar calendar =
                Calendar.getInstance();

        calendar.set(
                Calendar.HOUR_OF_DAY,
                hour
        );

        calendar.set(
                Calendar.MINUTE,
                minute
        );

        calendar.set(
                Calendar.SECOND,
                0
        );

        if (calendar.before(Calendar.getInstance())) {

            calendar.add(Calendar.DAY_OF_MONTH, 1);

        }

        if (alarmManager != null) {

            alarmManager.setRepeating(

                    AlarmManager.RTC_WAKEUP,

                    calendar.getTimeInMillis(),

                    AlarmManager.INTERVAL_DAY,

                    pendingIntent

            );

        }

    }

    public void cancelDailyReminder() {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent =
                new Intent(context, ReminderReceiver.class);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(

                        context,

                        NotificationConstants.DAILY_REMINDER_ID,

                        intent,

                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE

                );

        if (alarmManager != null) {

            alarmManager.cancel(pendingIntent);

        }

    }

}