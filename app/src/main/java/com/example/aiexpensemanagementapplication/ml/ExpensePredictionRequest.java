package com.example.aiexpensemanagementapplication.ml;

import com.google.gson.annotations.SerializedName;

public class ExpensePredictionRequest {

    @SerializedName("Year")
    private int year;

    @SerializedName("Month")
    private int month;

    @SerializedName("Monthly_Income")
    private double monthlyIncome;

    @SerializedName("Bills")
    private double bills;

    @SerializedName("Education")
    private double education;

    @SerializedName("Entertainment")
    private double entertainment;

    @SerializedName("Food")
    private double food;

    @SerializedName("Health")
    private double health;

    @SerializedName("Shopping")
    private double shopping;

    @SerializedName("Transport")
    private double transport;

    @SerializedName("Travel")
    private double travel;

    @SerializedName("Total_Expense")
    private double totalExpense;

    @SerializedName("Savings")
    private double savings;

    @SerializedName("Savings_Rate")
    private double savingsRate;

    @SerializedName("Expense_Ratio")
    private double expenseRatio;

    @SerializedName("Average_Daily_Expense")
    private double averageDailyExpense;

    @SerializedName("Weekend_Spending_Percentage")
    private double weekendSpendingPercentage;

    @SerializedName("Transaction_Count")
    private int transactionCount;

    @SerializedName("Spending_Profile")
    private String spendingProfile;


    public ExpensePredictionRequest(
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
        this.weekendSpendingPercentage = weekendSpendingPercentage;
        this.transactionCount = transactionCount;
        this.spendingProfile = spendingProfile;
    }
}