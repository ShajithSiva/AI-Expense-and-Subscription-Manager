package com.example.aiexpensemanagementapplication.ui.ai;

import android.database.Cursor;

import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.model.Budget;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class FinancialAdvisorEngine {

    private final DatabaseHelper databaseHelper;

    public FinancialAdvisorEngine(
            DatabaseHelper databaseHelper) {

        this.databaseHelper = databaseHelper;
    }

    // =====================================================
// ANALYZE USER
// =====================================================

    public FinancialAnalysis analyzeUser(
            int userId) {

        FinancialAnalysis analysis =
                new FinancialAnalysis();

        // -------------------------------------------------
        // GET CURRENT MONTH
        // Format: YYYY-MM
        // -------------------------------------------------

        Calendar calendar =
                Calendar.getInstance();

        String currentMonth =
                String.format(
                        Locale.getDefault(),
                        "%04d-%02d",
                        calendar.get(
                                Calendar.YEAR
                        ),
                        calendar.get(
                                Calendar.MONTH
                        ) + 1
                );

        // -------------------------------------------------
        // TOTAL INCOME
        // -------------------------------------------------

        double income =
                databaseHelper.getMonthlyIncome(
                        userId,
                        currentMonth
                );

        // -------------------------------------------------
        // CURRENT MONTH EXPENSE
        // -------------------------------------------------

        double expense =
                databaseHelper.getMonthlyExpense(
                        userId,
                        currentMonth
                );

        // -------------------------------------------------
        // BASIC VALUES
        // -------------------------------------------------

        analysis.setTotalIncome(
                income
        );

        analysis.setTotalExpense(
                expense
        );

        double savings =
                income - expense;

        analysis.setSavings(
                savings
        );

        // -------------------------------------------------
        // RATES
        // -------------------------------------------------

        double expenseRate = 0.0;
        double savingsRate = 0.0;

        if (income > 0) {

            expenseRate =
                    (expense / income) * 100.0;

            savingsRate =
                    (savings / income) * 100.0;
        }

        analysis.setExpenseRate(
                expenseRate
        );

        analysis.setSavingsRate(
                savingsRate
        );

        // =================================================
        // BUDGET ANALYSIS
        // =================================================

        loadBudgetAnalysis(
                userId,
                analysis
        );

        // =================================================
        // CATEGORY ANALYSIS
        // =================================================

        loadCategoryAnalysis(
                userId,
                analysis
        );

        // =================================================
        // MONTHLY TREND
        // =================================================

        loadMonthlyTrend(
                userId,
                analysis
        );

        // =================================================
        // FINANCIAL HEALTH
        // =================================================

        int score =
                calculateHealthScore(
                        analysis
                );

        analysis.setFinancialHealthScore(
                score
        );

        return analysis;
    }

    // =====================================================
// LOAD BUDGET ANALYSIS
// =====================================================

    private void loadBudgetAnalysis(
            int userId,
            FinancialAnalysis analysis) {

        try {

            // -------------------------------------------------
            // GET CURRENT MONTH
            // Format: YYYY-MM
            // -------------------------------------------------

            Calendar calendar =
                    Calendar.getInstance();

            String currentMonth =
                    String.format(
                            Locale.getDefault(),
                            "%04d-%02d",
                            calendar.get(
                                    Calendar.YEAR
                            ),
                            calendar.get(
                                    Calendar.MONTH
                            ) + 1
                    );

            // -------------------------------------------------
            // GET SAVED BUDGET
            // -------------------------------------------------

            Budget budget =
                    databaseHelper.getBudgetSettings(
                            userId
                    );

            // -------------------------------------------------
            // NO BUDGET FOUND
            // -------------------------------------------------

            if (budget == null) {

                analysis.setBudget(0.0);

                analysis.setBudgetUsed(0.0);

                analysis.setRemainingBudget(0.0);

                return;
            }

            // -------------------------------------------------
            // MONTHLY BUDGET
            // -------------------------------------------------

            double monthlyBudget =
                    budget.getMonthlyBudget();

            // -------------------------------------------------
            // CURRENT MONTH EXPENSE
            // -------------------------------------------------

            double currentMonthExpense =
                    databaseHelper.getMonthlyExpense(
                            userId,
                            currentMonth
                    );

            // -------------------------------------------------
            // SAVE BUDGET INFORMATION
            // -------------------------------------------------

            analysis.setBudget(
                    monthlyBudget
            );

            // -------------------------------------------------
            // CALCULATE BUDGET USED %
            // -------------------------------------------------

            double budgetUsed = 0.0;

            if (monthlyBudget > 0) {

                budgetUsed =
                        (
                                currentMonthExpense
                                        / monthlyBudget
                        ) * 100.0;
            }

            analysis.setBudgetUsed(
                    budgetUsed
            );

            // -------------------------------------------------
            // REMAINING BUDGET
            // -------------------------------------------------

            double remainingBudget =
                    monthlyBudget
                            - currentMonthExpense;

            analysis.setRemainingBudget(
                    remainingBudget
            );

        } catch (Exception e) {

            e.printStackTrace();

            // -------------------------------------------------
            // RESET IF ERROR
            // -------------------------------------------------

            analysis.setBudget(0.0);

            analysis.setBudgetUsed(0.0);

            analysis.setRemainingBudget(0.0);
        }
    }

    // =====================================================
    // CATEGORY ANALYSIS
    // =====================================================
    private void loadCategoryAnalysis(
            int userId,
            FinancialAnalysis analysis) {

        Cursor cursor = null;

        try {

            cursor =
                    databaseHelper.getExpenseByCategory(
                            userId
                    );

            if (cursor == null) {
                return;
            }

            /*
             * getExpenseByCategory() returns:
             *
             * CATEGORY_ID
             * SUM(AMOUNT) AS Total
             */

            int categoryIdIndex =
                    cursor.getColumnIndex(
                            "CategoryID"
                    );

            int totalIndex =
                    cursor.getColumnIndex(
                            "Total"
                    );

            if (categoryIdIndex == -1) {

                // Try the database constant name
                categoryIdIndex =
                        cursor.getColumnIndex(
                                "CATEGORY_ID"
                        );
            }

            if (totalIndex == -1) {

                totalIndex =
                        cursor.getColumnIndex(
                                "Total"
                        );
            }

            if (categoryIdIndex == -1 ||
                    totalIndex == -1) {

                return;
            }

            while (cursor.moveToNext()) {

                int categoryId =
                        cursor.getInt(
                                categoryIdIndex
                        );

                double amount =
                        cursor.getDouble(
                                totalIndex
                        );

                if (amount <= 0) {
                    continue;
                }

                String categoryName =
                        getCategoryName(
                                categoryId
                        );

                if (categoryName == null ||
                        categoryName.trim().isEmpty()) {

                    categoryName =
                            "Other";
                }

                analysis.addCategoryAmount(
                        categoryName,
                        amount
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        // -------------------------------------------------
        // FIND HIGHEST CATEGORY
        // -------------------------------------------------

        String highestCategory =
                "Other";

        double highestAmount =
                0.0;

        Map<String, Double> categoryTotals =
                analysis.getCategoryTotals();

        if (categoryTotals != null) {

            for (Map.Entry<String, Double> entry :
                    categoryTotals.entrySet()) {

                if (entry.getValue() != null &&
                        entry.getValue() >
                                highestAmount) {

                    highestAmount =
                            entry.getValue();

                    highestCategory =
                            entry.getKey();
                }
            }
        }

        analysis.setHighestCategory(
                highestCategory
        );

        analysis.setHighestCategoryAmount(
                highestAmount
        );
    }

    // =====================================================
    // GET CATEGORY NAME
    // =====================================================

    private String getCategoryName(
            int categoryId) {

        Cursor cursor = null;

        try {

            /*
             * We use the existing DatabaseHelper
             * category method if available.
             */

            cursor =
                    databaseHelper.getExpenseCategories();

            if (cursor == null) {
                return "Other";
            }

            int idIndex =
                    cursor.getColumnIndex(
                            "CategoryID"
                    );

            int nameIndex =
                    cursor.getColumnIndex(
                            "CategoryName"
                    );

            if (idIndex == -1 ||
                    nameIndex == -1) {

                return "Other";
            }

            while (cursor.moveToNext()) {

                int id =
                        cursor.getInt(
                                idIndex
                        );

                if (id == categoryId) {

                    String name =
                            cursor.getString(
                                    nameIndex
                            );

                    if (name != null &&
                            !name.trim().isEmpty()) {

                        return name.trim();
                    }

                    return "Other";
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return "Other";
    }

    // =====================================================
    // MONTHLY TREND
    // =====================================================

    private void loadMonthlyTrend(
            int userId,
            FinancialAnalysis analysis) {

        Calendar calendar =
                Calendar.getInstance();

        // -------------------------------------------------
        // CURRENT MONTH
        // -------------------------------------------------

        String currentMonth =
                String.format(
                        Locale.getDefault(),
                        "%04d-%02d",
                        calendar.get(
                                Calendar.YEAR
                        ),
                        calendar.get(
                                Calendar.MONTH
                        ) + 1
                );

        // -------------------------------------------------
        // PREVIOUS MONTH
        // -------------------------------------------------

        calendar.add(
                Calendar.MONTH,
                -1
        );

        String previousMonth =
                String.format(
                        Locale.getDefault(),
                        "%04d-%02d",
                        calendar.get(
                                Calendar.YEAR
                        ),
                        calendar.get(
                                Calendar.MONTH
                        ) + 1
                );

        // -------------------------------------------------
        // CURRENT MONTH EXPENSE
        // -------------------------------------------------

        double currentExpense =
                databaseHelper.getMonthlyExpense(
                        userId,
                        currentMonth
                );

        // -------------------------------------------------
        // PREVIOUS MONTH EXPENSE
        // -------------------------------------------------

        double previousExpense =
                databaseHelper.getMonthlyExpense(
                        userId,
                        previousMonth
                );

        // -------------------------------------------------
        // CURRENT MONTH INCOME
        // -------------------------------------------------

        double currentIncome =
                databaseHelper.getMonthlyIncome(
                        userId,
                        currentMonth
                );

        // -------------------------------------------------
        // PREVIOUS MONTH INCOME
        // -------------------------------------------------

        double previousIncome =
                databaseHelper.getMonthlyIncome(
                        userId,
                        previousMonth
                );

        // -------------------------------------------------
        // SET TREND DATA
        // -------------------------------------------------

        analysis.setCurrentMonthExpense(
                currentExpense
        );

        analysis.setPreviousMonthExpense(
                previousExpense
        );

        analysis.setCurrentMonthIncome(
                currentIncome
        );

        analysis.setPreviousMonthIncome(
                previousIncome
        );

        // -------------------------------------------------
        // EXPENSE CHANGE %
        // -------------------------------------------------

        double expenseChange = 0.0;

        if (previousExpense > 0) {

            expenseChange =
                    (
                            (
                                    currentExpense
                                            - previousExpense
                            )
                                    / previousExpense
                    ) * 100.0;

        } else if (currentExpense > 0) {

            // Previous month had no recorded expenses.
            // Keep percentage at 0 because a percentage
            // comparison cannot be calculated reliably.
            expenseChange = 0.0;
        }

        analysis.setExpenseChangePercentage(
                expenseChange
        );
    }

    // =====================================================
    // FINANCIAL HEALTH SCORE
    // =====================================================

    private int calculateHealthScore(
            FinancialAnalysis analysis) {

        double income =
                analysis.getTotalIncome();

        if (income <= 0) {

            return 0;
        }

        int score = 100;

        // -------------------------------------------------
        // EXPENSE RATIO
        // -------------------------------------------------

        double expenseRate =
                analysis.getExpenseRate();

        if (expenseRate > 100) {

            score -= 50;

        } else if (expenseRate > 90) {

            score -= 35;

        } else if (expenseRate > 80) {

            score -= 25;

        } else if (expenseRate > 70) {

            score -= 15;

        } else if (expenseRate > 60) {

            score -= 8;
        }

        // -------------------------------------------------
        // SAVINGS RATE
        // -------------------------------------------------

        double savingsRate =
                analysis.getSavingsRate();

        if (savingsRate < 0) {

            score -= 30;

        } else if (savingsRate < 5) {

            score -= 20;

        } else if (savingsRate < 10) {

            score -= 10;
        }

        // -------------------------------------------------
        // SPENDING TREND
        // -------------------------------------------------

        double expenseChange =
                analysis.getExpenseChangePercentage();

        if (expenseChange > 30) {

            score -= 10;

        } else if (expenseChange > 15) {

            score -= 5;
        }

        // -------------------------------------------------
        // BUDGET PERFORMANCE
        // -------------------------------------------------

        double budgetUsed =
                analysis.getBudgetUsed();

        if (analysis.getBudget() > 0) {

            if (budgetUsed > 100) {

                score -= 20;

            } else if (budgetUsed >= 90) {

                score -= 10;

            } else if (budgetUsed >= 80) {

                score -= 5;
            }
        }

        // -------------------------------------------------
        // LIMIT
        // -------------------------------------------------

        if (score < 0) {
            score = 0;
        }

        if (score > 100) {
            score = 100;
        }

        return score;
    }

    // =====================================================
    // ANSWER QUESTION
    // =====================================================

    public String answerQuestion(
            FinancialAnalysis analysis,
            String question) {

        if (analysis == null) {

            return "I don't have enough financial data "
                    + "to provide an analysis yet.";
        }

        if (question == null ||
                question.trim().isEmpty()) {

            return getGeneralAdvice(
                    analysis
            );
        }

        String q =
                question
                        .trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        // -------------------------------------------------
        // SAVINGS
        // -------------------------------------------------

        if (q.contains("save") ||
                q.contains("saving") ||
                q.contains("savings")) {

            return getSavingsAdvice(
                    analysis
            );
        }

        // -------------------------------------------------
        // SPENDING
        // -------------------------------------------------

        if (q.contains("spending") ||
                q.contains("expense") ||
                q.contains("spend")) {

            return getSpendingAdvice(
                    analysis
            );
        }

        // -------------------------------------------------
        // BUDGET
        // -------------------------------------------------

        if (q.contains("budget")) {

            return getBudgetAdvice(
                    analysis
            );
        }

        // -------------------------------------------------
        // HEALTH
        // -------------------------------------------------

        if (q.contains("health") ||
                q.contains("score")) {

            return getHealthAdvice(
                    analysis
            );
        }

        // -------------------------------------------------
        // TREND
        // -------------------------------------------------

        if (q.contains("trend") ||
                q.contains("increasing") ||
                q.contains("increased") ||
                q.contains("decreased") ||
                q.contains("last month")) {

            return getTrendAdvice(
                    analysis
            );
        }

        // -------------------------------------------------
        // CATEGORY
        // -------------------------------------------------

        if (q.contains("category") ||
                q.contains("highest") ||
                q.contains("largest")) {

            return getCategoryAdvice(
                    analysis
            );
        }

        // -------------------------------------------------
        // GENERAL
        // -------------------------------------------------

        return getGeneralAdvice(
                analysis
        );
    }

    // =====================================================
    // SAVINGS ADVICE
    // =====================================================

    private String getSavingsAdvice(
            FinancialAnalysis analysis) {

        double income =
                analysis.getTotalIncome();

        double savings =
                analysis.getSavings();

        double rate =
                analysis.getSavingsRate();

        if (income <= 0) {

            return "I don't have enough recorded income "
                    + "data to calculate a savings strategy yet. "
                    + "Add your income records and I'll analyze "
                    + "your savings potential.";
        }

        if (savings < 0) {

            return "⚠ Your expenses are higher than your "
                    + "recorded income by Rs "
                    + format(-savings)
                    + ". I recommend reducing non-essential "
                    + "spending, especially "
                    + analysis.getHighestCategory()
                    + ".";
        }

        if (rate < 10) {

            return "Your current savings are approximately "
                    + "Rs "
                    + format(savings)
                    + ", which is "
                    + format(rate)
                    + "% of your recorded income. "
                    + "Consider reducing "
                    + analysis.getHighestCategory()
                    + " spending and redirecting part of that "
                    + "amount into savings.";
        }

        if (rate < 20) {

            return "You are saving approximately Rs "
                    + format(savings)
                    + ", or "
                    + format(rate)
                    + "% of your recorded income. "
                    + "You're building a positive savings habit. "
                    + "You could improve it further by reviewing "
                    + analysis.getHighestCategory()
                    + " spending.";
        }

        return "Your savings are approximately Rs "
                + format(savings)
                + ", which is "
                + format(rate)
                + "% of your recorded income. "
                + "That's a strong savings rate. "
                + "Keep maintaining this habit.";
    }

    // =====================================================
    // SPENDING ADVICE
    // =====================================================

    private String getSpendingAdvice(
            FinancialAnalysis analysis) {

        double expense =
                analysis.getTotalExpense();

        if (expense <= 0) {

            return "There are not enough recorded expenses "
                    + "yet for me to identify a spending pattern.";
        }

        return "Your recorded expenses total Rs "
                + format(expense)
                + ". Your highest spending category is "
                + analysis.getHighestCategory()
                + ", at approximately Rs "
                + format(
                analysis.getHighestCategoryAmount()
        )
                + ". I recommend reviewing this category "
                + "first when looking for savings opportunities.";
    }

    // =====================================================
    // CATEGORY ADVICE
    // =====================================================

    private String getCategoryAdvice(
            FinancialAnalysis analysis) {

        if (analysis.getHighestCategoryAmount() <= 0) {

            return "I don't have enough category data yet "
                    + "to identify your highest spending area.";
        }

        return "Your highest spending category is "
                + analysis.getHighestCategory()
                + ", with approximately Rs "
                + format(
                analysis.getHighestCategoryAmount()
        )
                + " spent. This is the category I recommend "
                + "reviewing first.";
    }

    // =====================================================
    // TREND ADVICE
    // =====================================================

    private String getTrendAdvice(
            FinancialAnalysis analysis) {

        double current =
                analysis.getCurrentMonthExpense();

        double previous =
                analysis.getPreviousMonthExpense();

        double change =
                analysis.getExpenseChangePercentage();

        if (previous <= 0) {

            return "There isn't enough previous-month "
                    + "expense data yet to identify a reliable "
                    + "spending trend.";
        }

        if (change > 0) {

            return "Your expenses increased by approximately "
                    + format(change)
                    + "% compared with the previous month. "
                    + "Your current monthly expense is Rs "
                    + format(current)
                    + ", compared with Rs "
                    + format(previous)
                    + " last month.";
        }

        if (change < 0) {

            return "Good progress! Your expenses decreased by "
                    + format(Math.abs(change))
                    + "% compared with the previous month. "
                    + "Keep maintaining this spending pattern.";
        }

        return "Your expenses are approximately the same "
                + "as the previous month at Rs "
                + format(current)
                + ".";
    }

    // =====================================================
// BUDGET ADVICE
// =====================================================

    private String getBudgetAdvice(
            FinancialAnalysis analysis) {

        double budget =
                analysis.getBudget();

        double used =
                analysis.getBudgetUsed();

        double remaining =
                analysis.getRemainingBudget();

        // -------------------------------------------------
        // NO BUDGET
        // -------------------------------------------------

        if (budget <= 0) {

            return "I couldn't find an active monthly budget "
                    + "for your financial profile yet. "
                    + "Create a monthly budget and I'll help "
                    + "you monitor it.";
        }

        // -------------------------------------------------
        // OVER BUDGET
        // -------------------------------------------------

        if (remaining < 0) {

            double exceededBy =
                    Math.abs(remaining);

            return "⚠ You're currently over your budget.\n\n"
                    + "Monthly budget: Rs "
                    + format(budget)
                    + "\nCurrent month spending: Rs "
                    + format(
                    budget - remaining
            )
                    + "\nExceeded by: Rs "
                    + format(exceededBy)
                    + "\nBudget used: "
                    + format(used)
                    + "%\n\n"
                    + "I recommend reducing non-essential "
                    + "spending, especially in your highest "
                    + "spending category: "
                    + analysis.getHighestCategory()
                    + ".";
        }

        // -------------------------------------------------
        // NEAR BUDGET LIMIT
        // -------------------------------------------------

        if (used >= 90) {

            return "⚠ You're still within your budget, "
                    + "but you're getting close to the limit.\n\n"
                    + "Monthly budget: Rs "
                    + format(budget)
                    + "\nCurrent month spending: Rs "
                    + format(
                    budget - remaining
            )
                    + "\nRemaining: Rs "
                    + format(remaining)
                    + "\nBudget used: "
                    + format(used)
                    + "%\n\n"
                    + "Try to limit non-essential spending "
                    + "for the rest of the month.";
        }

        // -------------------------------------------------
        // WITHIN BUDGET
        // -------------------------------------------------

        return "✅ Yes, you're currently within your budget.\n\n"
                + "Monthly budget: Rs "
                + format(budget)
                + "\nCurrent month spending: Rs "
                + format(
                budget - remaining
        )
                + "\nRemaining: Rs "
                + format(remaining)
                + "\nBudget used: "
                + format(used)
                + "%\n\n"
                + "You still have "
                + format(100.0 - used)
                + "% of your monthly budget available.";
    }

    // =====================================================
    // HEALTH ADVICE
    // =====================================================

    private String getHealthAdvice(
            FinancialAnalysis analysis) {

        int score =
                analysis.getFinancialHealthScore();

        if (score >= 80) {

            return "🎯 Your financial health score is "
                    + score
                    + "/100. Your spending and savings "
                    + "pattern currently looks healthy. "
                    + "Keep monitoring "
                    + analysis.getHighestCategory()
                    + " spending.";
        }

        if (score >= 60) {

            return "💡 Your financial health score is "
                    + score
                    + "/100. Your finances are reasonably "
                    + "stable, but you have room to improve "
                    + "your savings and control "
                    + analysis.getHighestCategory()
                    + " spending.";
        }

        return "⚠ Your financial health score is "
                + score
                + "/100. Your current spending pattern "
                + "needs attention. Start by reviewing "
                + analysis.getHighestCategory()
                + " and reducing non-essential expenses.";
    }

    // =====================================================
    // GENERAL ADVICE
    // =====================================================

    private String getGeneralAdvice(
            FinancialAnalysis analysis) {

        double income =
                analysis.getTotalIncome();

        double expense =
                analysis.getTotalExpense();

        if (income <= 0 &&
                expense <= 0) {

            return "I don't have enough financial data yet. "
                    + "Start recording your income and expenses "
                    + "and I'll help you understand your financial health.";
        }

        return "Here's your current financial picture:\n\n"
                + "Income: Rs "
                + format(income)
                + "\nExpenses: Rs "
                + format(expense)
                + "\nSavings: Rs "
                + format(
                analysis.getSavings()
        )
                + "\nSavings rate: "
                + format(
                analysis.getSavingsRate()
        )
                + "%\nExpense rate: "
                + format(
                analysis.getExpenseRate()
        )
                + "%\nFinancial health: "
                + analysis.getFinancialHealthScore()
                + "/100\n\n"
                + "Largest spending category: "
                + analysis.getHighestCategory()
                + " (Rs "
                + format(
                analysis.getHighestCategoryAmount()
        )
                + ")";
    }

    // =====================================================
    // FORMAT
    // =====================================================

    private String format(
            double value) {

        return String.format(
                Locale.getDefault(),
                "%,.2f",
                value
        );
    }
}