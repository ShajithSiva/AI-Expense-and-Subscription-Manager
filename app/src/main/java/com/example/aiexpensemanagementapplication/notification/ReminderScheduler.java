package com.example.aiexpensemanagementapplication.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.model.Budget;
import com.example.aiexpensemanagementapplication.model.Subscription;

import java.util.ArrayList;
import java.util.Calendar;

public class ReminderScheduler {

    private final Context context;

    private static final int SUBSCRIPTION_ALARM_REQUEST_CODE =
            2001;

    public ReminderScheduler(Context context) {
        this.context = context.getApplicationContext();
    }

    /*--------------------------------------------------
     * Daily Reminder
     *--------------------------------------------------*/

    public void scheduleDailyReminder(int hour, int minute) {

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
                "subtitle",
                "Track your daily spending"
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
            calendar.add(
                    Calendar.DAY_OF_MONTH,
                    1
            );
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
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        Intent intent =
                new Intent(
                        context,
                        ReminderReceiver.class
                );

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

    /*--------------------------------------------------
     * Budget Reminder
     *--------------------------------------------------*/

    public void checkBudgetReminder(
            DatabaseHelper db,
            int userId
    ) {

        Budget budget =
                db.getBudgetSettings(userId);

        if (budget == null) {
            return;
        }

        double budgetAmount =
                budget.getMonthlyBudget();

        if (budgetAmount <= 0) {
            return;
        }

        double totalExpense =
                db.getTotalExpense(userId);

        double percentage =
                (totalExpense / budgetAmount) * 100;

        NotificationPreferences prefs =
                new NotificationPreferences(context);

        NotificationHelper helper =
                new NotificationHelper(context);

        if (!prefs.isBudgetNotificationShownToday()) {

            if (percentage >= 100) {

                helper.showNotification(
                        NotificationConstants.BUDGET_WARNING_ID,
                        "Budget Exceeded",
                        "You have exceeded your monthly budget.",
                        "Current Budget Status"
                );

                prefs.saveBudgetNotificationDate();

            } else if (percentage >= 80) {

                helper.showNotification(
                        NotificationConstants.BUDGET_WARNING_ID,
                        "Budget Warning",
                        "You have used over 80% of your monthly budget.",
                        "Spend Carefully"
                );

                prefs.saveBudgetNotificationDate();
            }
        }
    }

    /*--------------------------------------------------
     * Category Budget Reminder
     *--------------------------------------------------*/

    public void checkCategoryBudgetReminder(
            DatabaseHelper db,
            int userId,
            String categoryName
    ) {

        double budget =
                db.getBudgetByCategory(
                        userId,
                        categoryName
                );

        if (budget <= 0) {
            return;
        }

        double expense =
                db.getCategoryExpense(
                        userId,
                        categoryName
                );

        double percentage =
                (expense / budget) * 100;

        NotificationHelper helper =
                new NotificationHelper(context);

        if (percentage >= 100) {

            helper.showNotification(
                    NotificationConstants.BUDGET_WARNING_ID,
                    "Budget Exceeded",
                    "You have exceeded your " +
                            categoryName +
                            " budget.",
                    categoryName +
                            " Budget"
            );

        } else if (percentage >= 90) {

            helper.showNotification(
                    NotificationConstants.BUDGET_WARNING_ID,
                    "Budget Warning",
                    "You have used " +
                            (int) percentage +
                            "% of your " +
                            categoryName +
                            " budget.",
                    categoryName +
                            " Budget"
            );
        }
    }

    /*--------------------------------------------------
     * Subscription Reminder
     *--------------------------------------------------*/

    public void checkSubscriptionReminder(
            DatabaseHelper db,
            int userId
    ) {

        /*
         * IMPORTANT:
         *
         * The SharedPreferences class above only prevents
         * duplicate notifications during the same day.
         *
         * The database NotificationPreferences model stores
         * whether the user actually wants subscription
         * reminders.
         */

        com.example.aiexpensemanagementapplication.model.NotificationPreferences
                userPreferences =
                db.getNotificationPreferences(
                        String.valueOf(userId)
                );

        /*
         * If the user has disabled subscription reminders,
         * do not create any subscription notification.
         */
        if (userPreferences != null &&
                !userPreferences.isSubscriptionReminder()) {

            return;
        }

        ArrayList<Subscription> subscriptions =
                db.getUpcomingSubscriptions(userId);

        if (subscriptions == null ||
                subscriptions.isEmpty()) {

            return;
        }

        /*
         * SharedPreferences is used only for preventing
         * duplicate notifications on the same day.
         */
        NotificationPreferences notificationState =
                new NotificationPreferences(context);

        NotificationHelper helper =
                new NotificationHelper(context);

        if (!notificationState.isSubscriptionNotificationShownToday()) {

            for (Subscription subscription : subscriptions) {

                helper.showNotification(
                        NotificationConstants.SUBSCRIPTION_REMINDER_ID
                                + subscription.getSubscriptionId(),

                        "Subscription Reminder",

                        subscription.getServiceName() +
                                " renews soon.",

                        "Renewal Date: " +
                                subscription.getNextBillingDate()
                );
            }

            notificationState.saveSubscriptionNotificationDate();
        }
    }

    /*--------------------------------------------------
     * Renewal Reminder
     *--------------------------------------------------*/

    public void checkRenewalReminder(
            DatabaseHelper db,
            int userId
    ) {

        com.example.aiexpensemanagementapplication.model.NotificationPreferences
                userPreferences =
                db.getNotificationPreferences(
                        String.valueOf(userId)
                );

        /*
         * Respect the Renewal Reminders switch.
         */
        if (userPreferences != null &&
                !userPreferences.isRenewalReminder()) {

            return;
        }

        ArrayList<Subscription> subscriptions =
                db.getUpcomingSubscriptions(userId);

        if (subscriptions == null ||
                subscriptions.isEmpty()) {

            return;
        }

        NotificationHelper helper =
                new NotificationHelper(context);

        /*
         * Renewal alerts are generated from the upcoming
         * subscription records.
         */
        for (Subscription subscription : subscriptions) {

            helper.showNotification(
                    NotificationConstants.SUBSCRIPTION_REMINDER_ID
                            + subscription.getSubscriptionId(),

                    "Subscription Renewal",

                    subscription.getServiceName() +
                            " is due for renewal.",

                    "Renewal Date: " +
                            subscription.getNextBillingDate()
            );
        }
    }

    /*--------------------------------------------------
     * Monthly Report Reminder
     *--------------------------------------------------*/

    public void showMonthlyReportReminder() {

        Calendar calendar =
                Calendar.getInstance();

        // Show only on the first day of the month
        if (calendar.get(Calendar.DAY_OF_MONTH) != 1) {
            return;
        }

        NotificationPreferences prefs =
                new NotificationPreferences(context);

        NotificationHelper helper =
                new NotificationHelper(context);

        if (!prefs.isMonthlyReportShownToday()) {

            helper.showNotification(
                    NotificationConstants.MONTHLY_REPORT_ID,
                    "Monthly Report Ready",
                    "Your monthly financial report is now available.",
                    "Tap to view your financial insights"
            );

            prefs.saveMonthlyReportDate();
        }
    }

    /*--------------------------------------------------
     * Schedule Subscription Reminder
     *--------------------------------------------------*/

    public void scheduleSubscriptionReminder(
            int userId,
            int hour,
            int minute
    ) {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            return;
        }

        Intent intent =
                new Intent(
                        context,
                        ReminderReceiver.class
                );

        intent.setAction(
                ReminderReceiver.ACTION_SUBSCRIPTION_REMINDER
        );

        intent.putExtra(
                ReminderReceiver.EXTRA_USER_ID,
                userId
        );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        SUBSCRIPTION_ALARM_REQUEST_CODE,
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

        calendar.set(
                Calendar.MILLISECOND,
                0
        );

        /*
         * If today's selected time has already passed,
         * schedule it for tomorrow.
         */
        if (calendar.before(Calendar.getInstance())) {

            calendar.add(
                    Calendar.DAY_OF_MONTH,
                    1
            );
        }

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }


    /*--------------------------------------------------
     * Cancel Subscription Reminder
     *--------------------------------------------------*/

    public void cancelSubscriptionReminder() {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            return;
        }

        Intent intent =
                new Intent(
                        context,
                        ReminderReceiver.class
                );

        intent.setAction(
                ReminderReceiver.ACTION_SUBSCRIPTION_REMINDER
        );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        SUBSCRIPTION_ALARM_REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        alarmManager.cancel(pendingIntent);

        pendingIntent.cancel();
    }

    /*--------------------------------------------------
     * Apply Subscription Notification Settings
     *--------------------------------------------------*/

    public void applySubscriptionSettings(
            DatabaseHelper db,
            int userId
    ) {

        com.example.aiexpensemanagementapplication.model.NotificationPreferences
                preferences =
                db.getNotificationPreferences(
                        String.valueOf(userId)
                );

        if (preferences == null) {

            scheduleSubscriptionReminder(
                    userId,
                    9,
                    0
            );

            return;
        }

        boolean subscriptionEnabled =
                preferences.isSubscriptionReminder();

        boolean renewalEnabled =
                preferences.isRenewalReminder();

        /*
         * If both subscription and renewal notifications
         * are disabled, there is no reason to keep the
         * daily subscription alarm running.
         */
        if (!subscriptionEnabled &&
                !renewalEnabled) {

            cancelSubscriptionReminder();

            return;
        }

        scheduleSubscriptionReminder(
                userId,
                preferences.getReminderHour(),
                preferences.getReminderMinute()
        );
    }
}