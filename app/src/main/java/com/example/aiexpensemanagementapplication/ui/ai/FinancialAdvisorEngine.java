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


        // =================================================
        // CURRENT MONTH
        // =================================================

        Calendar calendar =
                Calendar.getInstance();

        String currentMonth =
                String.format(
                        Locale.getDefault(),
                        "%04d-%02d",
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1
                );


        // =================================================
        // TOTAL INCOME
        // =================================================

        double income =
                databaseHelper.getMonthlyIncome(
                        userId,
                        currentMonth
                );


        // =================================================
        // TOTAL EXPENSE
        // =================================================

        double expense =
                databaseHelper.getMonthlyExpense(
                        userId,
                        currentMonth
                );


        // =================================================
        // BASIC FINANCIAL VALUES
        // =================================================

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


        // =================================================
        // RATES
        // =================================================

        double expenseRate =
                0.0;

        double savingsRate =
                0.0;

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
        // BUDGET
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

            Calendar calendar =
                    Calendar.getInstance();


            String currentMonth =
                    String.format(
                            Locale.getDefault(),
                            "%04d-%02d",
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1
                    );


            // =================================================
            // GET BUDGET
            // =================================================

            Budget budget =
                    databaseHelper.getBudgetSettings(
                            userId
                    );


            // =================================================
            // NO BUDGET
            // =================================================

            if (budget == null) {

                analysis.setBudget(
                        0.0
                );

                analysis.setBudgetUsed(
                        0.0
                );

                analysis.setRemainingBudget(
                        0.0
                );

                return;
            }


            // =================================================
            // MONTHLY BUDGET
            // =================================================

            double monthlyBudget =
                    budget.getMonthlyBudget();


            // =================================================
            // CURRENT MONTH EXPENSE
            // =================================================

            double currentMonthExpense =
                    databaseHelper.getMonthlyExpense(
                            userId,
                            currentMonth
                    );


            // =================================================
            // SAVE BUDGET
            // =================================================

            analysis.setBudget(
                    monthlyBudget
            );


            // =================================================
            // BUDGET USED
            // =================================================

            double budgetUsed =
                    0.0;

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


            // =================================================
            // REMAINING BUDGET
            // =================================================

            double remainingBudget =
                    monthlyBudget
                            - currentMonthExpense;


            analysis.setRemainingBudget(
                    remainingBudget
            );


        } catch (Exception e) {

            e.printStackTrace();


            analysis.setBudget(
                    0.0
            );

            analysis.setBudgetUsed(
                    0.0
            );

            analysis.setRemainingBudget(
                    0.0
            );
        }
    }


    // =====================================================
    // CATEGORY ANALYSIS
    // =====================================================

    private void loadCategoryAnalysis(
            int userId,
            FinancialAnalysis analysis) {

        Cursor cursor =
                null;

        try {

            // =================================================
            // CLEAR OLD CATEGORY DATA
            // =================================================

            analysis.clearCategoryTotals();


            // =================================================
            // GET CATEGORY EXPENSE DATA
            // =================================================

            cursor =
                    databaseHelper.getExpenseByCategory(
                            userId
                    );


            if (cursor == null) {

                setHighestCategory(
                        analysis
                );

                return;
            }


            // =================================================
            // FIND CATEGORY ID COLUMN
            // =================================================

            int categoryIdIndex =
                    cursor.getColumnIndex(
                            "CategoryID"
                    );


            if (categoryIdIndex == -1) {

                categoryIdIndex =
                        cursor.getColumnIndex(
                                "CATEGORY_ID"
                        );
            }


            if (categoryIdIndex == -1) {

                categoryIdIndex =
                        cursor.getColumnIndex(
                                "category_id"
                        );
            }


            // =================================================
            // FIND TOTAL COLUMN
            // =================================================

            int totalIndex =
                    cursor.getColumnIndex(
                            "Total"
                    );


            if (totalIndex == -1) {

                totalIndex =
                        cursor.getColumnIndex(
                                "TOTAL"
                        );
            }


            if (totalIndex == -1) {

                totalIndex =
                        cursor.getColumnIndex(
                                "total"
                        );
            }


            // =================================================
            // INVALID CURSOR
            // =================================================

            if (
                    categoryIdIndex == -1 ||
                            totalIndex == -1
            ) {

                setHighestCategory(
                        analysis
                );

                return;
            }


            // =================================================
            // READ CATEGORY TOTALS
            // =================================================

            while (cursor.moveToNext()) {

                int categoryId =
                        cursor.getInt(
                                categoryIdIndex
                        );


                double amount =
                        cursor.getDouble(
                                totalIndex
                        );


                // Ignore zero/negative values

                if (amount <= 0) {
                    continue;
                }


                // =================================================
                // GET CATEGORY NAME
                // =================================================

                String categoryName =
                        getCategoryName(
                                categoryId
                        );


                if (
                        categoryName == null ||
                                categoryName.trim().isEmpty()
                ) {

                    categoryName =
                            "Other";
                }


                categoryName =
                        categoryName.trim();


                // =================================================
                // ADD CATEGORY AMOUNT
                // =================================================

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


        // =================================================
        // FIND HIGHEST CATEGORY
        // =================================================

        setHighestCategory(
                analysis
        );
    }


    // =====================================================
    // FIND HIGHEST CATEGORY
    // =====================================================

    private void setHighestCategory(
            FinancialAnalysis analysis) {

        String highestCategory =
                "Other";


        double highestAmount =
                0.0;


        Map<String, Double> categoryTotals =
                analysis.getCategoryTotals();


        if (categoryTotals != null) {

            for (
                    Map.Entry<String, Double> entry
                    : categoryTotals.entrySet()
            ) {

                if (
                        entry.getKey() == null ||
                                entry.getValue() == null
                ) {
                    continue;
                }


                double amount =
                        entry.getValue();


                if (amount > highestAmount) {

                    highestAmount =
                            amount;

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

        Cursor cursor =
                null;


        try {

            cursor =
                    databaseHelper.getExpenseCategories();


            if (cursor == null) {

                return "Other";
            }


            // =================================================
            // CATEGORY ID COLUMN
            // =================================================

            int idIndex =
                    cursor.getColumnIndex(
                            "CategoryID"
                    );


            if (idIndex == -1) {

                idIndex =
                        cursor.getColumnIndex(
                                "CATEGORY_ID"
                        );
            }


            if (idIndex == -1) {

                idIndex =
                        cursor.getColumnIndex(
                                "category_id"
                        );
            }


            // =================================================
            // CATEGORY NAME COLUMN
            // =================================================

            int nameIndex =
                    cursor.getColumnIndex(
                            "CategoryName"
                    );


            if (nameIndex == -1) {

                nameIndex =
                        cursor.getColumnIndex(
                                "CATEGORY_NAME"
                        );
            }


            if (nameIndex == -1) {

                nameIndex =
                        cursor.getColumnIndex(
                                "category_name"
                        );
            }


            if (
                    idIndex == -1 ||
                            nameIndex == -1
            ) {

                return "Other";
            }


            // =================================================
            // FIND CATEGORY
            // =================================================

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


                    if (
                            name != null &&
                                    !name.trim().isEmpty()
                    ) {

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


        // =================================================
        // CURRENT MONTH
        // =================================================

        String currentMonth =
                String.format(
                        Locale.getDefault(),
                        "%04d-%02d",
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1
                );


        // =================================================
        // PREVIOUS MONTH
        // =================================================

        calendar.add(
                Calendar.MONTH,
                -1
        );


        String previousMonth =
                String.format(
                        Locale.getDefault(),
                        "%04d-%02d",
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1
                );


        // =================================================
        // CURRENT EXPENSE
        // =================================================

        double currentExpense =
                databaseHelper.getMonthlyExpense(
                        userId,
                        currentMonth
                );


        // =================================================
        // PREVIOUS EXPENSE
        // =================================================

        double previousExpense =
                databaseHelper.getMonthlyExpense(
                        userId,
                        previousMonth
                );


        // =================================================
        // CURRENT INCOME
        // =================================================

        double currentIncome =
                databaseHelper.getMonthlyIncome(
                        userId,
                        currentMonth
                );


        // =================================================
        // PREVIOUS INCOME
        // =================================================

        double previousIncome =
                databaseHelper.getMonthlyIncome(
                        userId,
                        previousMonth
                );


        // =================================================
        // SAVE TREND DATA
        // =================================================

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


        // =================================================
        // EXPENSE CHANGE
        // =================================================

        double expenseChange =
                0.0;


        if (previousExpense > 0) {

            expenseChange =
                    (
                            (
                                    currentExpense
                                            - previousExpense
                            )
                                    / previousExpense
                    ) * 100.0;
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


        int score =
                100;


        // =================================================
        // EXPENSE RATE
        // =================================================

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


        // =================================================
        // SAVINGS RATE
        // =================================================

        double savingsRate =
                analysis.getSavingsRate();


        if (savingsRate < 0) {

            score -= 30;

        } else if (savingsRate < 5) {

            score -= 20;

        } else if (savingsRate < 10) {

            score -= 10;
        }


        // =================================================
        // EXPENSE TREND
        // =================================================

        double expenseChange =
                analysis.getExpenseChangePercentage();


        if (expenseChange > 30) {

            score -= 10;

        } else if (expenseChange > 15) {

            score -= 5;
        }


        // =================================================
        // BUDGET
        // =================================================

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


        // =================================================
        // LIMIT SCORE
        // =================================================

        if (score < 0) {
            score = 0;
        }


        if (score > 100) {
            score = 100;
        }


        return score;
    }


    // =====================================================
    // LOCAL QUESTION ANSWER
    // =====================================================

    public String answerQuestion(
            FinancialAnalysis analysis,
            String question) {

        if (analysis == null) {

            return "I don't have enough financial data "
                    + "to provide an answer yet.";
        }


        if (
                question == null ||
                        question.trim().isEmpty()
        ) {

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


        // =================================================
        // TOTAL INCOME
        // =================================================

        if (
                q.contains("total income") ||
                        q.equals("income") ||
                        q.contains("my income") ||
                        q.contains("how much did i earn") ||
                        q.contains("how much have i earned")
        ) {

            return "Your total income is Rs "
                    + format(
                    analysis.getTotalIncome()
            )
                    + ".";
        }


        // =================================================
        // TOTAL EXPENSE
        // =================================================

        if (
                q.contains("total expense") ||
                        q.contains("total expenses") ||
                        q.equals("expense") ||
                        q.equals("expenses") ||
                        q.contains("my expense") ||
                        q.contains("my expenses")
        ) {

            return "Your total expenses are Rs "
                    + format(
                    analysis.getTotalExpense()
            )
                    + ".";
        }


        // =================================================
        // BALANCE
        // =================================================

        if (
                q.equals("balance") ||
                        q.contains("my balance") ||
                        q.contains("current balance") ||
                        q.contains("what is my balance") ||
                        q.contains("what's my balance") ||
                        q.contains("how much money do i have") ||
                        q.contains("how much money is left") ||
                        q.contains("money left")
        ) {

            double balance =
                    analysis.getTotalIncome()
                            - analysis.getTotalExpense();


            return "Your current balance is Rs "
                    + format(balance)
                    + ".";
        }


        // =================================================
        // SAVINGS
        // =================================================

        if (
                q.equals("savings") ||
                        q.contains("my savings") ||
                        q.contains("how much did i save") ||
                        q.contains("how much have i saved")
        ) {

            return "Your current savings are Rs "
                    + format(
                    analysis.getSavings()
            )
                    + ".";
        }


        // =================================================
        // SAVINGS RATE
        // =================================================

        if (
                q.contains("savings rate") ||
                        q.contains("saving rate") ||
                        q.contains("percentage saved") ||
                        q.contains("percentage of income saved")
        ) {

            return "Your savings rate is "
                    + format(
                    analysis.getSavingsRate()
            )
                    + "%.";
        }


        // =================================================
        // CURRENT MONTH EXPENSE
        // =================================================

        if (
                q.contains("this month") &&
                        (
                                q.contains("expense") ||
                                        q.contains("expenses") ||
                                        q.contains("spending") ||
                                        q.contains("spent")
                        )
        ) {

            return "Your current month spending is Rs "
                    + format(
                    analysis.getCurrentMonthExpense()
            )
                    + ".";
        }


        // =================================================
        // BUDGET
        // =================================================

        if (
                q.equals("budget") ||
                        q.contains("my budget") ||
                        q.contains("monthly budget")
        ) {

            double budget =
                    analysis.getBudget();


            if (budget <= 0) {

                return "You don't have an active monthly "
                        + "budget recorded.";
            }


            return "Your monthly budget is Rs "
                    + format(budget)
                    + ". You have Rs "
                    + format(
                    analysis.getRemainingBudget()
            )
                    + " remaining.";
        }


        // =================================================
        // REMAINING BUDGET
        // =================================================

        if (
                q.contains("remaining budget") ||
                        q.contains("budget remaining") ||
                        q.contains("how much budget is left") ||
                        q.contains("how much of my budget is left")
        ) {

            double budget =
                    analysis.getBudget();


            if (budget <= 0) {

                return "You don't have an active monthly "
                        + "budget recorded.";
            }


            return "You have Rs "
                    + format(
                    analysis.getRemainingBudget()
            )
                    + " remaining from your monthly budget.";
        }


        // =================================================
        // BUDGET STATUS
        // =================================================

        if (
                q.contains("within my budget") ||
                        q.contains("over my budget") ||
                        q.contains("over budget") ||
                        q.contains("budget status") ||
                        q.contains("am i within budget")
        ) {

            double budget =
                    analysis.getBudget();


            if (budget <= 0) {

                return "You don't have an active monthly "
                        + "budget recorded.";
            }


            double remaining =
                    analysis.getRemainingBudget();


            if (remaining < 0) {

                return "You are over your budget by Rs "
                        + format(
                        Math.abs(remaining)
                )
                        + ".";
            }


            return "You are currently within your budget. "
                    + "You have Rs "
                    + format(remaining)
                    + " remaining.";
        }


        // =================================================
        // HIGHEST SPENDING CATEGORY
        // =================================================

        if (
                q.contains("highest spending") ||
                        q.contains("highest expense") ||
                        q.contains("most spending") ||
                        q.contains("most expensive category") ||
                        q.contains("largest expense") ||
                        q.contains("largest spending")
        ) {

            return "Your highest spending category is "
                    + analysis.getHighestCategory()
                    + " with Rs "
                    + format(
                    analysis.getHighestCategoryAmount()
            )
                    + " spent.";
        }


        // =================================================
        // SHOW ALL CATEGORIES
        // =================================================

        if (
                q.contains("spending by category") ||
                        q.contains("expenses by category") ||
                        q.contains("expense by category") ||
                        q.contains("category spending") ||
                        q.contains("category expenses") ||
                        q.contains("show categories") ||
                        q.contains("show my categories") ||
                        q.contains("show category spending") ||
                        q.contains("show category expenses") ||
                        q.contains("breakdown by category")
        ) {

            return getCategoryBreakdown(
                    analysis
            );
        }


        // =================================================
        // SPECIFIC CATEGORY
        // =================================================

        String category =
                findCategoryInQuestion(
                        analysis,
                        q
                );


        if (category != null) {

            double amount =
                    analysis.getCategoryAmount(
                            category
                    );


            return "You spent Rs "
                    + format(amount)
                    + " on "
                    + category
                    + ".";
        }


        // =================================================
        // FINANCIAL HEALTH
        // =================================================

        if (
                q.contains("financial health") ||
                        q.contains("health score") ||
                        q.contains("financial score") ||
                        q.equals("health") ||
                        q.equals("score")
        ) {

            return "Your financial health score is "
                    + analysis.getFinancialHealthScore()
                    + "/100.";
        }


        // =================================================
        // EXPENSE TREND
        // =================================================

        if (
                q.contains("expense change") ||
                        q.contains("expense trend") ||
                        q.contains("spending trend") ||
                        q.contains("last month") ||
                        q.contains("increased") ||
                        q.contains("decreased")
        ) {

            return getTrendAdvice(
                    analysis
            );
        }


        // =================================================
        // NO LOCAL ANSWER
        // =================================================

        return null;
    }


    // =====================================================
    // FIND CATEGORY IN QUESTION
    // =====================================================

    private String findCategoryInQuestion(
            FinancialAnalysis analysis,
            String question) {

        Map<String, Double> categoryTotals =
                analysis.getCategoryTotals();


        if (
                categoryTotals == null ||
                        categoryTotals.isEmpty()
        ) {

            return null;
        }


        for (
                String category
                : categoryTotals.keySet()
        ) {

            if (
                    category == null ||
                            category.trim().isEmpty()
            ) {
                continue;
            }


            String categoryLower =
                    category
                            .trim()
                            .toLowerCase(
                                    Locale.getDefault()
                            );


            if (
                    question.contains(
                            categoryLower
                    )
            ) {

                return category;
            }
        }


        return null;
    }


    // =====================================================
    // CATEGORY BREAKDOWN
    // =====================================================

    private String getCategoryBreakdown(
            FinancialAnalysis analysis) {

        Map<String, Double> categoryTotals =
                analysis.getCategoryTotals();


        if (
                categoryTotals == null ||
                        categoryTotals.isEmpty()
        ) {

            return "I don't have any category spending "
                    + "data available locally.";
        }


        StringBuilder response =
                new StringBuilder();


        response.append(
                "Your spending by category:\n\n"
        );


        for (
                Map.Entry<String, Double> entry
                : categoryTotals.entrySet()
        ) {

            if (
                    entry.getKey() == null ||
                            entry.getValue() == null
            ) {
                continue;
            }


            response.append(
                    "• "
            );


            response.append(
                    entry.getKey()
            );


            response.append(
                    ": Rs "
            );


            response.append(
                    format(
                            entry.getValue()
                    )
            );


            response.append(
                    "\n"
            );
        }


        return response
                .toString()
                .trim();
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
                    + "expense data to calculate a reliable "
                    + "spending trend.";
        }


        if (change > 0) {

            return "Your expenses increased by "
                    + format(change)
                    + "% compared with last month. "
                    + "This month you spent Rs "
                    + format(current)
                    + ", compared with Rs "
                    + format(previous)
                    + " last month.";
        }


        if (change < 0) {

            return "Your expenses decreased by "
                    + format(
                    Math.abs(change)
            )
                    + "% compared with last month. "
                    + "This month you spent Rs "
                    + format(current)
                    + ".";
        }


        return "Your expenses are approximately the same "
                + "as last month at Rs "
                + format(current)
                + ".";
    }


    // =====================================================
    // GENERAL ADVICE
    // =====================================================

    private String getGeneralAdvice(
            FinancialAnalysis analysis) {

        return "Income: Rs "
                + format(
                analysis.getTotalIncome()
        )
                + "\nExpenses: Rs "
                + format(
                analysis.getTotalExpense()
        )
                + "\nSavings: Rs "
                + format(
                analysis.getSavings()
        )
                + "\nSavings rate: "
                + format(
                analysis.getSavingsRate()
        )
                + "%\nBudget: Rs "
                + format(
                analysis.getBudget()
        )
                + "\nRemaining budget: Rs "
                + format(
                analysis.getRemainingBudget()
        )
                + "\nHighest category: "
                + analysis.getHighestCategory()
                + " (Rs "
                + format(
                analysis.getHighestCategoryAmount()
        )
                + ")";
    }


    // =====================================================
    // FORMAT MONEY
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