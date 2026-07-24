package com.example.aiexpensemanagementapplication.ui.reports;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ReportsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;

    private TextView tvTotalIncome;
    private TextView tvTotalExpense;
    private TextView tvSavings;
    private TextView tvBudgetUsed;
    private TextView tvAIInsights;

    private PieChart pieChart;
    private BarChart barChart;
    private LineChart lineChart;

    private MaterialButton btnExportPDF;
    private MaterialButton btnExportExcel;

    private DatabaseHelper databaseHelper;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        initializeViews();

        setupToolbar();

        databaseHelper = new DatabaseHelper(this);

        initializeUser();

        loadSummaryCards();

        loadAIInsights();

        setupListeners();
    }

    private void initializeViews(){

        toolbar = findViewById(R.id.toolbar);

        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvSavings = findViewById(R.id.tvSavings);
        tvBudgetUsed = findViewById(R.id.tvBudgetUsed);
        tvAIInsights = findViewById(R.id.tvAIInsights);

        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        lineChart = findViewById(R.id.lineChart);

        btnExportPDF = findViewById(R.id.btnExportPDF);
        btnExportExcel = findViewById(R.id.btnExportExcel);

    }

    private void setupToolbar() {

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initializeUser(){

        FirebaseUser firebaseUser =
                FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser==null){

            finish();

            return;

        }

        userId = databaseHelper.getUserIdByFirebaseUid(
                firebaseUser.getUid());

    }

    private void loadSummaryCards(){

        double totalIncome =
                databaseHelper.getTotalIncome(userId);

        double totalExpense =
                databaseHelper.getTotalExpense(userId);

        double savings =
                totalIncome-totalExpense;

        tvTotalIncome.setText(
                String.format("Rs %.2f",totalIncome));

        tvTotalExpense.setText(
                String.format("Rs %.2f",totalExpense));

        tvSavings.setText(
                String.format("Rs %.2f",savings));

        double budget =
                databaseHelper.getBudgetSettings(userId)
                        .getMonthlyBudget();

        if(budget>0){

            double percentage =
                    (totalExpense/budget)*100;

            tvBudgetUsed.setText(
                    String.format("%.0f%%",percentage));

        }else{

            tvBudgetUsed.setText("0%");
        }

    }

    private void loadAIInsights(){

        String insight =
                databaseHelper.generateAIInsight(userId);

        tvAIInsights.setText(insight);

    }

    private void setupListeners(){

        btnExportPDF.setOnClickListener(v->{

            // PDF Export
        });

        btnExportExcel.setOnClickListener(v->{

            // Excel Export
        });

    }

}