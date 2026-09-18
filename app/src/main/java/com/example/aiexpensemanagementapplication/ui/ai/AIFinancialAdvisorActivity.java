package com.example.aiexpensemanagementapplication.ui.ai;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AIFinancialAdvisorActivity
        extends AppCompatActivity {

    // =====================================================
    // TAGS
    // =====================================================

    private static final String TAG =
            "AI_FINANCIAL_ADVISOR";

    private static final String ROUTER_TAG =
            "ADVISOR_ROUTER";

    private static final String INSIGHT_TAG =
            "FINANCIAL_INSIGHTS";

    private static final String AI_INSIGHT_TAG =
            "AI_INSIGHT_RESULT";


    // =====================================================
    // UI
    // =====================================================

    private ProgressBar progressAI;

    private ImageButton btnBack;

    private RecyclerView rvMessages;

    private EditText etMessage;

    private ImageButton btnSend;

    private MaterialButton btnHowSave;

    private MaterialButton btnSpending;

    private MaterialButton btnBudget;


    // =====================================================
    // CHAT DATA
    // =====================================================

    private ArrayList<AdvisorMessage> messages;

    private AdvisorMessageAdapter adapter;


    // =====================================================
    // FINANCIAL ADVISOR
    // =====================================================

    private FinancialAdvisorEngine advisorEngine;

    private FinancialAdvisorApiService apiService;

    private FinancialAnalysis financialAnalysis;

    private AdvisorQuestionRouter questionRouter;


    // =====================================================
    // PROACTIVE INSIGHTS
    // =====================================================

    private ProactiveInsightEngine insightEngine;

    private List<FinancialInsight> proactiveInsights =
            new ArrayList<>();

    private List<AIInsightResult> aiInsightResults =
            new ArrayList<>();


    // =====================================================
    // DATABASE / FIREBASE
    // =====================================================

    private DatabaseHelper databaseHelper;

    private FirebaseAuth firebaseAuth;

    private FirebaseUser currentUser;

    private int currentUserId = -1;


    // =====================================================
    // STATE
    // =====================================================

    private boolean isAIThinking = false;

    private boolean initialInsightsGenerated = false;


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_ai_financial_advisor
        );


        // -------------------------------------------------
        // INITIALIZE
        // -------------------------------------------------

        initialize();


        // -------------------------------------------------
        // RECYCLER VIEW
        // -------------------------------------------------

        setupRecyclerView();


        // -------------------------------------------------
        // LISTENERS
        // -------------------------------------------------

        setupListeners();


        // -------------------------------------------------
        // WELCOME
        // -------------------------------------------------

        showWelcomeMessage();


        // -------------------------------------------------
        // LOAD LOCAL FINANCIAL DATA ONLY
        //
        // IMPORTANT:
        // This does NOT call Qwen3.
        // -------------------------------------------------

        loadFinancialAnalysis(false);
    }


    // =====================================================
    // INITIALIZE
    // =====================================================

    private void initialize() {

        // -------------------------------------------------
        // DATABASE
        // -------------------------------------------------

        databaseHelper =
                new DatabaseHelper(this);


        // -------------------------------------------------
        // FIREBASE
        // -------------------------------------------------

        firebaseAuth =
                FirebaseAuth.getInstance();

        currentUser =
                firebaseAuth.getCurrentUser();


        // -------------------------------------------------
        // FINANCIAL ENGINE
        // -------------------------------------------------

        advisorEngine =
                new FinancialAdvisorEngine(
                        databaseHelper
                );


        // -------------------------------------------------
        // INSIGHT ENGINE
        // -------------------------------------------------

        insightEngine =
                new ProactiveInsightEngine();


        // -------------------------------------------------
        // API SERVICE
        // -------------------------------------------------

        apiService =
                new FinancialAdvisorApiService();


        // -------------------------------------------------
        // QUESTION ROUTER
        // -------------------------------------------------

        questionRouter =
                new AdvisorQuestionRouter();


        // -------------------------------------------------
        // UI
        // -------------------------------------------------

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        progressAI =
                findViewById(
                        R.id.progressAI
                );

        rvMessages =
                findViewById(
                        R.id.rvMessages
                );

        etMessage =
                findViewById(
                        R.id.etMessage
                );

        btnSend =
                findViewById(
                        R.id.btnSend
                );

        btnHowSave =
                findViewById(
                        R.id.btnHowSave
                );

        btnSpending =
                findViewById(
                        R.id.btnSpending
                );

        btnBudget =
                findViewById(
                        R.id.btnBudget
                );


        // -------------------------------------------------
        // INITIAL PROGRESS STATE
        // -------------------------------------------------

        if (progressAI != null) {

            progressAI.setVisibility(
                    View.GONE
            );
        }
    }


    // =====================================================
    // RECYCLER VIEW
    // =====================================================

    private void setupRecyclerView() {

        messages =
                new ArrayList<>();


        adapter =
                new AdvisorMessageAdapter(
                        messages
                );


        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);


        layoutManager.setStackFromEnd(
                false
        );


        rvMessages.setLayoutManager(
                layoutManager
        );


        rvMessages.setAdapter(
                adapter
        );
    }


    // =====================================================
    // LISTENERS
    // =====================================================

    private void setupListeners() {

        // -------------------------------------------------
        // BACK
        // -------------------------------------------------

        btnBack.setOnClickListener(
                v -> finish()
        );


        // -------------------------------------------------
        // SEND
        // -------------------------------------------------

        btnSend.setOnClickListener(
                v -> sendMessage()
        );


        // -------------------------------------------------
        // QUICK QUESTION
        // -------------------------------------------------

        btnHowSave.setOnClickListener(
                v -> sendQuickQuestion(
                        "How can I save more money?"
                )
        );


        btnSpending.setOnClickListener(
                v -> sendQuickQuestion(
                        "Can you analyze my spending?"
                )
        );


        btnBudget.setOnClickListener(
                v -> sendQuickQuestion(
                        "Am I within my budget?"
                )
        );


        // -------------------------------------------------
        // ENTER KEY
        // -------------------------------------------------

        etMessage.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (
                            event != null &&
                                    event.getKeyCode()
                                            == KeyEvent.KEYCODE_ENTER
                    ) {

                        sendMessage();

                        return true;
                    }

                    return false;
                }
        );
    }


    // =====================================================
    // WELCOME MESSAGE
    // =====================================================

    private void showWelcomeMessage() {

        String name =
                "there";


        if (
                currentUser != null &&
                        currentUser.getDisplayName() != null &&
                        !currentUser
                                .getDisplayName()
                                .trim()
                                .isEmpty()
        ) {

            name =
                    currentUser
                            .getDisplayName()
                            .trim();
        }


        String welcome =
                "Hi " + name + "! 👋\n\n"
                        + "I'm your AI Financial Advisor. "
                        + "I can help you understand your "
                        + "spending, budget, savings and "
                        + "financial habits.\n\n"
                        + "Ask me a question or choose "
                        + "one of the suggestions above.";


        addAIMessage(
                welcome
        );
    }


    // =====================================================
    // SEND MESSAGE
    // =====================================================

    private void sendMessage() {

        if (isAIThinking) {
            return;
        }


        String message =
                etMessage
                        .getText()
                        .toString()
                        .trim();


        if (TextUtils.isEmpty(message)) {
            return;
        }


        addUserMessage(
                message
        );


        etMessage.setText("");


        generateAdvisorResponse(
                message
        );
    }


    // =====================================================
    // QUICK QUESTION
    // =====================================================

    private void sendQuickQuestion(
            String question
    ) {

        if (isAIThinking) {
            return;
        }


        addUserMessage(
                question
        );


        generateAdvisorResponse(
                question
        );
    }


    // =====================================================
    // GENERATE ADVISOR RESPONSE
    // =====================================================

    private void generateAdvisorResponse(
            String question
    ) {

        if (isAIThinking) {
            return;
        }


        if (
                question == null ||
                        question.trim().isEmpty()
        ) {

            return;
        }


        // =================================================
        // STEP 1 — ROUTE FIRST
        //
        // IMPORTANT:
        // We determine LOCAL vs AI BEFORE loading anything
        // that could potentially trigger AI.
        // =================================================

        Collection<String> availableCategories =
                financialAnalysis != null &&
                        financialAnalysis.getCategoryTotals() != null
                        ? financialAnalysis.getCategoryTotals().keySet()
                        : null;

        AdvisorQuestionRouter.Route route =
                questionRouter.route(
                        question,
                        availableCategories
                );


        Log.d(
                ROUTER_TAG,
                "Question = "
                        + question
                        + " | Route = "
                        + route
        );


        // =================================================
        // STEP 2 — LOCAL ROUTE
        // =================================================

        if (
                route ==
                        AdvisorQuestionRouter.Route.LOCAL
        ) {

            // -------------------------------------------------
            // Load local financial data only.
            //
            // FALSE = DO NOT CALL QWEN3
            // -------------------------------------------------

            if (financialAnalysis == null) {

                boolean loaded =
                        loadFinancialAnalysis(false);


                if (!loaded) {

                    addAIMessage(
                            "I couldn't load your local "
                                    + "financial data. Please "
                                    + "make sure your account "
                                    + "is synchronized and "
                                    + "try again."
                    );

                    return;
                }
            }


            // -------------------------------------------------
            // Handle locally
            // -------------------------------------------------

            boolean handled =
                    handleLocalFinancialQuestion(
                            question
                    );


            if (handled) {

                return;
            }


            // -------------------------------------------------
            // IMPORTANT
            //
            // Do NOT silently send a LOCAL question to Qwen3.
            // If router says LOCAL but handler cannot answer,
            // tell the user instead.
            // -------------------------------------------------

            addAIMessage(
                    "I recognized this as a local financial "
                            + "question, but I couldn't find "
                            + "the required information in "
                            + "your local financial data."
            );

            return;
        }


        // =================================================
        // STEP 3 — AI ROUTE
        // =================================================

        // -------------------------------------------------
        // Load financial data locally first.
        //
        // FALSE = analysis only.
        // The actual Qwen3 request happens below.
        // -------------------------------------------------

        if (financialAnalysis == null) {

            boolean loaded =
                    loadFinancialAnalysis(false);


            if (!loaded) {

                addAIMessage(
                        "I couldn't load your financial "
                                + "data. Please make sure "
                                + "your account is synchronized "
                                + "and try again."
                );

                return;
            }
        }


        // -------------------------------------------------
        // NOW QWEN3 IS ALLOWED
        // -------------------------------------------------

        showAIThinking(
                true
        );


        apiService.askAdvisor(
                financialAnalysis,
                question,
                getRecentMessages(),

                new FinancialAdvisorApiService.AdvisorCallback() {

                    @Override
                    public void onSuccess(
                            String response
                    ) {

                        runOnUiThread(() -> {

                            showAIThinking(
                                    false
                            );


                            if (
                                    response == null ||
                                            response.trim().isEmpty()
                            ) {

                                addAIMessage(
                                        "The AI returned an "
                                                + "empty response. "
                                                + "Please try again."
                                );

                                return;
                            }


                            addAIMessage(
                                    response.trim()
                            );
                        });
                    }


                    @Override
                    public void onFailure(
                            String message
                    ) {

                        runOnUiThread(() -> {

                            showAIThinking(
                                    false
                            );


                            String error =
                                    message;


                            if (
                                    error == null ||
                                            error.trim().isEmpty()
                            ) {

                                error =
                                        "Unknown error occurred.";
                            }


                            addAIMessage(
                                    "Sorry, I couldn't generate "
                                            + "your financial advice."
                                            + "\n\n"
                                            + error
                            );
                        });
                    }
                }
        );
    }


    // =====================================================
    // LOCAL FINANCIAL QUESTION HANDLER
    // =====================================================

    private boolean handleLocalFinancialQuestion(
            String question
    ) {

        if (
                financialAnalysis == null ||
                        question == null ||
                        question.trim().isEmpty()
        ) {
            return false;
        }


        String q =
                question
                        .trim()
                        .toLowerCase(Locale.ROOT)
                        .replaceAll("\\s+", " ")
                        .replaceAll("[?!.,]+$", "");


        // =====================================================
        // 1. TOTAL INCOME
        // =====================================================

        if (
                q.equals("income") ||
                        q.equals("my income") ||
                        q.equals("total income") ||
                        q.equals("my total income") ||
                        q.contains("how much did i earn") ||
                        q.contains("how much have i earned") ||
                        q.contains("how much money did i earn") ||
                        q.contains("how much money have i earned")
        ) {

            double income =
                    financialAnalysis.getTotalIncome();


            addAIMessage(
                    String.format(
                            Locale.US,
                            "Your total income is Rs %.2f.",
                            income
                    )
            );


            return true;
        }


        // =====================================================
        // 2. TOTAL EXPENSE
        // =====================================================

        if (
                q.equals("expense") ||
                        q.equals("expenses") ||
                        q.equals("my expense") ||
                        q.equals("my expenses") ||
                        q.equals("spending") ||
                        q.equals("my spending") ||
                        q.equals("total expense") ||
                        q.equals("total expenses") ||
                        q.equals("my total expense") ||
                        q.equals("my total expenses") ||
                        q.equals("total spending") ||
                        q.equals("my total spending") ||
                        q.contains("how much did i spend") ||
                        q.contains("how much have i spent") ||
                        q.contains("how much money did i spend") ||
                        q.contains("how much money have i spent")
        ) {

            double expense =
                    financialAnalysis.getTotalExpense();


            addAIMessage(
                    String.format(
                            Locale.US,
                            "Your total expense is Rs %.2f.",
                            expense
                    )
            );


            return true;
        }


        // =====================================================
        // 3. CURRENT BALANCE
        // =====================================================

        if (
                q.equals("balance") ||
                        q.equals("my balance") ||
                        q.equals("current balance") ||
                        q.equals("my current balance") ||
                        q.contains("what is my balance") ||
                        q.contains("what's my balance") ||
                        q.contains("what is my current balance") ||
                        q.contains("what's my current balance") ||
                        q.contains("how much money do i have")
        ) {

            double income =
                    financialAnalysis.getTotalIncome();


            double expense =
                    financialAnalysis.getTotalExpense();


            double balance =
                    income - expense;


            if (balance >= 0) {

                addAIMessage(
                        String.format(
                                Locale.US,
                                "Your current balance is Rs %.2f.",
                                balance
                        )
                );

            } else {

                addAIMessage(
                        String.format(
                                Locale.US,
                                "Your expenses exceed your income "
                                        + "by Rs %.2f.",
                                Math.abs(balance)
                        )
                );
            }


            return true;
        }


        // =====================================================
        // 4. CURRENT MONTH EXPENSE
        // =====================================================

        if (
                q.contains("this month") &&
                        (
                                q.contains("spend") ||
                                        q.contains("spent") ||
                                        q.contains("expense") ||
                                        q.contains("spending")
                        )
        ) {

            double expense =
                    financialAnalysis
                            .getCurrentMonthExpense();


            addAIMessage(
                    String.format(
                            Locale.US,
                            "You've spent Rs %.2f this month.",
                            expense
                    )
            );


            return true;
        }


        // =====================================================
        // 5. SAVINGS
        // =====================================================

        if (
                q.equals("savings") ||
                        q.equals("my savings") ||
                        q.equals("total savings") ||
                        q.equals("my total savings") ||
                        q.contains("how much did i save") ||
                        q.contains("how much have i saved") ||
                        q.contains("how much money did i save") ||
                        q.contains("how much money have i saved")
        ) {

            double savings =
                    financialAnalysis.getSavings();


            addAIMessage(
                    String.format(
                            Locale.US,
                            "You've saved Rs %.2f.",
                            savings
                    )
            );


            return true;
        }


        // =====================================================
        // 6. SAVINGS RATE
        // =====================================================

        if (
                q.equals("savings rate") ||
                        q.equals("my savings rate") ||
                        q.equals("saving rate") ||
                        q.equals("my saving rate") ||
                        q.contains("what is my savings rate") ||
                        q.contains("what's my savings rate") ||
                        q.contains("savings percentage") ||
                        q.contains("saving percentage")
        ) {

            double savingsRate =
                    financialAnalysis.getSavingsRate();


            addAIMessage(
                    String.format(
                            Locale.US,
                            "Your savings rate is %.1f%%.",
                            savingsRate
                    )
            );


            return true;
        }


        // =====================================================
        // 7. REMAINING BUDGET
        // =====================================================

        if (
                q.equals("remaining budget") ||
                        q.equals("budget remaining") ||
                        q.contains("how much budget is left") ||
                        q.contains("how much budget is remaining") ||
                        q.contains("how much money is left in my budget") ||
                        q.contains("how much money remains in my budget")
        ) {

            double budget =
                    financialAnalysis.getBudget();


            double remaining =
                    financialAnalysis.getRemainingBudget();


            if (budget <= 0) {

                addAIMessage(
                        "You don't currently have an active "
                                + "monthly budget."
                );

                return true;
            }


            if (remaining >= 0) {

                addAIMessage(
                        String.format(
                                Locale.US,
                                "You have Rs %.2f remaining from "
                                        + "your monthly budget of "
                                        + "Rs %.2f.",
                                remaining,
                                budget
                        )
                );

            } else {

                addAIMessage(
                        String.format(
                                Locale.US,
                                "You've exceeded your monthly "
                                        + "budget by Rs %.2f.",
                                Math.abs(remaining)
                        )
                );
            }


            return true;
        }


        // =====================================================
        // 8. BUDGET STATUS
        // =====================================================

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
                        q.equals("have i exceeded my budget")
        ) {

            double budget =
                    financialAnalysis.getBudget();


            double expense =
                    financialAnalysis
                            .getCurrentMonthExpense();


            if (budget <= 0) {

                addAIMessage(
                        "You don't currently have an active "
                                + "monthly budget."
                );

                return true;
            }


            double remaining =
                    budget - expense;


            double usedPercentage =
                    (expense / budget) * 100.0;


            if (expense <= budget) {

                addAIMessage(
                        String.format(
                                Locale.US,
                                "You're within your monthly "
                                        + "budget. You've spent "
                                        + "Rs %.2f of Rs %.2f "
                                        + "(%.1f%%), with "
                                        + "Rs %.2f remaining.",
                                expense,
                                budget,
                                usedPercentage,
                                remaining
                        )
                );

            } else {

                double exceeded =
                        expense - budget;


                double exceededPercentage =
                        (exceeded / budget) * 100.0;


                addAIMessage(
                        String.format(
                                Locale.US,
                                "You're over your monthly "
                                        + "budget. You've spent "
                                        + "Rs %.2f against Rs %.2f, "
                                        + "exceeding it by Rs %.2f "
                                        + "(%.1f%%).",
                                expense,
                                budget,
                                exceeded,
                                exceededPercentage
                        )
                );
            }


            return true;
        }


        // =====================================================
        // 9. HIGHEST SPENDING CATEGORY
        // =====================================================

        if (
                q.equals("highest spending category") ||
                        q.equals("highest expense category") ||
                        q.equals("most expensive category") ||
                        q.equals("biggest expense category") ||
                        q.equals("biggest spending category") ||
                        q.contains("where did i spend the most") ||
                        q.contains("where do i spend the most") ||
                        q.contains("what did i spend the most on") ||
                        q.contains("what do i spend the most on")
        ) {

            String category =
                    financialAnalysis
                            .getHighestCategory();


            double amount =
                    financialAnalysis
                            .getHighestCategoryAmount();


            if (
                    category == null ||
                            category.trim().isEmpty()
            ) {

                addAIMessage(
                        "I couldn't determine your highest "
                                + "spending category."
                );

                return true;
            }


            addAIMessage(
                    String.format(
                            Locale.US,
                            "Your highest spending category "
                                    + "is %s, with Rs %.2f spent.",
                            category,
                            amount
                    )
            );


            return true;
        }


        // =====================================================
        // 10. EXPENSE CHANGE
        // =====================================================

        if (
                q.equals("expense change") ||
                        q.equals("expense change percentage") ||
                        q.equals("expense change percent") ||
                        q.contains("how much did my expenses change") ||
                        q.contains("how much have my expenses changed") ||
                        q.contains("did my expenses increase") ||
                        q.contains("did my expenses decrease") ||
                        q.contains("did my spending increase") ||
                        q.contains("did my spending decrease") ||
                        q.contains("compared to last month") ||
                        q.contains("compared with last month")
        ) {

            double change =
                    financialAnalysis
                            .getExpenseChangePercentage();


            if (change > 0) {

                addAIMessage(
                        String.format(
                                Locale.US,
                                "Your expenses increased by "
                                        + "%.1f%% compared with "
                                        + "the previous month.",
                                change
                        )
                );

            } else if (change < 0) {

                addAIMessage(
                        String.format(
                                Locale.US,
                                "Your expenses decreased by "
                                        + "%.1f%% compared with "
                                        + "the previous month.",
                                Math.abs(change)
                        )
                );

            } else {

                addAIMessage(
                        "Your expenses are approximately "
                                + "the same as the previous month."
                );
            }


            return true;
        }


        // =====================================================
        // 11. CATEGORY BREAKDOWN
        // =====================================================

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

            Map<String, Double> categoryTotals =
                    financialAnalysis
                            .getCategoryTotals();


            if (
                    categoryTotals == null ||
                            categoryTotals.isEmpty()
            ) {

                addAIMessage(
                        "I don't have any category spending "
                                + "data available locally."
                );

                return true;
            }


            StringBuilder response =
                    new StringBuilder(
                            "Your spending by category:\n\n"
                    );


            for (
                    Map.Entry<String, Double> entry
                    : categoryTotals.entrySet()
            ) {

                if (
                        entry.getKey() == null ||
                                entry.getKey().trim().isEmpty()
                ) {
                    continue;
                }


                double amount =
                        entry.getValue() == null
                                ? 0.0
                                : entry.getValue();


                response.append(
                        String.format(
                                Locale.US,
                                "• %s: Rs %.2f\n",
                                entry.getKey(),
                                amount
                        )
                );
            }


            addAIMessage(
                    response.toString().trim()
            );


            return true;
        }


        // =====================================================
        // 12. SPECIFIC CATEGORY
        // =====================================================

        String requestedCategory =
                findRequestedCategory(
                        q
                );


        if (requestedCategory != null) {

            Double amount =
                    getCategoryAmount(
                            requestedCategory
                    );


            if (amount != null) {

                addAIMessage(
                        String.format(
                                Locale.US,
                                "You've spent Rs %.2f on %s.",
                                amount,
                                requestedCategory
                        )
                );


                return true;
            }


            addAIMessage(
                    "I couldn't find that category in "
                            + "your local expense data."
            );


            return true;
        }


        // =====================================================
        // NOT HANDLED
        // =====================================================

        return false;
    }


    // =====================================================
// FIND REQUESTED CATEGORY - DYNAMIC
// =====================================================

    private String findRequestedCategory(
            String question
    ) {

        if (
                question == null ||
                        financialAnalysis == null ||
                        financialAnalysis.getCategoryTotals() == null
        ) {
            return null;
        }

        String q =
                question
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (q.isEmpty()) {
            return null;
        }


        // =================================================
        // 1. EXACT CATEGORY MATCH
        // =================================================

        for (
                String category
                : financialAnalysis
                .getCategoryTotals()
                .keySet()
        ) {

            if (category == null) {
                continue;
            }

            String actualCategory =
                    category.trim();

            if (actualCategory.isEmpty()) {
                continue;
            }

            if (
                    q.equals(
                            actualCategory.toLowerCase(
                                    Locale.ROOT
                            )
                    )
            ) {

                return actualCategory;
            }
        }


        // =================================================
        // 2. FIND LONGEST CATEGORY MATCH
        //
        // Example:
        //
        // Food
        // Fast Food
        //
        // "How much did I spend on Fast Food?"
        //
        // → Fast Food
        // =================================================

        String bestCategory = null;

        int bestLength = 0;


        for (
                String category
                : financialAnalysis
                .getCategoryTotals()
                .keySet()
        ) {

            if (category == null) {
                continue;
            }

            String actualCategory =
                    category.trim();

            if (actualCategory.isEmpty()) {
                continue;
            }


            String normalizedCategory =
                    actualCategory
                            .toLowerCase(Locale.ROOT);


            if (
                    containsWholePhrase(
                            q,
                            normalizedCategory
                    )
            ) {

                if (
                        normalizedCategory.length()
                                > bestLength
                ) {

                    bestCategory =
                            actualCategory;

                    bestLength =
                            normalizedCategory.length();
                }
            }
        }


        if (bestCategory != null) {

            return bestCategory;
        }


        // =================================================
        // 3. CATEGORY ALIASES
        // =================================================

        return findCategoryUsingAlias(q);
    }
    // =====================================================
// WHOLE PHRASE MATCH
// =====================================================

    private boolean containsWholePhrase(
            String question,
            String phrase
    ) {

        if (
                question == null ||
                        phrase == null ||
                        phrase.trim().isEmpty()
        ) {

            return false;
        }


        String normalizedQuestion =
                " "
                        + question
                        .trim()
                        .toLowerCase(Locale.ROOT)
                        + " ";


        String normalizedPhrase =
                " "
                        + phrase
                        .trim()
                        .toLowerCase(Locale.ROOT)
                        + " ";


        return normalizedQuestion.contains(
                normalizedPhrase
        );
    }

    // =====================================================
// CATEGORY ALIAS MATCHING
// =====================================================

    private String findCategoryUsingAlias(
            String question
    ) {

        if (
                financialAnalysis == null ||
                        financialAnalysis.getCategoryTotals() == null
        ) {

            return null;
        }


        // =================================================
        // TRANSPORT
        // =================================================

        if (
                question.contains("transportation") ||
                        question.contains("travel")
        ) {

            String category =
                    findActualCategoryByName(
                            "transport"
                    );

            if (category != null) {
                return category;
            }

            category =
                    findActualCategoryByName(
                            "transportation"
                    );

            if (category != null) {
                return category;
            }
        }


        // =================================================
        // BILL
        // =================================================

        if (
                question.contains("bill") ||
                        question.contains("bills")
        ) {

            String category =
                    findActualCategoryByName(
                            "bill"
                    );

            if (category != null) {
                return category;
            }

            category =
                    findActualCategoryByName(
                            "bills"
                    );

            if (category != null) {
                return category;
            }

            category =
                    findActualCategoryByName(
                            "utilities"
                    );

            if (category != null) {
                return category;
            }
        }


        // =================================================
        // FOOD
        // =================================================

        if (
                question.contains("food") ||
                        question.contains("meals") ||
                        question.contains("meal")
        ) {

            String category =
                    findActualCategoryByName(
                            "food"
                    );

            if (category != null) {
                return category;
            }
        }


        // =================================================
        // SHOPPING
        // =================================================

        if (
                question.contains("shopping") ||
                        question.contains("shop")
        ) {

            String category =
                    findActualCategoryByName(
                            "shopping"
                    );

            if (category != null) {
                return category;
            }
        }


        return null;
    }

    // =====================================================
// FIND ACTUAL CATEGORY BY NAME
// =====================================================

    private String findActualCategoryByName(
            String requestedName
    ) {

        if (
                requestedName == null ||
                        financialAnalysis == null ||
                        financialAnalysis.getCategoryTotals() == null
        ) {

            return null;
        }


        String normalizedRequested =
                requestedName
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        for (
                String category
                : financialAnalysis
                .getCategoryTotals()
                .keySet()
        ) {

            if (category == null) {
                continue;
            }


            String normalizedActual =
                    category
                            .trim()
                            .toLowerCase(
                                    Locale.ROOT
                            );


            if (
                    normalizedActual.equals(
                            normalizedRequested
                    )
            ) {

                return category;
            }
        }


        return null;
    }


    // =====================================================
    // FIND ACTUAL CATEGORY
    // =====================================================

    private String findActualCategory(
            String requestedCategory
    ) {

        if (
                financialAnalysis == null ||
                        financialAnalysis
                                .getCategoryTotals() == null ||
                        requestedCategory == null
        ) {

            return null;
        }


        String requested =
                requestedCategory
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        for (
                String category
                : financialAnalysis
                .getCategoryTotals()
                .keySet()
        ) {

            if (category == null) {
                continue;
            }


            String normalized =
                    category
                            .trim()
                            .toLowerCase(
                                    Locale.ROOT
                            );


            if (
                    normalized.equals(
                            requested
                    )
            ) {

                return category;
            }


            if (
                    requested.equals("transport") &&
                            (
                                    normalized.equals("transportation") ||
                                            normalized.equals("transport")
                            )
            ) {

                return category;
            }


            if (
                    requested.equals("bill") &&
                            (
                                    normalized.equals("bills") ||
                                            normalized.equals("bill")
                            )
            ) {

                return category;
            }


            if (
                    requested.equals("other") &&
                            (
                                    normalized.equals("others") ||
                                            normalized.equals("other")
                            )
            ) {

                return category;
            }
        }


        return null;
    }


    // =====================================================
    // CATEGORY AMOUNT
    // =====================================================

    private Double getCategoryAmount(
            String category
    ) {

        if (
                financialAnalysis == null ||
                        category == null ||
                        financialAnalysis
                                .getCategoryTotals() == null
        ) {

            return null;
        }


        Double amount =
                financialAnalysis
                        .getCategoryTotals()
                        .get(category);


        if (amount == null) {

            return 0.0;
        }


        return amount;
    }


    // =====================================================
    // RECENT CHAT MESSAGES
    // =====================================================

    private ArrayList<AdvisorMessage>
    getRecentMessages() {

        ArrayList<AdvisorMessage> recent =
                new ArrayList<>();


        int start =
                Math.max(
                        0,
                        messages.size() - 10
                );


        for (
                int i = start;
                i < messages.size();
                i++
        ) {

            recent.add(
                    messages.get(i)
            );
        }


        return recent;
    }


    // =====================================================
    // LOAD FINANCIAL ANALYSIS
    // =====================================================

    private boolean loadFinancialAnalysis() {

        return loadFinancialAnalysis(
                false
        );
    }


    // =====================================================
    // LOAD FINANCIAL ANALYSIS
    //
    // allowAIInsights:
    //
    // true  → allow proactive Qwen3 generation
    // false → LOCAL DATA ONLY
    //
    // For advisor questions we use FALSE.
    // This guarantees LOCAL questions don't trigger Qwen3.
    // =====================================================

    private boolean loadFinancialAnalysis(
            boolean allowAIInsights
    ) {

        // -------------------------------------------------
        // FIREBASE USER
        // -------------------------------------------------

        if (currentUser == null) {

            Log.e(
                    TAG,
                    "Firebase user is null."
            );

            return false;
        }


        // -------------------------------------------------
        // EMAIL
        // -------------------------------------------------

        if (
                currentUser.getEmail() == null ||
                        currentUser
                                .getEmail()
                                .trim()
                                .isEmpty()
        ) {

            Log.e(
                    TAG,
                    "Firebase user email unavailable."
            );

            return false;
        }


        String email =
                currentUser
                        .getEmail()
                        .trim();


        // -------------------------------------------------
        // LOCAL USER
        // -------------------------------------------------

        currentUserId =
                databaseHelper.getUserIdByEmail(
                        email
                );


        Log.d(
                TAG,
                "Firebase email = "
                        + email
        );


        Log.d(
                TAG,
                "Local user ID = "
                        + currentUserId
        );


        if (currentUserId == -1) {

            Log.e(
                    TAG,
                    "Local user profile not found."
            );

            return false;
        }


        // =================================================
        // ANALYZE LOCAL DATA
        // =================================================

        try {

            financialAnalysis =
                    advisorEngine.analyzeUser(
                            currentUserId
                    );


            if (financialAnalysis == null) {

                Log.e(
                        TAG,
                        "FinancialAnalysis is null."
                );

                return false;
            }


            // =================================================
            // LOCAL PROACTIVE INSIGHTS
            //
            // This is completely local and does not use Qwen3.
            // =================================================

            proactiveInsights =
                    insightEngine.analyze(
                            financialAnalysis
                    );


            if (proactiveInsights == null) {

                proactiveInsights =
                        new ArrayList<>();
            }


            // -------------------------------------------------
            // LOG DATA
            // -------------------------------------------------

            logFinancialAnalysis();


            logProactiveInsights();


            // =================================================
            // AI PROACTIVE INSIGHTS
            //
            // ONLY when explicitly allowed.
            // =================================================

            if (
                    allowAIInsights &&
                            !initialInsightsGenerated &&
                            !proactiveInsights.isEmpty()
            ) {

                initialInsightsGenerated =
                        true;


                generateAIProactiveInsights();
            }


            return true;

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Error analyzing financial data.",
                    e
            );


            financialAnalysis =
                    null;


            proactiveInsights =
                    new ArrayList<>();


            return false;
        }
    }


    // =====================================================
    // LOG FINANCIAL ANALYSIS
    // =====================================================

    private void logFinancialAnalysis() {

        if (financialAnalysis == null) {
            return;
        }


        Log.d(
                TAG,
                "======================================"
        );


        Log.d(
                TAG,
                "FINANCIAL ANALYSIS"
        );


        Log.d(
                TAG,
                "======================================"
        );


        Log.d(
                TAG,
                "Income = Rs "
                        + financialAnalysis
                        .getTotalIncome()
        );


        Log.d(
                TAG,
                "Expense = Rs "
                        + financialAnalysis
                        .getTotalExpense()
        );


        Log.d(
                TAG,
                "Savings = Rs "
                        + financialAnalysis
                        .getSavings()
        );


        Log.d(
                TAG,
                "Savings Rate = "
                        + financialAnalysis
                        .getSavingsRate()
                        + "%"
        );


        Log.d(
                TAG,
                "Budget = Rs "
                        + financialAnalysis
                        .getBudget()
        );


        Log.d(
                TAG,
                "Budget Used = "
                        + financialAnalysis
                        .getBudgetUsed()
                        + "%"
        );


        Log.d(
                TAG,
                "Remaining Budget = Rs "
                        + financialAnalysis
                        .getRemainingBudget()
        );


        Log.d(
                TAG,
                "Current Month Expense = Rs "
                        + financialAnalysis
                        .getCurrentMonthExpense()
        );


        Log.d(
                TAG,
                "Previous Month Expense = Rs "
                        + financialAnalysis
                        .getPreviousMonthExpense()
        );


        Log.d(
                TAG,
                "Expense Change = "
                        + financialAnalysis
                        .getExpenseChangePercentage()
                        + "%"
        );


        Log.d(
                TAG,
                "Highest Category = "
                        + financialAnalysis
                        .getHighestCategory()
        );


        Log.d(
                TAG,
                "Highest Category Amount = Rs "
                        + financialAnalysis
                        .getHighestCategoryAmount()
        );


        Log.d(
                TAG,
                "Financial Health Score = "
                        + financialAnalysis
                        .getFinancialHealthScore()
                        + "/100"
        );


        Log.d(
                TAG,
                "Category Totals = "
                        + financialAnalysis
                        .getCategoryTotals()
        );


        Log.d(
                TAG,
                "======================================"
        );
    }


    // =====================================================
    // LOG PROACTIVE INSIGHTS
    // =====================================================

    private void logProactiveInsights() {

        Log.d(
                INSIGHT_TAG,
                "======================================"
        );


        Log.d(
                INSIGHT_TAG,
                "PROACTIVE INSIGHTS"
        );


        Log.d(
                INSIGHT_TAG,
                "Count = "
                        + proactiveInsights.size()
        );


        if (proactiveInsights.isEmpty()) {

            Log.d(
                    INSIGHT_TAG,
                    "No proactive insights detected."
            );

            return;
        }


        for (
                FinancialInsight insight
                : proactiveInsights
        ) {

            if (insight == null) {
                continue;
            }


            Log.d(
                    INSIGHT_TAG,
                    "Type = "
                            + insight.getType()
            );


            Log.d(
                    INSIGHT_TAG,
                    "Title = "
                            + insight.getTitle()
            );


            Log.d(
                    INSIGHT_TAG,
                    "Message = "
                            + insight.getMessage()
            );


            Log.d(
                    INSIGHT_TAG,
                    "----------------------------------"
            );
        }
    }


    // =====================================================
    // GENERATE AI PROACTIVE INSIGHTS
    // =====================================================

    private void generateAIProactiveInsights() {

        if (financialAnalysis == null) {

            Log.d(
                    AI_INSIGHT_TAG,
                    "Financial analysis unavailable."
            );

            return;
        }


        if (
                proactiveInsights == null ||
                        proactiveInsights.isEmpty()
        ) {

            Log.d(
                    AI_INSIGHT_TAG,
                    "No proactive insights to explain."
            );

            return;
        }


        Log.d(
                AI_INSIGHT_TAG,
                "Sending "
                        + proactiveInsights.size()
                        + " detected insights to AI."
        );


        apiService.generateProactiveInsights(

                financialAnalysis,

                proactiveInsights,

                new FinancialAdvisorApiService.InsightCallback() {

                    @Override
                    public void onSuccess(
                            List<AIInsightResult> results
                    ) {

                        runOnUiThread(() -> {

                            if (results == null) {

                                aiInsightResults =
                                        new ArrayList<>();

                            } else {

                                aiInsightResults =
                                        results;
                            }


                            Log.d(
                                    AI_INSIGHT_TAG,
                                    "AI insight results = "
                                            + aiInsightResults.size()
                            );


                            for (
                                    AIInsightResult result
                                    : aiInsightResults
                            ) {

                                if (result == null) {
                                    continue;
                                }


                                Log.d(
                                        AI_INSIGHT_TAG,
                                        "TITLE = "
                                                + result.getTitle()
                                );


                                Log.d(
                                        AI_INSIGHT_TAG,
                                        "MESSAGE = "
                                                + result.getMessage()
                                );


                                Log.d(
                                        AI_INSIGHT_TAG,
                                        "----------------------------------"
                                );
                            }
                        });
                    }


                    @Override
                    public void onFailure(
                            String message
                    ) {

                        Log.e(
                                AI_INSIGHT_TAG,
                                "AI proactive insight "
                                        + "generation failed: "
                                        + message
                        );
                    }
                }
        );
    }


    // =====================================================
    // ADD USER MESSAGE
    // =====================================================

    private void addUserMessage(
            String message
    ) {

        if (
                adapter == null ||
                        message == null
        ) {

            return;
        }


        adapter.addMessage(
                new AdvisorMessage(
                        message,
                        AdvisorMessage.TYPE_USER
                )
        );


        scrollToBottom();
    }


    // =====================================================
    // ADD AI MESSAGE
    // =====================================================

    private void addAIMessage(
            String message
    ) {

        if (
                adapter == null ||
                        message == null
        ) {

            return;
        }


        adapter.addMessage(
                new AdvisorMessage(
                        message,
                        AdvisorMessage.TYPE_AI
                )
        );


        scrollToBottom();
    }


    // =====================================================
    // SCROLL TO BOTTOM
    // =====================================================

    private void scrollToBottom() {

        if (
                rvMessages == null ||
                        adapter == null
        ) {

            return;
        }


        rvMessages.post(() -> {

            int position =
                    adapter.getItemCount() - 1;


            if (position >= 0) {

                rvMessages.smoothScrollToPosition(
                        position
                );
            }
        });
    }


    // =====================================================
    // AI THINKING
    // =====================================================

    private void showAIThinking(
            boolean show
    ) {

        isAIThinking =
                show;


        // -------------------------------------------------
        // PROGRESS
        // -------------------------------------------------

        if (progressAI != null) {

            progressAI.setVisibility(
                    show
                            ? View.VISIBLE
                            : View.GONE
            );
        }


        // -------------------------------------------------
        // SEND BUTTON
        // -------------------------------------------------

        if (btnSend != null) {

            btnSend.setEnabled(
                    !show
            );


            btnSend.setAlpha(
                    show
                            ? 0.5f
                            : 1.0f
            );
        }


        // -------------------------------------------------
        // INPUT
        // -------------------------------------------------

        if (etMessage != null) {

            etMessage.setEnabled(
                    !show
            );
        }


        // -------------------------------------------------
        // QUICK BUTTONS
        // -------------------------------------------------

        if (btnHowSave != null) {

            btnHowSave.setEnabled(
                    !show
            );
        }


        if (btnSpending != null) {

            btnSpending.setEnabled(
                    !show
            );
        }


        if (btnBudget != null) {

            btnBudget.setEnabled(
                    !show
            );
        }
    }


    // =====================================================
    // ON RESUME
    // =====================================================

    @Override
    protected void onResume() {

        super.onResume();


        /*
         * Refresh local financial calculations.
         *
         * IMPORTANT:
         * false = local analysis only.
         * Qwen3 is NOT called here.
         */

        if (
                advisorEngine != null &&
                        databaseHelper != null &&
                        currentUser != null
        ) {

            loadFinancialAnalysis(
                    false
            );
        }
    }


    // =====================================================
    // ON DESTROY
    // =====================================================

    @Override
    protected void onDestroy() {

        if (apiService != null) {

            apiService.shutdown();
        }


        super.onDestroy();
    }
}