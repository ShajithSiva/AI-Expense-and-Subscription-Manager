package com.example.aiexpensemanagementapplication.ui.ai;

import java.util.Collection;
import java.util.Locale;

public class AdvisorQuestionRouter {

    // =====================================================
    // ROUTE
    // =====================================================

    public enum Route {
        LOCAL,
        AI
    }


    // =====================================================
    // MAIN ROUTER
    // =====================================================

    public Route route(
            String question,
            Collection<String> categories
    ) {

        if (
                question == null ||
                        question.trim().isEmpty()
        ) {
            return Route.AI;
        }


        String q = normalize(question);


        // =================================================
        // IMPORTANT:
        // Advice / reasoning questions MUST go to AI.
        //
        // Example:
        // "Why is my food spending high?"
        // "How can I improve my budget?"
        // =================================================

        if (containsAdviceIntent(q)) {
            return Route.AI;
        }


        // =================================================
        // STANDARD LOCAL FINANCIAL QUESTIONS
        // =================================================

        if (isStandardLocalQuestion(q)) {
            return Route.LOCAL;
        }


        // =================================================
        // DYNAMIC CATEGORY ROUTING
        // =================================================

        if (
                categories != null &&
                        !categories.isEmpty()
        ) {

            String matchedCategory =
                    findMatchingCategory(
                            q,
                            categories
                    );


            if (matchedCategory != null) {

                if (
                        isLocalCategoryQuestion(
                                q,
                                matchedCategory
                        )
                ) {

                    return Route.LOCAL;
                }
            }
        }


        // =================================================
        // EVERYTHING ELSE → AI
        // =================================================

        return Route.AI;
    }


    // =====================================================
    // BACKWARD-COMPATIBLE ROUTER
    // =====================================================

    public Route route(
            String question
    ) {

        return route(
                question,
                null
        );
    }


    // =====================================================
    // NORMALIZE
    // =====================================================

    private String normalize(
            String question
    ) {

        return question
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .replaceAll("[?!.,]+$", "");
    }


    // =====================================================
    // STANDARD LOCAL QUESTIONS
    // =====================================================

    private boolean isStandardLocalQuestion(
            String q
    ) {

        // =================================================
        // TOTAL INCOME
        // =================================================

        if (
                q.equals("income") ||
                        q.equals("my income") ||
                        q.equals("total income") ||
                        q.equals("total incomes") ||
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
                        q.equals("spending") ||
                        q.equals("my expense") ||
                        q.equals("my expenses") ||
                        q.equals("my spending") ||
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
                        q.equals("how much money have i saved")
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
        // HIGHEST SPENDING
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
                        q.contains("spending breakdown") ||
                        q.contains("show categories") ||
                        q.contains("show category spending") ||
                        q.contains("show category expenses")
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
                        q.contains("budget left")
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


        return false;
    }


    // =====================================================
    // FIND MATCHING DATABASE CATEGORY
    // =====================================================

    private String findMatchingCategory(
            String question,
            Collection<String> categories
    ) {

        if (
                question == null ||
                        categories == null
        ) {
            return null;
        }


        String normalizedQuestion =
                normalize(question);


        // =================================================
        // EXACT MATCH FIRST
        // =================================================

        for (
                String category : categories
        ) {

            if (
                    category == null ||
                            category.trim().isEmpty()
            ) {
                continue;
            }


            String normalizedCategory =
                    normalize(category);


            if (
                    normalizedQuestion.equals(
                            normalizedCategory
                    )
            ) {
                return category;
            }
        }


        // =================================================
        // LONGEST PHRASE MATCH
        // =================================================

        String bestMatch = null;

        int bestLength = 0;


        for (
                String category : categories
        ) {

            if (
                    category == null ||
                            category.trim().isEmpty()
            ) {
                continue;
            }


            String normalizedCategory =
                    normalize(category);


            if (
                    containsWholePhrase(
                            normalizedQuestion,
                            normalizedCategory
                    )
            ) {

                if (
                        normalizedCategory.length()
                                > bestLength
                ) {

                    bestMatch = category;

                    bestLength =
                            normalizedCategory.length();
                }
            }
        }


        return bestMatch;
    }


    // =====================================================
    // CATEGORY QUESTION TYPE
    // =====================================================

    private boolean isLocalCategoryQuestion(
            String question,
            String category
    ) {

        if (
                question == null ||
                        category == null
        ) {
            return false;
        }


        // =================================================
        // STANDALONE CATEGORY
        // =================================================

        if (
                normalize(question)
                        .equals(
                                normalize(category)
                        )
        ) {
            return true;
        }


        // =================================================
        // FACTUAL CATEGORY QUESTION
        // =================================================

        if (
                question.contains("how much") ||
                        question.contains("how many") ||
                        question.contains("amount") ||
                        question.contains("spent") ||
                        question.contains("spend") ||
                        question.contains("expense") ||
                        question.contains("expenses") ||
                        question.contains("cost") ||
                        question.contains("total")
        ) {
            return true;
        }


        return false;
    }


    // =====================================================
    // ADVICE / REASONING INTENT
    // =====================================================

    private boolean containsAdviceIntent(
            String question
    ) {

        if (
                question == null ||
                        question.trim().isEmpty()
        ) {
            return false;
        }


        String[] adviceWords = {

                // Reasoning
                "why",

                // Advice
                "how can",
                "how do i",
                "should i",
                "should",
                "advice",
                "recommend",
                "recommendation",

                // Improvement
                "improve",
                "better",
                "manage",
                "optimize",

                // Saving / reducing
                "can i reduce",
                "reduce",
                "save more",
                "saving more",
                "cut down",

                // Analysis
                "analyze",
                "analysis",

                // Problem / concern
                "too much",
                "too high",
                "overspending",
                "afford",
                "habit",
                "problem",

                // Help
                "help me",

                // Suggestions
                "what should",
                "what can i",
                "ways to",
                "tips"
        };


        for (
                String word : adviceWords
        ) {

            if (question.contains(word)) {
                return true;
            }
        }


        return false;
    }


    // =====================================================
    // WHOLE PHRASE MATCH
    // =====================================================

    private boolean containsWholePhrase(
            String text,
            String phrase
    ) {

        if (
                text == null ||
                        phrase == null ||
                        phrase.trim().isEmpty()
        ) {
            return false;
        }


        String paddedText =
                " " +
                        text.trim() +
                        " ";


        String paddedPhrase =
                " " +
                        phrase.trim() +
                        " ";


        return paddedText.contains(
                paddedPhrase
        );
    }
}