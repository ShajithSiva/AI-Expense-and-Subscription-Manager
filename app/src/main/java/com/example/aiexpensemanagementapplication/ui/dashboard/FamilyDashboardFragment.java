package com.example.aiexpensemanagementapplication.ui.dashboard;


import java.util.LinkedHashMap;
import java.util.Map;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import com.example.aiexpensemanagementapplication.ui.family.SetFamilyBudgetActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.ui.expense.ExpenseModel;
import com.example.aiexpensemanagementapplication.ui.family.CreateFamilyActivity;
import com.example.aiexpensemanagementapplication.ui.family.InviteMemberActivity;
import com.example.aiexpensemanagementapplication.ui.family.JoinFamilyActivity;
import com.example.aiexpensemanagementapplication.ui.family.SetFamilyBudgetActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

public class FamilyDashboardFragment extends Fragment {

    // =====================================================
    // DATABASE
    // =====================================================

    private DatabaseHelper databaseHelper;


    // =====================================================
    // CURRENT USER / SELECTED FAMILY
    // =====================================================

    private int currentUserId = -1;
    private int selectedFamilyId = -1;

    private boolean userHasFamily = false;

    private String selectedFamilyRole = null;


    // =====================================================
    // FAMILY LIST
    // =====================================================

    private final List<Integer> familyIds = new ArrayList<>();
    private final List<String> familyNames = new ArrayList<>();
    private final List<String> familyRoles = new ArrayList<>();


    // =====================================================
    // NO FAMILY SCREEN
    // =====================================================

    private MaterialCardView cardCreateFamily;
    private MaterialCardView cardJoinFamily;


    // =====================================================
    // FAMILY DASHBOARD HEADER
    // =====================================================

    private View layoutFamilySelector;

    private TextView tvFamilyName;
    private TextView tvMemberCount;

    private ImageButton btnFamilyOptions;
    private MaterialButton btnInviteMember;


    // =====================================================
    // FAMILY EXPENSE UI
    // =====================================================

    private TextView tvFamilySpending;

    private TextView tvFamilySharedIncome;

    private LinearLayout familyTransactionsContainer;

    private PieChart familyPieChart;
    private TextView tvNoCategoryData;

    private MaterialButton btnSetFamilyBudget;
    private TextView tvFamilyBudgetRemaining;
    private ProgressBar progressFamilyBudget;
    private TextView tvBudgetAmount;
    private TextView tvBudgetPercentage;

    private TextView tvAIInsight;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FamilyDashboardFragment() {
        // Required empty constructor

    }


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        databaseHelper =
                new DatabaseHelper(requireContext());

        loadCurrentUser();

        loadUserFamilies();
    }


    // =====================================================
    // LOAD CURRENT USER
    // =====================================================

    private void loadCurrentUser() {

        FirebaseUser firebaseUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (firebaseUser == null) {

            currentUserId = -1;

            return;
        }

        currentUserId =
                databaseHelper
                        .getUserIdByFirebaseUid(
                                firebaseUser.getUid()
                        );
    }


    // =====================================================
    // LOAD USER FAMILIES
    // =====================================================

    private void loadUserFamilies() {

        int previousSelectedFamilyId =
                selectedFamilyId;

        familyIds.clear();
        familyNames.clear();
        familyRoles.clear();

        selectedFamilyRole = null;
        userHasFamily = false;


        // -------------------------------------------------
        // USER CHECK
        // -------------------------------------------------

        if (currentUserId == -1) {

            selectedFamilyId = -1;

            return;
        }


        // -------------------------------------------------
        // LOAD FAMILIES
        // -------------------------------------------------

        Cursor cursor =
                databaseHelper
                        .getFamiliesForUser(
                                currentUserId
                        );

        if (cursor == null) {

            selectedFamilyId = -1;

            return;
        }


        try {

            int familyIdIndex =
                    cursor.getColumnIndex(
                            "FamilyID"
                    );

            int familyNameIndex =
                    cursor.getColumnIndex(
                            "FamilyName"
                    );

            int familyRoleIndex =
                    cursor.getColumnIndex(
                            "Role"
                    );


            if (familyIdIndex == -1 ||
                    familyNameIndex == -1 ||
                    familyRoleIndex == -1) {

                selectedFamilyId = -1;

                return;
            }


            while (cursor.moveToNext()) {

                int familyId =
                        cursor.getInt(
                                familyIdIndex
                        );

                String familyName =
                        cursor.getString(
                                familyNameIndex
                        );

                String familyRole =
                        cursor.getString(
                                familyRoleIndex
                        );


                familyIds.add(familyId);
                familyNames.add(familyName);
                familyRoles.add(familyRole);
            }

        } finally {

            cursor.close();
        }


        // -------------------------------------------------
        // FAMILY STATUS
        // -------------------------------------------------

        userHasFamily =
                !familyIds.isEmpty();


        if (!userHasFamily) {

            selectedFamilyId = -1;
            selectedFamilyRole = null;

            return;
        }


        // -------------------------------------------------
        // KEEP PREVIOUS FAMILY SELECTION
        // -------------------------------------------------

        int previousIndex =
                familyIds.indexOf(
                        previousSelectedFamilyId
                );


        if (previousIndex != -1) {

            selectedFamilyId =
                    familyIds.get(
                            previousIndex
                    );

            selectedFamilyRole =
                    familyRoles.get(
                            previousIndex
                    );

        } else {

            selectedFamilyId =
                    familyIds.get(0);

            selectedFamilyRole =
                    familyRoles.get(0);
        }
    }


    // =====================================================
    // CREATE VIEW
    // =====================================================

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        if (userHasFamily) {

            return inflater.inflate(
                    R.layout.fragment_family_dashboard,
                    container,
                    false
            );

        } else {

            return inflater.inflate(
                    R.layout.view_no_family,
                    container,
                    false
            );
        }
    }


    // =====================================================
    // VIEW CREATED
    // =====================================================

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(
                view,
                savedInstanceState
        );


        if (userHasFamily) {

            setupFamilyDashboard(view);

        } else {

            setupNoFamilyScreen(view);
        }
    }


    // =====================================================
    // NO FAMILY SCREEN
    // =====================================================

    private void setupNoFamilyScreen(View view) {

        cardCreateFamily =
                view.findViewById(
                        R.id.cardCreateFamily
                );

        cardJoinFamily =
                view.findViewById(
                        R.id.cardJoinFamily
                );


        if (cardCreateFamily != null) {

            cardCreateFamily.setOnClickListener(
                    v -> openCreateFamilyActivity()
            );
        }


        if (cardJoinFamily != null) {

            cardJoinFamily.setOnClickListener(
                    v -> openJoinFamilyActivity()
            );
        }
    }


    // =====================================================
    // SETUP FAMILY DASHBOARD
    // =====================================================

    private void setupFamilyDashboard(View view) {

        // -------------------------------------------------
        // HEADER
        // -------------------------------------------------

        layoutFamilySelector =
                view.findViewById(
                        R.id.layoutFamilySelector
                );

        tvFamilyName =
                view.findViewById(
                        R.id.tvFamilyName
                );

        tvMemberCount =
                view.findViewById(
                        R.id.tvMemberCount
                );

        btnFamilyOptions =
                view.findViewById(
                        R.id.btnFamilyOptions
                );
        tvFamilySharedIncome =
                view.findViewById(
                        R.id.tvFamilySharedIncome
                );

        btnInviteMember =
                view.findViewById(
                        R.id.btnInviteMember
                );
        btnSetFamilyBudget =
                view.findViewById(R.id.btnSetFamilyBudget);

        btnSetFamilyBudget.setOnClickListener(v -> {

            Intent intent = new Intent(
                    requireContext(),
                    SetFamilyBudgetActivity.class
            );

            intent.putExtra(
                    "familyId",
                    selectedFamilyId
            );

            startActivity(intent);
        });


        // -------------------------------------------------
        // FAMILY EXPENSE UI
        // -------------------------------------------------

        tvFamilySpending =
                view.findViewById(
                        R.id.tvFamilySpending
                );

        tvAIInsight =
                view.findViewById(
                        R.id.tvAIInsight
                );

        familyTransactionsContainer =
                view.findViewById(
                        R.id.familyTransactionsContainer
                );




        familyPieChart =
                view.findViewById(R.id.familyPieChart);

        tvNoCategoryData =
                view.findViewById(R.id.tvNoCategoryData);

        btnSetFamilyBudget =
                view.findViewById(R.id.btnSetFamilyBudget);

        tvFamilyBudgetRemaining =
                view.findViewById(R.id.tvFamilyBudgetRemaining);

        progressFamilyBudget =
                view.findViewById(R.id.progressFamilyBudget);

        tvBudgetAmount =
                view.findViewById(R.id.tvBudgetAmount);

        tvBudgetPercentage =
                view.findViewById(R.id.tvBudgetPercentage);


        // -------------------------------------------------
        // FAMILY SELECTOR
        // -------------------------------------------------

        if (layoutFamilySelector != null) {

            layoutFamilySelector.setOnClickListener(
                    v -> showFamilySelectorMenu()
            );
        }


        // -------------------------------------------------
        // OPTIONS
        // -------------------------------------------------

        if (btnFamilyOptions != null) {

            btnFamilyOptions.setOnClickListener(
                    v -> showFamilyOptionsMenu()
            );
        }


        // -------------------------------------------------
        // INVITE MEMBER
        // -------------------------------------------------

        if (btnInviteMember != null) {

            btnInviteMember.setOnClickListener(
                    v -> openInviteMemberActivity()
            );
        }


        // -------------------------------------------------
        // LOAD DASHBOARD
        // -------------------------------------------------

        loadSelectedFamilyDashboard();
    }


    // =====================================================
    // LOAD SELECTED FAMILY DASHBOARD
    // =====================================================

    private void loadSelectedFamilyDashboard() {

        if (selectedFamilyId == -1 ||
                currentUserId == -1) {

            return;
        }


        // -------------------------------------------------
        // FAMILY NAME
        // -------------------------------------------------

        String familyName =
                databaseHelper
                        .getFamilyName(
                                selectedFamilyId
                        );


        if (familyName == null ||
                familyName.trim().isEmpty()) {

            familyName = "Family";
        }


        if (tvFamilyName != null) {

            tvFamilyName.setText(
                    familyName
            );
        }


        // -------------------------------------------------
        // USER ROLE
        // -------------------------------------------------

        selectedFamilyRole =
                databaseHelper
                        .getFamilyRole(
                                currentUserId,
                                selectedFamilyId
                        );


        // -------------------------------------------------
        // MEMBER COUNT
        // -------------------------------------------------

        int memberCount =
                databaseHelper
                        .getFamilyMemberCount(
                                selectedFamilyId
                        );


        if (tvMemberCount != null) {

            if (memberCount == 1) {

                tvMemberCount.setText(
                        "1 Member"
                );

            } else {

                tvMemberCount.setText(
                        memberCount + " Members"
                );
            }
        }


        // -------------------------------------------------
        // ROLE UI
        // -------------------------------------------------

        updateRoleBasedUI();


        // -------------------------------------------------
        // EXPENSES + GRAPH
        // -------------------------------------------------

        loadSharedExpenses();
    }


    // =====================================================
// LOAD SHARED EXPENSES
// =====================================================

    private void loadSharedExpenses() {

        if (selectedFamilyId == -1) {
            return;
        }


        // -------------------------------------------------
        // TOTAL FAMILY SPENDING
        // -------------------------------------------------

        double totalExpense =
                databaseHelper.getFamilyTotalExpense(
                        selectedFamilyId
                );


        // -------------------------------------------------
        // TOTAL FAMILY INCOME
        // -------------------------------------------------

        double totalIncome =
                databaseHelper.getFamilyTotalIncome(
                        selectedFamilyId
                );


        // -------------------------------------------------
        // UPDATE INCOME
        // -------------------------------------------------

        if (tvFamilySharedIncome != null) {

            tvFamilySharedIncome.setText(
                    String.format(
                            Locale.getDefault(),
                            "Rs %,.2f",
                            totalIncome
                    )
            );
        }


        // -------------------------------------------------
        // UPDATE EXPENSE
        // -------------------------------------------------

        if (tvFamilySpending != null) {

            tvFamilySpending.setText(
                    String.format(
                            Locale.getDefault(),
                            "Rs %,.2f",
                            totalExpense
                    )
            );
        }


        // -------------------------------------------------
        // GET FAMILY EXPENSES
        // -------------------------------------------------

        ArrayList<ExpenseModel> expenses =
                databaseHelper.getFamilyExpenses(
                        selectedFamilyId
                );


        // -------------------------------------------------
        // CATEGORY GRAPH
        // -------------------------------------------------

        loadFamilyCategorySpending(
                expenses
        );


        // -------------------------------------------------
        // RECENT TRANSACTIONS
        // -------------------------------------------------

        loadRecentFamilyTransactions(
                expenses
        );


        // -------------------------------------------------
        // FAMILY BUDGET
        // -------------------------------------------------

        loadFamilyBudget();


        // -------------------------------------------------
        // AI FAMILY INSIGHT
        // -------------------------------------------------

        loadFamilyAIInsight(
                totalIncome,
                totalExpense,
                expenses
        );
    }

    // =====================================================
// AI FAMILY INSIGHT
// =====================================================

    private void loadFamilyAIInsight(
            double totalIncome,
            double totalExpense,
            ArrayList<ExpenseModel> expenses
    ) {

        if (tvAIInsight == null) {
            return;
        }

        // -------------------------------------------------
        // NO FAMILY DATA
        // -------------------------------------------------

        if (totalIncome <= 0 && totalExpense <= 0) {

            tvAIInsight.setText(
                    "💡 Start recording family income and expenses "
                            + "to receive personalized AI financial insights."
            );

            return;
        }


        // -------------------------------------------------
        // EXPENSES WITHOUT INCOME
        // -------------------------------------------------

        if (totalIncome <= 0 && totalExpense > 0) {

            tvAIInsight.setText(
                    "⚠ Family expenses have been recorded, but "
                            + "no family income has been added yet. "
                            + "Add income to get more accurate financial insights."
            );

            return;
        }


        // -------------------------------------------------
        // CALCULATE SAVINGS
        // -------------------------------------------------

        double savings =
                totalIncome - totalExpense;


        // -------------------------------------------------
        // EXPENSE PERCENTAGE
        // -------------------------------------------------

        double expensePercentage =
                (totalExpense / totalIncome) * 100;


        // -------------------------------------------------
        // FIND HIGHEST SPENDING CATEGORY
        // -------------------------------------------------

        Map<String, Double> categoryTotals =
                new LinkedHashMap<>();


        if (expenses != null) {

            for (ExpenseModel expense : expenses) {

                if (expense == null) {
                    continue;
                }

                String category =
                        expense.getCategoryName();


                if (category == null ||
                        category.trim().isEmpty()) {

                    category = "Other";
                }


                double amount =
                        expense.getAmount();


                Double current =
                        categoryTotals.get(category);


                if (current == null) {
                    current = 0.0;
                }


                categoryTotals.put(
                        category,
                        current + amount
                );
            }
        }


        String highestCategory =
                "general spending";

        double highestCategoryAmount =
                0;


        for (Map.Entry<String, Double> entry :
                categoryTotals.entrySet()) {

            if (entry.getValue() >
                    highestCategoryAmount) {

                highestCategoryAmount =
                        entry.getValue();

                highestCategory =
                        entry.getKey();
            }
        }


        // =================================================
        // INSIGHT LOGIC
        // =================================================

        String insight;


        // -------------------------------------------------
        // CASE 1: EXPENSES > INCOME
        // -------------------------------------------------

        if (totalExpense > totalIncome) {

            double overspending =
                    totalExpense - totalIncome;


            insight =
                    "⚠ Your family is currently spending more "
                            + "than its recorded income by Rs "
                            + formatAmount(overspending)
                            + ". "
                            + "The highest spending category is "
                            + highestCategory
                            + ". "
                            + "Consider reducing non-essential "
                            + "expenses and reviewing this category.";


        }

        // -------------------------------------------------
        // CASE 2: VERY HIGH SPENDING
        // -------------------------------------------------

        else if (expensePercentage >= 80) {

            insight =
                    "⚠ Your family has used "
                            + formatAmount(expensePercentage)
                            + "% of its recorded income. "
                            + highestCategory
                            + " is currently the highest spending "
                            + "category. "
                            + "Try to reduce unnecessary spending "
                            + "to protect your remaining savings.";


        }

        // -------------------------------------------------
        // CASE 3: MODERATE-HIGH SPENDING
        // -------------------------------------------------

        else if (expensePercentage >= 60) {

            insight =
                    "💡 Your family has used "
                            + formatAmount(expensePercentage)
                            + "% of its recorded income. "
                            + highestCategory
                            + " is the largest expense category. "
                            + "Your family finances are manageable, "
                            + "but controlling discretionary spending "
                            + "could improve savings.";


        }

        // -------------------------------------------------
        // CASE 4: HEALTHY SPENDING
        // -------------------------------------------------

        else if (expensePercentage <= 40) {

            insight =
                    "🎯 Your family is maintaining a healthy "
                            + "spending pattern. Only "
                            + formatAmount(expensePercentage)
                            + "% of recorded income has been spent. "
                            + "Approximately Rs "
                            + formatAmount(savings)
                            + " remains after expenses. "
                            + "Keep maintaining this positive habit.";


        }

        // -------------------------------------------------
        // CASE 5: NORMAL
        // -------------------------------------------------

        else {

            insight =
                    "✓ Your family spending is currently "
                            + "under control. Approximately Rs "
                            + formatAmount(savings)
                            + " remains after recorded expenses. "
                            + "Continue monitoring "
                            + highestCategory
                            + " and maintain consistent savings.";
        }


        // -------------------------------------------------
        // DISPLAY
        // -------------------------------------------------

        tvAIInsight.setText(
                insight
        );
    }

    // =====================================================
// FORMAT AMOUNT
// =====================================================

    private String formatAmount(double amount) {

        return String.format(
                Locale.getDefault(),
                "%,.2f",
                amount
        );
    }


    // =====================================================
    // LOAD RECENT FAMILY TRANSACTIONS
    // =====================================================

    private void loadRecentFamilyTransactions(
            ArrayList<ExpenseModel> expenses
    ) {

        if (familyTransactionsContainer == null) {

            return;
        }


        familyTransactionsContainer
                .removeAllViews();


        // -------------------------------------------------
        // EMPTY
        // -------------------------------------------------

        if (expenses == null ||
                expenses.isEmpty()) {

            showNoFamilyTransactions();

            return;
        }


        // -------------------------------------------------
        // MAXIMUM 5
        // -------------------------------------------------

        int displayCount =
                Math.min(
                        expenses.size(),
                        5
                );


        for (int i = 0;
             i < displayCount;
             i++) {

            ExpenseModel expense =
                    expenses.get(i);


            addFamilyExpenseRow(
                    expense,
                    i < displayCount - 1
            );
        }
    }

    // =====================================================
// LOAD SELECTED FAMILY BUDGET
// =====================================================

    private void loadFamilyBudget() {

        if (selectedFamilyId == -1) {
            return;
        }


        // -------------------------------------------------
        // GET BUDGET FOR SELECTED FAMILY
        // -------------------------------------------------

        double budget =
                databaseHelper.getFamilyBudgetLimit(
                        selectedFamilyId
                );


        // -------------------------------------------------
        // GET SPENDING FOR SELECTED FAMILY
        // -------------------------------------------------

        double spent =
                databaseHelper.getFamilyTotalExpense(
                        selectedFamilyId
                );


        // -------------------------------------------------
        // NO BUDGET
        // -------------------------------------------------

        if (budget <= 0) {

            if (tvBudgetAmount != null) {

                tvBudgetAmount.setText(
                        "Rs. 0.00"
                );
            }


            if (tvFamilyBudgetRemaining != null) {

                tvFamilyBudgetRemaining.setText(
                        "Rs. 0.00"
                );
            }


            if (tvBudgetPercentage != null) {

                tvBudgetPercentage.setText(
                        "No Budget Set"
                );
            }


            if (progressFamilyBudget != null) {

                progressFamilyBudget.setProgress(
                        0
                );
            }

            return;
        }


        // -------------------------------------------------
        // REMAINING
        // -------------------------------------------------

        double remaining =
                budget - spent;


        // Don't show negative remaining amount
        if (remaining < 0) {
            remaining = 0;
        }


        // -------------------------------------------------
        // PERCENTAGE
        // -------------------------------------------------

        int percentage =
                (int) Math.round(
                        (spent / budget) * 100
                );


        if (percentage < 0) {
            percentage = 0;
        }


        if (percentage > 100) {
            percentage = 100;
        }


        // -------------------------------------------------
        // UPDATE BUDGET UI
        // -------------------------------------------------

        if (tvBudgetAmount != null) {

            tvBudgetAmount.setText(
                    String.format(
                            Locale.getDefault(),
                            "Rs. %.2f",
                            budget
                    )
            );
        }


        if (tvFamilyBudgetRemaining != null) {

            tvFamilyBudgetRemaining.setText(
                    String.format(
                            Locale.getDefault(),
                            "Rs. %.2f",
                            remaining
                    )
            );
        }


        if (tvBudgetPercentage != null) {

            tvBudgetPercentage.setText(
                    percentage + "% Used"
            );
        }


        if (progressFamilyBudget != null) {

            progressFamilyBudget.setProgress(
                    percentage
            );
        }
    }
    // =====================================================
    // NO TRANSACTIONS
    // =====================================================

    private void showNoFamilyTransactions() {

        if (familyTransactionsContainer == null) {

            return;
        }


        TextView emptyView =
                new TextView(
                        requireContext()
                );


        emptyView.setText(
                "No shared transactions yet"
        );

        emptyView.setTextSize(12);

        emptyView.setTextColor(
                Color.parseColor(
                        "#9CA3AF"
                )
        );

        emptyView.setGravity(
                Gravity.CENTER
        );

        emptyView.setPadding(
                0,
                dpToPx(24),
                0,
                dpToPx(24)
        );


        familyTransactionsContainer
                .addView(
                        emptyView
                );
    }


    // =====================================================
    // ADD FAMILY EXPENSE ROW
    // =====================================================

    private void addFamilyExpenseRow(
            ExpenseModel expense,
            boolean showDivider
    ) {

        if (expense == null ||
                familyTransactionsContainer == null) {

            return;
        }


        LinearLayout row =
                new LinearLayout(
                        requireContext()
                );

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                0,
                dpToPx(12),
                0,
                dpToPx(12)
        );


        // -------------------------------------------------
        // LEFT INFO
        // -------------------------------------------------

        LinearLayout informationLayout =
                new LinearLayout(
                        requireContext()
                );

        informationLayout.setOrientation(
                LinearLayout.VERTICAL
        );


        LinearLayout.LayoutParams informationParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                );


        // -------------------------------------------------
        // CATEGORY
        // -------------------------------------------------

        TextView categoryView =
                new TextView(
                        requireContext()
                );


        String categoryName =
                expense.getCategoryName();


        if (categoryName == null ||
                categoryName.trim().isEmpty()) {

            categoryName = "Expense";
        }


        categoryView.setText(
                categoryName
        );

        categoryView.setTextSize(14);

        categoryView.setTextColor(
                Color.parseColor(
                        "#111827"
                )
        );

        categoryView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        // -------------------------------------------------
        // DATE + NOTE
        // -------------------------------------------------

        TextView detailView =
                new TextView(
                        requireContext()
                );


        String date =
                expense.getTransactionDate();

        if (date == null) {
            date = "";
        }


        String note =
                expense.getNote();


        String detailText =
                date;


        if (note != null &&
                !note.trim().isEmpty()) {

            if (!detailText.isEmpty()) {

                detailText += "  •  ";
            }

            detailText +=
                    note.trim();
        }


        detailView.setText(
                detailText
        );

        detailView.setTextSize(11);

        detailView.setTextColor(
                Color.parseColor(
                        "#8A909C"
                )
        );

        detailView.setPadding(
                0,
                dpToPx(4),
                0,
                0
        );


        informationLayout.addView(
                categoryView
        );

        informationLayout.addView(
                detailView
        );


        // -------------------------------------------------
        // AMOUNT
        // -------------------------------------------------

        TextView amountView =
                new TextView(
                        requireContext()
                );


        amountView.setText(

                String.format(
                        Locale.getDefault(),
                        "- Rs %,.2f",
                        expense.getAmount()
                )
        );

        amountView.setTextSize(13);

        amountView.setTextColor(
                Color.parseColor(
                        "#DC2626"
                )
        );

        amountView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        // -------------------------------------------------
        // ADD
        // -------------------------------------------------

        row.addView(
                informationLayout,
                informationParams
        );

        row.addView(
                amountView
        );


        familyTransactionsContainer
                .addView(
                        row
                );


        // -------------------------------------------------
        // DIVIDER
        // -------------------------------------------------

        if (showDivider) {

            View divider =
                    new View(
                            requireContext()
                    );

            divider.setBackgroundColor(
                    Color.parseColor(
                            "#E5E7EB"
                    )
            );


            LinearLayout.LayoutParams dividerParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dpToPx(1)
                    );


            familyTransactionsContainer
                    .addView(
                            divider,
                            dividerParams
                    );
        }
    }


    // =====================================================
// LOAD FAMILY CATEGORY PIE CHART
// =====================================================

    private void loadFamilyCategorySpending(
            ArrayList<ExpenseModel> expenses
    ) {

        if (familyPieChart == null) {
            return;
        }

        ArrayList<PieEntry> entries =
                new ArrayList<>();


        // -------------------------------------------------
        // EMPTY CHECK
        // -------------------------------------------------

        if (expenses == null || expenses.isEmpty()) {

            showNoCategoryData();

            return;
        }


        // -------------------------------------------------
        // CALCULATE CATEGORY TOTALS
        // -------------------------------------------------

        Map<String, Float> categoryTotals =
                new LinkedHashMap<>();


        for (ExpenseModel expense : expenses) {

            if (expense == null) {
                continue;
            }

            String category =
                    expense.getCategoryName();

            if (category == null ||
                    category.trim().isEmpty()) {

                category = "Other";
            }

            float amount =
                    (float) expense.getAmount();


            if (categoryTotals.containsKey(category)) {

                Float current =
                        categoryTotals.get(category);

                if (current == null) {
                    current = 0f;
                }

                categoryTotals.put(
                        category,
                        current + amount
                );

            } else {

                categoryTotals.put(
                        category,
                        amount
                );
            }
        }


        // -------------------------------------------------
        // CREATE PIE ENTRIES
        // -------------------------------------------------

        for (Map.Entry<String, Float> entry :
                categoryTotals.entrySet()) {

            if (entry.getValue() > 0) {

                entries.add(
                        new PieEntry(
                                entry.getValue(),
                                entry.getKey()
                        )
                );
            }
        }


        if (entries.isEmpty()) {

            showNoCategoryData();

            return;
        }


        // -------------------------------------------------
        // SHOW CHART
        // -------------------------------------------------

        if (tvNoCategoryData != null) {

            tvNoCategoryData.setVisibility(
                    View.GONE
            );
        }

        familyPieChart.setVisibility(
                View.VISIBLE
        );


        // -------------------------------------------------
        // DATA SET
        // -------------------------------------------------

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


        // -------------------------------------------------
        // PIE DATA
        // -------------------------------------------------

        PieData pieData =
                new PieData(dataSet);

        pieData.setValueTextSize(11f);

        pieData.setValueTextColor(
                Color.WHITE
        );


        // -------------------------------------------------
        // CHART CONFIGURATION
        // -------------------------------------------------

        familyPieChart.setData(
                pieData
        );

        familyPieChart
                .getDescription()
                .setEnabled(false);

        familyPieChart
                .getLegend()
                .setEnabled(false);

        familyPieChart.setDrawHoleEnabled(
                true
        );

        familyPieChart.setHoleRadius(
                68f
        );

        familyPieChart.setTransparentCircleRadius(
                72f
        );

        familyPieChart.setDrawEntryLabels(
                false
        );

        familyPieChart.setRotationEnabled(
                true
        );

        familyPieChart.setHighlightPerTapEnabled(
                true
        );

        familyPieChart.animateY(
                700
        );

        familyPieChart.invalidate();
    }


// =====================================================
// NO CATEGORY DATA
// =====================================================

    private void showNoCategoryData() {

        if (familyPieChart != null) {

            familyPieChart.clear();

            familyPieChart.setVisibility(
                    View.GONE
            );
        }


        if (tvNoCategoryData != null) {

            tvNoCategoryData.setVisibility(
                    View.VISIBLE
            );
        }
    }

    // =====================================================
    // UPDATE ROLE BASED UI
    // =====================================================

    private void updateRoleBasedUI() {

        if (btnInviteMember == null) {

            return;
        }


        if (selectedFamilyRole == null) {

            btnInviteMember.setVisibility(
                    View.GONE
            );

            return;
        }


        if ("PRIMARY".equalsIgnoreCase(
                selectedFamilyRole
        )) {

            btnInviteMember.setVisibility(
                    View.VISIBLE
            );

        } else {

            btnInviteMember.setVisibility(
                    View.GONE
            );
        }
    }


    // =====================================================
    // FAMILY SELECTOR
    // =====================================================

    private void showFamilySelectorMenu() {

        if (layoutFamilySelector == null) {

            return;
        }


        PopupMenu popupMenu =
                new PopupMenu(
                        requireContext(),
                        layoutFamilySelector
                );


        Menu menu =
                popupMenu.getMenu();


        // -------------------------------------------------
        // EXISTING FAMILIES
        // -------------------------------------------------

        for (int i = 0;
             i < familyIds.size();
             i++) {

            int familyId =
                    familyIds.get(i);

            String familyName =
                    familyNames.get(i);

            String role =
                    familyRoles.get(i);


            String title =
                    familyName +
                            "  •  " +
                            role;


            MenuItem item =
                    menu.add(
                            Menu.NONE,
                            familyId,
                            Menu.NONE,
                            title
                    );


            item.setCheckable(true);


            if (familyId ==
                    selectedFamilyId) {

                item.setChecked(true);
            }
        }


        // -------------------------------------------------
        // EXTRA OPTIONS
        // -------------------------------------------------

        final int MENU_CREATE_FAMILY =
                900001;

        final int MENU_JOIN_FAMILY =
                900002;


        menu.add(
                Menu.NONE,
                MENU_CREATE_FAMILY,
                Menu.NONE,
                "+ Create New Family"
        );


        menu.add(
                Menu.NONE,
                MENU_JOIN_FAMILY,
                Menu.NONE,
                "+ Join Another Family"
        );


        // -------------------------------------------------
        // MENU CLICK
        // -------------------------------------------------

        popupMenu.setOnMenuItemClickListener(
                item -> {

                    int itemId =
                            item.getItemId();


                    if (itemId ==
                            MENU_CREATE_FAMILY) {

                        openCreateFamilyActivity();

                        return true;
                    }


                    if (itemId ==
                            MENU_JOIN_FAMILY) {

                        openJoinFamilyActivity();

                        return true;
                    }


                    int index =
                            familyIds.indexOf(
                                    itemId
                            );


                    if (index != -1) {

                        selectFamily(index);

                        return true;
                    }


                    return false;
                }
        );


        popupMenu.show();
    }


    // =====================================================
    // SELECT FAMILY
    // =====================================================

    private void selectFamily(int index) {

        if (index < 0 ||
                index >= familyIds.size()) {

            return;
        }


        selectedFamilyId =
                familyIds.get(index);

        selectedFamilyRole =
                familyRoles.get(index);


        // Reload everything for selected family
        loadSelectedFamilyDashboard();
    }


    // =====================================================
    // FAMILY OPTIONS
    // =====================================================

    private void showFamilyOptionsMenu() {

        if (btnFamilyOptions == null ||
                selectedFamilyId == -1) {

            return;
        }


        PopupMenu popupMenu =
                new PopupMenu(
                        requireContext(),
                        btnFamilyOptions
                );


        Menu menu =
                popupMenu.getMenu();


        final int MENU_INVITE =
                100001;

        final int MENU_LEAVE =
                100002;

        final int MENU_DELETE =
                100003;


        // -------------------------------------------------
        // PRIMARY USER
        // -------------------------------------------------

        if ("PRIMARY".equalsIgnoreCase(
                selectedFamilyRole
        )) {

            menu.add(
                    Menu.NONE,
                    MENU_INVITE,
                    Menu.NONE,
                    "Invite Member"
            );


            menu.add(
                    Menu.NONE,
                    MENU_DELETE,
                    Menu.NONE,
                    "Delete Family"
            );

        } else {

            menu.add(
                    Menu.NONE,
                    MENU_LEAVE,
                    Menu.NONE,
                    "Leave Family"
            );
        }


        // -------------------------------------------------
        // MENU CLICK
        // -------------------------------------------------

        popupMenu.setOnMenuItemClickListener(
                item -> {

                    int itemId =
                            item.getItemId();


                    if (itemId ==
                            MENU_INVITE) {

                        openInviteMemberActivity();

                        return true;
                    }


                    if (itemId ==
                            MENU_LEAVE) {

                        confirmLeaveFamily();

                        return true;
                    }


                    if (itemId ==
                            MENU_DELETE) {

                        confirmDeleteFamily();

                        return true;
                    }


                    return false;
                }
        );


        popupMenu.show();
    }


    // =====================================================
    // CREATE FAMILY
    // =====================================================

    private void openCreateFamilyActivity() {

        Intent intent =
                new Intent(
                        requireContext(),
                        CreateFamilyActivity.class
                );

        startActivity(intent);
    }


    // =====================================================
    // JOIN FAMILY
    // =====================================================

    private void openJoinFamilyActivity() {

        Intent intent =
                new Intent(
                        requireContext(),
                        JoinFamilyActivity.class
                );

        startActivity(intent);
    }


    // =====================================================
    // INVITE MEMBER
    // =====================================================

    private void openInviteMemberActivity() {

        if (selectedFamilyId == -1) {

            Toast.makeText(
                    requireContext(),
                    "No family selected",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        Intent intent =
                new Intent(
                        requireContext(),
                        InviteMemberActivity.class
                );


        intent.putExtra(
                "FAMILY_ID",
                selectedFamilyId
        );


        startActivity(intent);
    }


    // =====================================================
    // CONFIRM LEAVE FAMILY
    // =====================================================

    private void confirmLeaveFamily() {

        if (selectedFamilyId == -1 ||
                currentUserId == -1) {

            return;
        }


        String familyName =
                databaseHelper
                        .getFamilyName(
                                selectedFamilyId
                        );


        if (familyName == null) {

            familyName = "this family";
        }


        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "Leave Family"
                )

                .setMessage(
                        "Are you sure you want to leave "
                                + familyName
                                + "?"
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Leave",
                        (dialog, which) ->
                                leaveSelectedFamily()
                )

                .show();
    }


    // =====================================================
    // LEAVE FAMILY
    // =====================================================

    private void leaveSelectedFamily() {

        if (selectedFamilyId == -1 ||
                currentUserId == -1) {

            return;
        }


        boolean success =
                databaseHelper
                        .leaveFamily(
                                selectedFamilyId,
                                currentUserId
                        );


        if (success) {

            Toast.makeText(
                    requireContext(),
                    "You left the family",
                    Toast.LENGTH_SHORT
            ).show();


            reloadFragment();

        } else {

            Toast.makeText(
                    requireContext(),
                    "Unable to leave this family",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // =====================================================
    // CONFIRM DELETE FAMILY
    // =====================================================

    private void confirmDeleteFamily() {

        if (selectedFamilyId == -1 ||
                currentUserId == -1) {

            return;
        }


        String familyName =
                databaseHelper
                        .getFamilyName(
                                selectedFamilyId
                        );


        if (familyName == null) {

            familyName = "this family";
        }


        String message =
                "Delete \"" +
                        familyName +
                        "\"?\n\n" +
                        "All memberships for this family " +
                        "will be removed.";


        new AlertDialog.Builder(
                requireContext()
        )

                .setTitle(
                        "Delete Family"
                )

                .setMessage(
                        message
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteSelectedFamily()
                )

                .show();
    }


    // =====================================================
    // DELETE FAMILY
    // =====================================================

    private void deleteSelectedFamily() {

        if (selectedFamilyId == -1 ||
                currentUserId == -1) {

            return;
        }


        boolean success =
                databaseHelper
                        .deleteFamily(
                                selectedFamilyId,
                                currentUserId
                        );


        if (success) {

            Toast.makeText(
                    requireContext(),
                    "Family deleted",
                    Toast.LENGTH_SHORT
            ).show();


            reloadFragment();

        } else {

            Toast.makeText(
                    requireContext(),
                    "Only the primary user can delete this family",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // =====================================================
    // RELOAD FRAGMENT
    // =====================================================

    private void reloadFragment() {

        if (!isAdded()) {

            return;
        }


        loadCurrentUser();
        loadUserFamilies();


        getParentFragmentManager()
                .beginTransaction()
                .detach(this)
                .attach(this)
                .commit();
    }


    // =====================================================
    // DP TO PX
    // =====================================================

    private int dpToPx(int dp) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;


        return Math.round(
                dp * density
        );
    }


    // =====================================================
    // ON RESUME
    // =====================================================

    @Override
    public void onResume() {

        super.onResume();


        if (databaseHelper == null ||
                !isAdded()) {

            return;
        }


        int oldFamilyCount =
                familyIds.size();


        loadCurrentUser();
        loadUserFamilies();


        int newFamilyCount =
                familyIds.size();


        // -------------------------------------------------
        // FAMILY LIST CHANGED
        // -------------------------------------------------

        if (oldFamilyCount !=
                newFamilyCount) {

            getParentFragmentManager()
                    .beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();

            return;
        }


        // -------------------------------------------------
        // REFRESH FAMILY DASHBOARD
        // -------------------------------------------------

        if (userHasFamily &&
                getView() != null) {

            loadSelectedFamilyDashboard();
        }
    }


    // =====================================================
    // CLEANUP
    // =====================================================

    @Override
    public void onDestroy() {

        super.onDestroy();


        if (databaseHelper != null) {

            databaseHelper.close();
        }
    }
}