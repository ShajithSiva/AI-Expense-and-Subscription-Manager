package com.example.aiexpensemanagementapplication.ui.expense;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.ui.dashboard.DashboardActivity;
import com.example.aiexpensemanagementapplication.ui.income.IncomeListActivity;
import com.example.aiexpensemanagementapplication.ui.profile.ProfileActivity;
import com.example.aiexpensemanagementapplication.ui.subscription.SubscriptionActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Locale;

public class ExpenseListActivity extends AppCompatActivity {

    // =========================================================
    // UI
    // =========================================================

    private RecyclerView rvExpenses;

    private TextView tvTotalExpense;
    private TextView tvExpenseCount;

    private LinearLayout layoutEmpty;

    private ExtendedFloatingActionButton fabAddExpense;

    private EditText etSearch;

    private Chip chipAll;
    private Chip chipFood;
    private Chip chipTransport;
    private Chip chipShopping;
    private Chip chipBills;

    private ImageButton btnFilter;

    private BottomNavigationView bottomNavigation;


    // =========================================================
    // DATA
    // =========================================================

    private ExpenseAdapter adapter;

    private ArrayList<ExpenseModel> expenseList;

    private DatabaseHelper databaseHelper;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth mAuth;

    private FirebaseUser currentUser;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_expense_list);

        initialize();

        loadExpenses();

        setupBottomNavigation();

        listeners();
    }


    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initialize() {

        databaseHelper =
                new DatabaseHelper(this);


        mAuth =
                FirebaseAuth.getInstance();


        currentUser =
                mAuth.getCurrentUser();


        // -----------------------------------------------------
        // RecyclerView
        // -----------------------------------------------------

        rvExpenses =
                findViewById(R.id.rvExpenses);


        rvExpenses.setLayoutManager(
                new LinearLayoutManager(this)
        );


        // -----------------------------------------------------
        // Summary
        // -----------------------------------------------------

        tvTotalExpense =
                findViewById(R.id.tvTotalExpense);


        tvExpenseCount =
                findViewById(R.id.tvExpenseCount);


        // -----------------------------------------------------
        // Empty state
        // -----------------------------------------------------

        layoutEmpty =
                findViewById(R.id.layoutEmpty);


        // -----------------------------------------------------
        // Add button
        // -----------------------------------------------------

        fabAddExpense =
                findViewById(R.id.fabAddExpense);


        // -----------------------------------------------------
        // Search
        // -----------------------------------------------------

        etSearch =
                findViewById(R.id.etSearch);


        // -----------------------------------------------------
        // Chips
        // -----------------------------------------------------

        chipAll =
                findViewById(R.id.chipAll);


        chipFood =
                findViewById(R.id.chipFood);


        chipTransport =
                findViewById(R.id.chipTransport);


        chipShopping =
                findViewById(R.id.chipShopping);


        chipBills =
                findViewById(R.id.chipBills);


        // -----------------------------------------------------
        // Filter
        // -----------------------------------------------------

        btnFilter =
                findViewById(R.id.btnFilter);


        // -----------------------------------------------------
        // Bottom Navigation
        // -----------------------------------------------------

        bottomNavigation =
                findViewById(R.id.bottomNavigation);


        // -----------------------------------------------------
        // Expense list
        // -----------------------------------------------------

        expenseList =
                new ArrayList<>();
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void listeners() {

        // -----------------------------------------------------
        // ADD EXPENSE
        // -----------------------------------------------------

        fabAddExpense.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    ExpenseListActivity.this,
                                    AddExpenseActivity.class
                            );


                    startActivity(intent);
                }
        );


        // -----------------------------------------------------
        // CATEGORY CHIPS
        // -----------------------------------------------------

        chipAll.setOnClickListener(
                v -> loadExpenses()
        );


        chipFood.setOnClickListener(
                v -> filterCategory("Food")
        );


        chipTransport.setOnClickListener(
                v -> filterCategory("Transport")
        );


        chipShopping.setOnClickListener(
                v -> filterCategory("Shopping")
        );


        chipBills.setOnClickListener(
                v -> filterCategory("Bills")
        );


        // -----------------------------------------------------
        // FILTER
        // -----------------------------------------------------

        btnFilter.setOnClickListener(
                v -> showFilterDialog()
        );


        // -----------------------------------------------------
        // SEARCH
        // -----------------------------------------------------

        etSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {

                    }


                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        searchExpense(
                                s.toString()
                        );
                    }


                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {

                    }
                }
        );
    }


    // =========================================================
    // FILTER CATEGORY
    // =========================================================

    private void filterCategory(
            String category
    ) {

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


        ArrayList<ExpenseModel> filteredList =
                databaseHelper.getExpensesByCategory(
                        userId,
                        category
                );


        adapter.updateList(
                filteredList
        );


        updateDisplayedListSummary(
                filteredList
        );
    }


    // =========================================================
    // FILTER DIALOG
    // =========================================================

    private void showFilterDialog() {

        BottomSheetDialog dialog =
                new BottomSheetDialog(this);


        View view =
                getLayoutInflater().inflate(
                        R.layout.dialog_filter_expense,
                        null
                );


        dialog.setContentView(
                view
        );


        Spinner spCategory =
                view.findViewById(
                        R.id.spCategoryFilter
                );


        Spinner spPayment =
                view.findViewById(
                        R.id.spPaymentFilter
                );


        Spinner spMode =
                view.findViewById(
                        R.id.spModeFilter
                );


        Spinner spSort =
                view.findViewById(
                        R.id.spSort
                );


        Button btnApply =
                view.findViewById(
                        R.id.btnApply
                );


        Button btnReset =
                view.findViewById(
                        R.id.btnReset
                );


        loadFilterData(
                spCategory,
                spPayment,
                spMode,
                spSort
        );


        // -----------------------------------------------------
        // APPLY
        // -----------------------------------------------------

        btnApply.setOnClickListener(
                v -> {

                    if (spCategory.getSelectedItem() == null ||
                            spPayment.getSelectedItem() == null ||
                            spMode.getSelectedItem() == null ||
                            spSort.getSelectedItem() == null) {

                        return;
                    }


                    applyFilters(

                            spCategory
                                    .getSelectedItem()
                                    .toString(),

                            spPayment
                                    .getSelectedItem()
                                    .toString(),

                            spMode
                                    .getSelectedItem()
                                    .toString(),

                            spSort
                                    .getSelectedItem()
                                    .toString()
                    );


                    dialog.dismiss();
                }
        );


        // -----------------------------------------------------
        // RESET
        // -----------------------------------------------------

        btnReset.setOnClickListener(
                v -> {

                    loadExpenses();

                    dialog.dismiss();
                }
        );


        dialog.show();
    }


    // =========================================================
    // LOAD FILTER DATA
    // =========================================================

    private void loadFilterData(

            Spinner spCategory,

            Spinner spPayment,

            Spinner spSharing,

            Spinner spSort

    ) {

        // =====================================================
        // CATEGORY
        // =====================================================

        ArrayList<String> categories =
                databaseHelper.getExpenseCategoryNames();


        /*
         * Create a copy before inserting "All".
         * This prevents accidental modification if DatabaseHelper
         * later returns a shared list.
         */

        ArrayList<String> categoryOptions =
                new ArrayList<>(
                        categories
                );


        categoryOptions.add(
                0,
                "All"
        );


        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        categoryOptions
                );


        spCategory.setAdapter(
                categoryAdapter
        );


        // =====================================================
        // PAYMENT METHOD
        // =====================================================

        ArrayList<String> methods =
                databaseHelper.getPaymentMethodNames();


        ArrayList<String> paymentOptions =
                new ArrayList<>(
                        methods
                );


        paymentOptions.add(
                0,
                "All"
        );


        ArrayAdapter<String> paymentAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        paymentOptions
                );


        spPayment.setAdapter(
                paymentAdapter
        );


        // =====================================================
        // SHARING FILTER
        //
        // OLD:
        // Personal / Family
        //
        // NEW:
        // All / Not Shared / Shared
        // =====================================================

        ArrayList<String> sharingOptions =
                new ArrayList<>();


        sharingOptions.add(
                "All"
        );


        sharingOptions.add(
                "Not Shared"
        );


        sharingOptions.add(
                "Shared"
        );


        spSharing.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        sharingOptions
                )
        );


        // =====================================================
        // SORT
        // =====================================================

        ArrayList<String> sortOptions =
                new ArrayList<>();


        sortOptions.add(
                "Newest"
        );


        sortOptions.add(
                "Oldest"
        );


        sortOptions.add(
                "Highest Amount"
        );


        sortOptions.add(
                "Lowest Amount"
        );


        spSort.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        sortOptions
                )
        );
    }


    // =========================================================
    // APPLY FILTERS
    // =========================================================

    private void applyFilters(

            String category,

            String payment,

            String sharing,

            String sort

    ) {

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


        /*
         * IMPORTANT:
         *
         * Do NOT call the old:
         *
         * databaseHelper.filterExpenses(
         *      userId,
         *      category,
         *      payment,
         *      mode,
         *      sort
         * );
         *
         * That method still filters ExpenseMode.
         *
         * We now use the new sharing-aware method.
         */


        ArrayList<ExpenseModel> filteredList =
                databaseHelper.filterExpensesBySharing(

                        userId,

                        category,

                        payment,

                        sharing,

                        sort
                );


        adapter.updateList(
                filteredList
        );


        updateDisplayedListSummary(
                filteredList
        );
    }


    // =========================================================
    // LOAD ALL USER EXPENSES
    // =========================================================

    private void loadExpenses() {

        if (currentUser == null) {

            showEmptyList();

            return;
        }


        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid()
                );


        if (userId == -1) {

            showEmptyList();

            return;
        }


        /*
         * getAllExpenses(userId) is correct here.
         *
         * Shared expenses must NOT disappear from the personal
         * expense list.
         *
         * A shared expense is still owned by this user.
         */

        expenseList =
                databaseHelper.getAllExpenses(
                        userId
                );


        // -----------------------------------------------------
        // CREATE ADAPTER ONLY ONCE
        // -----------------------------------------------------

        if (adapter == null) {

            adapter =
                    new ExpenseAdapter(

                            this,

                            expenseList,

                            new ExpenseAdapter.OnExpenseClickListener() {


                                // =========================================
                                // DETAILS
                                // =========================================

                                @Override
                                public void onExpenseClick(
                                        ExpenseModel expense
                                ) {

                                    Intent intent =
                                            new Intent(
                                                    ExpenseListActivity.this,
                                                    ExpenseDetailsActivity.class
                                            );


                                    intent.putExtra(
                                            "transactionId",
                                            expense.getTransactionId()
                                    );


                                    startActivity(
                                            intent
                                    );
                                }


                                // =========================================
                                // EDIT
                                // =========================================

                                @Override
                                public void onEditClick(
                                        ExpenseModel expense
                                ) {

                                    Intent intent =
                                            new Intent(
                                                    ExpenseListActivity.this,
                                                    EditExpenseActivity.class
                                            );


                                    intent.putExtra(
                                            "transactionId",
                                            expense.getTransactionId()
                                    );


                                    startActivity(
                                            intent
                                    );
                                }


                                // =========================================
                                // DELETE
                                // =========================================

                                @Override
                                public void onDeleteClick(
                                        ExpenseModel expense
                                ) {

                                    confirmDeleteExpense(
                                            expense
                                    );
                                }
                            }
                    );


            rvExpenses.setAdapter(
                    adapter
            );

        } else {

            adapter.updateList(
                    expenseList
            );
        }


        updateSummary();
    }


    // =========================================================
    // DELETE CONFIRMATION
    // =========================================================

    private void confirmDeleteExpense(
            ExpenseModel expense
    ) {

        new MaterialAlertDialogBuilder(this)

                .setTitle(
                        "Delete Expense"
                )

                .setMessage(
                        "Are you sure you want to delete this expense?\n\n" +
                                "If it is shared with a family, it will also " +
                                "be removed from that family dashboard."
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            int result =
                                    databaseHelper.deleteExpense(
                                            expense.getTransactionId()
                                    );


                            if (result > 0) {

                                Toast.makeText(
                                        ExpenseListActivity.this,
                                        "Expense deleted successfully.",
                                        Toast.LENGTH_SHORT
                                ).show();


                                loadExpenses();

                            } else {

                                Toast.makeText(
                                        ExpenseListActivity.this,
                                        "Failed to delete expense.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )

                .show();
    }


    // =========================================================
    // FULL SUMMARY
    // =========================================================

    private void updateSummary() {

        if (currentUser == null) {

            showEmptyList();

            return;
        }


        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid()
                );


        if (userId == -1) {

            showEmptyList();

            return;
        }


        double total =
                databaseHelper.getTotalExpense(
                        userId
                );


        int count =
                databaseHelper.getExpenseCount(
                        userId
                );


        tvTotalExpense.setText(
                String.format(
                        Locale.getDefault(),
                        "Rs. %.2f",
                        total
                )
        );


        tvExpenseCount.setText(
                count + " Transactions"
        );


        if (count == 0) {

            layoutEmpty.setVisibility(
                    View.VISIBLE
            );


            rvExpenses.setVisibility(
                    View.GONE
            );

        } else {

            layoutEmpty.setVisibility(
                    View.GONE
            );


            rvExpenses.setVisibility(
                    View.VISIBLE
            );
        }
    }


    // =========================================================
    // FILTERED / SEARCHED LIST SUMMARY
    // =========================================================

    private void updateDisplayedListSummary(
            ArrayList<ExpenseModel> list
    ) {

        if (list == null ||
                list.isEmpty()) {

            tvTotalExpense.setText(
                    "Rs. 0.00"
            );


            tvExpenseCount.setText(
                    "0 Transactions"
            );


            layoutEmpty.setVisibility(
                    View.VISIBLE
            );


            rvExpenses.setVisibility(
                    View.GONE
            );


            return;
        }


        double total = 0;


        for (ExpenseModel expense : list) {

            total +=
                    expense.getAmount();
        }


        tvTotalExpense.setText(
                String.format(
                        Locale.getDefault(),
                        "Rs. %.2f",
                        total
                )
        );


        tvExpenseCount.setText(
                list.size()
                        + " Transactions"
        );


        layoutEmpty.setVisibility(
                View.GONE
        );


        rvExpenses.setVisibility(
                View.VISIBLE
        );
    }


    // =========================================================
    // SEARCH
    // =========================================================

    private void searchExpense(
            String keyword
    ) {

        if (currentUser == null ||
                adapter == null) {

            return;
        }


        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid()
                );


        if (userId == -1) {

            return;
        }


        ArrayList<ExpenseModel> searchList =
                databaseHelper.searchExpenses(
                        userId,
                        keyword
                );


        adapter.updateList(
                searchList
        );


        updateDisplayedListSummary(
                searchList
        );
    }


    // =========================================================
    // EMPTY LIST
    // =========================================================

    private void showEmptyList() {

        tvTotalExpense.setText(
                "Rs. 0.00"
        );


        tvExpenseCount.setText(
                "0 Transactions"
        );


        layoutEmpty.setVisibility(
                View.VISIBLE
        );


        rvExpenses.setVisibility(
                View.GONE
        );
    }


    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private void setupBottomNavigation() {

        bottomNavigation.setSelectedItemId(
                R.id.nav_expenses
        );


        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int id =
                            item.getItemId();


                    // -------------------------------------------------
                    // EXPENSES
                    // -------------------------------------------------

                    if (id == R.id.nav_expenses) {

                        return true;
                    }


                    // -------------------------------------------------
                    // DASHBOARD
                    // -------------------------------------------------

                    else if (id == R.id.nav_dashboard) {

                        Intent intent =
                                new Intent(
                                        ExpenseListActivity.this,
                                        DashboardActivity.class
                                );


                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        );


                        startActivity(
                                intent
                        );


                        return true;
                    }


                    // -------------------------------------------------
                    // PROFILE
                    // -------------------------------------------------

                    else if (id == R.id.nav_profile) {

                        Intent intent =
                                new Intent(
                                        ExpenseListActivity.this,
                                        ProfileActivity.class
                                );


                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        );


                        startActivity(
                                intent
                        );


                        return true;
                    }


                    // -------------------------------------------------
                    // SUBSCRIPTIONS
                    // -------------------------------------------------

                    else if (id == R.id.nav_subscriptions) {

                        Intent intent =
                                new Intent(
                                        ExpenseListActivity.this,
                                        SubscriptionActivity.class
                                );


                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        );


                        startActivity(
                                intent
                        );


                        return true;
                    }


                    // -------------------------------------------------
                    // INCOME
                    // -------------------------------------------------

                    else if (id == R.id.nav_income) {

                        Intent intent =
                                new Intent(
                                        ExpenseListActivity.this,
                                        IncomeListActivity.class
                                );


                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        );


                        startActivity(
                                intent
                        );


                        return true;
                    }


                    return false;
                }
        );
    }


    // =========================================================
    // ON RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();


        /*
         * Refresh after:
         *
         * - Add Expense
         * - Edit Expense
         * - Expense Details
         */

        if (databaseHelper != null) {

            loadExpenses();
        }
    }
}