package com.example.aiexpensemanagementapplication.ui.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.Calendar;
import android.widget.TextView;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.ui.reports.ReportsActivity;
import com.example.aiexpensemanagementapplication.ui.subscription.SubscriptionActivity;
import com.example.aiexpensemanagementapplication.ml.ExpensePredictionFeatureGenerator;
import com.example.aiexpensemanagementapplication.ml.ExpensePredictionRequest;
import com.example.aiexpensemanagementapplication.ml.ExpensePredictionApiService;
import com.example.aiexpensemanagementapplication.ui.subscription.CurrencyRateManager;

import java.util.Map;
import java.util.Collections;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.example.aiexpensemanagementapplication.ui.common.MonthNavigator;
import com.example.aiexpensemanagementapplication.ui.common.MonthSwipeHelper;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PersonalDashboardFragment extends Fragment {

    // =========================================================
    // DATABASE / AUTH
    // =========================================================

    private DatabaseHelper databaseHelper;

    private FirebaseAuth mAuth;

    private FirebaseUser currentUser;


    // =========================================================
    // BALANCE
    // =========================================================

    private TextView tvTotalBalance;

    private TextView tvIncome;

    private TextView tvRemainingBudget;

    private TextView tvBudgetPercentage;


    // =========================================================
    // CATEGORY CHART
    // =========================================================

    private PieChart pieChart;


    // =========================================================
    // WEEKLY CHART
    // =========================================================

    private LineChart lineChart;


    // =========================================================
    // SUBSCRIPTIONS
    // =========================================================

    private RecyclerView rvSubscriptions;

    private TextView tvSubscriptionTotal;

    private TextView tvSubscriptionCount;

    private TextView tvNoSubscriptions;

    private TextView tvViewAllSubscriptions;

    private Map<String, Double> exchangeRates = Collections.emptyMap();
    private CurrencyRateManager currencyRateManager;


    // =========================================================
    // REPORTS
    // =========================================================

    private MaterialButton btnReports;


    // =========================================================
    // AI INSIGHTS
    // =========================================================

    private TextView tvAIInsight;

    private MaterialButton btnAIFinancialAdvisor;

    private TextView tvRecommendation;

    private MaterialButton btnViewInsights;

    // =========================================================
// EXPENSE PREDICTION
// =========================================================

    private TextView tvPredictedExpense;

    private TextView tvPredictionStatus;

    private TextView tvPredictionMonth;

    private ExpensePredictionApiService expensePredictionApiService;

    private ExpensePredictionFeatureGenerator expensePredictionFeatureGenerator;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public PersonalDashboardFragment() {
        // Required empty public constructor
    }


    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_personal_dashboard,
                container,
                false
        );
    }


    // =========================================================
    // VIEW CREATED
    // =========================================================

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        initialize(view);

        setupDashboardMonthNavigation(view);

        setupClickListeners();

        loadDashboard();
    }


    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initialize(View view) {

        // Database
        databaseHelper = new DatabaseHelper(requireContext());


        // Firebase
        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();


        // -----------------------------------------------------
        // Balance
        // -----------------------------------------------------

        tvTotalBalance =
                view.findViewById(R.id.tvTotalBalance);

        tvIncome =
                view.findViewById(R.id.tvIncome);

        tvRemainingBudget =
                view.findViewById(R.id.tvRemainingBudget);

        tvBudgetPercentage =
                view.findViewById(R.id.tvBudgetPercentage);


        // -----------------------------------------------------
        // Charts
        // -----------------------------------------------------

        pieChart =
                view.findViewById(R.id.pieChart);

        lineChart =
                view.findViewById(R.id.lineChart);


        // -----------------------------------------------------
        // Subscription views
        // -----------------------------------------------------

        rvSubscriptions =
                view.findViewById(R.id.rvSubscriptions);

        tvSubscriptionTotal =
                view.findViewById(R.id.tvSubscriptionTotal);

        tvSubscriptionCount =
                view.findViewById(R.id.tvSubscriptionCount);

        tvNoSubscriptions =
                view.findViewById(R.id.tvNoSubscriptions);

        tvViewAllSubscriptions =
                view.findViewById(R.id.tvViewAllSubscriptions);

        // -----------------------------------------------------
        // Currency rates for converting subscription amounts to LKR
        // Initialize this only after the subscription TextViews exist.
        // -----------------------------------------------------

        currencyRateManager = new CurrencyRateManager(requireContext());
        exchangeRates = currencyRateManager.getRates();

        currencyRateManager.refresh(false, (rates, updatedAt, fromCache, error) -> {
            if (!isAdded() || getView() == null) {
                return;
            }

            exchangeRates = rates == null
                    ? Collections.emptyMap()
                    : rates;

            // Recalculate the total using the latest available rates.
            loadSubscriptions();
        });


        // -----------------------------------------------------
        // Reports
        // -----------------------------------------------------

        btnReports =
                view.findViewById(R.id.btnReports);


        // -----------------------------------------------------
        // AI
        // -----------------------------------------------------

        tvAIInsight =
                view.findViewById(R.id.tvAIInsight);

        tvRecommendation =
                view.findViewById(R.id.tvRecommendation);

        btnViewInsights =
                view.findViewById(R.id.btnViewInsights);

        btnAIFinancialAdvisor =
                view.findViewById(R.id.btnAIFinancialAdvisor);
        // =====================================================
// EXPENSE PREDICTION
// =====================================================

        tvPredictedExpense =
                view.findViewById(
                        R.id.tvPredictedExpense
                );

        tvPredictionStatus =
                view.findViewById(
                        R.id.tvPredictionStatus
                );

        tvPredictionMonth = view.findViewById(R.id.tvPredictionMonth);


        expensePredictionApiService =
                new ExpensePredictionApiService();


        expensePredictionFeatureGenerator =
                new ExpensePredictionFeatureGenerator(
                        requireContext()
                );
    }


    private void setupDashboardMonthNavigation(View root) {

        TextView previous =
                root.findViewById(R.id.btnPreviousMonth);

        TextView next =
                root.findViewById(R.id.btnNextMonth);

        previous.setOnClickListener(v ->
                changeDashboardMonth(-1));

        next.setOnClickListener(v ->
                changeDashboardMonth(1));

        MonthSwipeHelper.attach(
                root.findViewById(R.id.cardBalance),
                () -> changeDashboardMonth(-1),
                () -> changeDashboardMonth(1)
        );

        updateDashboardMonthLabel(root);
    }

    private void changeDashboardMonth(int offset) {

        MonthNavigator.changeMonth(offset);

        View root = getView();

        if (root == null) {
            return;
        }

        updateDashboardMonthLabel(root);

        loadBalance();
        loadPieChart();
        loadWeeklyChart();
    }

    private void updateDashboardMonthLabel(View root) {

        TextView monthLabel =
                root.findViewById(R.id.tvMonthLabel);

        monthLabel.setText(
                MonthNavigator.getMonthTitle()
        );
    }


    // =========================================================
    // CLICK LISTENERS
    // =========================================================

    private void setupClickListeners() {

        // -----------------------------------------------------
        // Reports
        // -----------------------------------------------------

        btnReports.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireContext(),
                            ReportsActivity.class
                    );

            startActivity(intent);
        });


        // -----------------------------------------------------
        // View All Subscriptions
        // -----------------------------------------------------

        if (tvViewAllSubscriptions != null) {

            tvViewAllSubscriptions.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                requireContext(),
                                SubscriptionActivity.class
                        );

                startActivity(intent);
            });
        }


        // -----------------------------------------------------
        // Detailed AI Insights
        // -----------------------------------------------------

        if (btnAIFinancialAdvisor != null) {

            btnAIFinancialAdvisor.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                requireContext(),
                                com.example.aiexpensemanagementapplication
                                        .ui.ai
                                        .AIFinancialAdvisorActivity.class
                        );

                startActivity(intent);
            });
        }

        if (btnViewInsights != null) {

            btnViewInsights.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                requireContext(),
                                com.example.aiexpensemanagementapplication
                                        .ui.ai
                                        .AIInsightsActivity.class
                        );

                startActivity(intent);

            });
        }
    }


    // =========================================================
    // LOAD DASHBOARD
    // =========================================================

    private void loadDashboard() {

        if (currentUser == null) {
            return;
        }

        View root = getView();

        if (root != null) {
            updateDashboardMonthLabel(root);
        }

        loadBalance();

        loadPieChart();

        loadWeeklyChart();

        loadSubscriptions();

        loadAIInsights();

        loadExpensePrediction();
    }


    // =========================================================
    // BALANCE
    // =========================================================


    private void loadBalance() {

        if (currentUser == null) {
            return;
        }

        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid()
                );

        if (userId == -1) {
            return;
        }

        String month =
                MonthNavigator.getSelectedMonth();

        double income =
                databaseHelper.getMonthlyTransactionTotal(
                        userId, "Income", month
                );

        double expense =
                databaseHelper.getMonthlyTransactionTotal(
                        userId, "Expense", month
                );

        double balance = income - expense;

        tvIncome.setText(
                String.format(Locale.getDefault(),
                        "Rs %.2f", income)
        );

        tvRemainingBudget.setText(
                String.format(Locale.getDefault(),
                        "Rs %.2f", expense)
        );

        tvTotalBalance.setText(
                String.format(Locale.getDefault(),
                        "Rs %.2f", balance)
        );

        if (income > 0) {
            double percentage = (expense / income) * 100;

            tvBudgetPercentage.setText(
                    String.format(Locale.getDefault(),
                            "%.0f%%", percentage)
            );
        } else {
            tvBudgetPercentage.setText("—");
        }
    }



    // =========================================================
    // PIE CHART
    // =========================================================

    private void loadPieChart() {

        if (currentUser == null) {
            return;
        }


        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid()
                );


        if (userId == -1) {
            return;
        }


        ArrayList<PieEntry> entries =
                databaseHelper.getCategoryPieEntriesForMonth(
                        userId,
                        MonthNavigator.getSelectedMonth()
                );


        // -----------------------------------------------------
        // No data
        // -----------------------------------------------------

        if (entries == null || entries.isEmpty()) {

            pieChart.clear();

            pieChart.setNoDataText(
                    "No expense data available"
            );

            pieChart.invalidate();

            return;
        }


        // -----------------------------------------------------
        // Dataset
        // -----------------------------------------------------

        PieDataSet dataSet =
                new PieDataSet(
                        entries,
                        ""
                );


        dataSet.setColors(
                ColorTemplate.MATERIAL_COLORS
        );


        dataSet.setSliceSpace(3f);

        dataSet.setSelectionShift(5f);


        PieData data =
                new PieData(dataSet);


        data.setValueTextSize(12f);


        // -----------------------------------------------------
        // Chart
        // -----------------------------------------------------

        pieChart.setData(data);


        pieChart
                .getDescription()
                .setEnabled(false);


        pieChart
                .getLegend()
                .setEnabled(false);


        pieChart.setDrawHoleEnabled(true);

        pieChart.setHoleRadius(68f);

        pieChart.setTransparentCircleRadius(72f);

        pieChart.setRotationEnabled(true);

        pieChart.setHighlightPerTapEnabled(true);


        pieChart.invalidate();
    }


    // =========================================================
    // WEEKLY SPENDING CHART
    // =========================================================

    private void loadWeeklyChart() {

        if (currentUser == null) {
            return;
        }


        int userId =
                databaseHelper.getUserIdByEmail(
                        currentUser.getEmail()
                );


        if (userId == -1) {
            return;
        }


        List<Entry> entries =
                databaseHelper.getWeeklyExpenseEntriesForMonth(
                        userId,
                        MonthNavigator.getSelectedMonth()
                );


        // -----------------------------------------------------
        // No data
        // -----------------------------------------------------

        if (entries == null || entries.isEmpty()) {

            lineChart.clear();

            lineChart.setNoDataText(
                    "No weekly spending data available"
            );

            lineChart.invalidate();

            return;
        }


        // -----------------------------------------------------
        // Dataset
        // -----------------------------------------------------

        LineDataSet dataSet =
                new LineDataSet(
                        entries,
                        "Weekly Expense"
                );


        dataSet.setLineWidth(3f);

        dataSet.setCircleRadius(5f);

        dataSet.setDrawFilled(false);


        dataSet.setColor(
                Color.parseColor("#22C55E")
        );


        dataSet.setCircleColor(
                Color.parseColor("#22C55E")
        );


        LineData lineData =
                new LineData(dataSet);


        // -----------------------------------------------------
        // Chart
        // -----------------------------------------------------

        lineChart.setData(lineData);


        lineChart
                .getDescription()
                .setEnabled(false);


        lineChart
                .getLegend()
                .setEnabled(false);


        lineChart.invalidate();
    }


    // =========================================================
    // SUBSCRIPTIONS
    // =========================================================

    private void loadSubscriptions() {

        if (!isAdded() ||
                getView() == null ||
                databaseHelper == null ||
                currentUser == null ||
                currentUser.getEmail() == null ||
                tvSubscriptionTotal == null ||
                tvSubscriptionCount == null ||
                tvNoSubscriptions == null ||
                rvSubscriptions == null) {
            return;
        }

        int userId = databaseHelper.getUserIdByEmail(
                currentUser.getEmail()
        );

        if (userId <= 0) {
            tvSubscriptionTotal.setText("LKR 0.00");
            tvSubscriptionCount.setText("0 Subscriptions");
            tvNoSubscriptions.setVisibility(View.VISIBLE);
            rvSubscriptions.setVisibility(View.GONE);
            return;
        }

        Cursor cursor = null;
        double totalLkr = 0.0;
        int count = 0;
        int convertedCount = 0;
        int unconvertedCount = 0;

        try {
            cursor = databaseHelper.getSubscriptions(userId);

            if (cursor != null) {
                while (cursor.moveToNext()) {

                    double amount = cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.AMOUNT
                            )
                    );

                    String currency = "LKR";

                    int currencyIndex = cursor.getColumnIndex(
                            DatabaseHelper.SUBSCRIPTION_CURRENCY
                    );

                    if (currencyIndex != -1 &&
                            !cursor.isNull(currencyIndex)) {
                        currency = cursor.getString(currencyIndex);
                    }

                    // Convert original amount to LKR.
                    Double amountLkr =
                            CurrencyRateManager.convertToLkr(
                                    amount,
                                    currency,
                                    exchangeRates
                            );

                    count++;

                    if (amountLkr != null) {
                        totalLkr += amountLkr;
                        convertedCount++;
                    } else {
                        unconvertedCount++;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        tvSubscriptionCount.setText(
                count + " Subscriptions"
        );

        if (count == 0) {
            tvSubscriptionTotal.setText("LKR 0.00");
            tvNoSubscriptions.setVisibility(View.VISIBLE);
            rvSubscriptions.setVisibility(View.GONE);
            return;
        }

        tvNoSubscriptions.setVisibility(View.GONE);
        rvSubscriptions.setVisibility(View.VISIBLE);

        if (convertedCount == 0) {
            tvSubscriptionTotal.setText(
                    "Exchange rates unavailable"
            );
        } else if (unconvertedCount > 0) {
            tvSubscriptionTotal.setText(
                    String.format(
                            Locale.US,
                            "LKR %,.2f + review",
                            totalLkr
                    )
            );
        } else {
            tvSubscriptionTotal.setText(
                    String.format(
                            Locale.US,
                            "LKR %,.2f",
                            totalLkr
                    )
            );
        }
    }


    // =========================================================
    // AI INSIGHTS
    // =========================================================

    private void loadAIInsights() {

        if (currentUser == null) {
            return;
        }

        String email = currentUser.getEmail();

        if (email == null || email.trim().isEmpty()) {

            tvAIInsight.setText(
                    "Unable to identify your account."
            );

            tvRecommendation.setText(
                    "Please login again."
            );

            return;
        }

        int userId =
                databaseHelper.getUserIdByEmail(
                        email.trim()
                );

        if (userId == -1) {

            tvAIInsight.setText(
                    "Your financial profile could not be found."
            );

            tvRecommendation.setText(
                    "Please make sure your account is properly registered."
            );

            return;
        }

        // -------------------------------------------------
        // GENERATE INSIGHT
        // -------------------------------------------------

        String insight =
                databaseHelper.generateAIInsight(userId);

        if (insight == null ||
                insight.trim().isEmpty()) {

            tvAIInsight.setText(
                    "No financial insight is available yet."
            );

        } else {

            tvAIInsight.setText(insight);
        }


        // -------------------------------------------------
        // GENERATE RECOMMENDATION
        // -------------------------------------------------

        double income =
                databaseHelper.getTotalIncome(userId);

        double expense =
                databaseHelper.getTotalExpense(userId);

        double remaining =
                income - expense;


        if (income <= 0) {

            tvRecommendation.setText(
                    "Add your income to receive personalized "
                            + "budget and saving recommendations."
            );

            return;
        }


        double percentage =
                (expense / income) * 100;


        if (expense > income) {

            tvRecommendation.setText(
                    "Recommendation: Reduce non-essential expenses "
                            + "and review your highest spending categories."
            );

        } else if (percentage >= 80) {

            tvRecommendation.setText(
                    "Recommendation: Your spending is high. "
                            + "Try to reduce discretionary expenses this month."
            );

        } else if (percentage >= 60) {

            tvRecommendation.setText(
                    "Recommendation: Keep monitoring your spending "
                            + "and try to increase your monthly savings."
            );

        } else {

            tvRecommendation.setText(
                    "Recommendation: Your spending is healthy. "
                            + "Consider saving or investing part of your remaining income."
            );
        }
    }

    // =========================================================
// EXPENSE PREDICTION
// =========================================================

    private void loadExpensePrediction() {

        if (currentUser == null) {
            return;
        }

        String email = currentUser.getEmail();

        if (email == null || email.trim().isEmpty()) {

            showPredictionError(
                    "Unable to identify your account."
            );

            return;
        }

        int userId =
                databaseHelper.getUserIdByEmail(
                        email.trim()
                );

        if (userId == -1) {

            showPredictionError(
                    "User account was not found."
            );

            return;
        }

        // ---------------------------------------------------------
        // Latest completed month
        // ---------------------------------------------------------

        Calendar calendar =
                Calendar.getInstance();

        calendar.add(
                Calendar.MONTH,
                -1
        );

        int year =
                calendar.get(Calendar.YEAR);

        int month =
                calendar.get(Calendar.MONTH) + 1;

        String monthString =
                String.format(
                        Locale.US,
                        "%04d-%02d",
                        year,
                        month
                );

        // ---------------------------------------------------------
        // Actual expense of completed month
        // ---------------------------------------------------------

        double actualExpense =
                databaseHelper.getMonthlyExpense(
                        userId,
                        monthString
                );

        // ---------------------------------------------------------
        // Generate ML features
        // ---------------------------------------------------------

        ExpensePredictionRequest request;

        try {

            request =
                    expensePredictionFeatureGenerator
                            .generateRequest(
                                    userId,
                                    year,
                                    month
                            );

        } catch (Exception e) {

            showPredictionError(
                    "Unable to prepare prediction data."
            );

            return;
        }

        // ---------------------------------------------------------
        // Show loading state
        // ---------------------------------------------------------

        tvPredictedExpense.setText(
                "Calculating..."
        );

        tvPredictionStatus.setText(
                "Analyzing your latest completed month..."
        );

        // ---------------------------------------------------------
        // Send to LightGBM API
        // ---------------------------------------------------------

        expensePredictionApiService.predict(
                request,
                new ExpensePredictionApiService.PredictionCallback() {

                    @Override
                    public void onSuccess(
                            double predictedExpense
                    ) {

                        if (!isAdded()) {
                            return;
                        }

                        // -----------------------------------------
                        // Predicted expense
                        // -----------------------------------------

                        tvPredictedExpense.setText(
                                String.format(
                                        Locale.US,
                                        "Rs %.2f",
                                        predictedExpense
                                )
                        );

                        tvPredictionMonth.setText(
                                String.format(
                                        Locale.US,
                                        "%s %d forecast",
                                        getMonthName(month),
                                        year
                                )
                        );

                        // -----------------------------------------
                        // Compare with actual expense
                        // -----------------------------------------

                        if (actualExpense <= 0) {

                            tvPredictionStatus.setText(
                                    String.format(
                                            Locale.US,
                                            "Based on %s spending",
                                            getMonthName(
                                                    month
                                            )
                                    )
                            );

                            return;
                        }

                        double difference =
                                predictedExpense
                                        - actualExpense;

                        double percentageChange =
                                (difference
                                        / actualExpense)
                                        * 100.0;

                        String monthName =
                                getMonthName(month);

                        if (difference > 0) {

                            tvPredictionStatus.setText(
                                    String.format(
                                            Locale.US,
                                            "↑ %.1f%% vs %s actual expense",
                                            percentageChange,
                                            monthName
                                    )
                            );

                            tvPredictionStatus.setTextColor(
                                    Color.parseColor(
                                            "#DC2626"
                                    )
                            );

                        } else if (difference < 0) {

                            tvPredictionStatus.setText(
                                    String.format(
                                            Locale.US,
                                            "↓ %.1f%% vs %s actual expense",
                                            Math.abs(
                                                    percentageChange
                                            ),
                                            monthName
                                    )
                            );

                            tvPredictionStatus.setTextColor(
                                    Color.parseColor(
                                            "#16A34A"
                                    )
                            );

                        } else {

                            tvPredictionStatus.setText(
                                    String.format(
                                            Locale.US,
                                            "Same as %s actual expense",
                                            monthName
                                    )
                            );

                            tvPredictionStatus.setTextColor(
                                    Color.parseColor(
                                            "#64748B"
                                    )
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            String message
                    ) {

                        if (!isAdded()) {
                            return;
                        }

                        showPredictionError(
                                message
                        );
                    }
                }
        );
    }

    // =========================================================
// MONTH NAME
// =========================================================

    private String getMonthName(int month) {

        String[] months = {

                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December"
        };

        if (month < 1 || month > 12) {
            return "";
        }

        return months[month - 1];
    }

    private void showPredictionError(
            String message
    ) {

        if (tvPredictedExpense != null) {

            tvPredictedExpense.setText(
                    "Prediction unavailable"
            );
        }


        if (tvPredictionStatus != null) {

            tvPredictionStatus.setText(
                    message != null
                            ? message
                            : "Unable to generate prediction."
            );
        }
    }


    // =========================================================
    // RESUME
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();


        /*
         * When the user returns after:
         *
         * Add Expense
         * Add Income
         * Edit Expense
         * Subscription changes
         *
         * reload the Personal Dashboard.
         */

        if (databaseHelper != null &&
                currentUser != null) {

            loadDashboard();
        }
    }


    // =========================================================
    // CLEANUP
    // =========================================================

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        /*
         * We don't close DatabaseHelper here because
         * your existing helper may be shared internally.
         *
         * We simply release View references.
         */

        tvTotalBalance = null;
        tvIncome = null;
        tvRemainingBudget = null;
        tvBudgetPercentage = null;

        pieChart = null;
        lineChart = null;

        rvSubscriptions = null;
        tvSubscriptionTotal = null;
        tvSubscriptionCount = null;
        tvNoSubscriptions = null;
        tvViewAllSubscriptions = null;

        btnReports = null;

        tvAIInsight = null;
        tvRecommendation = null;
        btnViewInsights = null;
        tvPredictedExpense = null;
        tvPredictionStatus = null;
    }
}