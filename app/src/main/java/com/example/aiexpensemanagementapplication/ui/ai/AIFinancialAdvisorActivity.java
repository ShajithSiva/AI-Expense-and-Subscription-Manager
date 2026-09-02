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
        // INITIALIZE EVERYTHING
        // -------------------------------------------------

        initialize();


        // -------------------------------------------------
        // SETUP RECYCLER VIEW
        // -------------------------------------------------

        setupRecyclerView();


        // -------------------------------------------------
        // SETUP BUTTONS / INPUT
        // -------------------------------------------------

        setupListeners();


        // -------------------------------------------------
        // WELCOME
        // -------------------------------------------------

        showWelcomeMessage();


        // -------------------------------------------------
        // LOAD FINANCIAL DATA
        // -------------------------------------------------

        loadFinancialAnalysis();
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
        // PROACTIVE INSIGHT ENGINE
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
        // INITIAL STATE
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


        // -------------------------------------------------
        // MAKE SURE FINANCIAL DATA EXISTS
        // -------------------------------------------------

        if (financialAnalysis == null) {

            boolean loaded =
                    loadFinancialAnalysis();


            if (!loaded) {

                addAIMessage(
                        "I couldn't analyze your financial "
                                + "data yet. Please make sure "
                                + "your account is synchronized "
                                + "and try again."
                );

                return;
            }
        }


        // -------------------------------------------------
        // QUESTION ROUTING
        // -------------------------------------------------

        AdvisorQuestionRouter.Route route =
                questionRouter.route(
                        question
                );


        Log.d(
                "ADVISOR_ROUTER",
                "Question = "
                        + question
                        + " | Route = "
                        + route
        );


        // -------------------------------------------------
        // LOCAL QUESTION
        // -------------------------------------------------

        if (
                route ==
                        AdvisorQuestionRouter.Route.LOCAL
        ) {

            boolean handled =
                    handleLocalFinancialQuestion(
                            question
                    );


            if (handled) {

                return;
            }
        }


        // -------------------------------------------------
        // AI QUESTION
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

        if (financialAnalysis == null) {
            return false;
        }


        if (
                question == null ||
                        question.trim().isEmpty()
        ) {

            return false;
        }


        String q =
                question
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        // =================================================
        // BUDGET STATUS
        // =================================================

        if (
                q.contains("am i within my budget") ||
                        q.contains("am i within budget") ||
                        q.contains("within my budget") ||
                        q.contains("within budget")
        ) {

            double budget =
                    financialAnalysis
                            .getBudget();


            double expense =
                    financialAnalysis
                            .getCurrentMonthExpense();


            double remaining =
                    financialAnalysis
                            .getRemainingBudget();


            if (budget <= 0) {

                addAIMessage(
                        "I couldn't find an active monthly "
                                + "budget for your financial "
                                + "profile. Create a monthly "
                                + "budget and I'll help you "
                                + "monitor it."
                );

                return true;
            }


            double usedPercentage =
                    (expense / budget)
                            * 100.0;


            if (expense <= budget) {

                addAIMessage(
                        String.format(
                                Locale.US,

                                "Yes, you're within your "
                                        + "monthly budget. You've "
                                        + "spent Rs %.2f of "
                                        + "Rs %.2f, which is "
                                        + "%.1f%% of your budget. "
                                        + "You have Rs %.2f "
                                        + "remaining.",

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
                        (
                                exceeded / budget
                        ) * 100.0;


                addAIMessage(
                        String.format(
                                Locale.US,

                                "You're currently over your "
                                        + "monthly budget. You've "
                                        + "spent Rs %.2f against "
                                        + "a budget of Rs %.2f, "
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


        // =================================================
        // REMAINING BUDGET
        // =================================================

        if (
                q.contains("how much budget") &&
                        (
                                q.contains("left") ||
                                        q.contains("remaining")
                        )
        ) {

            double budget =
                    financialAnalysis
                            .getBudget();


            double remaining =
                    financialAnalysis
                            .getRemainingBudget();


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

                                "You have Rs %.2f remaining "
                                        + "from your monthly "
                                        + "budget of Rs %.2f.",

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


        // =================================================
        // CURRENT MONTH EXPENSE
        // =================================================

        if (
                q.contains("how much did i spend") ||
                        q.contains("how much have i spent") ||
                        q.contains("my spending this month") ||
                        q.contains("my expenses this month")
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


        // =================================================
        // SAVINGS
        // =================================================

        if (
                q.contains("how much did i save") ||
                        q.contains("how much have i saved") ||
                        q.contains("my savings")
        ) {

            double savings =
                    financialAnalysis
                            .getSavings();


            double savingsRate =
                    financialAnalysis
                            .getSavingsRate();


            addAIMessage(
                    String.format(
                            Locale.US,

                            "You've saved Rs %.2f, with "
                                    + "a savings rate of %.1f%%.",

                            savings,
                            savingsRate
                    )
            );


            return true;
        }


        // =================================================
        // HIGHEST SPENDING
        // =================================================

        if (
                q.contains("highest spending") ||
                        q.contains("most spending") ||
                        q.contains("spend the most") ||
                        q.contains("highest expense category")
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

                category =
                        "Unknown";
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


        // =================================================
        // FINANCIAL HEALTH
        // =================================================

        if (
                q.contains("financial health score") ||
                        q.contains("financial health") ||
                        q.contains("health score")
        ) {

            int score =
                    financialAnalysis
                            .getFinancialHealthScore();


            addAIMessage(
                    String.format(
                            Locale.US,

                            "Your current financial health "
                                    + "score is %d out of 100.",

                            score
                    )
            );


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


        // =================================================
        // CATEGORY QUESTION
        // =================================================

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
        }


        return false;
    }


    // =====================================================
    // FIND REQUESTED CATEGORY
    // =====================================================

    private String findRequestedCategory(
            String question
    ) {

        if (question == null) {
            return null;
        }


        String q =
                question
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        if (q.contains("food")) {

            return findActualCategory(
                    "food"
            );
        }


        if (
                q.contains("transport") ||
                        q.contains("transportation")
        ) {

            return findActualCategory(
                    "transport"
            );
        }


        if (q.contains("shopping")) {

            return findActualCategory(
                    "shopping"
            );
        }


        if (
                q.contains("bills") ||
                        q.contains("bill")
        ) {

            return findActualCategory(
                    "bill"
            );
        }


        if (q.contains("health")) {

            return findActualCategory(
                    "health"
            );
        }


        if (q.contains("education")) {

            return findActualCategory(
                    "education"
            );
        }


        if (q.contains("entertainment")) {

            return findActualCategory(
                    "entertainment"
            );
        }


        if (
                q.contains("others") ||
                        q.contains("other")
        ) {

            return findActualCategory(
                    "other"
            );
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
                                .getCategoryTotals() == null
        ) {

            return null;
        }


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
                            requestedCategory
                    )
            ) {

                return category;
            }


            if (
                    requestedCategory.equals(
                            "transport"
                    ) &&
                            normalized.equals(
                                    "transportation"
                            )
            ) {

                return category;
            }


            if (
                    requestedCategory.equals(
                            "bill"
                    ) &&
                            normalized.equals(
                                    "bills"
                            )
            ) {

                return category;
            }


            if (
                    requestedCategory.equals(
                            "other"
                    ) &&
                            normalized.equals(
                                    "others"
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

        // -------------------------------------------------
        // CHECK FIREBASE USER
        // -------------------------------------------------

        if (currentUser == null) {

            Log.e(
                    TAG,
                    "Firebase user is null."
            );

            return false;
        }


        // -------------------------------------------------
        // CHECK EMAIL
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


        // -------------------------------------------------
        // GET EMAIL
        // -------------------------------------------------

        String email =
                currentUser
                        .getEmail()
                        .trim();


        // -------------------------------------------------
        // FIND LOCAL USER
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


        // -------------------------------------------------
        // LOCAL USER NOT FOUND
        // -------------------------------------------------

        if (currentUserId == -1) {

            Log.e(
                    TAG,
                    "Local user profile not found."
            );

            return false;
        }


        // =================================================
        // ANALYZE USER
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
            // GENERATE PROACTIVE INSIGHTS
            // =================================================

            proactiveInsights =
                    insightEngine.analyze(
                            financialAnalysis
                    );


            if (proactiveInsights == null) {

                proactiveInsights =
                        new ArrayList<>();
            }


            // =================================================
            // LOG FINANCIAL DATA
            // =================================================

            logFinancialAnalysis();


            // =================================================
            // LOG DETECTED INSIGHTS
            // =================================================

            logProactiveInsights();


            // =================================================
            // GENERATE AI EXPLANATIONS
            // ONLY ON INITIAL LOAD
            // =================================================

            if (
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


        // -------------------------------------------------
        // CATEGORY TOTALS
        // -------------------------------------------------

        Map<String, Double> categoryTotals =
                financialAnalysis
                        .getCategoryTotals();


        Log.d(
                TAG,
                "Category Totals = "
                        + categoryTotals
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
        // SEND
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
         * Refresh financial calculations when the Activity
         * becomes visible again.
         *
         * The AI proactive insight generation itself is
         * protected by initialInsightsGenerated so that
         * Qwen is not called repeatedly.
         */

        if (
                advisorEngine != null &&
                        databaseHelper != null &&
                        currentUser != null
        ) {

            loadFinancialAnalysis();
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