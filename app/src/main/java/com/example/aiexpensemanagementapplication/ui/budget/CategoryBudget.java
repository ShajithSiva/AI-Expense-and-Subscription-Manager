package com.example.aiexpensemanagementapplication.ui.budget;

public class CategoryBudget {

    private String categoryName;

    private double budgetAmount;

    private double spentAmount;

    public CategoryBudget(String categoryName,
                          double budgetAmount,
                          double spentAmount) {

        this.categoryName = categoryName;

        this.budgetAmount = budgetAmount;

        this.spentAmount = spentAmount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public double getRemainingAmount() {

        return budgetAmount - spentAmount;
    }

    public int getPercentageUsed() {

        if (budgetAmount <= 0) {
            return 0;
        }

        double percentage =
                (spentAmount / budgetAmount) * 100;

        return (int) Math.min(percentage, 100);
    }
}