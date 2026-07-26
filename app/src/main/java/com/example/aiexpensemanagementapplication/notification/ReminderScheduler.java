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

    public ReminderScheduler(Context context) {
        this.context = context;
    }

    /*--------------------------------------------------
     * Daily Reminder
     *--------------------------------------------------*/

    public void scheduleDailyReminder(int hour, int minute) {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, ReminderReceiver.class);

        intent.putExtra("title", "Daily Expense Reminder");
        intent.putExtra("message", "Don't forget to record today's expenses.");
        intent.putExtra("subtitle", "Track your daily spending");
        intent.putExtra("id", NotificationConstants.DAILY_REMINDER_ID);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        NotificationConstants.DAILY_REMINDER_ID,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

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

        Intent intent = new Intent(context, ReminderReceiver.class);

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

    public void checkBudgetReminder(DatabaseHelper db, int userId) {

        Budget budget = db.getBudgetSettings(userId);

        if (budget == null) {
            return;
        }

        double budgetAmount = budget.getMonthlyBudget();

        if (budgetAmount <= 0) {
            return;
        }

        double totalExpense = db.getTotalExpense(userId);

        double percentage = (totalExpense / budgetAmount) * 100;

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

    public void checkCategoryBudgetReminder(
            DatabaseHelper db,
            int userId,
            String categoryName
    ) {

        double budget = db.getBudgetByCategory(userId, categoryName);

        if (budget <= 0) {
            return;
        }

        double expense = db.getCategoryExpense(userId, categoryName);

        double percentage = (expense / budget) * 100;

        NotificationHelper helper =
                new NotificationHelper(context);

        if (percentage >= 100) {

            helper.showNotification(
                    NotificationConstants.BUDGET_WARNING_ID,
                    "Budget Exceeded",
                    "You have exceeded your " + categoryName + " budget.",
                    categoryName + " Budget"
            );

        } else if (percentage >= 90) {

            helper.showNotification(
                    NotificationConstants.BUDGET_WARNING_ID,
                    "Budget Warning",
                    "You have used " + (int) percentage +
                            "% of your " + categoryName + " budget.",
                    categoryName + " Budget"
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

        ArrayList<Subscription> subscriptions =
                db.getUpcomingSubscriptions(userId);

        if (subscriptions == null || subscriptions.isEmpty()) {
            return;
        }

        NotificationPreferences prefs =
                new NotificationPreferences(context);

        NotificationHelper helper =
                new NotificationHelper(context);

        if (!prefs.isSubscriptionNotificationShownToday()) {

            for (Subscription subscription : subscriptions) {

                helper.showNotification(
                        NotificationConstants.SUBSCRIPTION_REMINDER_ID + subscription.getSubscriptionId(),
                        "Subscription Reminder",
                        subscription.getServiceName() + " renews soon.",
                        "Renewal Date: " + subscription.getNextBillingDate()
                );
            }

            prefs.saveSubscriptionNotificationDate();
        }
    }

    /*--------------------------------------------------
     * Monthly Report Reminder
     *--------------------------------------------------*/

    public void showMonthlyReportReminder() {

        Calendar calendar = Calendar.getInstance();

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
}