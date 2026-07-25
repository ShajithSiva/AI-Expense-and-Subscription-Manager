package com.example.aiexpensemanagementapplication.ui.reports;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;

import com.github.mikephil.charting.components.Description;

import android.graphics.Paint;
import android.graphics.Typeface;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.view.View;
import android.graphics.Bitmap;


import android.widget.Toast;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import com.github.mikephil.charting.data.Entry;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;

import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileWriter;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.model.Budget;
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

    private TextView tvHighestCategory;
    private TextView tvLargestExpense;
    private TextView tvAverageExpense;
    private TextView tvSavingsRate;

    private TextView tvBudgetRemaining;
    private TextView tvBudgetStatus;
    private TextView tvHealthScore;
    private TextView tvSubscriptionCost;
    private TextView tvSpendingTrend;
    private TextView tvTopCategories;

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

        loadAnalytics();

        loadAIInsights();

        setupPieChart();

        setupBarChart();

        setupLineChart();

        setupListeners();

        loadAdvancedAnalytics();
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

        tvHighestCategory = findViewById(R.id.tvHighestCategory);
        tvLargestExpense = findViewById(R.id.tvLargestExpense);
        tvAverageExpense = findViewById(R.id.tvAverageExpense);
        tvSavingsRate = findViewById(R.id.tvSavingsRate);

        tvBudgetRemaining = findViewById(R.id.tvBudgetRemaining);
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus);
        tvHealthScore = findViewById(R.id.tvHealthScore);
        tvSubscriptionCost = findViewById(R.id.tvSubscriptionCost);
        tvSpendingTrend = findViewById(R.id.tvSpendingTrend);
        tvTopCategories = findViewById(R.id.tvTopCategories);

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

        Budget budgetSettings = databaseHelper.getBudgetSettings(userId);

        double budget = 0;

        if (budgetSettings != null) {
            budget = budgetSettings.getMonthlyBudget();
        }

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

            exportPDF();

        });

        btnExportExcel.setOnClickListener(v->{

            exportCSV();

        });

    }

    private void exportPDF() {

        File folder = getExternalFilesDir("Reports");

        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, "ExpenseReport.pdf");

        PdfDocument pdfDocument = new PdfDocument();

        Paint titlePaint = new Paint();
        Paint headingPaint = new Paint();
        Paint subHeadingPaint = new Paint();
        Paint normalPaint = new Paint();
        Paint smallPaint = new Paint();
        Paint linePaint = new Paint();

        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(28);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));

        headingPaint.setColor(Color.parseColor("#1976D2"));
        headingPaint.setTextSize(20);
        headingPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));

        subHeadingPaint.setColor(Color.parseColor("#424242"));
        subHeadingPaint.setTextSize(16);
        subHeadingPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));

        normalPaint.setColor(Color.BLACK);
        normalPaint.setTextSize(14);

        smallPaint.setColor(Color.DKGRAY);
        smallPaint.setTextSize(11);

        linePaint.setColor(Color.LTGRAY);
        linePaint.setStrokeWidth(2);

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(
                        595,
                        842,
                        1
                ).create();

        PdfDocument.Page page =
                pdfDocument.startPage(pageInfo);

        Canvas canvas =
                page.getCanvas();

        int y = 45;

        canvas.drawText(
                "AI Expense Management Application",
                40,
                y,
                titlePaint);

        y += 28;

        canvas.drawText(
                "Financial Report",
                40,
                y,
                headingPaint);

        y += 20;

        String date =
                new SimpleDateFormat(
                        "dd MMM yyyy  HH:mm",
                        Locale.getDefault())
                        .format(new Date());

        canvas.drawText(
                "Generated on : " + date,
                40,
                y,
                smallPaint);

        y += 20;

        canvas.drawLine(
                40,
                y,
                555,
                y,
                linePaint);

        y += 30;

        canvas.drawText(
                "Financial Summary",
                40,
                y,
                headingPaint);

        y += 25;

        canvas.drawText(
                "Total Income",
                60,
                y,
                subHeadingPaint);

        canvas.drawText(
                "Rs " +
                        String.format(
                                "%.2f",
                                databaseHelper.getTotalIncome(userId)),
                350,
                y,
                normalPaint);

        y += 22;

        canvas.drawText(
                "Total Expense",
                60,
                y,
                subHeadingPaint);

        canvas.drawText(
                "Rs " +
                        String.format(
                                "%.2f",
                                databaseHelper.getTotalExpense(userId)),
                350,
                y,
                normalPaint);

        y += 22;

        canvas.drawText(
                "Savings",
                60,
                y,
                subHeadingPaint);

        canvas.drawText(
                "Rs " +
                        String.format(
                                "%.2f",
                                databaseHelper.getTotalSavings(userId)),
                350,
                y,
                normalPaint);

        y += 22;

        canvas.drawText(
                "Budget Used",
                60,
                y,
                subHeadingPaint);

        canvas.drawText(
                String.format(
                        "%.1f%%",
                        databaseHelper.getDashboardBudgetUsed(userId)),
                350,
                y,
                normalPaint);

        y += 35;

        canvas.drawLine(
                40,
                y,
                555,
                y,
                linePaint);

        y += 30;
        // ================= FINANCIAL ANALYTICS =================

        canvas.drawText(
                "Financial Analytics",
                40,
                y,
                headingPaint);

        y += 30;

        canvas.drawText(
                "Highest Expense Category",
                60,
                y,
                subHeadingPaint);

        canvas.drawText(
                databaseHelper.getHighestExpenseCategory(userId),
                350,
                y,
                normalPaint);

        y += 22;

        canvas.drawText(
                "Largest Expense",
                60,
                y,
                subHeadingPaint);

        canvas.drawText(
                "Rs " +
                        String.format(
                                "%.2f",
                                databaseHelper.getLargestExpense(userId)),
                350,
                y,
                normalPaint);

        y += 22;

        canvas.drawText(
                "Average Daily Expense",
                60,
                y,
                subHeadingPaint);

        canvas.drawText(
                "Rs " +
                        String.format(
                                "%.2f",
                                databaseHelper.getAverageDailyExpense(userId)),
                350,
                y,
                normalPaint);

        y += 22;

        canvas.drawText(
                "Savings Rate",
                60,
                y,
                subHeadingPaint);

        canvas.drawText(
                String.format(
                        "%.1f%%",
                        databaseHelper.getSavingsRate(userId)),
                350,
                y,
                normalPaint);

        y += 35;

        canvas.drawLine(
                40,
                y,
                555,
                y,
                linePaint);

        y += 30;

        // ================= ADVANCED INSIGHTS =================

        canvas.drawText(
                "Advanced Financial Insights",
                40,
                y,
                headingPaint);

        y += 30;

        canvas.drawText(
                "Financial Health Score",
                60,
                y,
                subHeadingPaint);

        canvas.drawText(
                databaseHelper.getFinancialHealthScore(userId) + "/100",
                350,
                y,
                normalPaint);

        y += 22;

        canvas.drawText(
                "Budget Status",
                60,
                y,
                subHeadingPaint);

        canvas.drawText(
                databaseHelper.getBudgetStatus(userId),
                350,
                y,
                normalPaint);

        y += 22;

        canvas.drawText(
                "Budget Remaining",
                60,
                y,
                subHeadingPaint);

        canvas.drawText(
                "Rs " +
                        String.format(
                                "%.2f",
                                databaseHelper.getBudgetRemaining(userId)),
                350,
                y,
                normalPaint);

        y += 22;

        canvas.drawText(
                "Monthly Subscription Cost",
                60,
                y,
                subHeadingPaint);

        canvas.drawText(
                "Rs " +
                        String.format(
                                "%.2f",
                                databaseHelper.getMonthlySubscriptionCost(userId)),
                350,
                y,
                normalPaint);

        y += 22;

        canvas.drawText(
                "Spending Trend",
                60,
                y,
                subHeadingPaint);

        canvas.drawText(
                databaseHelper.getSpendingTrend(userId),
                350,
                y,
                normalPaint);

        y += 35;

        canvas.drawLine(
                40,
                y,
                555,
                y,
                linePaint);

        y += 30;

        // ================= AI INSIGHT =================

        canvas.drawText(
                "AI Financial Recommendation",
                40,
                y,
                headingPaint);

        y += 25;

        String insight =
                databaseHelper.generateAIInsight(userId);

        String[] lines = insight.split("\\.");

        for (String line : lines) {

            line = line.trim();

            if (!line.isEmpty()) {

                canvas.drawText(
                        "• " + line + ".",
                        60,
                        y,
                        normalPaint);

                y += 20;
            }
        }

        y += 20;

        canvas.drawLine(
                40,
                780,
                555,
                780,
                linePaint);

        canvas.drawText(
                "Page 1",
                280,
                805,
                smallPaint);

        pdfDocument.finishPage(page);
        // ================= PAGE 2 =================

        PdfDocument.PageInfo pageInfo2 =
                new PdfDocument.PageInfo.Builder(
                        595,
                        842,
                        2
                ).create();

        PdfDocument.Page page2 =
                pdfDocument.startPage(pageInfo2);

        Canvas canvas2 =
                page2.getCanvas();

        int chartY = 45;

        canvas2.drawText(
                "Financial Charts",
                40,
                chartY,
                titlePaint);

        chartY += 30;

        canvas2.drawLine(
                40,
                chartY,
                555,
                chartY,
                linePaint);

        chartY += 25;

        // ================= PIE CHART =================

        canvas2.drawText(
                "Expense Category Breakdown",
                40,
                chartY,
                headingPaint);

        chartY += 20;

        Bitmap pieBitmap =
                getChartBitmap(pieChart);

        Bitmap scaledPie =
                Bitmap.createScaledBitmap(
                        pieBitmap,
                        250,
                        250,
                        true);

        canvas2.drawBitmap(
                scaledPie,
                40,
                chartY,
                null);

        chartY += 270;

        canvas2.drawLine(
                40,
                chartY,
                555,
                chartY,
                linePaint);

        chartY += 25;

        // ================= BAR CHART =================

        canvas2.drawText(
                "Weekly Expense Analysis",
                40,
                chartY,
                headingPaint);

        chartY += 20;

        Bitmap barBitmap =
                getChartBitmap(barChart);

        Bitmap scaledBar =
                Bitmap.createScaledBitmap(
                        barBitmap,
                        500,
                        220,
                        true);

        canvas2.drawBitmap(
                scaledBar,
                40,
                chartY,
                null);

        canvas2.drawLine(
                40,
                780,
                555,
                780,
                linePaint);

        canvas2.drawText(
                "Page 2",
                280,
                805,
                smallPaint);

        pdfDocument.finishPage(page2);

        // ================= PAGE 3 =================

        PdfDocument.PageInfo pageInfo3 =
                new PdfDocument.PageInfo.Builder(
                        595,
                        842,
                        3
                ).create();

        PdfDocument.Page page3 =
                pdfDocument.startPage(pageInfo3);

        Canvas canvas3 =
                page3.getCanvas();

        int y3 = 45;

        canvas3.drawText(
                "Income vs Expense",
                40,
                y3,
                titlePaint);

        y3 += 30;

        canvas3.drawLine(
                40,
                y3,
                555,
                y3,
                linePaint);

        y3 += 25;

        Bitmap lineBitmap =
                getChartBitmap(lineChart);

        Bitmap scaledLine =
                Bitmap.createScaledBitmap(
                        lineBitmap,
                        500,
                        300,
                        true);

        canvas3.drawBitmap(
                scaledLine,
                40,
                y3,
                null);

        y3 += 330;

        canvas3.drawText(
                "Report generated by AI Expense Management Application",
                40,
                y3,
                normalPaint);

        y3 += 20;

        canvas3.drawText(
                "Date : " + date,
                40,
                y3,
                smallPaint);

        canvas3.drawLine(
                40,
                780,
                555,
                780,
                linePaint);

        canvas3.drawText(
                "Page 3",
                280,
                805,
                smallPaint);

        pdfDocument.finishPage(page3);
        try {

            FileOutputStream outputStream =
                    new FileOutputStream(file);

            pdfDocument.writeTo(outputStream);

            outputStream.flush();

            outputStream.close();

            pdfDocument.close();

            Toast.makeText(
                    this,
                    "PDF exported successfully!\n\nLocation:\n"
                            + file.getAbsolutePath(),
                    Toast.LENGTH_LONG
            ).show();

        } catch (IOException e) {

            e.printStackTrace();

            pdfDocument.close();

            Toast.makeText(
                    this,
                    "Failed to export PDF.",
                    Toast.LENGTH_SHORT
            ).show();
        }

    }

    private Bitmap getChartBitmap(View chart){

        if(chart.getWidth() <= 0 || chart.getHeight() <= 0){

            throw new IllegalStateException("Chart is not ready.");

        }

        Bitmap bitmap = Bitmap.createBitmap(
                chart.getWidth(),
                chart.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);

        chart.draw(canvas);

        return bitmap;

    }


    private void exportCSV() {

        File folder = getExternalFilesDir("Reports");

        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, "ExpenseReport.csv");

        try {

            FileWriter writer = new FileWriter(file);

            writer.append("Expense Management Report\n\n");

            writer.append("Item,Value\n");

            writer.append("Total Income,")
                    .append(tvTotalIncome.getText().toString())
                    .append("\n");

            writer.append("Total Expense,")
                    .append(tvTotalExpense.getText().toString())
                    .append("\n");

            writer.append("Savings,")
                    .append(tvSavings.getText().toString())
                    .append("\n");

            writer.append("Budget Used,")
                    .append(tvBudgetUsed.getText().toString())
                    .append("\n");

            writer.append("\n");

            writer.append("AI Insight\n");

            writer.append(tvAIInsights.getText().toString());

            writer.flush();
            writer.close();

            Toast.makeText(
                    this,
                    "CSV exported successfully!",
                    Toast.LENGTH_LONG
            ).show();

        } catch (IOException e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Export failed!",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void setupPieChart(){

        ArrayList<PieEntry> entries =
                databaseHelper.getCategoryPieEntries(userId);

        PieDataSet dataSet =
                new PieDataSet(entries,"Expenses");

        dataSet.setColors(

                Color.parseColor("#F44336"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#9C27B0"),
                Color.parseColor("#009688"),
                Color.parseColor("#795548"),
                Color.parseColor("#607D8B")

        );

        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);

        pieChart.setData(data);

        Description description = new Description();

        description.setText("");

        pieChart.setDescription(description);

        pieChart.setUsePercentValues(false);

        pieChart.animateY(1200);

        pieChart.invalidate();

    }

    private void setupBarChart(){

        ArrayList<BarEntry> barEntries = new ArrayList<>();

        for(Entry entry :
                databaseHelper.getWeeklyExpenseEntries(userId)){

            barEntries.add(
                    new BarEntry(
                            entry.getX(),
                            entry.getY()
                    )
            );

        }

        BarDataSet dataSet =
                new BarDataSet(barEntries,"Weekly Expense");

        dataSet.setColor(Color.parseColor("#2196F3"));

        BarData data =
                new BarData(dataSet);

        data.setBarWidth(0.6f);

        barChart.setData(data);

        Description description =
                new Description();

        description.setText("");

        barChart.setDescription(description);

        barChart.animateY(1200);

        barChart.invalidate();

    }

    private void setupLineChart(){

        ArrayList<Entry> incomeEntries =
                new ArrayList<>();

        ArrayList<Entry> expenseEntries =
                new ArrayList<>();

        Calendar calendar =
                Calendar.getInstance();

        calendar.add(Calendar.MONTH,-5);

        SimpleDateFormat format =
                new SimpleDateFormat("yyyy-MM",
                        Locale.getDefault());

        for(int i=0;i<6;i++){

            String month =
                    format.format(calendar.getTime());

            incomeEntries.add(

                    new Entry(

                            i,

                            (float) databaseHelper
                                    .getMonthlyIncome(
                                            userId,
                                            month)

                    )

            );

            expenseEntries.add(

                    new Entry(

                            i,

                            (float) databaseHelper
                                    .getMonthlyExpense(
                                            userId,
                                            month)

                    )

            );

            calendar.add(Calendar.MONTH,1);

        }

        LineDataSet incomeDataSet =
                new LineDataSet(
                        incomeEntries,
                        "Income");

        incomeDataSet.setColor(
                Color.parseColor("#4CAF50"));

        incomeDataSet.setCircleColor(
                Color.parseColor("#4CAF50"));

        LineDataSet expenseDataSet =
                new LineDataSet(
                        expenseEntries,
                        "Expense");

        expenseDataSet.setColor(
                Color.parseColor("#F44336"));

        expenseDataSet.setCircleColor(
                Color.parseColor("#F44336"));

        LineData lineData =
                new LineData(
                        incomeDataSet,
                        expenseDataSet);

        lineChart.setData(lineData);

        Description description =
                new Description();

        description.setText("");

        lineChart.setDescription(description);

        lineChart.animateX(1200);

        lineChart.invalidate();

    }
    private void loadAnalytics() {

        tvHighestCategory.setText(
                databaseHelper.getHighestExpenseCategory(userId)
        );

        tvLargestExpense.setText(
                String.format(
                        "Rs %.2f",
                        databaseHelper.getLargestExpense(userId)
                )
        );

        tvAverageExpense.setText(
                String.format(
                        "Rs %.2f",
                        databaseHelper.getAverageDailyExpense(userId)
                )
        );

        tvSavingsRate.setText(
                String.format(
                        "%.1f%%",
                        databaseHelper.getSavingsRate(userId)
                )
        );
    }

    private void loadAdvancedAnalytics() {

        tvBudgetRemaining.setText(
                String.format(
                        "Rs %.2f",
                        databaseHelper.getBudgetRemaining(userId)
                )
        );

        String status =
                databaseHelper.getBudgetStatus(userId);

        tvBudgetStatus.setText(status);

        switch (status){

            case "Good":

                tvBudgetStatus.setTextColor(
                        Color.parseColor("#4CAF50"));
                break;

            case "Warning":

                tvBudgetStatus.setTextColor(
                        Color.parseColor("#FF9800"));
                break;

            case "Critical":

                tvBudgetStatus.setTextColor(
                        Color.parseColor("#F44336"));
                break;

            default:

                tvBudgetStatus.setTextColor(
                        Color.GRAY);
        }

        tvHealthScore.setText(
                databaseHelper.getFinancialHealthScore(userId)
                        + "/100"
        );

        tvSubscriptionCost.setText(
                String.format(
                        "Rs %.2f",
                        databaseHelper.getMonthlySubscriptionCost(userId)
                )
        );

        ArrayList<String> list =
                databaseHelper.getTopExpenseCategories(userId);

        if(list.isEmpty()){

            tvTopCategories.setText("No expense data available");

        }else{

            StringBuilder builder = new StringBuilder();

            for(String item : list){

                builder.append("• ")
                        .append(item)
                        .append("\n");

            }

            tvTopCategories.setText(builder.toString());

        }

        String trend =
                databaseHelper.getSpendingTrend(userId);

        tvSpendingTrend.setText(trend);

        if(trend.equals("Increasing")){

            tvSpendingTrend.setTextColor(
                    Color.parseColor("#F44336"));

        }
        else if(trend.equals("Decreasing")){

            tvSpendingTrend.setTextColor(
                    Color.parseColor("#4CAF50"));

        }
        else{
            tvSpendingTrend.setTextColor(
                    Color.parseColor("#2196F3"));

        }
    }



}