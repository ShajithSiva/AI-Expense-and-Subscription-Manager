package com.example.aiexpensemanagementapplication.ui.ai;

import java.util.Locale;

public class FinancialAdvisorAIService {

    // =====================================================
    // CALLBACK
    // =====================================================

    public interface AdvisorCallback {

        void onSuccess(String response);

        void onFailure(String message);
    }

    // =====================================================
    // GENERATE ADVICE
    // =====================================================

    public void generateAdvice(
            FinancialAnalysis analysis,
            String question,
            AdvisorCallback callback) {

        if (analysis == null) {

            callback.onFailure(
                    "Financial analysis is not available."
            );

            return;
        }

        if (question == null ||
                question.trim().isEmpty()) {

            callback.onFailure(
                    "Please enter a financial question."
            );

            return;
        }

        /*
         * Phase 3 currently prepares the complete
         * financial context.
         *
         * The actual AI API connection will be added
         * in the next step.
         */

        String prompt =
                buildFinancialPrompt(
                        analysis,
                        question
                );

        /*
         * Temporary:
         *
         * We use the existing rule-based engine
         * until the AI API layer is connected.
         */

        callback.onSuccess(
                buildTemporaryResponse(
                        analysis,
                        question
                )
        );
    }

    // =====================================================
    // BUILD FINANCIAL PROMPT
    // =====================================================

    private String buildFinancialPrompt(
            FinancialAnalysis analysis,
            String question) {

        StringBuilder prompt =
                new StringBuilder();

        prompt.append(
                "You are a personal financial advisor.\n\n"
        );

        prompt.append(
                "Use the following user's financial data "
                        + "to provide personalized financial guidance.\n\n"
        );

        // -------------------------------------------------
        // INCOME
        // -------------------------------------------------

        prompt.append(
                "Income: Rs "
                        + format(
                        analysis.getTotalIncome()
                )
                        + "\n"
        );

        // -------------------------------------------------
        // EXPENSE
        // -------------------------------------------------

        prompt.append(
                "Expenses: Rs "
                        + format(
                        analysis.getTotalExpense()
                )
                        + "\n"
        );

        // -------------------------------------------------
        // SAVINGS
        // -------------------------------------------------

        prompt.append(
                "Savings: Rs "
                        + format(
                        analysis.getSavings()
                )
                        + "\n"
        );

        prompt.append(
                "Savings rate: "
                        + format(
                        analysis.getSavingsRate()
                )
                        + "%\n"
        );

        // -------------------------------------------------
        // EXPENSE RATE
        // -------------------------------------------------

        prompt.append(
                "Expense rate: "
                        + format(
                        analysis.getExpenseRate()
                )
                        + "%\n"
        );

        // -------------------------------------------------
        // BUDGET
        // -------------------------------------------------

        prompt.append(
                "Monthly budget: Rs "
                        + format(
                        analysis.getBudget()
                )
                        + "\n"
        );

        prompt.append(
                "Budget used: "
                        + format(
                        analysis.getBudgetUsed()
                )
                        + "%\n"
        );

        prompt.append(
                "Remaining budget: Rs "
                        + format(
                        analysis.getRemainingBudget()
                )
                        + "\n"
        );

        // -------------------------------------------------
        // HIGHEST CATEGORY
        // -------------------------------------------------

        prompt.append(
                "Highest spending category: "
                        + analysis.getHighestCategory()
                        + "\n"
        );

        prompt.append(
                "Highest category spending: Rs "
                        + format(
                        analysis.getHighestCategoryAmount()
                )
                        + "\n"
        );

        // -------------------------------------------------
        // MONTHLY TREND
        // -------------------------------------------------

        prompt.append(
                "Current month expenses: Rs "
                        + format(
                        analysis.getCurrentMonthExpense()
                )
                        + "\n"
        );

        prompt.append(
                "Previous month expenses: Rs "
                        + format(
                        analysis.getPreviousMonthExpense()
                )
                        + "\n"
        );

        prompt.append(
                "Expense change: "
                        + format(
                        analysis.getExpenseChangePercentage()
                )
                        + "%\n"
        );

        // -------------------------------------------------
        // HEALTH
        // -------------------------------------------------

        prompt.append(
                "Financial health score: "
                        + analysis.getFinancialHealthScore()
                        + "/100\n\n"
        );

        // -------------------------------------------------
        // QUESTION
        // -------------------------------------------------

        prompt.append(
                "User question:\n"
                        + question.trim()
                        + "\n\n"
        );

        // -------------------------------------------------
        // INSTRUCTIONS
        // -------------------------------------------------

        prompt.append(
                "Give practical, personalized financial "
                        + "guidance based only on the supplied "
                        + "financial information. "
                        + "Do not invent transactions, income, "
                        + "expenses, or budgets. "
                        + "Use Sri Lankan Rupees (Rs) when "
                        + "mentioning money. "
                        + "Keep the response clear and useful. "
                        + "Do not present yourself as a licensed "
                        + "financial professional."
        );

        return prompt.toString();
    }

    // =====================================================
    // TEMPORARY RESPONSE
    // =====================================================

    private String buildTemporaryResponse(
            FinancialAnalysis analysis,
            String question) {

        String q =
                question
                        .trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        if (q.contains("budget")) {

            return buildBudgetResponse(
                    analysis
            );
        }

        if (q.contains("saving") ||
                q.contains("save")) {

            return buildSavingsResponse(
                    analysis
            );
        }

        if (q.contains("spending") ||
                q.contains("expense") ||
                q.contains("spend")) {

            return buildSpendingResponse(
                    analysis
            );
        }

        return buildGeneralResponse(
                analysis
        );
    }

    // =====================================================
    // BUDGET
    // =====================================================

    private String buildBudgetResponse(
            FinancialAnalysis analysis) {

        double budget =
                analysis.getBudget();

        double remaining =
                analysis.getRemainingBudget();

        double used =
                analysis.getBudgetUsed();

        if (budget <= 0) {

            return "I couldn't find an active monthly "
                    + "budget for your account yet.";
        }

        if (remaining < 0) {

            return "You're currently over your monthly "
                    + "budget by Rs "
                    + format(
                    Math.abs(remaining)
            )
                    + ". Your budget usage is "
                    + format(used)
                    + "%. I recommend reviewing your "
                    + analysis.getHighestCategory()
                    + " spending.";
        }

        return "Yes, you're currently within your budget. "
                + "You've used "
                + format(used)
                + "% of your Rs "
                + format(budget)
                + " monthly budget and have Rs "
                + format(remaining)
                + " remaining.";
    }

    // =====================================================
    // SAVINGS
    // =====================================================

    private String buildSavingsResponse(
            FinancialAnalysis analysis) {

        double savings =
                analysis.getSavings();

        double savingsRate =
                analysis.getSavingsRate();

        if (savings < 0) {

            return "Your expenses are currently higher "
                    + "than your income by Rs "
                    + format(
                    Math.abs(savings)
            )
                    + ". Focus first on reducing "
                    + "non-essential spending.";
        }

        return "You're currently saving approximately "
                + "Rs "
                + format(savings)
                + ", which is "
                + format(savingsRate)
                + "% of your recorded income. "
                + "Your largest spending category is "
                + analysis.getHighestCategory()
                + ", so reviewing that category could "
                + "help increase your savings.";
    }

    // =====================================================
    // SPENDING
    // =====================================================

    private String buildSpendingResponse(
            FinancialAnalysis analysis) {

        return "Your current expenses are approximately "
                + "Rs "
                + format(
                analysis.getTotalExpense()
        )
                + ". Your largest spending category is "
                + analysis.getHighestCategory()
                + " at approximately Rs "
                + format(
                analysis.getHighestCategoryAmount()
        )
                + ".";
    }

    // =====================================================
    // GENERAL
    // =====================================================

    private String buildGeneralResponse(
            FinancialAnalysis analysis) {

        return "Your current financial picture is:\n\n"
                + "Income: Rs "
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
                + "%\nBudget used: "
                + format(
                analysis.getBudgetUsed()
        )
                + "%\nFinancial health: "
                + analysis.getFinancialHealthScore()
                + "/100";
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