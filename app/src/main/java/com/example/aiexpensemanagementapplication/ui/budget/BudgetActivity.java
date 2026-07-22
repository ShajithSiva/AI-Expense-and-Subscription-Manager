package com.example.aiexpensemanagementapplication.ui.budget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BudgetActivity extends AppCompatActivity {

    // =========================================================
    // UI COMPONENTS
    // =========================================================

    private ImageButton btnBack;
    private ImageButton btnAddBudget;
    private ImageButton btnPreviousMonth;
    private ImageButton btnNextMonth;
    private ImageButton btnEditBudget;

    private TextView tvSelectedMonth;
    private TextView tvRemainingDays;

    private TextView tvTotalBudget;
    private TextView tvTotalSpent;
    private TextView tvRemaining;

    private TextView tvBudgetPercentage;
    private TextView tvBudgetStatus;

    private MaterialButton btnAddCategoryBudget;

    private SwitchMaterial switchCarryForward;

    private LinearProgressIndicator progressBudget;

    private LinearLayout categoryBudgetContainer;
    private LinearLayout budgetHistoryContainer;


    // =========================================================
    // DATABASE
    // =========================================================

    private DatabaseHelper databaseHelper;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    // =========================================================
    // USER
    // =========================================================

    private int userId = -1;


    // =========================================================
    // MONTH
    // =========================================================

    private Calendar selectedCalendar;


    // =========================================================
    // CURRENCY FORMAT
    // =========================================================

    private final NumberFormat currencyFormat =
            NumberFormat.getNumberInstance(
                    new Locale("en", "LK")
            );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_budget);

        initialize();

        selectedCalendar = Calendar.getInstance();

        loadUser();

        updateMonthDisplay();

        loadBudgetData();

        listeners();
    }


    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initialize() {

        databaseHelper = new DatabaseHelper(this);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();


        // Header

        btnBack = findViewById(R.id.btnBack);

        btnAddBudget = findViewById(R.id.btnAddBudget);


        // Month selector

        btnPreviousMonth =
                findViewById(R.id.btnPreviousMonth);

        btnNextMonth =
                findViewById(R.id.btnNextMonth);

        tvSelectedMonth =
                findViewById(R.id.tvSelectedMonth);

        tvRemainingDays =
                findViewById(R.id.tvRemainingDays);


        // Budget summary

        btnEditBudget =
                findViewById(R.id.btnEditBudget);

        tvTotalBudget =
                findViewById(R.id.tvTotalBudget);

        tvTotalSpent =
                findViewById(R.id.tvTotalSpent);

        tvRemaining =
                findViewById(R.id.tvRemaining);

        progressBudget =
                findViewById(R.id.progressBudget);

        tvBudgetPercentage =
                findViewById(R.id.tvBudgetPercentage);

        tvBudgetStatus =
                findViewById(R.id.tvBudgetStatus);


        // Category budget

        categoryBudgetContainer =
                findViewById(
                        R.id.categoryBudgetContainer
                );

        btnAddCategoryBudget =
                findViewById(
                        R.id.btnAddCategoryBudget
                );


        // Carry forward

        switchCarryForward =
                findViewById(
                        R.id.switchCarryForward
                );


        // History

        budgetHistoryContainer =
                findViewById(
                        R.id.budgetHistoryContainer
                );
    }


    // =========================================================
    // LOAD USER
    // =========================================================

    private void loadUser() {

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "User not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid()
                );


        if (userId == -1) {

            Toast.makeText(
                    this,
                    "User account not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }


    // =========================================================
    // MONTH DISPLAY
    // =========================================================

    private void updateMonthDisplay() {

        SimpleDateFormat monthFormat =
                new SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.getDefault()
                );


        tvSelectedMonth.setText(
                monthFormat.format(
                        selectedCalendar.getTime()
                )
        );


        updateRemainingDays();
    }


    // =========================================================
    // REMAINING DAYS
    // =========================================================

    private void updateRemainingDays() {

        Calendar today =
                Calendar.getInstance();


        int selectedYear =
                selectedCalendar.get(
                        Calendar.YEAR
                );


        int selectedMonth =
                selectedCalendar.get(
                        Calendar.MONTH
                );


        int currentYear =
                today.get(
                        Calendar.YEAR
                );


        int currentMonth =
                today.get(
                        Calendar.MONTH
                );


        // If selected month is current month

        if (selectedYear == currentYear &&
                selectedMonth == currentMonth) {


            int lastDay =
                    selectedCalendar.getActualMaximum(
                            Calendar.DAY_OF_MONTH
                    );


            int currentDay =
                    today.get(
                            Calendar.DAY_OF_MONTH
                    );


            int remaining =
                    lastDay - currentDay;


            if (remaining == 0) {

                tvRemainingDays.setText(
                        "Last day of the month"
                );

            } else {

                tvRemainingDays.setText(
                        remaining +
                                " days remaining"
                );
            }

        } else {

            tvRemainingDays.setText(
                    "Monthly budget"
            );
        }
    }


    // =========================================================
    // GET DATABASE MONTH FORMAT
    // =========================================================

    private String getSelectedMonth() {

        SimpleDateFormat sdf =
                new SimpleDateFormat(
                        "yyyy-MM",
                        Locale.getDefault()
                );


        return sdf.format(
                selectedCalendar.getTime()
        );
    }


    // =========================================================
    // LOAD BUDGET DATA
    // =========================================================

    private void loadBudgetData() {

        if (userId == -1) {
            return;
        }


        String month =
                getSelectedMonth();


        double totalBudget = 0;


        // -----------------------------------------------------
        // GET MONTHLY BUDGET
        // -----------------------------------------------------

        Cursor budgetCursor =
                databaseHelper.getBudgetByUserAndMonth(
                        userId,
                        month
                );


        if (budgetCursor != null &&
                budgetCursor.moveToFirst()) {


            int amountIndex =
                    budgetCursor.getColumnIndex(
                            DatabaseHelper.BUDGET_AMOUNT
                    );


            if (amountIndex != -1) {

                totalBudget =
                        budgetCursor.getDouble(
                                amountIndex
                        );
            }
        }


        if (budgetCursor != null) {

            budgetCursor.close();
        }


        // -----------------------------------------------------
        // GET TOTAL EXPENSE
        // -----------------------------------------------------

        double totalSpent =
                databaseHelper.getMonthlyExpenseTotal(
                        userId,
                        month
                );


        // -----------------------------------------------------
        // UPDATE SUMMARY
        // -----------------------------------------------------

        updateBudgetSummary(
                totalBudget,
                totalSpent
        );


        // -----------------------------------------------------
        // LOAD CARRY FORWARD SETTING
        // -----------------------------------------------------

        loadCarryForwardSetting();


        // -----------------------------------------------------
        // LOAD CATEGORY BUDGETS
        // -----------------------------------------------------

        loadCategoryBudgets();


        // -----------------------------------------------------
        // LOAD BUDGET HISTORY
        // -----------------------------------------------------

        loadBudgetHistory();
    }


    // =========================================================
    // UPDATE BUDGET SUMMARY
    // =========================================================

    private void updateBudgetSummary(
            double totalBudget,
            double totalSpent) {


        double remaining =
                totalBudget - totalSpent;


        // Total Budget

        tvTotalBudget.setText(
                formatCurrency(totalBudget)
        );


        // Total Spent

        tvTotalSpent.setText(
                formatCurrency(totalSpent)
        );


        // Remaining

        tvRemaining.setText(
                formatCurrency(remaining)
        );


        // -----------------------------------------------------
        // PROGRESS
        // -----------------------------------------------------

        if (totalBudget > 0) {


            double percentage =
                    (totalSpent /
                            totalBudget) * 100;


            int progress =
                    (int) percentage;


            if (progress < 0) {
                progress = 0;
            }


            if (progress > 100) {
                progress = 100;
            }


            progressBudget.setProgress(
                    progress
            );


            tvBudgetPercentage.setText(
                    String.format(
                            Locale.getDefault(),
                            "%.0f%% used",
                            percentage
                    )
            );


            // -------------------------------------------------
            // STATUS
            // -------------------------------------------------

            if (totalSpent > totalBudget) {

                tvBudgetStatus.setText(
                        "Over Budget"
                );

                tvRemaining.setTextColor(
                        getColor(
                                android.R.color.holo_red_dark
                        )
                );

            } else if (percentage >= 80) {

                tvBudgetStatus.setText(
                        "Near Limit"
                );

            } else {

                tvBudgetStatus.setText(
                        "On Track"
                );
            }


        } else {

            progressBudget.setProgress(
                    0
            );

            tvBudgetPercentage.setText(
                    "0% used"
            );

            tvBudgetStatus.setText(
                    "No Budget Set"
            );
        }
    }


    // =========================================================
    // ADD / EDIT BUDGET
    // =========================================================

    private void openBudgetDialog() {

        BudgetDialog dialog =
                new BudgetDialog(
                        this,
                        userId,
                        getSelectedMonth(),
                        () -> loadBudgetData()
                );

        dialog.show();
    }


    // =========================================================
    // LOAD CARRY FORWARD
    // =========================================================

    private void loadCarryForwardSetting() {

        // This will be connected after
        // the DatabaseHelper carry-forward method
        // is added.

        // Example:
        //
        // boolean enabled =
        //     databaseHelper.getCarryForwardSetting(userId);
        //
        // switchCarryForward.setChecked(enabled);
    }


    // =========================================================
    // LOAD CATEGORY BUDGETS
    // =========================================================

    private void loadCategoryBudgets() {

        categoryBudgetContainer.removeAllViews();


        // Category budget cards will be added here
        // after the category budget database methods
        // are connected.
    }


    // =========================================================
    // LOAD BUDGET HISTORY
    // =========================================================

    private void loadBudgetHistory() {

        budgetHistoryContainer.removeAllViews();


        // Budget history will be loaded here
        // after DatabaseHelper history methods
        // are connected.
    }


    // =========================================================
    // MONTH NAVIGATION
    // =========================================================

    private void showPreviousMonth() {

        selectedCalendar.add(
                Calendar.MONTH,
                -1
        );


        updateMonthDisplay();

        loadBudgetData();
    }


    private void showNextMonth() {

        selectedCalendar.add(
                Calendar.MONTH,
                1
        );


        updateMonthDisplay();

        loadBudgetData();
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void listeners() {

        // Back

        btnBack.setOnClickListener(
                v -> finish()
        );


        // Add budget

        btnAddBudget.setOnClickListener(
                v -> openBudgetDialog()
        );


        // Edit budget

        btnEditBudget.setOnClickListener(
                v -> openBudgetDialog()
        );


        // Previous month

        btnPreviousMonth.setOnClickListener(
                v -> showPreviousMonth()
        );


        // Next month

        btnNextMonth.setOnClickListener(
                v -> showNextMonth()
        );


        // Add category budget

        btnAddCategoryBudget.setOnClickListener(
                v -> {

                    Toast.makeText(
                            this,
                            "Category budget feature coming next",
                            Toast.LENGTH_SHORT
                    ).show();

                }
        );


        // Carry forward

        switchCarryForward.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    Toast.makeText(
                            this,
                            isChecked
                                    ? "Carry forward enabled"
                                    : "Carry forward disabled",
                            Toast.LENGTH_SHORT
                    ).show();

                }
        );
    }


    // =========================================================
    // FORMAT CURRENCY
    // =========================================================

    private String formatCurrency(
            double amount) {

        return "Rs. " +
                currencyFormat.format(
                        amount
                );
    }


    // =========================================================
    // REFRESH
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();


        if (userId != -1) {

            loadBudgetData();
        }
    }
}