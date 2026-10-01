package com.example.aiexpensemanagementapplication.ml;

public class ExpensePredictionFeatures {

    private final int year;
    private final int month;

    private final double monthlyIncome;

    private final double bills;
    private final double education;
    private final double entertainment;
    private final double food;
    private final double health;
    private final double shopping;
    private final double transport;
    private final double travel;

    private final double totalExpense;
    private final double savings;
    private final double savingsRate;
    private final double expenseRatio;
    private final double averageDailyExpense;
    private final double weekendSpendingPercentage;

    private final int transactionCount;

    private final String spendingProfile;

    public ExpensePredictionFeatures(
            int year,
            int month,
            double monthlyIncome,
            double bills,
            double education,
            double entertainment,
            double food,
            double health,
            double shopping,
            double transport,
            double travel,
            double totalExpense,
            double savings,
            double savingsRate,
            double expenseRatio,
            double averageDailyExpense,
            double weekendSpendingPercentage,
            int transactionCount,
            String spendingProfile
    ) {

        this.year = year;
        this.month = month;

        this.monthlyIncome = monthlyIncome;

        this.bills = bills;
        this.education = education;
        this.entertainment = entertainment;
        this.food = food;
        this.health = health;
        this.shopping = shopping;
        this.transport = transport;
        this.travel = travel;

        this.totalExpense = totalExpense;
        this.savings = savings;
        this.savingsRate = savingsRate;
        this.expenseRatio = expenseRatio;
        this.averageDailyExpense = averageDailyExpense;
        this.weekendSpendingPercentage =
                weekendSpendingPercentage;

        this.transactionCount = transactionCount;

        this.spendingProfile = spendingProfile;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public double getBills() {
        return bills;
    }

    public double getEducation() {
        return education;
    }

    public double getEntertainment() {
        return entertainment;
    }

    public double getFood() {
        return food;
    }

    public double getHealth() {
        return health;
    }

    public double getShopping() {
        return shopping;
    }

    public double getTransport() {
        return transport;
    }

    public double getTravel() {
        return travel;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public double getSavings() {
        return savings;
    }

    public double getSavingsRate() {
        return savingsRate;
    }

    public double getExpenseRatio() {
        return expenseRatio;
    }

    public double getAverageDailyExpense() {
        return averageDailyExpense;
    }

    public double getWeekendSpendingPercentage() {
        return weekendSpendingPercentage;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public String getSpendingProfile() {
        return spendingProfile;
    }
}