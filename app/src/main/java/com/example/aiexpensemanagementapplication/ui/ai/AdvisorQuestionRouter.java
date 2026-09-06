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


        String q =
                normalize(question);


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


            if (
                    matchedCategory != null &&
                            isLocalCategoryQuestion(
                                    q,
                                    matchedCategory
                            )
            ) {

                return Route.LOCAL;
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
                .toLowerCase(
                        Locale.ROOT
                )
                .replaceAll(
                        "\\s+",
                        " "
                );
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
                        q.equals("how much did i spend this month") ||
                        q.equals("how much have i spent this month") ||
                        q.contains("current month expense") ||
                        q.contains("this month expense")
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
                        q.equals("how much did i save") ||
                        q.equals("how much have i saved") ||
                        q.equals("how much money did i save")
        ) {

            return true;
        }


        // =================================================
        // SAVINGS RATE
        // =================================================

        if (
                q.equals("savings rate") ||
                        q.equals("my savings rate") ||
                        q.equals("what is my savings rate") ||
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
                        q.equals("where did i spend the most") ||
                        q.equals("where do i spend the most") ||
                        q.equals("what did i spend the most on") ||
                        q.contains("highest spending") ||
                        q.contains("spent the most")
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
                        q.equals("what is my financial health") ||
                        q.equals("how is my financial health") ||
                        q.contains("financial health score")
        ) {

            return true;
        }


        // =================================================
        // EXPENSE CHANGE
        // =================================================

        if (
                q.equals("expense change") ||
                        q.equals("expense change percentage") ||
                        q.equals("how much did my expenses change") ||
                        q.equals("did my expenses increase") ||
                        q.equals("did my expenses decrease") ||
                        q.contains("expense change") ||
                        q.contains("compared to last month") ||
                        q.contains("compared with last month")
        ) {

            return true;
        }


        // =================================================
        // BUDGET
        // =================================================

        if (
                q.equals("budget") ||
                        q.equals("my budget") ||
                        q.equals("budget status") ||
                        q.equals("how is my budget") ||
                        q.equals("am i within my budget") ||
                        q.equals("am i within budget") ||
                        q.equals("am i over budget") ||
                        q.contains("budget")
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
                        q.equals(
                                "how much money is left in my budget"
                        ) ||
                        q.contains("remaining budget") ||
                        q.contains("budget left")
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
        // FIRST: EXACT CATEGORY MATCH
        // =================================================

        for (
                String category
                : categories
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
        // SECOND: CATEGORY AS PHRASE
        // =================================================

        String bestMatch = null;

        int bestLength = 0;


        for (
                String category
                : categories
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

                /*
                 * Prefer the longest category name.
                 *
                 * Example:
                 *
                 * "Food"
                 * "Fast Food"
                 *
                 * Question:
                 * "How much did I spend on Fast Food?"
                 *
                 * → Fast Food
                 */

                if (
                        normalizedCategory.length()
                                > bestLength
                ) {

                    bestMatch =
                            category;

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
        // EXPLICIT AI / ADVICE LANGUAGE
        //
        // These MUST remain AI.
        // =================================================

        if (containsAdviceIntent(question)) {

            return false;
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


        // =================================================
        // CATEGORY + SIMPLE FACTUAL WORD
        // =================================================

        String normalizedCategory =
                normalize(category);


        if (
                question.equals(
                        normalizedCategory
                )
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

        String[] adviceWords = {

                "why",

                "how can",

                "how do i",

                "should i",

                "should",

                "can i reduce",

                "reduce",

                "save",

                "saving",

                "improve",

                "better",

                "advice",

                "recommend",

                "recommendation",

                "analyze",

                "analysis",

                "too much",

                "too high",

                "high spending",

                "overspending",

                "afford",

                "habit",

                "problem",

                "help me",

                "what should",

                "what can i",

                "ways to"
        };


        for (
                String word
                : adviceWords
        ) {

            if (
                    question.contains(word)
            ) {

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
                " "
                        + text.trim()
                        + " ";


        String paddedPhrase =
                " "
                        + phrase.trim()
                        + " ";


        return paddedText.contains(
                paddedPhrase
        );
    }
}