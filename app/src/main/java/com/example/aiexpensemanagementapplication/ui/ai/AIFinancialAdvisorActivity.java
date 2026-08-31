package com.example.aiexpensemanagementapplication.ui.ai;

import android.os.Bundle;
import android.text.TextUtils;
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

public class AIFinancialAdvisorActivity
        extends AppCompatActivity {

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
    // DATA
    // =====================================================

    private ArrayList<AdvisorMessage> messages;

    private AdvisorMessageAdapter adapter;

    private FinancialAdvisorEngine advisorEngine;

    private FinancialAdvisorApiService apiService;

    private FinancialAnalysis financialAnalysis;

    private int currentUserId = -1;

    private boolean isAIThinking = false;


    // =====================================================
    // DATABASE / AUTH
    // =====================================================

    private DatabaseHelper databaseHelper;

    private FirebaseAuth firebaseAuth;

    private FirebaseUser currentUser;


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

        initialize();

        setupRecyclerView();

        setupListeners();

        showWelcomeMessage();

        /*
         * Load the user's financial information
         * immediately when the Activity opens.
         */
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
        // FIREBASE AUTH
        // -------------------------------------------------

        firebaseAuth =
                FirebaseAuth.getInstance();

        currentUser =
                firebaseAuth.getCurrentUser();


        // -------------------------------------------------
        // FINANCIAL ADVISOR ENGINE
        // -------------------------------------------------

        advisorEngine =
                new FinancialAdvisorEngine(
                        databaseHelper
                );


        // -------------------------------------------------
        // REAL AI API SERVICE
        // -------------------------------------------------

        apiService =
                new FinancialAdvisorApiService();


        // -------------------------------------------------
        // VIEWS
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
        // INITIAL LOADING STATE
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
        // QUICK QUESTION - SAVE
        // -------------------------------------------------

        btnHowSave.setOnClickListener(
                v -> sendQuickQuestion(
                        "How can I save more money?"
                )
        );


        // -------------------------------------------------
        // QUICK QUESTION - SPENDING
        // -------------------------------------------------

        btnSpending.setOnClickListener(
                v -> sendQuickQuestion(
                        "Can you analyze my spending?"
                )
        );


        // -------------------------------------------------
        // QUICK QUESTION - BUDGET
        // -------------------------------------------------

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

                    if (event != null &&
                            event.getKeyCode()
                                    == KeyEvent.KEYCODE_ENTER) {

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

        if (currentUser != null &&
                currentUser.getDisplayName() != null &&
                !currentUser.getDisplayName()
                        .trim()
                        .isEmpty()) {

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
                        + "Ask me a question or choose one "
                        + "of the suggestions above.";

        addAIMessage(
                welcome
        );
    }


    // =====================================================
    // SEND MESSAGE
    // =====================================================

    private void sendMessage() {

        // -------------------------------------------------
        // PREVENT DUPLICATE REQUESTS
        // -------------------------------------------------

        if (isAIThinking) {
            return;
        }


        // -------------------------------------------------
        // GET MESSAGE
        // -------------------------------------------------

        String message =
                etMessage
                        .getText()
                        .toString()
                        .trim();


        // -------------------------------------------------
        // EMPTY MESSAGE
        // -------------------------------------------------

        if (TextUtils.isEmpty(message)) {
            return;
        }


        // -------------------------------------------------
        // ADD USER MESSAGE
        // -------------------------------------------------

        addUserMessage(
                message
        );


        // -------------------------------------------------
        // CLEAR INPUT
        // -------------------------------------------------

        etMessage.setText("");


        // -------------------------------------------------
        // GENERATE RESPONSE
        // -------------------------------------------------

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

        // -------------------------------------------------
        // PREVENT DUPLICATE REQUESTS
        // -------------------------------------------------

        if (isAIThinking) {
            return;
        }


        // -------------------------------------------------
        // ADD USER MESSAGE
        // -------------------------------------------------

        addUserMessage(
                question
        );


        // -------------------------------------------------
        // GENERATE RESPONSE
        // -------------------------------------------------

        generateAdvisorResponse(
                question
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

        if (question == null ||
                question.trim().isEmpty()) {

            return false;
        }

        String q =
                question
                        .trim()
                        .toLowerCase();


        // =================================================
        // 1. AM I WITHIN MY BUDGET?
        // =================================================

        if (
                q.contains("am i within my budget") ||
                        q.contains("am i within budget") ||
                        q.contains("within my budget") ||
                        q.contains("within budget")
        ) {

            double budget =
                    financialAnalysis.getBudget();

            double expense =
                    financialAnalysis.getCurrentMonthExpense();

            double remaining =
                    financialAnalysis.getRemainingBudget();


            // -------------------------------------------------
            // NO ACTIVE BUDGET
            // -------------------------------------------------

            if (budget <= 0) {

                addAIMessage(
                        "I couldn't find an active monthly "
                                + "budget for your financial profile. "
                                + "Create a monthly budget and I'll "
                                + "help you monitor it."
                );

                return true;
            }


            // -------------------------------------------------
            // WITHIN BUDGET
            // -------------------------------------------------

            if (expense <= budget) {

                double usedPercentage =
                        (expense / budget) * 100.0;

                addAIMessage(
                        String.format(
                                java.util.Locale.US,
                                "Yes, you're within your monthly "
                                        + "budget. You've spent Rs %.2f "
                                        + "of Rs %.2f, which is %.1f%% "
                                        + "of your budget. You have "
                                        + "Rs %.2f remaining.",
                                expense,
                                budget,
                                usedPercentage,
                                remaining
                        )
                );

            }

            // -------------------------------------------------
            // OVER BUDGET
            // -------------------------------------------------

            else {

                double exceeded =
                        expense - budget;

                double exceededPercentage =
                        ((expense - budget)
                                / budget) * 100.0;

                addAIMessage(
                        String.format(
                                java.util.Locale.US,
                                "You're currently over your "
                                        + "monthly budget. You've spent "
                                        + "Rs %.2f against a budget of "
                                        + "Rs %.2f, exceeding it by "
                                        + "Rs %.2f (%.1f%%).",
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
        // 2. HOW MUCH BUDGET IS LEFT?
        // =================================================

        if (
                q.contains("how much budget") &&
                        (
                                q.contains("left") ||
                                        q.contains("remaining")
                        )
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
                                java.util.Locale.US,
                                "You have Rs %.2f remaining "
                                        + "from your monthly budget "
                                        + "of Rs %.2f.",
                                remaining,
                                budget
                        )
                );

            } else {

                addAIMessage(
                        String.format(
                                java.util.Locale.US,
                                "You've exceeded your monthly "
                                        + "budget by Rs %.2f.",
                                Math.abs(remaining)
                        )
                );
            }


            return true;
        }


        // =================================================
        // 3. HOW MUCH DID I SPEND?
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
                            java.util.Locale.US,
                            "You've spent Rs %.2f this month.",
                            expense
                    )
            );


            return true;
        }


        // =================================================
        // 4. HOW MUCH DID I SAVE?
        // =================================================

        if (
                q.contains("how much did i save") ||
                        q.contains("how much have i saved") ||
                        q.contains("my savings")
        ) {

            double savings =
                    financialAnalysis.getSavings();


            double savingsRate =
                    financialAnalysis.getSavingsRate();


            addAIMessage(
                    String.format(
                            java.util.Locale.US,
                            "You've saved Rs %.2f, with a "
                                    + "savings rate of %.1f%%.",
                            savings,
                            savingsRate
                    )
            );


            return true;
        }


        // =================================================
        // 5. HIGHEST SPENDING CATEGORY
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
                            java.util.Locale.US,
                            "Your highest spending category "
                                    + "is %s, with Rs %.2f spent.",
                            category,
                            amount
                    )
            );


            return true;
        }


        // =================================================
        // 6. FINANCIAL HEALTH SCORE
        // =================================================

        if (
                q.contains("financial health score") ||
                        q.contains("financial health") ||
                        q.contains("health score")
        ) {

            double score =
                    financialAnalysis
                            .getFinancialHealthScore();


            addAIMessage(
                    String.format(
                            java.util.Locale.US,
                            "Your current financial health "
                                    + "score is %.0f out of 100.",
                            score
                    )
            );


            return true;
        }


        // =================================================
        // 7. EXPENSE CHANGE
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
                                java.util.Locale.US,
                                "Your expenses increased by "
                                        + "%.1f%% compared with "
                                        + "the previous month.",
                                change
                        )
                );

            } else if (change < 0) {

                addAIMessage(
                        String.format(
                                java.util.Locale.US,
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
        // NOT A LOCAL QUESTION
        // =================================================

        return false;
    }

    // =====================================================
    // GENERATE ADVISOR RESPONSE
    // =====================================================

    // =====================================================
// GENERATE ADVISOR RESPONSE
// =====================================================

    private void generateAdvisorResponse(
            String question
    ) {

        // -------------------------------------------------
        // PREVENT DUPLICATE REQUESTS
        // -------------------------------------------------

        if (isAIThinking) {
            return;
        }


        // -------------------------------------------------
        // CHECK FINANCIAL ANALYSIS
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
        // TRY LOCAL FINANCIAL ANSWER FIRST
        // -------------------------------------------------

        boolean handledLocally =
                handleLocalFinancialQuestion(
                        question
                );


        // -------------------------------------------------
        // LOCAL ANSWER FOUND
        // -------------------------------------------------

        if (handledLocally) {

            return;
        }


        // -------------------------------------------------
        // QUESTION NEEDS AI
        // -------------------------------------------------

        showAIThinking(
                true
        );


        // -------------------------------------------------
        // SEND TO OLLAMA BACKEND
        // -------------------------------------------------

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

                            // -----------------------------
                            // STOP THINKING
                            // -----------------------------

                            showAIThinking(
                                    false
                            );


                            // -----------------------------
                            // CHECK RESPONSE
                            // -----------------------------

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


                            // -----------------------------
                            // ADD AI RESPONSE
                            // -----------------------------

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

                            // -----------------------------
                            // STOP THINKING
                            // -----------------------------

                            showAIThinking(
                                    false
                            );


                            // -----------------------------
                            // ERROR
                            // -----------------------------

                            String errorMessage =
                                    message;


                            if (
                                    errorMessage == null ||
                                            errorMessage.trim().isEmpty()
                            ) {

                                errorMessage =
                                        "Unknown error occurred.";
                            }


                            addAIMessage(
                                    "Sorry, I couldn't generate "
                                            + "your financial advice."
                                            + "\n\n"
                                            + errorMessage
                            );
                        });
                    }
                }
        );
    }


        private ArrayList<AdvisorMessage> getRecentMessages() {

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
        // CHECK AUTHENTICATION
        // -------------------------------------------------

        if (currentUser == null) {

            return false;
        }


        // -------------------------------------------------
        // GET EMAIL
        // -------------------------------------------------

        if (currentUser.getEmail() == null ||
                currentUser.getEmail()
                        .trim()
                        .isEmpty()) {

            return false;
        }


        // -------------------------------------------------
        // NORMALIZE EMAIL
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


        // -------------------------------------------------
        // USER NOT FOUND
        // -------------------------------------------------

        if (currentUserId == -1) {

            return false;
        }


        // -------------------------------------------------
        // ANALYZE FINANCIAL DATA
        // -------------------------------------------------

        try {

            financialAnalysis =
                    advisorEngine.analyzeUser(
                            currentUserId
                    );

        } catch (Exception e) {

            e.printStackTrace();

            financialAnalysis =
                    null;

            return false;
        }


        // -------------------------------------------------
        // VERIFY RESULT
        // -------------------------------------------------

        return financialAnalysis != null;
    }


    // =====================================================
    // ADD USER MESSAGE
    // =====================================================

    private void addUserMessage(
            String message
    ) {

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

        if (rvMessages == null ||
                adapter == null) {

            return;
        }

        rvMessages.post(() -> {

            int lastPosition =
                    adapter.getItemCount() - 1;

            if (lastPosition >= 0) {

                rvMessages.smoothScrollToPosition(
                        lastPosition
                );
            }
        });
    }


    // =====================================================
    // AI THINKING STATE
    // =====================================================

    private void showAIThinking(
            boolean show
    ) {

        isAIThinking =
                show;


        // -------------------------------------------------
        // PROGRESS BAR
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
        // TEXT INPUT
        // -------------------------------------------------

        if (etMessage != null) {

            etMessage.setEnabled(
                    !show
            );
        }


        // -------------------------------------------------
        // QUICK QUESTIONS
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
    // ACTIVITY DESTROY
    // =====================================================

    @Override
    protected void onDestroy() {

        // -------------------------------------------------
        // STOP API SERVICE
        // -------------------------------------------------

        if (apiService != null) {

            apiService.shutdown();
        }

        super.onDestroy();
    }
}