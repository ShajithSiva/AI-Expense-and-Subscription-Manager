package com.example.aiexpensemanagementapplication.ui.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ProactiveInsightEngine {

    // ==============================
    // THRESHOLDS
    // ==============================

    private static final double BUDGET_WARNING_PERCENT = 80.0;
    private static final double BUDGET_HIGH_PERCENT = 90.0;
    private static final double BUDGET_CRITICAL_PERCENT = 100.0;

    private static final double EXPENSE_INCREASE_MEDIUM = 10.0;
    private static final double EXPENSE_INCREASE_HIGH = 30.0;

    private static final double SAVINGS_LOW = 10.0;
    private static final double SAVINGS_HIGH = 5.0;

    private static final double POSITIVE_EXPENSE_CHANGE = -10.0;


    // ==============================
    // MAIN ANALYSIS
    // ==============================

    public List<FinancialInsight> analyze(FinancialAnalysis analysis) {

        List<FinancialInsight> insights = new ArrayList<>();

        if (analysis == null) {
            return insights;
        }

        // 1. Budget
        analyzeBudget(analysis, insights);

        // 2. Income vs expenses
        analyzeIncomeVsExpense(analysis, insights);

        // 3. Savings
        analyzeSavings(analysis, insights);

        // 4. Expense trend
        analyzeExpenseTrend(analysis, insights);

        // 5. Highest spending category
        analyzeHighestCategory(analysis, insights);

        // 6. General positive trend
        analyzePositiveTrend(analysis, insights);

        // ==============================
        // SORT BY SEVERITY
        // ==============================

        insights.sort(
                Comparator.comparingInt(
                        (FinancialInsight insight) ->
                                insight.getSeverity().getPriority()
                ).reversed()
        );

        return insights;
    }


    // ==============================
    // BUDGET ANALYSIS
    // ==============================

    private void analyzeBudget(
            FinancialAnalysis analysis,
            List<FinancialInsight> insights
    ) {

        double budget = analysis.getBudget();
        double expense = analysis.getCurrentMonthExpense();

        if (budget <= 0) {
            return;
        }

        double budgetUsedPercentage = (expense / budget) * 100.0;

        // Budget exceeded
        if (budgetUsedPercentage >= BUDGET_CRITICAL_PERCENT) {

            insights.add(
                    new FinancialInsight(
                            FinancialInsight.Type.BUDGET_EXCEEDED,
                            FinancialInsight.Severity.CRITICAL,
                            "Budget Exceeded",
                            String.format(
                                    "You have used %.1f%% of your monthly budget. Your spending has exceeded the planned budget.",
                                    budgetUsedPercentage
                            ),
                            budgetUsedPercentage
                    )
            );

            return;
        }

        // 90%+
        if (budgetUsedPercentage >= BUDGET_HIGH_PERCENT) {

            insights.add(
                    new FinancialInsight(
                            FinancialInsight.Type.BUDGET_WARNING,
                            FinancialInsight.Severity.HIGH,
                            "Budget Almost Exhausted",
                            String.format(
                                    "You have used %.1f%% of your monthly budget. Only %.1f%% remains.",
                                    budgetUsedPercentage,
                                    100.0 - budgetUsedPercentage
                            ),
                            budgetUsedPercentage
                    )
            );

            return;
        }

        // 80%+
        if (budgetUsedPercentage >= BUDGET_WARNING_PERCENT) {

            insights.add(
                    new FinancialInsight(
                            FinancialInsight.Type.BUDGET_WARNING,
                            FinancialInsight.Severity.MEDIUM,
                            "Budget Warning",
                            String.format(
                                    "You have already used %.1f%% of your monthly budget.",
                                    budgetUsedPercentage
                            ),
                            budgetUsedPercentage
                    )
            );
        }
    }


    // ==============================
    // INCOME VS EXPENSE ANALYSIS
    // ==============================

    private void analyzeIncomeVsExpense(
            FinancialAnalysis analysis,
            List<FinancialInsight> insights
    ) {

        double income = analysis.getCurrentMonthIncome();
        double expense = analysis.getCurrentMonthExpense();

        if (income <= 0) {
            return;
        }

        if (expense > income) {

            double difference = expense - income;

            insights.add(
                    new FinancialInsight(
                            FinancialInsight.Type.EXPENSES_EXCEED_INCOME,
                            FinancialInsight.Severity.CRITICAL,
                            "Expenses Exceed Income",
                            String.format(
                                    "Your expenses exceed your income by %.2f this month. Consider reducing non-essential spending.",
                                    difference
                            ),
                            difference
                    )
            );
        }
    }


    // ==============================
    // SAVINGS ANALYSIS
    // ==============================

    private void analyzeSavings(
            FinancialAnalysis analysis,
            List<FinancialInsight> insights
    ) {

        double savings = analysis.getSavings();
        double savingsRate = analysis.getSavingsRate();

        // Do not create a misleading LOW_SAVINGS insight
        // when expenses already exceed income.
        if (savings < 0) {
            return;
        }

        // Very low savings
        if (savingsRate < SAVINGS_HIGH) {

            insights.add(
                    new FinancialInsight(
                            FinancialInsight.Type.LOW_SAVINGS,
                            FinancialInsight.Severity.HIGH,
                            "Very Low Savings",
                            String.format(
                                    "Your savings rate is %.1f%%. Try to increase the amount you save each month.",
                                    savingsRate
                            ),
                            savingsRate
                    )
            );

            return;
        }

        // Low savings
        if (savingsRate < SAVINGS_LOW) {

            insights.add(
                    new FinancialInsight(
                            FinancialInsight.Type.LOW_SAVINGS,
                            FinancialInsight.Severity.MEDIUM,
                            "Low Savings",
                            String.format(
                                    "Your savings rate is %.1f%%. Consider reducing unnecessary expenses to improve your savings.",
                                    savingsRate
                            ),
                            savingsRate
                    )
            );

            return;
        }

        // Good savings
        if (savingsRate >= 20.0) {

            insights.add(
                    new FinancialInsight(
                            FinancialInsight.Type.GOOD_SAVINGS,
                            FinancialInsight.Severity.POSITIVE,
                            "Good Savings",
                            String.format(
                                    "You are saving %.1f%% of your income. Keep maintaining this healthy savings habit.",
                                    savingsRate
                            ),
                            savingsRate
                    )
            );
        }
    }


    // ==============================
    // EXPENSE TREND ANALYSIS
    // ==============================

    private void analyzeExpenseTrend(
            FinancialAnalysis analysis,
            List<FinancialInsight> insights
    ) {

        double change = analysis.getExpenseChangePercentage();

        // Significant expense increase
        if (change >= EXPENSE_INCREASE_HIGH) {

            insights.add(
                    new FinancialInsight(
                            FinancialInsight.Type.EXPENSE_INCREASE,
                            FinancialInsight.Severity.HIGH,
                            "Spending Increased Significantly",
                            String.format(
                                    "Your expenses increased by %.1f%% compared with the previous month.",
                                    change
                            ),
                            change
                    )
            );

            return;
        }

        // Moderate expense increase
        if (change >= EXPENSE_INCREASE_MEDIUM) {

            insights.add(
                    new FinancialInsight(
                            FinancialInsight.Type.EXPENSE_INCREASE,
                            FinancialInsight.Severity.MEDIUM,
                            "Spending Increased",
                            String.format(
                                    "Your expenses increased by %.1f%% compared with the previous month.",
                                    change
                            ),
                            change
                    )
            );

            return;
        }

        // Expense decrease
        if (change <= POSITIVE_EXPENSE_CHANGE) {

            insights.add(
                    new FinancialInsight(
                            FinancialInsight.Type.EXPENSE_DECREASE,
                            FinancialInsight.Severity.POSITIVE,
                            "Spending Decreased",
                            String.format(
                                    "Your expenses decreased by %.1f%% compared with the previous month. Great progress.",
                                    Math.abs(change)
                            ),
                            change
                    )
            );
        }
    }


    // ==============================
    // HIGHEST SPENDING CATEGORY
    // ==============================

    private void analyzeHighestCategory(
            FinancialAnalysis analysis,
            List<FinancialInsight> insights
    ) {

        String highestCategory = analysis.getHighestCategory();
        double highestAmount = analysis.getHighestCategoryAmount();

        if (highestCategory == null ||
                highestCategory.trim().isEmpty() ||
                highestAmount <= 0) {
            return;
        }

        insights.add(
                new FinancialInsight(
                        FinancialInsight.Type.HIGH_SPENDING_CATEGORY,
                        FinancialInsight.Severity.LOW,
                        "Highest Spending Category",
                        String.format(
                                "%s is your highest spending category this month, with %.2f spent.",
                                highestCategory,
                                highestAmount
                        ),
                        highestAmount
                )
        );
    }


    // ==============================
    // POSITIVE TREND
    // ==============================

    private void analyzePositiveTrend(
            FinancialAnalysis analysis,
            List<FinancialInsight> insights
    ) {

        double change = analysis.getExpenseChangePercentage();
        double savingsRate = analysis.getSavingsRate();

        /*
         * A stronger expense decrease already creates
         * an EXPENSE_DECREASE insight.
         *
         * Therefore this insight is only generated for
         * a smaller decrease, avoiding duplicate messages.
         */
        if (change < 0 &&
                change > POSITIVE_EXPENSE_CHANGE &&
                savingsRate >= SAVINGS_LOW) {

            insights.add(
                    new FinancialInsight(
                            FinancialInsight.Type.POSITIVE_TREND,
                            FinancialInsight.Severity.POSITIVE,
                            "Positive Financial Trend",
                            String.format(
                                    "Your spending is trending downward while maintaining a %.1f%% savings rate.",
                                    savingsRate
                            ),
                            savingsRate
                    )
            );
        }
    }
}