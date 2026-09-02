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

    public Route route(
            String question
    ) {

        if (
                question == null ||
                        question.trim().isEmpty()
        ) {

            return Route.AI;
        }


        String q =
                question
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        // =================================================
        // LOCAL QUESTIONS
        // =================================================

        if (isLocalQuestion(q)) {

            return Route.LOCAL;
        }


        // =================================================
        // AI QUESTIONS
        // =================================================

        return Route.AI;
    }


    // =====================================================
    // LOCAL QUESTION DETECTION
    // =====================================================

    private boolean isLocalQuestion(
            String q
    ) {

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
                        q.contains("my expenses this month")
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
                        q.contains("highest expense category")
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