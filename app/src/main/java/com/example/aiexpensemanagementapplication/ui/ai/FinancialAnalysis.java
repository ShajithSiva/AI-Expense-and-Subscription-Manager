package com.example.aiexpensemanagementapplication.ui.ai;

import java.util.LinkedHashMap;
import java.util.Map;

public class FinancialAnalysis {

    // =====================================================
    // BASIC FINANCIAL DATA
    // =====================================================

    private double totalIncome;
    private double totalExpense;
    private double savings;
    private double savingsRate;
    private double expenseRate;


    // =====================================================
    // BUDGET DATA
    // =====================================================

    private double budget;
    private double budgetUsed;
    private double remainingBudget;


    // =====================================================
    // MONTHLY FINANCIAL DATA
    // =====================================================

    private double currentMonthExpense;
    private double previousMonthExpense;

    private double currentMonthIncome;
    private double previousMonthIncome;

    private double expenseChangePercentage;


    // =====================================================
    // HIGHEST SPENDING CATEGORY
    // =====================================================

    private String highestCategory;

    private double highestCategoryAmount;


    // =====================================================
    // FINANCIAL HEALTH
    // =====================================================

    private int financialHealthScore;


    // =====================================================
    // CATEGORY TOTALS
    // =====================================================

    /*
     * Stores total spending for each expense category.
     *
     * Example:
     *
     * Food          -> 12500
     * Shopping      -> 8500
     * Transport     -> 4200
     * Entertainment -> 3000
     *
     * LinkedHashMap is used so that the insertion order
     * is preserved when displaying categories.
     */

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


    // =====================================================
    // BUDGET USED
    // =====================================================

    public double getBudgetUsed() {
        return budgetUsed;
    }

    public void setBudgetUsed(double budgetUsed) {
        this.budgetUsed = budgetUsed;
    }


    // =====================================================
    // REMAINING BUDGET
    // =====================================================

    public double getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(double remainingBudget) {
        this.remainingBudget = remainingBudget;
    }


    // =====================================================
    // CURRENT MONTH EXPENSE
    // =====================================================

    public double getCurrentMonthExpense() {
        return currentMonthExpense;
    }

    public void setCurrentMonthExpense(double value) {
        this.currentMonthExpense = value;
    }


    // =====================================================
    // PREVIOUS MONTH EXPENSE
    // =====================================================

    public double getPreviousMonthExpense() {
        return previousMonthExpense;
    }

    public void setPreviousMonthExpense(double value) {
        this.previousMonthExpense = value;
    }


    // =====================================================
    // CURRENT MONTH INCOME
    // =====================================================

    public double getCurrentMonthIncome() {
        return currentMonthIncome;
    }

    public void setCurrentMonthIncome(double value) {
        this.currentMonthIncome = value;
    }


    // =====================================================
    // PREVIOUS MONTH INCOME
    // =====================================================

    public double getPreviousMonthIncome() {
        return previousMonthIncome;
    }

    public void setPreviousMonthIncome(double value) {
        this.previousMonthIncome = value;
    }


    // =====================================================
    // EXPENSE CHANGE PERCENTAGE
    // =====================================================

    public double getExpenseChangePercentage() {
        return expenseChangePercentage;
    }

    public void setExpenseChangePercentage(double value) {
        this.expenseChangePercentage = value;
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


    // =====================================================
    // HIGHEST CATEGORY AMOUNT
    // =====================================================

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

    /**
     * Returns all category spending totals.
     *
     * Example:
     *
     * {
     *     Food=12500,
     *     Shopping=8500,
     *     Transport=4200
     * }
     */
    public Map<String, Double> getCategoryTotals() {
        return categoryTotals;
    }


    /**
     * Adds an expense amount to a category.
     *
     * If the category already exists, the new amount
     * is added to the existing total.
     *
     * Example:
     *
     * Food = 5000
     *
     * addCategoryAmount("Food", 2000)
     *
     * Result:
     *
     * Food = 7000
     */
    public void addCategoryAmount(
            String category,
            double amount) {

        if (
                category == null ||
                        category.trim().isEmpty()
        ) {
            category = "Other";
        }

        category =
                category.trim();

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


    /**
     * Clears all category totals.
     *
     * This is useful when FinancialAnalysis is being
     * recalculated from the database.
     */
    public void clearCategoryTotals() {
        categoryTotals.clear();
    }


    /**
     * Returns the spending amount for a specific category.
     *
     * Category matching is case-insensitive.
     *
     * Example:
     *
     * getCategoryAmount("food")
     *
     * can find:
     *
     * Food
     */
    public double getCategoryAmount(
            String requestedCategory) {

        if (
                requestedCategory == null ||
                        requestedCategory.trim().isEmpty()
        ) {
            return 0.0;
        }

        String requested =
                requestedCategory
                        .trim();

        for (
                Map.Entry<String, Double> entry
                : categoryTotals.entrySet()
        ) {

            String category =
                    entry.getKey();

            if (category == null) {
                continue;
            }

            if (
                    category.equalsIgnoreCase(
                            requested
                    )
            ) {

                Double amount =
                        entry.getValue();

                return amount == null
                        ? 0.0
                        : amount;
            }
        }

        return 0.0;
    }


    /**
     * Checks whether a category exists.
     *
     * Category matching is case-insensitive.
     */
    public boolean hasCategory(
            String requestedCategory) {

        if (
                requestedCategory == null ||
                        requestedCategory.trim().isEmpty()
        ) {
            return false;
        }

        String requested =
                requestedCategory
                        .trim();

        for (
                String category
                : categoryTotals.keySet()
        ) {

            if (
                    category != null &&
                            category.equalsIgnoreCase(
                                    requested
                            )
            ) {

                return true;
            }
        }

        return false;
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