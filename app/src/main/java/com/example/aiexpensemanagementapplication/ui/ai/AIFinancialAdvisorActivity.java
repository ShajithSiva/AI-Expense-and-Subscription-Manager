package com.example.aiexpensemanagementapplication.ui.ai;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class AIFinancialAdvisorActivity extends AppCompatActivity {

    // =====================================================
    // UI
    // =====================================================

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
    private FinancialAdvisorAIService aiService;
    private FinancialAnalysis financialAnalysis;
    private int currentUserId = -1;


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
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_ai_financial_advisor
        );

        initialize();

        setupRecyclerView();

        setupListeners();

        showWelcomeMessage();

        loadFinancialAnalysis();
    }


    // =====================================================
    // INITIALIZE
    // =====================================================

    private void initialize() {

        databaseHelper =
                new DatabaseHelper(this);

        firebaseAuth =
                FirebaseAuth.getInstance();

        currentUser =
                firebaseAuth.getCurrentUser();

        databaseHelper =
                new DatabaseHelper(this);

        advisorEngine =
                new FinancialAdvisorEngine(
                        databaseHelper
                );

        aiService =
                new FinancialAdvisorAIService();


        btnBack =
                findViewById(R.id.btnBack);

        rvMessages =
                findViewById(R.id.rvMessages);

        etMessage =
                findViewById(R.id.etMessage);

        btnSend =
                findViewById(R.id.btnSend);

        btnHowSave =
                findViewById(R.id.btnHowSave);

        btnSpending =
                findViewById(R.id.btnSpending);

        btnBudget =
                findViewById(R.id.btnBudget);
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

        layoutManager.setStackFromEnd(false);

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

        btnBack.setOnClickListener(v -> finish());


        // -------------------------------------------------
        // SEND
        // -------------------------------------------------

        btnSend.setOnClickListener(
                v -> sendMessage()
        );


        // -------------------------------------------------
        // QUICK QUESTION 1
        // -------------------------------------------------

        btnHowSave.setOnClickListener(v -> {

            sendQuickQuestion(
                    "How can I save more money?"
            );

        });


        // -------------------------------------------------
        // QUICK QUESTION 2
        // -------------------------------------------------

        btnSpending.setOnClickListener(v -> {

            sendQuickQuestion(
                    "Can you analyze my spending?"
            );

        });


        // -------------------------------------------------
        // QUICK QUESTION 3
        // -------------------------------------------------

        btnBudget.setOnClickListener(v -> {

            sendQuickQuestion(
                    "Am I within my budget?"
            );

        });


        // -------------------------------------------------
        // ENTER KEY
        // -------------------------------------------------

        etMessage.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (event != null &&
                            event.getKeyCode() ==
                                    android.view.KeyEvent.KEYCODE_ENTER) {

                        sendMessage();

                        return true;
                    }

                    return false;
                }
        );
    }


    // =====================================================
    // WELCOME
    // =====================================================

    private void showWelcomeMessage() {

        String name = "there";

        if (currentUser != null &&
                currentUser.getDisplayName() != null &&
                !currentUser.getDisplayName().trim().isEmpty()) {

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

        addAIMessage(welcome);
    }


    // =====================================================
    // SEND MESSAGE
    // =====================================================

    private void sendMessage() {

        String message =
                etMessage
                        .getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(message)) {

            return;
        }

        addUserMessage(message);

        etMessage.setText("");

        generateAdvisorResponse(message);
    }


    // =====================================================
    // QUICK QUESTION
    // =====================================================

    private void sendQuickQuestion(
            String question
    ) {

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
            String question) {

        if (financialAnalysis == null) {

            loadFinancialAnalysis();

            if (financialAnalysis == null) {

                addAIMessage(
                        "I couldn't analyze your financial "
                                + "data yet. Please try again."
                );

                return;
            }
        }

        aiService.generateAdvice(
                financialAnalysis,
                question,
                new FinancialAdvisorAIService.AdvisorCallback() {

                    @Override
                    public void onSuccess(
                            String response) {

                        runOnUiThread(() ->
                                addAIMessage(response)
                        );
                    }

                    @Override
                    public void onFailure(
                            String message) {

                        runOnUiThread(() ->
                                addAIMessage(
                                        message
                                )
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
    // SCROLL
    // =====================================================

    private void scrollToBottom() {

        rvMessages.post(() -> {

            if (adapter.getItemCount() > 0) {

                rvMessages.smoothScrollToPosition(
                        adapter.getItemCount() - 1
                );
            }
        });
    }


    // =====================================================
    // PHASE 1 DEMO RESPONSE
    // =====================================================

    private void generateDemoResponse(
            String question
    ) {

        String lowerQuestion =
                question.toLowerCase();


        if (lowerQuestion.contains("save")) {

            addAIMessage(
                    "💡 A good starting point is to "
                            + "identify your highest spending "
                            + "categories and reduce non-essential "
                            + "expenses.\n\n"
                            + "In the next phase, I'll analyze "
                            + "your actual income, expenses and "
                            + "budget to give you a personalized "
                            + "savings recommendation."
            );

            return;
        }


        if (lowerQuestion.contains("spending") ||
                lowerQuestion.contains("expense")) {

            addAIMessage(
                    "📊 I can analyze your spending patterns "
                            + "and identify your largest expense "
                            + "categories.\n\n"
                            + "Phase 2 will connect this advisor "
                            + "to your real expense data so the "
                            + "recommendation is based on your "
                            + "actual transactions."
            );

            return;
        }


        if (lowerQuestion.contains("budget")) {

            addAIMessage(
                    "💰 I can check your budget usage, "
                            + "remaining budget and spending "
                            + "rate.\n\n"
                            + "Once Phase 2 is connected, I'll "
                            + "use your actual Budget and Expense "
                            + "records instead of a generic response."
            );

            return;
        }


        if (lowerQuestion.contains("hello") ||
                lowerQuestion.contains("hi")) {

            addAIMessage(
                    "Hello! 👋\n\n"
                            + "I'm ready to help you understand "
                            + "your finances. You can ask me about "
                            + "saving, spending, budgets or "
                            + "subscriptions."
            );

            return;
        }


        addAIMessage(
                "🤖 That's a good financial question.\n\n"
                        + "I'm currently running in Phase 1, "
                        + "so my conversational interface is ready "
                        + "but the real financial analysis engine "
                        + "has not been connected yet.\n\n"
                        + "In Phase 2, I'll use your actual "
                        + "financial data to provide a personalized "
                        + "answer."
        );
    }

    private void loadFinancialAnalysis() {

        if (currentUser == null ||
                currentUser.getEmail() == null) {

            addAIMessage(
                    "I couldn't identify your account. "
                            + "Please login again."
            );

            return;
        }

        String email =
                currentUser
                        .getEmail()
                        .trim();

        currentUserId =
                databaseHelper.getUserIdByEmail(
                        email
                );

        if (currentUserId == -1) {

            addAIMessage(
                    "I couldn't find your local financial "
                            + "profile. Please make sure your "
                            + "account is synchronized."
            );

            return;
        }

        financialAnalysis =
                advisorEngine.analyzeUser(
                        currentUserId
                );
    }
}