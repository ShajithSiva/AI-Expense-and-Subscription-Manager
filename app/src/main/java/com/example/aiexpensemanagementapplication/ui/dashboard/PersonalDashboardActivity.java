package com.example.aiexpensemanagementapplication.ui.dashboard;

import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.ui.expense.AddExpenseActivity;
import com.example.aiexpensemanagementapplication.ui.expense.ExpenseListActivity;
import com.example.aiexpensemanagementapplication.ui.profile.ProfileActivity;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import android.view.View;

import java.util.List;
import java.util.ArrayList;

import java.util.ArrayList;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PersonalDashboardActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private FirebaseUser currentUser;

    // Greeting

    private TextView tvGreeting;
    private TextView tvUserName;

    // Balance

    private TextView tvTotalBalance;
    private TextView tvIncome;
    private TextView tvRemainingBudget;
    private TextView tvBudgetPercentage;

    // Category

    private PieChart pieChart;

    // Weekly

    private LineChart lineChart;

    // Subscription

    private RecyclerView rvSubscriptions;

    private TextView tvSubscriptionTotal;
    private TextView tvSubscriptionCount;
    private TextView tvNoSubscriptions;

    // AI

    private TextView tvAIInsight;

    // FAB

    private FloatingActionButton fabAddExpense;

    // Bottom Navigation

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard_personal);

        initialize();

        loadDashboard();

        setupBottomNavigation();

        fabAddExpense.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            PersonalDashboardActivity.this,
                            AddExpenseActivity.class
                    )
            );

        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadDashboard();
    }

    private void initialize() {

        databaseHelper = new DatabaseHelper(this);

        mAuth = FirebaseAuth.getInstance();

        firestore = FirebaseFirestore.getInstance();

        currentUser = mAuth.getCurrentUser();

        // Greeting

        tvGreeting = findViewById(R.id.tvGreeting);
        tvUserName = findViewById(R.id.tvUserName);

        // Balance

        tvTotalBalance = findViewById(R.id.tvTotalBalance);

        tvIncome = findViewById(R.id.tvIncome);

        tvRemainingBudget = findViewById(R.id.tvRemainingBudget);

        tvBudgetPercentage = findViewById(R.id.tvBudgetPercentage);

        // Charts

        pieChart = findViewById(R.id.pieChart);

        lineChart = findViewById(R.id.lineChart);

        // Subscription

        rvSubscriptions = findViewById(R.id.rvSubscriptions);

        tvSubscriptionTotal = findViewById(R.id.tvSubscriptionTotal);

        tvSubscriptionCount = findViewById(R.id.tvSubscriptionCount);

        tvNoSubscriptions = findViewById(R.id.tvNoSubscriptions);

        // AI

        tvAIInsight = findViewById(R.id.tvAIInsight);

        // FAB

        fabAddExpense = findViewById(R.id.fabAddExpense);

        // Bottom Navigation

        bottomNavigation = findViewById(R.id.bottomNavigation);

    }
    private void loadDashboard() {

        loadUser();

        loadBalance();

        loadPieChart();

        loadWeeklyChart();

        loadSubscriptions();

        loadAIInsights();
    }

    private void setupBottomNavigation() {

        bottomNavigation.setSelectedItemId(R.id.nav_dashboard);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {

                return true;

            } else if (id == R.id.nav_expenses) {

                Intent intent = new Intent(
                        PersonalDashboardActivity.this,
                        ExpenseListActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);

                return true;

            } else if (id == R.id.nav_profile) {

                Intent intent = new Intent(
                        PersonalDashboardActivity.this,
                        ProfileActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);

                return true;

            }

            return false;

        });

    }

    private void loadUser() {

        if (currentUser == null) {
            return;
        }

        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {

                    String name = document.getString("fullName");

                    if (name == null || name.isEmpty()) {
                        name = currentUser.getDisplayName();
                    }

                    if (name == null || name.isEmpty()) {
                        name = "User";
                    }

                    tvUserName.setText(name);

                    int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);

                    if (hour < 12) {
                        tvGreeting.setText("Good Morning,");
                    } else if (hour < 17) {
                        tvGreeting.setText("Good Afternoon,");
                    } else {
                        tvGreeting.setText("Good Evening,");
                    }

                });

    }

    private void loadBalance() {

        int userId =
                databaseHelper.getUserIdByEmail(currentUser.getEmail());

        if (userId == -1) {
            return;
        }

        double income = databaseHelper.getTotalIncome(userId);

        double expense = databaseHelper.getTotalExpense(userId);

        double balance = databaseHelper.getTotalBalance(userId);

        double budgetRemaining = databaseHelper.getDashboardRemainingBudget(userId);

        double budgetUsed = databaseHelper.getDashboardBudgetUsed(userId);

        tvIncome.setText(String.format("Rs %.2f", income));

        tvTotalBalance.setText(String.format("Rs %.2f", balance));

        tvRemainingBudget.setText(String.format("Rs %.2f", budgetRemaining));

        if (budgetUsed > 100) {
            budgetUsed = 100;
        }

        tvBudgetPercentage.setText(String.format("%.0f%%", budgetUsed));

    }

    private void loadPieChart() {

        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid());

        if (userId == -1)
            return;

        ArrayList<PieEntry> entries = databaseHelper.getCategoryPieEntries(userId);

        float food =
                (float) databaseHelper.getCategoryExpense(userId, "Food");

        float transport =
                (float) databaseHelper.getCategoryExpense(userId, "Transport");

        float utilities =
                (float) databaseHelper.getCategoryExpense(userId, "Utilities");

        float entertainment =
                (float) databaseHelper.getCategoryExpense(userId, "Entertainment");

        if (food > 0)
            entries.add(new PieEntry(food, "Food"));

        if (transport > 0)
            entries.add(new PieEntry(transport, "Transport"));

        if (utilities > 0)
            entries.add(new PieEntry(utilities, "Utilities"));

        if (entertainment > 0)
            entries.add(new PieEntry(entertainment, "Entertainment"));

        PieDataSet dataSet =
                new PieDataSet(entries, "");

        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        dataSet.setSliceSpace(3f);

        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);

        data.setValueTextSize(12f);

        pieChart.setData(data);

        pieChart.getDescription().setEnabled(false);

        pieChart.getLegend().setEnabled(false);

        pieChart.setDrawHoleEnabled(true);

        pieChart.setHoleRadius(68f);

        pieChart.invalidate();
    }

    private void loadWeeklyChart() {

        int userId =
                databaseHelper.getUserIdByEmail(currentUser.getEmail());

        if (userId == -1)
            return;

        List<Entry> entries = databaseHelper.getWeeklyExpenseEntries(userId);

        LineDataSet dataSet =
                new LineDataSet(entries, "Weekly Expense");

        dataSet.setLineWidth(3f);

        dataSet.setCircleRadius(5f);

        dataSet.setDrawFilled(false);

        dataSet.setColor(android.graphics.Color.parseColor("#22C55E"));

        dataSet.setCircleColor(android.graphics.Color.parseColor("#22C55E"));

        LineData lineData = new LineData(dataSet);

        lineChart.setData(lineData);

        lineChart.getDescription().setEnabled(false);

        lineChart.getLegend().setEnabled(false);

        lineChart.invalidate();

    }
    private void loadSubscriptions() {

        int userId =
                databaseHelper.getUserIdByEmail(currentUser.getEmail());

        if (userId == -1)
            return;

        int count =
                databaseHelper.getActiveSubscriptionCount(userId);

        double total =
                databaseHelper.getMonthlySubscriptionAmount(userId);

        tvSubscriptionCount.setText(
                count + " Active Subscriptions");

        tvSubscriptionTotal.setText(
                String.format("Rs %.2f", total));

        if (count == 0) {

            tvNoSubscriptions.setVisibility(View.VISIBLE);

            rvSubscriptions.setVisibility(View.GONE);

        }

        else {

            tvNoSubscriptions.setVisibility(View.GONE);

            rvSubscriptions.setVisibility(View.VISIBLE);

        }

    }
    private void loadAIInsights() {

        int userId =
                databaseHelper.getUserIdByEmail(currentUser.getEmail());

        if (userId == -1)
            return;

        String insight =
                databaseHelper.generateAIInsight(userId);

        tvAIInsight.setText(insight);

    }

}