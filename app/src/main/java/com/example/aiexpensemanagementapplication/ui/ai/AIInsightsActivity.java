package com.example.aiexpensemanagementapplication.ui.ai;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AIInsightsActivity extends AppCompatActivity {

    private static final String TAG =
            "AI_INSIGHTS";


    // =====================================================
    // UI
    // =====================================================

    private MaterialToolbar toolbar;

    private RecyclerView rvInsights;

    private ProgressBar progressInsights;

    private TextView tvNoInsights;

    private AIInsightAdapter insightAdapter;


    // =====================================================
    // AI SERVICES
    // =====================================================

    private FinancialAdvisorApiService apiService;

    private FinancialAdvisorEngine advisorEngine;

    private ProactiveInsightEngine proactiveInsightEngine;


    // =====================================================
    // DATABASE / FIREBASE
    // =====================================================

    private DatabaseHelper databaseHelper;

    private FirebaseAuth firebaseAuth;

    private FirebaseUser currentUser;


    // =====================================================
    // FINANCIAL DATA
    // =====================================================

    private FinancialAnalysis financialAnalysis;

    private List<FinancialInsight> detectedInsights =
            new ArrayList<>();


    // =====================================================
    // CURRENT USER
    // =====================================================

    private int currentLocalUserId =
            -1;


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );


        setContentView(
                R.layout.activity_ai_insights
        );


        // =================================================
        // DATABASE
        // =================================================

        databaseHelper =
                new DatabaseHelper(
                        this
                );


        // =================================================
        // FIREBASE
        // =================================================

        firebaseAuth =
                FirebaseAuth.getInstance();


        currentUser =
                firebaseAuth.getCurrentUser();


        // =================================================
        // AI SERVICES
        // =================================================

        apiService =
                new FinancialAdvisorApiService();


        advisorEngine =
                new FinancialAdvisorEngine(
                        databaseHelper
                );


        proactiveInsightEngine =
                new ProactiveInsightEngine();


        // =================================================
        // FIND VIEWS
        // =================================================

        toolbar =
                findViewById(
                        R.id.toolbar
                );


        rvInsights =
                findViewById(
                        R.id.rvInsights
                );


        progressInsights =
                findViewById(
                        R.id.progressInsights
                );


        tvNoInsights =
                findViewById(
                        R.id.tvNoInsights
                );


        // =================================================
        // TOOLBAR
        // =================================================

        setSupportActionBar(
                toolbar
        );


        if (
                getSupportActionBar() != null
        ) {

            getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(
                            true
                    );
        }


        toolbar.setNavigationOnClickListener(
                v -> finish()
        );


        // =================================================
        // RECYCLER VIEW
        // =================================================

        insightAdapter =
                new AIInsightAdapter(
                        new ArrayList<>()
                );


        rvInsights.setLayoutManager(
                new LinearLayoutManager(
                        this
                )
        );


        rvInsights.setAdapter(
                insightAdapter
        );


        rvInsights.setHasFixedSize(
                false
        );


        // =================================================
        // INITIAL STATE
        // =================================================

        if (
                tvNoInsights != null
        ) {

            tvNoInsights.setVisibility(
                    View.GONE
            );
        }


        // =================================================
        // LOAD INSIGHTS
        // =================================================

        loadFinancialInsights();
    }


    // =====================================================
    // LOAD FINANCIAL INSIGHTS
    // =====================================================

    private void loadFinancialInsights() {

        Log.d(
                TAG,
                "Loading financial insights..."
        );


        // =================================================
        // CHECK FIREBASE USER
        // =================================================

        if (
                currentUser == null ||
                        currentUser.getEmail() == null
        ) {

            Log.e(
                    TAG,
                    "Firebase user is unavailable."
            );


            showEmptyInsights();

            return;
        }


        // =================================================
        // GET EMAIL
        // =================================================

        String email =
                currentUser
                        .getEmail()
                        .trim();


        if (
                email.isEmpty()
        ) {

            Log.e(
                    TAG,
                    "Firebase email is empty."
            );


            showEmptyInsights();

            return;
        }


        // =================================================
        // FIND LOCAL USER
        // =================================================

        try {

            currentLocalUserId =
                    databaseHelper
                            .getUserIdByEmail(
                                    email
                            );

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Unable to find local user.",
                    e
            );


            showEmptyInsights();

            return;
        }


        Log.d(
                TAG,
                "Local user ID = "
                        + currentLocalUserId
        );


        // =================================================
        // USER NOT FOUND
        // =================================================

        if (
                currentLocalUserId == -1
        ) {

            Log.e(
                    TAG,
                    "Local user was not found."
            );


            showEmptyInsights();

            return;
        }


        // =================================================
        // ANALYZE USER
        // =================================================

        try {

            financialAnalysis =
                    advisorEngine.analyzeUser(
                            currentLocalUserId
                    );

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Financial analysis failed.",
                    e
            );


            showEmptyInsights();

            return;
        }


        // =================================================
        // VERIFY ANALYSIS
        // =================================================

        if (
                financialAnalysis == null
        ) {

            Log.e(
                    TAG,
                    "FinancialAnalysis is null."
            );


            showEmptyInsights();

            return;
        }


        // =================================================
        // LOG FINANCIAL DATA
        // =================================================

        Log.d(
                TAG,
                "Income = "
                        + financialAnalysis
                        .getTotalIncome()
        );


        Log.d(
                TAG,
                "Expense = "
                        + financialAnalysis
                        .getTotalExpense()
        );


        Log.d(
                TAG,
                "Budget = "
                        + financialAnalysis
                        .getBudget()
        );


        Log.d(
                TAG,
                "Remaining budget = "
                        + financialAnalysis
                        .getRemainingBudget()
        );


        Log.d(
                TAG,
                "Category totals = "
                        + financialAnalysis
                        .getCategoryTotals()
        );


        // =================================================
        // DETECT PROACTIVE INSIGHTS
        // =================================================

        try {

            detectedInsights =
                    proactiveInsightEngine.analyze(
                            financialAnalysis
                    );

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Proactive insight detection failed.",
                    e
            );


            detectedInsights =
                    new ArrayList<>();
        }


        if (
                detectedInsights == null
        ) {

            detectedInsights =
                    new ArrayList<>();
        }


        Log.d(
                TAG,
                "Detected insights = "
                        + detectedInsights.size()
        );

        // =================================================
        // SHOW LOCAL INSIGHTS IMMEDIATELY
        // =================================================

        showLocalInsights();


        // =================================================
        // CREATE FINGERPRINT
        // =================================================

        String fingerprint =
                createFinancialFingerprint();


        Log.d(
                TAG,
                "Financial fingerprint = "
                        + fingerprint
        );


        // =================================================
        // CHECK CACHE
        // =================================================

        List<AIInsightResult> cachedInsights =
                AIInsightCacheManager
                        .getCachedInsights(
                                this,
                                currentLocalUserId,
                                fingerprint
                        );


        if (
                cachedInsights != null &&
                        !cachedInsights.isEmpty()
        ) {

            Log.d(
                    TAG,
                    "Using cached AI insights."
            );


            showAIInsights(
                    cachedInsights
            );


            return;
        }


        // =================================================
        // CACHE MISS
        // =================================================

        Log.d(
                TAG,
                "AI insight cache miss."
        );


        generateAIInsights(
                fingerprint
        );
    }


    // =====================================================
    // GENERATE AI INSIGHTS
    // =====================================================

    private void generateAIInsights(
            String fingerprint
    ) {

        if (
                financialAnalysis == null
        ) {

            Log.w(
                    TAG,
                    "FinancialAnalysis is null."
            );


            return;
        }


        if (
                detectedInsights == null ||
                        detectedInsights.isEmpty()
        ) {

            showEmptyInsights();

            return;
        }


        // =================================================
        // BACKGROUND AI ENHANCEMENT
        // =================================================

        // Keep locally generated insights visible.
        // Ollama runs in the background and can enhance
        // the messages when the response arrives.

        if (progressInsights != null) {

            progressInsights.setVisibility(
                    View.VISIBLE
            );
        }


        Log.d(
                TAG,
                "Sending insights to AI..."
        );


        // =================================================
        // CALL AI API
        // =================================================

        apiService.generateProactiveInsights(

                financialAnalysis,

                detectedInsights,

                new FinancialAdvisorApiService.InsightCallback() {

                    @Override
                    public void onSuccess(
                            List<AIInsightResult> insights
                    ) {

                        runOnUiThread(() -> {

                            setLoading(
                                    false
                            );


                            if (
                                    insights == null ||
                                            insights.isEmpty()
                            ) {

                                Log.w(
                                        TAG,
                                        "AI returned empty insights."
                                );


                                showLocalInsights();

                                return;
                            }


                            Log.d(
                                    TAG,
                                    "AI returned "
                                            + insights.size()
                                            + " insights."
                            );


                            // =================================
                            // SAVE CACHE
                            // =================================

                            AIInsightCacheManager
                                    .saveInsights(
                                            AIInsightsActivity.this,
                                            currentLocalUserId,
                                            fingerprint,
                                            insights
                                    );


                            // =================================
                            // DISPLAY
                            // =================================

                            showAIInsights(
                                    insights
                            );
                        });
                    }


                    @Override
                    public void onFailure(
                            String message
                    ) {

                        runOnUiThread(() -> {

                            setLoading(
                                    false
                            );


                            Log.e(
                                    TAG,
                                    "AI insight generation failed: "
                                            + message
                            );


                            // Keep the locally generated insights visible
                            showLocalInsights();
                        });
                    }
                }
        );
    }


    // =====================================================
    // CREATE FINANCIAL FINGERPRINT
    // =====================================================

    private String createFinancialFingerprint() {

        if (
                financialAnalysis == null
        ) {

            return "";
        }


        StringBuilder data =
                new StringBuilder();


        // =================================================
        // USER
        // =================================================

        data.append(
                "user="
        );


        data.append(
                currentLocalUserId
        );


        // =================================================
        // FINANCIAL VALUES
        // =================================================

        appendDouble(
                data,
                "totalIncome",
                financialAnalysis
                        .getTotalIncome()
        );


        appendDouble(
                data,
                "totalExpense",
                financialAnalysis
                        .getTotalExpense()
        );


        appendDouble(
                data,
                "savings",
                financialAnalysis
                        .getSavings()
        );


        appendDouble(
                data,
                "savingsRate",
                financialAnalysis
                        .getSavingsRate()
        );


        appendDouble(
                data,
                "expenseRate",
                financialAnalysis
                        .getExpenseRate()
        );


        appendDouble(
                data,
                "budget",
                financialAnalysis
                        .getBudget()
        );


        appendDouble(
                data,
                "budgetUsed",
                financialAnalysis
                        .getBudgetUsed()
        );


        appendDouble(
                data,
                "remainingBudget",
                financialAnalysis
                        .getRemainingBudget()
        );


        appendDouble(
                data,
                "currentMonthExpense",
                financialAnalysis
                        .getCurrentMonthExpense()
        );


        appendDouble(
                data,
                "previousMonthExpense",
                financialAnalysis
                        .getPreviousMonthExpense()
        );


        appendDouble(
                data,
                "currentMonthIncome",
                financialAnalysis
                        .getCurrentMonthIncome()
        );


        appendDouble(
                data,
                "previousMonthIncome",
                financialAnalysis
                        .getPreviousMonthIncome()
        );


        appendDouble(
                data,
                "expenseChangePercentage",
                financialAnalysis
                        .getExpenseChangePercentage()
        );


        // =================================================
        // HIGHEST CATEGORY
        // =================================================

        data.append(
                "|highestCategory="
        );


        if (
                financialAnalysis
                        .getHighestCategory() != null
        ) {

            data.append(
                    financialAnalysis
                            .getHighestCategory()
            );
        }


        appendDouble(
                data,
                "highestCategoryAmount",
                financialAnalysis
                        .getHighestCategoryAmount()
        );


        // =================================================
        // HEALTH SCORE
        // =================================================

        data.append(
                "|healthScore="
        );


        data.append(
                financialAnalysis
                        .getFinancialHealthScore()
        );


        // =================================================
        // CATEGORY TOTALS
        // =================================================

        Map<String, Double> categories =
                financialAnalysis
                        .getCategoryTotals();


        if (
                categories != null
        ) {

            for (
                    Map.Entry<String, Double> entry
                    : categories.entrySet()
            ) {

                data.append(
                        "|category="
                );


                data.append(
                        entry.getKey() == null
                                ? ""
                                : entry.getKey()
                );


                data.append(
                        ":"
                );


                data.append(
                        entry.getValue() == null
                                ? "0.00"
                                : String.format(
                                Locale.US,
                                "%.2f",
                                entry.getValue()
                        )
                );
            }
        }


        // =================================================
        // DETECTED INSIGHTS
        // =================================================

        if (
                detectedInsights != null
        ) {

            for (
                    FinancialInsight insight
                    : detectedInsights
            ) {

                if (
                        insight == null
                ) {

                    continue;
                }


                data.append(
                        "|insightType="
                );


                if (
                        insight.getType() != null
                ) {

                    data.append(
                            insight
                                    .getType()
                                    .name()
                    );
                }


                data.append(
                        "|insightTitle="
                );


                if (
                        insight.getTitle() != null
                ) {

                    data.append(
                            insight.getTitle()
                    );
                }


                data.append(
                        "|insightMessage="
                );

                if (
                        insight.getMessage() != null
                ) {

                    data.append(
                            insight.getMessage()
                    );
                }


// =================================================
// INSIGHT SEVERITY
// =================================================

                data.append(
                        "|insightSeverity="
                );

                if (
                        insight.getSeverity() != null
                ) {

                    data.append(
                            insight
                                    .getSeverity()
                                    .name()
                    );

                } else {

                    data.append(
                            FinancialInsight.Severity.LOW.name()
                    );
                }

            }
        }


        return sha256(
                data.toString()
        );
    }


    // =====================================================
    // APPEND DOUBLE
    // =====================================================

    private void appendDouble(
            StringBuilder builder,
            String name,
            double value
    ) {

        builder.append(
                "|"
        );


        builder.append(
                name
        );


        builder.append(
                "="
        );


        builder.append(
                String.format(
                        Locale.US,
                        "%.2f",
                        value
                )
        );
    }


    // =====================================================
    // SHA-256
    // =====================================================

    private String sha256(
            String value
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );


            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );


            StringBuilder hex =
                    new StringBuilder();


            for (
                    byte b
                    : hash
            ) {

                hex.append(
                        String.format(
                                Locale.US,
                                "%02x",
                                b & 0xff
                        )
                );
            }


            return hex.toString();

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Unable to create fingerprint.",
                    e
            );


            return value;
        }
    }

    // =====================================================
    // SHOW LOCAL INSIGHTS IMMEDIATELY
    // =====================================================

    private void showLocalInsights() {

        List<AIInsightResult> localResults =
                convertLocalInsights();


        Log.d(
                TAG,
                "Local insight results = "
                        + localResults.size()
        );


        if (
                localResults.isEmpty()
        ) {

            Log.d(
                    TAG,
                    "No local insights available."
            );

            return;
        }


        // ================================================
        // DISPLAY IMMEDIATELY
        // ================================================

        if (
                tvNoInsights != null
        ) {

            tvNoInsights.setVisibility(
                    View.GONE
            );
        }


        if (
                rvInsights != null
        ) {

            rvInsights.setVisibility(
                    View.VISIBLE
            );
        }


        if (
                insightAdapter != null
        ) {

            insightAdapter.setInsights(
                    localResults
            );
        }


        Log.d(
                TAG,
                "Displayed "
                        + localResults.size()
                        + " local insight cards."
        );
    }

    // =====================================================
// CONVERT LOCAL INSIGHTS TO ADAPTER RESULTS
// =====================================================

    private List<AIInsightResult> convertLocalInsights() {

        List<AIInsightResult> results =
                new ArrayList<>();


        if (
                detectedInsights == null ||
                        detectedInsights.isEmpty()
        ) {

            return results;
        }


        for (
                FinancialInsight insight
                : detectedInsights
        ) {

            if (insight == null) {
                continue;
            }


            FinancialInsight.Severity severity =
                    insight.getSeverity();


            if (severity == null) {

                severity =
                        FinancialInsight.Severity.LOW;
            }


            AIInsightResult result =
                    new AIInsightResult(
                            insight.getTitle(),
                            insight.getMessage(),
                            severity
                    );


            results.add(
                    result
            );
        }


        return results;
    }


    // =====================================================
    // SHOW AI INSIGHTS
    // =====================================================

    private void showAIInsights(
            List<AIInsightResult> insights
    ) {

        setLoading(
                false
        );


        if (
                insights == null ||
                        insights.isEmpty()
        ) {

            showEmptyInsights();

            return;
        }


        if (
                tvNoInsights != null
        ) {

            tvNoInsights.setVisibility(
                    View.GONE
            );
        }


        if (
                rvInsights != null
        ) {

            rvInsights.setVisibility(
                    View.VISIBLE
            );
        }


        insightAdapter.setInsights(
                insights
        );


        Log.d(
                TAG,
                "Displaying "
                        + insights.size()
                        + " AI insight cards."
        );
    }


    // =====================================================
    // SHOW EMPTY
    // =====================================================

    private void showEmptyInsights() {

        setLoading(
                false
        );


        if (
                insightAdapter != null
        ) {

            insightAdapter.setInsights(
                    new ArrayList<>()
            );
        }


        if (
                rvInsights != null
        ) {

            rvInsights.setVisibility(
                    View.GONE
            );
        }


        if (
                tvNoInsights != null
        ) {

            tvNoInsights.setVisibility(
                    View.VISIBLE
            );
        }
    }


    // =====================================================
    // LOADING
    // =====================================================

    private void setLoading(
            boolean loading
    ) {

        if (
                progressInsights != null
        ) {

            progressInsights.setVisibility(
                    loading
                            ? View.VISIBLE
                            : View.GONE
            );
        }


        if (
                loading
        ) {

            if (
                    rvInsights != null
            ) {

                rvInsights.setVisibility(
                        View.GONE
                );
            }


            if (
                    tvNoInsights != null
            ) {

                tvNoInsights.setVisibility(
                        View.GONE
                );
            }
        }
    }


    // =====================================================
    // NORMAL REFRESH
    // =====================================================

    public void refreshAIInsights() {

        Log.d(
                TAG,
                "Refreshing AI insights..."
        );


        /*
         * Normal refresh respects the fingerprint cache.
         */

        loadFinancialInsights();
    }


    // =====================================================
    // FORCE REFRESH
    // =====================================================

    public void forceRefreshAIInsights() {

        Log.d(
                TAG,
                "Force refreshing AI insights..."
        );


        if (
                currentLocalUserId != -1
        ) {

            AIInsightCacheManager.invalidate(
                    this,
                    currentLocalUserId
            );
        }


        loadFinancialInsights();
    }


    // =====================================================
    // EXTERNAL FINANCIAL ANALYSIS
    // =====================================================

    public void setFinancialAnalysis(
            FinancialAnalysis analysis
    ) {

        this.financialAnalysis =
                analysis;


        if (
                analysis == null
        ) {

            showEmptyInsights();

            return;
        }


        detectedInsights =
                proactiveInsightEngine.analyze(
                        analysis
                );


        String fingerprint =
                createFinancialFingerprint();


        List<AIInsightResult> cached =
                AIInsightCacheManager
                        .getCachedInsights(
                                this,
                                currentLocalUserId,
                                fingerprint
                        );


        if (
                cached != null
        ) {

            showAIInsights(
                    cached
            );

        } else {

            generateAIInsights(
                    fingerprint
            );
        }
    }


    // =====================================================
    // EXTERNAL DETECTED INSIGHTS
    // =====================================================

    public void setDetectedInsights(
            List<FinancialInsight> insights
    ) {

        if (
                insights == null
        ) {

            detectedInsights =
                    new ArrayList<>();

        } else {

            detectedInsights =
                    new ArrayList<>(
                            insights
                    );
        }


        if (
                financialAnalysis == null
        ) {

            return;
        }


        String fingerprint =
                createFinancialFingerprint();


        List<AIInsightResult> cached =
                AIInsightCacheManager
                        .getCachedInsights(
                                this,
                                currentLocalUserId,
                                fingerprint
                        );


        if (
                cached != null
        ) {

            showAIInsights(
                    cached
            );

        } else {

            generateAIInsights(
                    fingerprint
            );
        }
    }


    // =====================================================
    // ON DESTROY
    // =====================================================

    @Override
    protected void onDestroy() {

        if (
                apiService != null
        ) {

            apiService.shutdown();
        }


        if (
                databaseHelper != null
        ) {

            databaseHelper.close();
        }


        super.onDestroy();
    }
}