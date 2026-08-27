package com.example.aiexpensemanagementapplication.ui.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.ui.reports.ReportsActivity;
import com.example.aiexpensemanagementapplication.ui.subscription.SubscriptionActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;


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
        // Subscription
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

                /*
                 * Later we can open a dedicated
                 * AI Insights Activity here.
                 *
                 * For now we keep the button ready.
                 */

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

        loadBalance();

        loadPieChart();

        loadWeeklyChart();

        loadSubscriptions();

        loadAIInsights();
    }


    // =========================================================
    // BALANCE
    // =========================================================

    private void loadBalance() {

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


        double income =
                databaseHelper.getTotalIncome(userId);


        double balance =
                databaseHelper.getTotalBalance(userId);


        double budgetRemaining =
                databaseHelper.getDashboardRemainingBudget(userId);


        double budgetUsed =
                databaseHelper.getDashboardBudgetUsed(userId);


        // -----------------------------------------------------
        // Display values
        // -----------------------------------------------------

        tvIncome.setText(
                String.format(
                        "Rs %.2f",
                        income
                )
        );


        tvTotalBalance.setText(
                String.format(
                        "Rs %.2f",
                        balance
                )
        );


        tvRemainingBudget.setText(
                String.format(
                        "Rs %.2f",
                        budgetRemaining
                )
        );


        // Prevent percentage > 100
        if (budgetUsed > 100) {
            budgetUsed = 100;
        }


        if (budgetUsed < 0) {
            budgetUsed = 0;
        }


        tvBudgetPercentage.setText(
                String.format(
                        "%.0f%%",
                        budgetUsed
                )
        );
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
                databaseHelper.getCategoryPieEntries(
                        userId
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
                databaseHelper.getWeeklyExpenseEntries(
                        userId
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


        int count =
                databaseHelper
                        .getActiveSubscriptionCount(
                                userId
                        );


        double total =
                databaseHelper
                        .getMonthlySubscriptionAmount(
                                userId
                        );


        // -----------------------------------------------------
        // Display
        // -----------------------------------------------------

        tvSubscriptionCount.setText(
                count + " Active Subscriptions"
        );


        tvSubscriptionTotal.setText(
                String.format(
                        "Rs %.2f",
                        total
                )
        );


        // -----------------------------------------------------
        // Empty state
        // -----------------------------------------------------

        if (count == 0) {

            tvNoSubscriptions.setVisibility(
                    View.VISIBLE
            );


            rvSubscriptions.setVisibility(
                    View.GONE
            );

        } else {

            tvNoSubscriptions.setVisibility(
                    View.GONE
            );


            rvSubscriptions.setVisibility(
                    View.VISIBLE
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
    }
}