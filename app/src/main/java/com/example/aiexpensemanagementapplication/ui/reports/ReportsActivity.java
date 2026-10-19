package com.example.aiexpensemanagementapplication.ui.reports;

import android.os.Bundle;
import android.widget.TextView;
import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;

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
import com.github.mikephil.charting.charts.PieChart;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.Intent;
import android.net.Uri;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;

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
    private com.google.android.material.progressindicator.LinearProgressIndicator progressHealthScore;

    private PieChart pieChart;
    private BarChart barChart;

    private MaterialButton btnExportPDF;
    private MaterialButton btnExportExcel;

    private DatabaseHelper databaseHelper;

    private int userId;

    private static final int CREATE_EXPORT_FILE = 2001;
    private File pendingExportFile;
    private String pendingFileName;

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


        setupListeners();

        loadAdvancedAnalytics();
    }



    private void saveFileWithPicker(File sourceFile, String mimeType,
                                    String fileName) {
        if (sourceFile == null || !sourceFile.exists()) {
            Toast.makeText(this, "Export file not found",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        pendingExportFile = sourceFile;
        pendingFileName = fileName;

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        startActivityForResult(intent, CREATE_EXPORT_FILE);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_EXPORT_FILE) {

            if (resultCode == RESULT_OK
                    && data != null
                    && data.getData() != null) {

                Uri destinationUri = data.getData();

                if (pendingExportFile == null
                        || !pendingExportFile.exists()) {

                    Toast.makeText(
                            this,
                            "Source export file not found",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                try (InputStream inputStream =
                             new FileInputStream(pendingExportFile);
                     OutputStream outputStream =
                             getContentResolver()
                                     .openOutputStream(destinationUri, "w")) {

                    if (outputStream == null) {
                        throw new IOException(
                                "Unable to open destination file");
                    }

                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.flush();

                    android.util.Log.d(
                            "REPORT_EXPORT",
                            "Source bytes: " + pendingExportFile.length()
                    );

                    Toast.makeText(
                            this,
                            "Report saved successfully",
                            Toast.LENGTH_LONG
                    ).show();

                } catch (IOException e) {
                    android.util.Log.e(
                            "REPORT_EXPORT",
                            "Failed to save report",
                            e
                    );

                    Toast.makeText(
                            this,
                            "Failed to save report: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                } finally {
                    pendingExportFile = null;
                    pendingFileName = null;
                }

            } else {
                pendingExportFile = null;
                pendingFileName = null;
            }
        }
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
        progressHealthScore = findViewById(R.id.progressHealthScore);

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

    private String getSelectedMonth() {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("yyyy-MM", Locale.US)
                .format(calendar.getTime());
    }

    private void loadSummaryCards(){
        String month = getSelectedMonth();
        double totalIncome = databaseHelper.getMonthlyIncome(userId, month);
        double totalExpense = databaseHelper.getMonthlyExpense(userId, month);
        double savings = totalIncome - totalExpense;

        tvTotalIncome.setText(String.format(Locale.getDefault(), "LKR %.2f", totalIncome));
        tvTotalExpense.setText(String.format(Locale.getDefault(), "LKR %.2f", totalExpense));
        tvSavings.setText(String.format(Locale.getDefault(), "LKR %.2f", savings));

        Budget budgetSettings = databaseHelper.getBudgetSettings(userId);
        double budget = budgetSettings != null ? budgetSettings.getMonthlyBudget() : 0;
        double percentage = budget > 0 ? (totalExpense / budget) * 100 : 0;
        tvBudgetUsed.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));

        View progressView = findViewById(R.id.progressBudgetUsed);
        if (progressView instanceof com.google.android.material.progressindicator.LinearProgressIndicator) {
            ((com.google.android.material.progressindicator.LinearProgressIndicator) progressView)
                    .setProgressCompat((int) Math.max(0, Math.min(100, percentage)), false);
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
                databaseHelper.getFinancialHealthScore(userId, getSelectedMonth()) + "/100",
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
                "Weekly Income vs Expenses",
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
                getChartBitmap(barChart);

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

            saveFileWithPicker(
                    file,
                    "application/pdf",
                    "ExpenseReport.pdf"
            );

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

            saveFileWithPicker(
                    file,
                    "text/csv",
                    "ExpenseReport.csv"
            );

        } catch (IOException e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Export failed!",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void setupPieChart() {
        String month = getSelectedMonth();
        ArrayList<PieEntry> sourceEntries =
                databaseHelper.getCategoryPieEntriesForMonth(userId, month);

        // Match the donut and legend: top four categories plus Others.
        ArrayList<PieEntry> entries = new ArrayList<>();
        float total = 0f;
        for (PieEntry entry : sourceEntries) {
            if (entry.getY() > 0) total += entry.getY();
        }

        if (sourceEntries.size() > 5) {
            ArrayList<PieEntry> sorted = new ArrayList<>(sourceEntries);
            java.util.Collections.sort(sorted,
                    (a, b) -> Float.compare(b.getY(), a.getY()));
            float others = 0f;
            for (int i = 0; i < sorted.size(); i++) {
                PieEntry item = sorted.get(i);
                if (item.getY() <= 0) continue;
                if (i < 4) entries.add(new PieEntry(item.getY(), item.getLabel()));
                else others += item.getY();
            }
            if (others > 0) entries.add(new PieEntry(others, "Others"));
        } else {
            for (PieEntry item : sourceEntries) {
                if (item.getY() > 0) entries.add(new PieEntry(item.getY(), item.getLabel()));
            }
        }

        int[] colors = new int[]{
                Color.parseColor("#16AD72"), Color.parseColor("#168CF0"),
                Color.parseColor("#F59A16"), Color.parseColor("#8750D8"),
                Color.parseColor("#AAB5BE"), Color.parseColor("#0D9488"),
                Color.parseColor("#E45C68")
        };

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(3f);
        pieChart.setData(new PieData(dataSet));
        pieChart.setUsePercentValues(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(64f);
        pieChart.setTransparentCircleRadius(68f);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(150);
        pieChart.setDrawCenterText(true);
        double monthExpense = databaseHelper.getMonthlyExpense(userId, month);
        pieChart.setCenterText(String.format(Locale.getDefault(),
                "LKR %,.0f\nTotal Spent", monthExpense));
        pieChart.setCenterTextSize(13f);
        pieChart.setCenterTextColor(Color.parseColor("#10213D"));
        pieChart.getLegend().setEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(2f, 2f, 2f, 2f);
        pieChart.setMinAngleForSlices(3f);
        pieChart.animateY(850);
        pieChart.invalidate();
        buildExpenseLegend(entries, colors, total);
    }

    private void buildExpenseLegend(ArrayList<PieEntry> entries, int[] colors, float total) {
        LinearLayout legend = findViewById(R.id.expenseLegendContainer);
        if (legend == null) return;
        legend.removeAllViews();

        if (entries == null || entries.isEmpty() || total <= 0f) {
            TextView empty = new TextView(this);
            empty.setText("No expenses recorded this month");
            empty.setTextColor(Color.parseColor("#74827D"));
            empty.setTextSize(12f);
            legend.addView(empty);
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            PieEntry entry = entries.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(8), 0, dp(8));

            TextView dot = new TextView(this);
            dot.setText("●");
            dot.setTextColor(colors[i % colors.length]);
            dot.setTextSize(12f);
            row.addView(dot, new LinearLayout.LayoutParams(dp(15), -2));

            TextView name = new TextView(this);
            name.setText(entry.getLabel() == null ? "Other" : entry.getLabel());
            name.setTextColor(Color.parseColor("#24344A"));
            name.setTextSize(11f);
            name.setMaxLines(2);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, -2, 1f);
            nameParams.setMargins(dp(3), 0, dp(2), 0);
            row.addView(name, nameParams);

            TextView percent = new TextView(this);
            percent.setText(String.format(Locale.getDefault(), "%.0f%%", entry.getY() * 100f / total));
            percent.setTextColor(Color.parseColor("#10213D"));
            percent.setTextSize(10f);
            percent.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
            row.addView(percent, new LinearLayout.LayoutParams(dp(31), -2));

            TextView amount = new TextView(this);
            amount.setText(String.format(Locale.getDefault(), "%,.0f", entry.getY()));
            amount.setTextColor(Color.parseColor("#24344A"));
            amount.setTextSize(10f);
            amount.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
            amount.setMaxLines(1);
            amount.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            amount.setAutoSizeTextTypeUniformWithConfiguration(7, 10, 1,
                    android.util.TypedValue.COMPLEX_UNIT_SP);
            row.addView(amount, new LinearLayout.LayoutParams(dp(52), -2));
            legend.addView(row, new LinearLayout.LayoutParams(-1, -2));

            if (i < entries.size() - 1) {
                View divider = new View(this);
                divider.setBackgroundColor(Color.parseColor("#EEF1F0"));
                legend.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));
            }
        }
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int getCalendarWeekCount(String month) {
        Calendar firstDay = Calendar.getInstance();
        try {
            Date parsedMonth = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .parse(month + "-01");
            if (parsedMonth != null) firstDay.setTime(parsedMonth);
        } catch (java.text.ParseException ignored) { }
        int leadingDays = (firstDay.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        return (leadingDays + firstDay.getActualMaximum(Calendar.DAY_OF_MONTH) + 6) / 7;
    }

    private void setupBarChart(){
        String month = getSelectedMonth();
        ArrayList<BarEntry> incomeEntries = new ArrayList<>();
        ArrayList<BarEntry> expenseEntries = new ArrayList<>();

        for (Entry entry : databaseHelper.getWeeklyIncomeEntriesForMonth(userId, month)) {
            incomeEntries.add(new BarEntry(entry.getX(), entry.getY()));
        }
        for (Entry entry : databaseHelper.getWeeklyExpenseEntriesByWeekForMonth(userId, month)) {
            expenseEntries.add(new BarEntry(entry.getX(), entry.getY()));
        }

        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "Income");
        incomeDataSet.setColor(Color.parseColor("#20B878"));
        incomeDataSet.setDrawValues(false);

        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Expenses");
        expenseDataSet.setColor(Color.parseColor("#173653"));
        expenseDataSet.setDrawValues(false);

        BarData data = new BarData(incomeDataSet, expenseDataSet);
        data.setBarWidth(0.36f);
        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setFitBars(false);
        barChart.setExtraBottomOffset(8f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#536575"));
        xAxis.setTextSize(10f);
        int weekCount = getCalendarWeekCount(month);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum((float) weekCount);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int week = (int) value + 1;
                return week >= 1 && week <= weekCount ? "Week " + week : "";
            }
        });

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(Color.parseColor("#536575"));
        leftAxis.setDrawGridLines(true);
        barChart.getAxisRight().setEnabled(false);

        // DatabaseHelper returns every calendar week (including zero-value weeks),
        // so bars remain aligned even when a week has no income or expenses.
        data.groupBars(0f, 0.08f, 0.02f);
        barChart.getLegend().setTextColor(Color.parseColor("#25384D"));
        barChart.getLegend().setTextSize(11f);
        barChart.animateY(900);
        barChart.invalidate();
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

        int healthScore = databaseHelper.getFinancialHealthScore(
                userId, getSelectedMonth()
        );

        tvHealthScore.setText(healthScore + "/100");

        if (progressHealthScore != null) {
            progressHealthScore.setMax(100);
            progressHealthScore.setProgressCompat(healthScore, false);
        }

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