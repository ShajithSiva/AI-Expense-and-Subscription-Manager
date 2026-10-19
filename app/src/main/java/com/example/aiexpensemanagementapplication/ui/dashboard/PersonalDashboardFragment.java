package com.example.aiexpensemanagementapplication.ui.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.Calendar;

import android.widget.LinearLayout;
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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
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

    private TextView tvWeeklyPercentage;
    private TextView tvWeeklyTotal;

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

        tvWeeklyPercentage =
                view.findViewById(R.id.tvWeeklyPercentage);

        tvWeeklyTotal =
                view.findViewById(R.id.tvWeeklyTotal);


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

        // Remove invalid or zero-value categories
        entries.removeIf(entry -> entry.getValue() <= 0f);


        // -----------------------------------------------------
        // No data
        // -----------------------------------------------------


        // Professional expense category pie chart
        if (entries == null || entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No expenses recorded for this month");
            pieChart.setNoDataTextColor(Color.GRAY);
            pieChart.invalidate();
            return;
        }

        entries.removeIf(entry -> entry.getValue() <= 0f);

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No expenses recorded for this month");
            pieChart.invalidate();
            return;
        }

        // Calculate total expense for the selected month
        float total = 0f;

        for (PieEntry entry : entries) {
            total += entry.getValue();
        }

        final float totalExpense = total;

        updateCategoryBreakdown(entries, totalExpense);

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Premium, coordinated colour palette
        ArrayList<Integer> chartColors = new ArrayList<>();
        chartColors.add(Color.parseColor("#2563EB")); // Blue
        chartColors.add(Color.parseColor("#14B8A6")); // Teal
        chartColors.add(Color.parseColor("#F59E0B")); // Amber
        chartColors.add(Color.parseColor("#8B5CF6")); // Purple
        chartColors.add(Color.parseColor("#F43F5E")); // Rose
        chartColors.add(Color.parseColor("#64748B")); // Slate
        chartColors.add(Color.parseColor("#06B6D4")); // Cyan
        chartColors.add(Color.parseColor("#84CC16")); // Lime
        chartColors.add(Color.parseColor("#F97316")); // Orange

        dataSet.setColors(chartColors);
        dataSet.setSliceSpace(3f);
        dataSet.setDrawValues(false);
        dataSet.setSelectionShift(6f);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setYValuePosition(
                PieDataSet.ValuePosition.INSIDE_SLICE
        );

        // Display each category's percentage
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (totalExpense <= 0f) {
                    return "";
                }

                return String.format(
                        Locale.getDefault(),
                        "%.0f%%",
                        (value / totalExpense) * 100f
                );
            }
        });

        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        pieChart.setHoleRadius(68f);
        pieChart.setTransparentCircleRadius(72f);
        pieChart.setExtraTopOffset(8f);
        pieChart.setExtraBottomOffset(8f);
        pieChart.setExtraLeftOffset(8f);
        pieChart.setExtraRightOffset(8f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(true);

        pieChart.setData(data);
        pieChart.setUsePercentValues(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(70f);

        pieChart.setTransparentCircleColor(
                Color.parseColor("#E2E8F0")
        );
        pieChart.setTransparentCircleAlpha(100);

        // Center total
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText(
                "TOTAL\n" +
                        String.format(
                                Locale.getDefault(),
                                "Rs. %,.0f",
                                (double) totalExpense
                        )
        );
        pieChart.setCenterTextSize(15f);
        pieChart.setCenterTextColor(
                Color.parseColor("#14213D")
        );

        pieChart.setEntryLabelColor(Color.DKGRAY);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.setDrawEntryLabels(false);

        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setTextColor(
                Color.parseColor("#475569")
        );
        pieChart.getLegend().setTextSize(11f);
        pieChart.getLegend().setWordWrapEnabled(true);
        pieChart.getLegend().setForm(
                com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
        );
        pieChart.getLegend().setFormSize(9f);
        pieChart.getLegend().setXEntrySpace(10f);
        pieChart.getLegend().setYEntrySpace(5f);

        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setExtraOffsets(8f, 8f, 8f, 8f);
        pieChart.setMinAngleForSlices(5f);
        pieChart.animateY(700);
        pieChart.invalidate();

    }


    // =========================================================
    // WEEKLY SPENDING CHART
    // =========================================================


    private void loadWeeklyChart() {

        if (currentUser == null) return;

        int userId = databaseHelper.getUserIdByEmail(
                currentUser.getEmail()
        );

        if (userId == -1) return;

        List<Entry> entries =
                databaseHelper.getCurrentWeekExpenseEntries(userId);


        float weeklyTotal = 0f;

        for (Entry entry : entries) {
            weeklyTotal += entry.getY();
        }

        double previousWeekTotal =
                databaseHelper.getPreviousWeekExpenseTotal(userId);

        if (tvWeeklyPercentage != null) {

            if (previousWeekTotal > 0) {

                double changePercent =
                        ((weeklyTotal - previousWeekTotal)
                                / previousWeekTotal) * 100;

                if (changePercent > 0) {
                    tvWeeklyPercentage.setText(
                            String.format(
                                    Locale.getDefault(),
                                    "↑ %.1f%% vs last week",
                                    changePercent
                            )
                    );
                    tvWeeklyPercentage.setTextColor(
                            Color.parseColor("#DC2626")
                    );

                } else if (changePercent < 0) {
                    tvWeeklyPercentage.setText(
                            String.format(
                                    Locale.getDefault(),
                                    "↓ %.1f%% vs last week",
                                    Math.abs(changePercent)
                            )
                    );
                    tvWeeklyPercentage.setTextColor(
                            Color.parseColor("#12805C")
                    );

                } else {
                    tvWeeklyPercentage.setText("0% vs last week");
                    tvWeeklyPercentage.setTextColor(Color.GRAY);
                }

            } else if (weeklyTotal > 0) {

                tvWeeklyPercentage.setText("New spending");
                tvWeeklyPercentage.setTextColor(
                        Color.parseColor("#D97706")
                );

            } else {

                tvWeeklyPercentage.setText("No spending");
                tvWeeklyPercentage.setTextColor(Color.GRAY);
            }
        }


        tvWeeklyTotal.setText(
                String.format(
                        Locale.getDefault(),
                        "Rs. %,.2f",
                        weeklyTotal
                )
        );


        LineDataSet dataSet = new LineDataSet(
                entries,
                "Daily Spending (Rs.)"
        );

        dataSet.setColor(Color.parseColor("#2563EB"));
        dataSet.setCircleColor(Color.parseColor("#2563EB"));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.DKGRAY);

        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#DBEAFE"));
        dataSet.setMode(LineDataSet.Mode.LINEAR);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Monday to Sunday labels
        final String[] days = {
                "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
        };

        XAxis xAxis = lineChart.getXAxis();

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);

                if (index >= 0 && index < days.length) {
                    return days[index];
                }

                return "";
            }
        });

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(6f);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7, true);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(0f);
        xAxis.setTextColor(Color.DKGRAY);

        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisLeft().setTextColor(Color.DKGRAY);

        lineChart.getAxisRight().setEnabled(false);

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
        lineChart.setNoDataText("No spending recorded this week");
        lineChart.setExtraBottomOffset(8f);

        lineChart.animateX(500);
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
        tvWeeklyPercentage = null;
        tvWeeklyTotal = null;

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


    private void updateCategoryBreakdown(
            ArrayList<PieEntry> entries,
            float totalExpense
    ) {
        View root = getView();

        if (root == null) return;

        LinearLayout container = root.findViewById(
                R.id.categoryBreakdownContainer
        );

        if (container == null) return;

        container.removeAllViews();

        if (entries == null || entries.isEmpty() || totalExpense <= 0f) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("No category expenses available");
            emptyView.setTextColor(Color.GRAY);
            emptyView.setTextSize(13f);
            emptyView.setPadding(0, 12, 0, 12);
            container.addView(emptyView);
            return;
        }

        int[] colors = {
                Color.parseColor("#2563EB"),
                Color.parseColor("#14B8A6"),
                Color.parseColor("#F59E0B"),
                Color.parseColor("#8B5CF6"),
                Color.parseColor("#F43F5E"),
                Color.parseColor("#64748B"),
                Color.parseColor("#06B6D4"),
                Color.parseColor("#84CC16"),
                Color.parseColor("#F97316")
        };

        for (int i = 0; i < entries.size(); i++) {
            PieEntry entry = entries.get(i);

            String category = entry.getLabel();
            float amount = entry.getValue();
            float percentage = (amount / totalExpense) * 100f;
            int color = colors[i % colors.length];

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, 10, 0, 10);

            // Category colour indicator
            TextView dot = new TextView(requireContext());
            dot.setText("●");
            dot.setTextColor(color);
            dot.setTextSize(16f);

            LinearLayout.LayoutParams dotParams =
                    new LinearLayout.LayoutParams(
                            24,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            row.addView(dot, dotParams);

            // Category name and percentage
            LinearLayout details = new LinearLayout(requireContext());
            details.setOrientation(LinearLayout.VERTICAL);

            TextView nameView = new TextView(requireContext());
            nameView.setText(category);
            nameView.setTextColor(Color.parseColor("#14213D"));
            nameView.setTextSize(14f);
            nameView.setMaxLines(1);

            TextView percentView = new TextView(requireContext());
            percentView.setText(
                    String.format(
                            Locale.getDefault(),
                            "%.1f%% of expenses",
                            percentage
                    )
            );
            percentView.setTextColor(Color.parseColor("#64748B"));
            percentView.setTextSize(12f);

            details.addView(nameView);
            details.addView(percentView);

            LinearLayout.LayoutParams detailsParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                    );

            row.addView(details, detailsParams);

            // Category amount in LKR
            TextView amountView = new TextView(requireContext());
            amountView.setText(
                    String.format(
                            Locale.US,
                            "Rs. %,.2f",
                            (double) amount
                    )
            );
            amountView.setTextColor(Color.parseColor("#14213D"));
            amountView.setTextSize(13f);
            amountView.setTypeface(null, android.graphics.Typeface.BOLD);
            amountView.setGravity(Gravity.END);

            row.addView(amountView);

            container.addView(row);

            // Subtle divider between categories
            if (i < entries.size() - 1) {
                View divider = new View(requireContext());
                divider.setBackgroundColor(
                        Color.parseColor("#E2E8F0")
                );

                LinearLayout.LayoutParams dividerParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                1
                        );

                container.addView(divider, dividerParams);
            }
        }
    }

}