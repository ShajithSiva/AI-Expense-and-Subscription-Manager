package com.example.aiexpensemanagementapplication.model;

import com.google.firebase.Timestamp;

public class NotificationPreferences {

    private boolean notificationsEnabled;
    private boolean expenseReminder;
    private boolean budgetAlert;
    private boolean largeTransactionAlert;

    private boolean subscriptionReminder;
    private boolean renewalReminder;

    private boolean weeklyReport;
    private boolean monthlyReport;

    private int reminderHour;
    private int reminderMinute;

    private Timestamp updatedAt;

    // Required empty constructor for Firestore
    public NotificationPreferences() {
    }

    // Default values
    public NotificationPreferences(boolean notificationsEnabled,
                                   boolean expenseReminder,
                                   boolean budgetAlert,
                                   boolean largeTransactionAlert,
                                   boolean subscriptionReminder,
                                   boolean renewalReminder,
                                   boolean weeklyReport,
                                   boolean monthlyReport,
                                   int reminderHour,
                                   int reminderMinute,
                                   Timestamp updatedAt) {

        this.notificationsEnabled = notificationsEnabled;
        this.expenseReminder = expenseReminder;
        this.budgetAlert = budgetAlert;
        this.largeTransactionAlert = largeTransactionAlert;
        this.subscriptionReminder = subscriptionReminder;
        this.renewalReminder = renewalReminder;
        this.weeklyReport = weeklyReport;
        this.monthlyReport = monthlyReport;
        this.reminderHour = reminderHour;
        this.reminderMinute = reminderMinute;
        this.updatedAt = updatedAt;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isExpenseReminder() {
        return expenseReminder;
    }

    public void setExpenseReminder(boolean expenseReminder) {
        this.expenseReminder = expenseReminder;
    }

    public boolean isBudgetAlert() {
        return budgetAlert;
    }

    public void setBudgetAlert(boolean budgetAlert) {
        this.budgetAlert = budgetAlert;
    }

    public boolean isLargeTransactionAlert() {
        return largeTransactionAlert;
    }

    public void setLargeTransactionAlert(boolean largeTransactionAlert) {
        this.largeTransactionAlert = largeTransactionAlert;
    }

    public boolean isSubscriptionReminder() {
        return subscriptionReminder;
    }

    public void setSubscriptionReminder(boolean subscriptionReminder) {
        this.subscriptionReminder = subscriptionReminder;
    }

    public boolean isRenewalReminder() {
        return renewalReminder;
    }

    public void setRenewalReminder(boolean renewalReminder) {
        this.renewalReminder = renewalReminder;
    }

    public boolean isWeeklyReport() {
        return weeklyReport;
    }

    public void setWeeklyReport(boolean weeklyReport) {
        this.weeklyReport = weeklyReport;
    }

    public boolean isMonthlyReport() {
        return monthlyReport;
    }

    public void setMonthlyReport(boolean monthlyReport) {
        this.monthlyReport = monthlyReport;
    }

    public int getReminderHour() {
        return reminderHour;
    }

    public void setReminderHour(int reminderHour) {
        this.reminderHour = reminderHour;
    }

    public int getReminderMinute() {
        return reminderMinute;
    }

    public void setReminderMinute(int reminderMinute) {
        this.reminderMinute = reminderMinute;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
