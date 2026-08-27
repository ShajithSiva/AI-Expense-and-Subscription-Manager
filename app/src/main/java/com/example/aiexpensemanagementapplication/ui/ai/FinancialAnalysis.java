package com.example.aiexpensemanagementapplication.ui.ai;

import java.util.LinkedHashMap;
import java.util.Map;

public class FinancialAnalysis {

    private double totalIncome;
    private double totalExpense;
    private double savings;
    private double savingsRate;
    private double expenseRate;

    private double budget;
    private double budgetUsed;
    private double remainingBudget;

    private double currentMonthExpense;
    private double previousMonthExpense;
    private double currentMonthIncome;
    private double previousMonthIncome;
    private double expenseChangePercentage;

    private String highestCategory;
    private double highestCategoryAmount;

    private int financialHealthScore;

    private final Map<String, Double> categoryTotals =
            new LinkedHashMap<>();

    // =====================================================
    // INCOME
    // =====================================================

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    // =====================================================
    // EXPENSE
    // =====================================================

    public double getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
    }

    // =====================================================
    // SAVINGS
    // =====================================================

    public double getSavings() {
        return savings;
    }

    public void setSavings(double savings) {
        this.savings = savings;
    }

    // =====================================================
    // SAVINGS RATE
    // =====================================================

    public double getSavingsRate() {
        return savingsRate;
    }

    public void setSavingsRate(double savingsRate) {
        this.savingsRate = savingsRate;
    }

    // =====================================================
    // EXPENSE RATE
    // =====================================================

    public double getExpenseRate() {
        return expenseRate;
    }

    public void setExpenseRate(double expenseRate) {
        this.expenseRate = expenseRate;
    }

    // =====================================================
    // BUDGET
    // =====================================================

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public double getBudgetUsed() {
        return budgetUsed;
    }

    public void setBudgetUsed(double budgetUsed) {
        this.budgetUsed = budgetUsed;
    }

    public double getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(double remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    // =====================================================
    // HIGHEST CATEGORY
    // =====================================================

    public String getHighestCategory() {
        return highestCategory;
    }

    public void setHighestCategory(String highestCategory) {
        this.highestCategory = highestCategory;
    }

    public double getHighestCategoryAmount() {
        return highestCategoryAmount;
    }

    public void setHighestCategoryAmount(
            double highestCategoryAmount) {

        this.highestCategoryAmount =
                highestCategoryAmount;
    }

    // =====================================================
    // CATEGORY TOTALS
    // =====================================================

    public Map<String, Double> getCategoryTotals() {
        return categoryTotals;
    }

    public void addCategoryAmount(
            String category,
            double amount) {

        Double current =
                categoryTotals.get(category);

        if (current == null) {
            current = 0.0;
        }

        categoryTotals.put(
                category,
                current + amount
        );
    }

    public double getCurrentMonthExpense() {
        return currentMonthExpense;
    }

    public void setCurrentMonthExpense(double value) {
        this.currentMonthExpense = value;
    }

    public double getPreviousMonthExpense() {
        return previousMonthExpense;
    }

    public void setPreviousMonthExpense(double value) {
        this.previousMonthExpense = value;
    }

    public double getCurrentMonthIncome() {
        return currentMonthIncome;
    }

    public void setCurrentMonthIncome(double value) {
        this.currentMonthIncome = value;
    }

    public double getPreviousMonthIncome() {
        return previousMonthIncome;
    }

    public void setPreviousMonthIncome(double value) {
        this.previousMonthIncome = value;
    }

    public double getExpenseChangePercentage() {
        return expenseChangePercentage;
    }

    public void setExpenseChangePercentage(double value) {
        this.expenseChangePercentage = value;
    }

    // =====================================================
    // FINANCIAL HEALTH
    // =====================================================

    public int getFinancialHealthScore() {
        return financialHealthScore;
    }

    public void setFinancialHealthScore(
            int financialHealthScore) {

        this.financialHealthScore =
                financialHealthScore;
    }
}