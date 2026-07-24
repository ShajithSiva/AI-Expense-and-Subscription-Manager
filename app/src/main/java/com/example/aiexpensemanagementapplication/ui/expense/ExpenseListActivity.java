package com.example.aiexpensemanagementapplication.ui.expense;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.aiexpensemanagementapplication.ui.dashboard.PersonalDashboardActivity;
import com.example.aiexpensemanagementapplication.ui.income.IncomeListActivity;
import com.example.aiexpensemanagementapplication.ui.profile.EditProfileActivity;
import com.example.aiexpensemanagementapplication.ui.profile.ProfileActivity;
import com.example.aiexpensemanagementapplication.ui.subscription.SubscriptionActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.chip.Chip;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import com.google.android.material.bottomsheet.BottomSheetDialog;


import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;


public class ExpenseListActivity extends AppCompatActivity {

    private RecyclerView rvExpenses;

    private ExpenseAdapter adapter;

    private ArrayList<ExpenseModel> expenseList;

    private DatabaseHelper databaseHelper;

    private FirebaseAuth mAuth;

    private BottomNavigationView bottomNavigation;

    private ImageButton btnFilter;

    private FirebaseUser currentUser;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_expense_list);

        initialize();

        loadExpenses();

        setupBottomNavigation();

        listeners();
    }
    private void initialize() {

        databaseHelper = new DatabaseHelper(this);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        rvExpenses = findViewById(R.id.rvExpenses);

        tvTotalExpense = findViewById(R.id.tvTotalExpense);

        tvExpenseCount = findViewById(R.id.tvExpenseCount);

        layoutEmpty = findViewById(R.id.layoutEmpty);

        fabAddExpense = findViewById(R.id.fabAddExpense);

        etSearch = findViewById(R.id.etSearch);

        chipAll = findViewById(R.id.chipAll);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        chipFood = findViewById(R.id.chipFood);

        chipTransport = findViewById(R.id.chipTransport);

        chipShopping = findViewById(R.id.chipShopping);

        chipBills = findViewById(R.id.chipBills);

        btnFilter = findViewById(R.id.btnFilter);

        expenseList = new ArrayList<>();

        rvExpenses.setLayoutManager(
                new LinearLayoutManager(this));
    }

    private void listeners() {

        fabAddExpense.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            this,
                            AddExpenseActivity.class);

            startActivity(intent);

        });
        chipAll.setOnClickListener(v -> loadExpenses());

        chipFood.setOnClickListener(v ->
                filterCategory("Food"));

        chipTransport.setOnClickListener(v ->
                filterCategory("Transport"));

        chipShopping.setOnClickListener(v ->
                filterCategory("Shopping"));

        chipBills.setOnClickListener(v ->
                filterCategory("Bills"));

        btnFilter.setOnClickListener(v -> showFilterDialog());

        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start,
                                          int count,
                                          int after) {

            }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start,
                                      int before,
                                      int count) {

                searchExpense(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

    }
    private void filterCategory(String category) {

        if (currentUser == null)
            return;

        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid());

        adapter.updateList(

                databaseHelper.getExpensesByCategory(
                        userId,
                        category)
        );
        tvExpenseCount.setText(adapter.getItemCount() + " Transactions");

    }

    private void showFilterDialog() {

        BottomSheetDialog dialog =
                new BottomSheetDialog(this);

        View view = getLayoutInflater()
                .inflate(R.layout.dialog_filter_expense, null);

        dialog.setContentView(view);

        Spinner spCategory =
                view.findViewById(R.id.spCategoryFilter);

        Spinner spPayment =
                view.findViewById(R.id.spPaymentFilter);

        Spinner spMode =
                view.findViewById(R.id.spModeFilter);

        Spinner spSort =
                view.findViewById(R.id.spSort);

        Button btnApply =
                view.findViewById(R.id.btnApply);

        Button btnReset =
                view.findViewById(R.id.btnReset);

        loadFilterData(
                spCategory,
                spPayment,
                spMode,
                spSort
        );

        btnApply.setOnClickListener(v -> {

            applyFilters(

                    spCategory.getSelectedItem().toString(),

                    spPayment.getSelectedItem().toString(),

                    spMode.getSelectedItem().toString(),

                    spSort.getSelectedItem().toString()

            );

            dialog.dismiss();

        });

        btnReset.setOnClickListener(v -> {

            loadExpenses();

            dialog.dismiss();

        });

        dialog.show();

    }
    private void loadFilterData(

            Spinner spCategory,

            Spinner spPayment,

            Spinner spMode,

            Spinner spSort

    ) {

        ArrayList<String> categories =
                databaseHelper.getExpenseCategoryNames();

        categories.add(0, "All");

        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(

                        this,

                        android.R.layout.simple_spinner_dropdown_item,

                        categories

                );

        spCategory.setAdapter(categoryAdapter);

        ArrayList<String> methods =
                databaseHelper.getPaymentMethodNames();

        methods.add(0, "All");

        ArrayAdapter<String> paymentAdapter =
                new ArrayAdapter<>(

                        this,

                        android.R.layout.simple_spinner_dropdown_item,

                        methods

                );

        spPayment.setAdapter(paymentAdapter);

        ArrayList<String> modes = new ArrayList<>();

        modes.add("All");
        modes.add("Personal");
        modes.add("Family");

        spMode.setAdapter(

                new ArrayAdapter<>(

                        this,

                        android.R.layout.simple_spinner_dropdown_item,

                        modes

                )

        );

        ArrayList<String> sort = new ArrayList<>();

        sort.add("Newest");
        sort.add("Oldest");
        sort.add("Highest Amount");
        sort.add("Lowest Amount");

        spSort.setAdapter(

                new ArrayAdapter<>(

                        this,

                        android.R.layout.simple_spinner_dropdown_item,

                        sort

                )

        );

    }

    private void applyFilters(

            String category,

            String payment,

            String mode,

            String sort

    ) {

        if(currentUser == null)
            return;

        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid());

        ArrayList<ExpenseModel> filteredList =
                databaseHelper.filterExpenses(

                        userId,

                        category,

                        payment,

                        mode,

                        sort

                );

        adapter.updateList(filteredList);

        updateSummary();

    }

    private void loadExpenses() {

        if (currentUser == null)
            return;

        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid());

        expenseList =
                databaseHelper.getAllExpenses(userId);

        adapter = new ExpenseAdapter(

                this,

                expenseList,

                new ExpenseAdapter.OnExpenseClickListener() {


                    @Override
                    public void onExpenseClick(ExpenseModel expense) {

                        Intent intent = new Intent(
                                ExpenseListActivity.this,
                                ExpenseDetailsActivity.class);

                        intent.putExtra(
                                "transactionId",
                                expense.getTransactionId());

                        startActivity(intent);

                    }

                    @Override
                    public void onEditClick(ExpenseModel expense) {

                        Toast.makeText(
                                ExpenseListActivity.this,
                                "Edit Coming Soon",
                                Toast.LENGTH_SHORT
                        ).show();

                    }

                    @Override
                    public void onDeleteClick(ExpenseModel expense) {

                        databaseHelper.deleteExpense(
                                expense.getTransactionId());

                        loadExpenses();

                    }

                });

        rvExpenses.setAdapter(adapter);

        updateSummary();

    }

    private void updateSummary() {

        if (currentUser == null)
            return;

        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid());

        double total =
                databaseHelper.getTotalExpense(userId);

        int count =
                databaseHelper.getExpenseCount(userId);

        tvTotalExpense.setText(
                String.format("Rs. %.2f", total));

        tvExpenseCount.setText(
                count + " Transactions");

        if (count == 0) {

            layoutEmpty.setVisibility(View.VISIBLE);

            rvExpenses.setVisibility(View.GONE);

        } else {

            layoutEmpty.setVisibility(View.GONE);

            rvExpenses.setVisibility(View.VISIBLE);

        }

    }

    private void searchExpense(String keyword) {

        if (currentUser == null)
            return;

        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid());

        adapter.updateList(

                databaseHelper.searchExpenses(
                        userId,
                        keyword
                )

        );

    }

    private void setupBottomNavigation() {

        bottomNavigation.setSelectedItemId(R.id.nav_expenses);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_expenses) {

                return true;

            } else if (id == R.id.nav_dashboard) {

                Intent intent = new Intent(
                        ExpenseListActivity.this,
                        PersonalDashboardActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);

                return true;

            } else if (id == R.id.nav_profile) {

                Intent intent = new Intent(
                        ExpenseListActivity.this,
                        ProfileActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);

                return true;

            } else if (id == R.id.nav_subscriptions) {

                Intent intent = new Intent(
                        ExpenseListActivity.this,
                        SubscriptionActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);

                return true;

            } else if (id == R.id.nav_income) {

                Intent intent = new Intent(
                        ExpenseListActivity.this,
                        IncomeListActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);

                return true;

            }

            return false;

        });

    }

    @Override
    protected void onResume() {

        super.onResume();

        loadExpenses();

    }
}