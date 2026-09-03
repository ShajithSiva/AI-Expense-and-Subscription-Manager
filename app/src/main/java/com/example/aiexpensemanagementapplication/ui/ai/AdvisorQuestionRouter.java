package com.example.aiexpensemanagementapplication.ui.ai;

import java.util.Locale;

public class AdvisorQuestionRouter {

    // =====================================================
    // ROUTE TYPES
    // =====================================================

    public enum Route {

        LOCAL,

        AI
    }


    // =====================================================
    // ROUTE QUESTION
    // =====================================================

    public Route route(String question) {

        if (question == null) {
            return Route.AI;
        }

        String q = question
                .trim()
                .toLowerCase(Locale.ROOT);

        if (q.isEmpty()) {
            return Route.AI;
        }


        // =====================================================
        // TOTAL INCOME
        // =====================================================

        if (
                q.equals("total income") ||
                        q.equals("my total income") ||
                        q.equals("income total") ||
                        q.equals("how much income") ||
                        q.equals("how much did i earn") ||
                        q.equals("how much have i earned") ||
                        q.equals("how much money did i earn") ||
                        q.equals("how much money have i earned") ||
                        q.equals("my income") ||
                        q.contains("total income")
        ) {

            return Route.LOCAL;
        }


        // =====================================================
        // TOTAL EXPENSE
        // =====================================================

        if (
                q.equals("total expense") ||
                        q.equals("total expenses") ||
                        q.equals("my total expense") ||
                        q.equals("my total expenses") ||
                        q.equals("expense total") ||
                        q.equals("expenses total") ||
                        q.equals("total spending") ||
                        q.equals("my total spending") ||
                        q.equals("how much did i spend") ||
                        q.equals("how much have i spent") ||
                        q.contains("how much money did i spend") ||
                        q.contains("how much money have i spent") ||
                        q.contains("total expense") ||
                        q.contains("total expenses")
        ) {

            return Route.LOCAL;
        }


        // =====================================================
        // BALANCE
        // =====================================================

        if (
                q.equals("balance") ||
                        q.equals("my balance") ||
                        q.equals("what is my balance") ||
                        q.equals("what's my balance") ||
                        q.equals("how much balance") ||
                        q.equals("how much money do i have") ||
                        q.equals("how much money is left") ||
                        q.equals("money left") ||
                        q.contains("current balance")
        ) {

            return Route.LOCAL;
        }


        // =====================================================
        // CURRENT MONTH EXPENSE
        // =====================================================

        if (
                q.equals("current month expense") ||
                        q.equals("current month expenses") ||
                        q.equals("this month expense") ||
                        q.equals("this month expenses") ||
                        q.equals("how much did i spend this month") ||
                        q.equals("how much have i spent this month") ||
                        q.contains("this month")
                                && q.contains("spend")
        ) {

            return Route.LOCAL;
        }


        // =====================================================
        // SAVINGS
        // =====================================================

        if (
                q.equals("savings") ||
                        q.equals("my savings") ||
                        q.equals("total savings") ||
                        q.equals("how much did i save") ||
                        q.equals("how much have i saved") ||
                        q.contains("how much savings") ||
                        q.contains("my savings")
        ) {

            return Route.LOCAL;
        }


        // =====================================================
        // SAVINGS RATE
        // =====================================================

        if (
                q.equals("savings rate") ||
                        q.equals("my savings rate") ||
                        q.equals("what is my savings rate") ||
                        q.contains("savings percentage") ||
                        q.contains("saving percentage")
        ) {

            return Route.LOCAL;
        }


        // =====================================================
        // HIGHEST SPENDING CATEGORY
        // =====================================================

        if (
                q.equals("highest spending category") ||
                        q.equals("highest expense category") ||
                        q.equals("most expensive category") ||
                        q.equals("where did i spend the most") ||
                        q.equals("where do i spend the most") ||
                        q.equals("what did i spend the most on") ||
                        q.contains("highest spending") ||
                        q.contains("spent the most")
        ) {

            return Route.LOCAL;
        }


        // =====================================================
        // FINANCIAL HEALTH
        // =====================================================

        if (
                q.equals("financial health") ||
                        q.equals("financial health score") ||
                        q.equals("my financial health") ||
                        q.equals("what is my financial health") ||
                        q.equals("how is my financial health") ||
                        q.contains("financial health score")
        ) {

            return Route.LOCAL;
        }


        // =====================================================
        // EXPENSE CHANGE
        // =====================================================

        if (
                q.equals("expense change") ||
                        q.equals("expense change percentage") ||
                        q.equals("how much did my expenses change") ||
                        q.equals("did my expenses increase") ||
                        q.equals("did my expenses decrease") ||
                        q.contains("expense change")
        ) {

            return Route.LOCAL;
        }


        // =====================================================
        // BUDGET
        // =====================================================

        if (
                q.equals("budget") ||
                        q.equals("my budget") ||
                        q.equals("budget status") ||
                        q.equals("how is my budget") ||
                        q.equals("am i within my budget") ||
                        q.equals("am i over budget") ||
                        q.contains("budget")
        ) {

            return Route.LOCAL;
        }


        // =====================================================
        // REMAINING BUDGET
        // =====================================================

        if (
                q.equals("remaining budget") ||
                        q.equals("budget remaining") ||
                        q.equals("how much budget is left") ||
                        q.equals("how much money is left in my budget") ||
                        q.contains("remaining budget") ||
                        q.contains("budget left")
        ) {

            return Route.LOCAL;
        }


        // =====================================================
        // CATEGORY AMOUNT
        // =====================================================

        if (
                q.contains("how much")
                        && q.contains("category")
        ) {

            return Route.LOCAL;
        }


        // =====================================================
        // DEFAULT → AI
        // =====================================================

        return Route.AI;
    }


    // =====================================================
    // LOCAL QUESTION DETECTION
    // =====================================================

    private boolean isLocalQuestion(
            String q
    ) {

        // =================================================
        // TOTAL INCOME
        // =================================================

        if (
                q.contains("total income") ||
                        q.contains("my total income") ||
                        q.contains("income total") ||
                        q.contains("how much income") ||
                        q.contains("how much did i earn") ||
                        q.contains("how much have i earned") ||
                        q.contains("how much money did i earn") ||
                        q.contains("how much money have i earned") ||
                        q.contains("my income")
        ) {

            return true;
        }


        // =================================================
        // TOTAL EXPENSE
        // =================================================

        if (
                q.contains("total expense") ||
                        q.contains("total expenses") ||
                        q.contains("my total expense") ||
                        q.contains("my total expenses") ||
                        q.contains("expense total") ||
                        q.contains("expenses total") ||
                        q.equals("how much did i spend") ||
                        q.equals("how much have i spent") ||
                        q.contains("how much money did i spend") ||
                        q.contains("how much money have i spent") ||
                        q.contains("total spending") ||
                        q.contains("my total spending")
        ) {

            return true;
        }


        // =================================================
        // BALANCE
        // =================================================

        if (
                q.equals("balance") ||
                        q.equals("my balance") ||
                        q.contains("what is my balance") ||
                        q.contains("what's my balance") ||
                        q.contains("how much balance") ||
                        q.contains("how much money do i have") ||
                        q.contains("how much money is left") ||
                        q.contains("money left")
        ) {

            return true;
        }


        // =================================================
        // BUDGET
        // =================================================

        if (
                q.contains("am i within my budget") ||
                        q.contains("am i within budget") ||
                        q.contains("within my budget") ||
                        q.contains("within budget")
        ) {

            return true;
        }


        if (
                q.contains("how much budget") &&
                        (
                                q.contains("left") ||
                                        q.contains("remaining")
                        )
        ) {

            return true;
        }


        // =================================================
        // GENERAL EXPENSE
        // =================================================

        if (
                q.contains("how much did i spend") ||
                        q.contains("how much have i spent") ||
                        q.contains("my spending this month") ||
                        q.contains("my expenses this month") ||
                        q.contains("this month's expenses") ||
                        q.contains("this month spending")
        ) {

            return true;
        }


        // =================================================
        // SAVINGS
        // =================================================

        if (
                q.contains("how much did i save") ||
                        q.contains("how much have i saved") ||
                        q.equals("my savings") ||
                        q.contains("my savings")
        ) {

            return true;
        }


        // =================================================
        // HIGHEST CATEGORY
        // =================================================

        if (
                q.contains("highest spending") ||
                        q.contains("most spending") ||
                        q.contains("spend the most") ||
                        q.contains("highest expense category") ||
                        q.contains("biggest expense") ||
                        q.contains("biggest spending")
        ) {

            return true;
        }


        // =================================================
        // FINANCIAL HEALTH
        // =================================================

        if (
                q.contains("financial health score") ||
                        q.equals("financial health") ||
                        q.contains("health score")
        ) {

            return true;
        }


        // =================================================
        // EXPENSE CHANGE
        // =================================================

        if (
                q.contains("expenses increased") ||
                        q.contains("expenses decrease") ||
                        q.contains("expenses decreased") ||
                        q.contains("spending increased") ||
                        q.contains("spending decreased") ||
                        q.contains("compared to last month") ||
                        q.contains("compared with last month")
        ) {

            return true;
        }


        // =================================================
        // CATEGORY FACTUAL QUESTIONS
        // =================================================

        if (isCategoryAmountQuestion(q)) {

            return true;
        }


        return false;
    }


    // =====================================================
    // CATEGORY AMOUNT QUESTIONS
    // =====================================================

    private boolean isCategoryAmountQuestion(
            String q
    ) {

        // -------------------------------------------------
        // MUST ASK ABOUT AN AMOUNT
        // -------------------------------------------------

        boolean asksAmount =
                q.contains("how much") ||
                        q.contains("amount") ||
                        q.contains("spent") ||
                        q.contains("spending") ||
                        q.contains("expense");


        if (!asksAmount) {

            return false;
        }


        // -------------------------------------------------
        // MUST CONTAIN A CATEGORY
        // -------------------------------------------------

        boolean containsCategory =
                q.contains("food") ||
                        q.contains("transport") ||
                        q.contains("transportation") ||
                        q.contains("shopping") ||
                        q.contains("bills") ||
                        q.contains("bill") ||
                        q.contains("health") ||
                        q.contains("education") ||
                        q.contains("entertainment") ||
                        q.contains("other") ||
                        q.contains("others");


        if (!containsCategory) {

            return false;
        }


        // -------------------------------------------------
        // EXPLANATION / ADVICE QUESTIONS
        // -------------------------------------------------

        if (
                q.contains("why") ||
                        q.contains("how can i") ||
                        q.contains("how do i") ||
                        q.contains("should i") ||
                        q.contains("what should") ||
                        q.contains("recommend") ||
                        q.contains("reduce") ||
                        q.contains("improve") ||
                        q.contains("save")
        ) {

            return false;
        }


        return true;
    }
}