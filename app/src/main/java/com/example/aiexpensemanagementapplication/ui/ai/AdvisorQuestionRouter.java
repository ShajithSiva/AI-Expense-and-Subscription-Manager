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

        String q = normalize(question);

        if (q.isEmpty()) {
            return Route.AI;
        }

        // =================================================
        // LOCAL FACTUAL QUESTION
        // =================================================

        if (isLocalQuestion(q)) {
            return Route.LOCAL;
        }

        // =================================================
        // DEFAULT → AI / QWEN3
        // =================================================

        return Route.AI;
    }


    // =====================================================
    // NORMALIZE QUESTION
    // =====================================================

    private String normalize(String question) {

        return question
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[?!.,]+$", "")
                .replaceAll("\\s+", " ");
    }


    // =====================================================
    // LOCAL QUESTION DETECTION
    // =====================================================

    private boolean isLocalQuestion(String q) {

        // =================================================
        // TOTAL INCOME
        // =================================================

        if (
                q.equals("income") ||
                        q.equals("my income") ||
                        q.equals("total income") ||
                        q.equals("my total income") ||
                        q.equals("income total") ||
                        q.equals("how much income") ||
                        q.equals("how much did i earn") ||
                        q.equals("how much have i earned") ||
                        q.equals("how much money did i earn") ||
                        q.equals("how much money have i earned") ||
                        q.contains("total income")
        ) {
            return true;
        }


        // =================================================
        // TOTAL EXPENSE
        // =================================================

        if (
                q.equals("expense") ||
                        q.equals("expenses") ||
                        q.equals("my expense") ||
                        q.equals("my expenses") ||
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
                        q.equals("how much money did i spend") ||
                        q.equals("how much money have i spent") ||
                        q.contains("total expense") ||
                        q.contains("total expenses")
        ) {
            return true;
        }


        // =================================================
        // BALANCE
        // =================================================

        if (
                q.equals("balance") ||
                        q.equals("my balance") ||
                        q.equals("current balance") ||
                        q.equals("my current balance") ||
                        q.equals("what is my balance") ||
                        q.equals("what's my balance") ||
                        q.equals("what is my current balance") ||
                        q.equals("what's my current balance") ||
                        q.equals("how much balance") ||
                        q.equals("how much money do i have") ||
                        q.equals("how much money is left") ||
                        q.equals("money left")
        ) {
            return true;
        }


        // =================================================
        // CURRENT MONTH EXPENSE
        // =================================================

        if (
                q.equals("current month expense") ||
                        q.equals("current month expenses") ||
                        q.equals("this month expense") ||
                        q.equals("this month expenses") ||
                        q.equals("my current month expense") ||
                        q.equals("my current month expenses") ||
                        q.equals("my spending this month") ||
                        q.equals("my expenses this month") ||
                        q.equals("this month's expenses") ||
                        q.equals("this month spending") ||
                        q.equals("how much did i spend this month") ||
                        q.equals("how much have i spent this month") ||
                        (
                                q.contains("this month") &&
                                        (
                                                q.contains("spend") ||
                                                        q.contains("spent") ||
                                                        q.contains("expense") ||
                                                        q.contains("spending")
                                        )
                        )
        ) {
            return true;
        }


        // =================================================
        // SAVINGS
        // =================================================

        if (
                q.equals("savings") ||
                        q.equals("my savings") ||
                        q.equals("total savings") ||
                        q.equals("my total savings") ||
                        q.equals("how much did i save") ||
                        q.equals("how much have i saved") ||
                        q.equals("how much money did i save") ||
                        q.equals("how much money have i saved") ||
                        q.contains("how much savings") ||
                        q.contains("my savings")
        ) {
            return true;
        }


        // =================================================
        // SAVINGS RATE
        // =================================================

        if (
                q.equals("savings rate") ||
                        q.equals("my savings rate") ||
                        q.equals("saving rate") ||
                        q.equals("my saving rate") ||
                        q.equals("what is my savings rate") ||
                        q.equals("what's my savings rate") ||
                        q.equals("what is my saving rate") ||
                        q.equals("what's my saving rate") ||
                        q.contains("savings percentage") ||
                        q.contains("saving percentage")
        ) {
            return true;
        }


        // =================================================
        // HIGHEST SPENDING CATEGORY
        // =================================================

        if (
                q.equals("highest spending category") ||
                        q.equals("highest expense category") ||
                        q.equals("most expensive category") ||
                        q.equals("biggest expense category") ||
                        q.equals("biggest spending category") ||
                        q.equals("where did i spend the most") ||
                        q.equals("where do i spend the most") ||
                        q.equals("what did i spend the most on") ||
                        q.equals("what do i spend the most on") ||
                        q.contains("highest spending") ||
                        q.contains("highest expense category") ||
                        q.contains("spent the most") ||
                        q.contains("spend the most") ||
                        q.contains("most spending") ||
                        q.contains("biggest expense") ||
                        q.contains("biggest spending")
        ) {
            return true;
        }


        // =================================================
        // FINANCIAL HEALTH
        // =================================================

        if (
                q.equals("financial health") ||
                        q.equals("financial health score") ||
                        q.equals("my financial health") ||
                        q.equals("my financial health score") ||
                        q.equals("what is my financial health") ||
                        q.equals("what's my financial health") ||
                        q.equals("what is my financial health score") ||
                        q.equals("what's my financial health score") ||
                        q.equals("how is my financial health") ||
                        q.contains("financial health score") ||
                        q.contains("health score")
        ) {
            return true;
        }


        // =================================================
        // EXPENSE CHANGE
        // =================================================

        if (
                q.equals("expense change") ||
                        q.equals("expense change percentage") ||
                        q.equals("expense change percent") ||
                        q.equals("how much did my expenses change") ||
                        q.equals("how much have my expenses changed") ||
                        q.equals("did my expenses increase") ||
                        q.equals("did my expenses decrease") ||
                        q.equals("did my spending increase") ||
                        q.equals("did my spending decrease") ||
                        q.contains("expense change") ||
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
        // REMAINING BUDGET
        // =================================================

        if (
                q.equals("remaining budget") ||
                        q.equals("budget remaining") ||
                        q.equals("how much budget is left") ||
                        q.equals("how much budget is remaining") ||
                        q.equals("how much money is left in my budget") ||
                        q.equals("how much money remains in my budget") ||
                        q.contains("remaining budget") ||
                        q.contains("budget left") ||
                        (
                                q.contains("budget") &&
                                        (
                                                q.contains("remaining") ||
                                                        q.contains("left")
                                        )
                        )
        ) {
            return true;
        }


        // =================================================
        // BUDGET STATUS
        // =================================================

        if (
                q.equals("budget") ||
                        q.equals("my budget") ||
                        q.equals("budget status") ||
                        q.equals("my budget status") ||
                        q.equals("how is my budget") ||
                        q.equals("am i within my budget") ||
                        q.equals("am i within budget") ||
                        q.equals("am i over budget") ||
                        q.equals("am i under budget") ||
                        q.equals("did i exceed my budget") ||
                        q.equals("have i exceeded my budget") ||
                        q.contains("within my budget") ||
                        q.contains("within budget")
        ) {
            return true;
        }


        // =================================================
        // CATEGORY BREAKDOWN
        // =================================================

        if (
                q.contains("spending by category") ||
                        q.contains("expenses by category") ||
                        q.contains("expense by category") ||
                        q.contains("category spending") ||
                        q.contains("category expenses") ||
                        q.contains("breakdown by category") ||
                        q.contains("expense breakdown") ||
                        q.contains("spending breakdown")
        ) {
            return true;
        }


        // =================================================
        // SPECIFIC CATEGORY AMOUNT
        // =================================================

        if (isCategoryAmountQuestion(q)) {
            return true;
        }


        // =================================================
        // STANDALONE CATEGORY
        //
        // Example:
        // Food
        // Transport
        // Shopping
        // Bills
        // =================================================

        if (isStandaloneCategory(q)) {
            return true;
        }


        // =================================================
        // NOT LOCAL → AI
        // =================================================

        return false;
    }


    // =====================================================
    // CATEGORY AMOUNT QUESTION
    // =====================================================

    private boolean isCategoryAmountQuestion(String q) {

        // =================================================
        // EXPLANATION / ADVICE QUESTIONS MUST GO TO AI
        // =================================================

        if (
                q.contains("why") ||
                        q.contains("how can i") ||
                        q.contains("how do i") ||
                        q.contains("should i") ||
                        q.contains("what should") ||
                        q.contains("recommend") ||
                        q.contains("recommendation") ||
                        q.contains("advice") ||
                        q.contains("reduce") ||
                        q.contains("improve") ||
                        q.contains("save more")
        ) {
            return false;
        }


        // =================================================
        // MUST ASK ABOUT A FACTUAL AMOUNT
        // =================================================

        boolean asksAmount =
                q.contains("how much") ||
                        q.contains("amount") ||
                        q.contains("spent") ||
                        q.contains("spending") ||
                        q.contains("expense") ||
                        q.contains("expenses");


        if (!asksAmount) {
            return false;
        }


        // =================================================
        // COMMON CATEGORY WORDS
        // =================================================

        boolean containsCategory =
                q.contains("food") ||
                        q.contains("grocery") ||
                        q.contains("groceries") ||
                        q.contains("transport") ||
                        q.contains("transportation") ||
                        q.contains("travel") ||
                        q.contains("shopping") ||
                        q.contains("bills") ||
                        q.contains("bill") ||
                        q.contains("utilities") ||
                        q.contains("utility") ||
                        q.contains("rent") ||
                        q.contains("housing") ||
                        q.contains("health") ||
                        q.contains("medical") ||
                        q.contains("education") ||
                        q.contains("entertainment") ||
                        q.contains("dining") ||
                        q.contains("restaurant") ||
                        q.contains("fuel") ||
                        q.contains("pet") ||
                        q.contains("pets") ||
                        q.contains("other") ||
                        q.contains("others");


        return containsCategory;
    }


    // =====================================================
    // STANDALONE CATEGORY
    // =====================================================

    private boolean isStandaloneCategory(String q) {

        return
                q.equals("food") ||
                        q.equals("grocery") ||
                        q.equals("groceries") ||
                        q.equals("transport") ||
                        q.equals("transportation") ||
                        q.equals("travel") ||
                        q.equals("shopping") ||
                        q.equals("bills") ||
                        q.equals("bill") ||
                        q.equals("utilities") ||
                        q.equals("utility") ||
                        q.equals("rent") ||
                        q.equals("housing") ||
                        q.equals("health") ||
                        q.equals("medical") ||
                        q.equals("education") ||
                        q.equals("entertainment") ||
                        q.equals("dining") ||
                        q.equals("restaurant") ||
                        q.equals("fuel") ||
                        q.equals("pet") ||
                        q.equals("pets") ||
                        q.equals("other") ||
                        q.equals("others");
    }
}