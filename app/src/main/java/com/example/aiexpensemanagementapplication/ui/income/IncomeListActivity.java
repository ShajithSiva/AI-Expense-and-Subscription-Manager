package com.example.aiexpensemanagementapplication.ui.income;

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
import com.example.aiexpensemanagementapplication.ui.dashboard.PersonalDashboardActivity;
import com.example.aiexpensemanagementapplication.ui.expense.ExpenseListActivity;
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

public class IncomeListActivity extends AppCompatActivity {

    private RecyclerView rvIncome;

    private IncomeAdapter adapter;

    private ArrayList<IncomeModel> incomeList;

    private DatabaseHelper databaseHelper;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private TextView tvTotalIncome;
    private TextView tvIncomeCount;

    private LinearLayout layoutEmpty;

    private ExtendedFloatingActionButton fabAddIncome;

    private EditText etSearch;

    private Chip chipAll;
    private Chip chipSalary;
    private Chip chipBusiness;
    private Chip chipInvestment;
    private Chip chipGift;

    private ImageButton btnFilter;
    private ImageButton btnMore;

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_income_list);

        initialize();

        setupRecyclerView();

        setListeners();

        setupBottomNavigation();
    }

    private void initialize() {

        databaseHelper = new DatabaseHelper(this);

        firebaseAuth = FirebaseAuth.getInstance();

        currentUser = firebaseAuth.getCurrentUser();

        rvIncome = findViewById(R.id.rvIncome);

        tvTotalIncome = findViewById(R.id.tvTotalIncome);

        tvIncomeCount = findViewById(R.id.tvIncomeCount);

        layoutEmpty = findViewById(R.id.layoutEmpty);

        fabAddIncome = findViewById(R.id.fabAddIncome);

        etSearch = findViewById(R.id.etSearch);

        chipAll = findViewById(R.id.chipAll);

        chipSalary = findViewById(R.id.chipSalary);

        chipBusiness = findViewById(R.id.chipBusiness);

        chipInvestment = findViewById(R.id.chipInvestment);

        chipGift = findViewById(R.id.chipGift);

        btnFilter = findViewById(R.id.btnFilter);

        btnMore = findViewById(R.id.btnMore);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        incomeList = new ArrayList<>();
    }

    private void setupRecyclerView() {

        rvIncome.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new IncomeAdapter(
                this,
                incomeList,
                new IncomeAdapter.OnIncomeClickListener() {

                    @Override
                    public void onIncomeClick(IncomeModel income) {

                        openIncomeDetails(
                                income.getTransactionId()
                        );
                    }

                    @Override
                    public void onEditClick(IncomeModel income) {

                        openEditIncome(
                                income.getTransactionId()
                        );
                    }

                    @Override
                    public void onDeleteClick(IncomeModel income) {

                        showDeleteConfirmation(income);
                    }
                }
        );

        rvIncome.setAdapter(adapter);
    }

    private void setListeners() {

        fabAddIncome.setOnClickListener(view -> {

            Intent intent = new Intent(
                    IncomeListActivity.this,
                    AddIncomeActivity.class
            );

            startActivity(intent);
        });

        chipAll.setOnClickListener(
                view -> loadIncome()
        );

        chipSalary.setOnClickListener(
                view -> filterCategory("Salary")
        );

        chipBusiness.setOnClickListener(
                view -> filterCategory("Business")
        );

        chipInvestment.setOnClickListener(
                view -> filterCategory("Investment")
        );

        chipGift.setOnClickListener(
                view -> filterCategory("Gift")
        );

        btnFilter.setOnClickListener(
                view -> showFilterDialog()
        );

        btnMore.setOnClickListener(view -> {

            Toast.makeText(
                    this,
                    "More options coming soon.",
                    Toast.LENGTH_SHORT
            ).show();
        });

        etSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence charSequence,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence charSequence,
                            int start,
                            int before,
                            int count) {

                        searchIncome(
                                charSequence.toString()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable editable) {
                    }
                }
        );
    }

    private int getCurrentUserId() {

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "User not logged in.",
                    Toast.LENGTH_SHORT
            ).show();

            return -1;
        }

        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid()
                );

        if (userId == -1) {

            Toast.makeText(
                    this,
                    "User record not found.",
                    Toast.LENGTH_SHORT
            ).show();
        }

        return userId;
    }

    private void loadIncome() {

        int userId = getCurrentUserId();

        if (userId == -1) {
            return;
        }

        incomeList =
                databaseHelper.getAllIncome(userId);

        adapter.updateList(incomeList);

        updateSummary();
    }

    private void filterCategory(String category) {

        int userId = getCurrentUserId();

        if (userId == -1) {
            return;
        }

        ArrayList<IncomeModel> filteredList =
                databaseHelper.getIncomeByCategory(
                        userId,
                        category
                );

        adapter.updateList(filteredList);

        updateVisibleListState(filteredList.size());
    }

    private void searchIncome(String keyword) {

        int userId = getCurrentUserId();

        if (userId == -1) {
            return;
        }

        ArrayList<IncomeModel> searchResults =
                databaseHelper.searchIncome(
                        userId,
                        keyword.trim()
                );

        adapter.updateList(searchResults);

        updateVisibleListState(searchResults.size());
    }

    private void updateSummary() {

        int userId = getCurrentUserId();

        if (userId == -1) {
            return;
        }

        double totalIncome =
                databaseHelper.getTotalIncome(userId);

        int incomeCount =
                databaseHelper.getIncomeCount(userId);

        tvTotalIncome.setText(
                String.format(
                        Locale.getDefault(),
                        "Rs. %.2f",
                        totalIncome
                )
        );

        tvIncomeCount.setText(
                incomeCount + " Transactions"
        );

        updateVisibleListState(incomeCount);
    }

    private void updateVisibleListState(int count) {

        if (count == 0) {

            layoutEmpty.setVisibility(View.VISIBLE);

            rvIncome.setVisibility(View.GONE);

        } else {

            layoutEmpty.setVisibility(View.GONE);

            rvIncome.setVisibility(View.VISIBLE);
        }
    }

    private void openIncomeDetails(int transactionId) {

        Intent intent = new Intent(
                IncomeListActivity.this,
                IncomeDetailsActivity.class
        );

        intent.putExtra(
                "transactionId",
                transactionId
        );

        startActivity(intent);
    }

    private void openEditIncome(int transactionId) {

        Intent intent = new Intent(
                IncomeListActivity.this,
                EditIncomeActivity.class
        );

        intent.putExtra(
                "transactionId",
                transactionId
        );

        startActivity(intent);
    }

    private void showDeleteConfirmation(
            IncomeModel income) {

        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Income")
                .setMessage(
                        "Are you sure you want to delete this income?\n\n" +
                                "This action cannot be undone."
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            deleteIncome(
                                    income.getTransactionId()
                            );
                        }
                )
                .show();
    }

    private void deleteIncome(int transactionId) {

        int result =
                databaseHelper.deleteTransaction(
                        transactionId
                );

        if (result > 0) {

            Toast.makeText(
                    this,
                    "Income deleted successfully.",
                    Toast.LENGTH_SHORT
            ).show();

            loadIncome();

        } else {

            Toast.makeText(
                    this,
                    "Failed to delete income.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void showFilterDialog() {

        BottomSheetDialog dialog =
                new BottomSheetDialog(this);

        /*
         * Temporary-ah expense filter layout reuse pannrom.
         * IDs common-ah irukkurathala compile and work aagum.
         */
        View view = getLayoutInflater().inflate(
                R.layout.dialog_filter_expense,
                null
        );

        dialog.setContentView(view);

        Spinner spCategory =
                view.findViewById(
                        R.id.spCategoryFilter
                );

        Spinner spIncomeSource =
                view.findViewById(
                        R.id.spPaymentFilter
                );

        Spinner spIncomeMode =
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
                spIncomeSource,
                spIncomeMode,
                spSort
        );

        btnApply.setOnClickListener(view1 -> {

            String category =
                    spCategory.getSelectedItem()
                            .toString();

            String incomeSource =
                    spIncomeSource.getSelectedItem()
                            .toString();

            String incomeMode =
                    spIncomeMode.getSelectedItem()
                            .toString();

            String sort =
                    spSort.getSelectedItem()
                            .toString();

            applyFilters(
                    category,
                    incomeSource,
                    incomeMode,
                    sort
            );

            dialog.dismiss();
        });

        btnReset.setOnClickListener(view1 -> {

            chipAll.setChecked(true);

            etSearch.setText("");

            loadIncome();

            dialog.dismiss();
        });

        dialog.show();
    }

    private void loadFilterData(
            Spinner spCategory,
            Spinner spIncomeSource,
            Spinner spIncomeMode,
            Spinner spSort) {

        ArrayList<String> categories =
                databaseHelper.getIncomeCategoryNames();

        categories.add(0, "All");

        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_spinner_dropdown_item,
                        categories
                );

        spCategory.setAdapter(categoryAdapter);

        ArrayList<String> sources =
                databaseHelper.getPaymentMethodNames();

        sources.add(0, "All");

        ArrayAdapter<String> sourceAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_spinner_dropdown_item,
                        sources
                );

        spIncomeSource.setAdapter(sourceAdapter);

        ArrayList<String> modes =
                new ArrayList<>();

        modes.add("All");
        modes.add("Personal");
        modes.add("Family");

        ArrayAdapter<String> modeAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_spinner_dropdown_item,
                        modes
                );

        spIncomeMode.setAdapter(modeAdapter);

        ArrayList<String> sortingOptions =
                new ArrayList<>();

        sortingOptions.add("Newest");
        sortingOptions.add("Oldest");
        sortingOptions.add("Highest Amount");
        sortingOptions.add("Lowest Amount");

        ArrayAdapter<String> sortAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_spinner_dropdown_item,
                        sortingOptions
                );

        spSort.setAdapter(sortAdapter);
    }

    private void applyFilters(
            String category,
            String incomeSource,
            String incomeMode,
            String sort) {

        int userId = getCurrentUserId();

        if (userId == -1) {
            return;
        }

        ArrayList<IncomeModel> filteredList =
                databaseHelper.filterIncome(
                        userId,
                        category,
                        incomeSource,
                        incomeMode,
                        sort
                );

        adapter.updateList(filteredList);

        updateVisibleListState(
                filteredList.size()
        );
    }

    private void setupBottomNavigation() {

        bottomNavigation.setSelectedItemId(R.id.nav_income);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_income) {

                return true;

            } else if (id == R.id.nav_expenses) {

                Intent intent = new Intent(
                        IncomeListActivity.this,
                        ExpenseListActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);

                return true;

            }else if (id == R.id.nav_subscriptions) {

                Intent intent = new Intent(
                        IncomeListActivity.this,
                        SubscriptionActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);

                return true;

            }   else if (id == R.id.nav_profile) {

                Intent intent = new Intent(
                        IncomeListActivity.this,
                        ProfileActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);

                return true;

            } else if (id == R.id.nav_dashboard) {

                Intent intent = new Intent(
                        IncomeListActivity.this,
                        PersonalDashboardActivity.class);

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

        loadIncome();
    }
}