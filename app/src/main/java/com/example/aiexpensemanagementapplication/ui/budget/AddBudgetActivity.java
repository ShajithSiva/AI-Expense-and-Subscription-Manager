package com.example.aiexpensemanagementapplication.ui.budget;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AddBudgetActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private TextInputEditText etMonth;
    private TextInputEditText etTotalBudget;

    private SwitchMaterial switchCarryForward;

    private LinearLayout categoryContainer;

    private MaterialButton btnAddCategory;
    private MaterialButton btnSaveBudget;

    private CircularProgressIndicator progressBar;

    private final Calendar selectedMonth =
            Calendar.getInstance();

    private final ArrayList<String> categories =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_add_budget
        );

        initializeViews();

        setupCategoryList();

        setupListeners();

        setCurrentMonth();
    }

    private void initializeViews() {

        btnBack =
                findViewById(R.id.btnBack);

        etMonth =
                findViewById(R.id.etMonth);

        etTotalBudget =
                findViewById(R.id.etTotalBudget);

        switchCarryForward =
                findViewById(
                        R.id.switchCarryForward
                );

        categoryContainer =
                findViewById(
                        R.id.categoryContainer
                );

        btnAddCategory =
                findViewById(
                        R.id.btnAddCategory
                );

        btnSaveBudget =
                findViewById(
                        R.id.btnSaveBudget
                );

        progressBar =
                findViewById(
                        R.id.progressBar
                );
    }

    private void setupCategoryList() {

        categories.add("Food");
        categories.add("Transport");
        categories.add("Shopping");
        categories.add("Bills");
        categories.add("Entertainment");
        categories.add("Health");
        categories.add("Education");
        categories.add("Other");
    }

    private void setupListeners() {

        btnBack.setOnClickListener(
                v -> finish()
        );

        etMonth.setOnClickListener(
                v -> showMonthPicker()
        );

        btnAddCategory.setOnClickListener(
                v -> addCategoryRow()
        );

        btnSaveBudget.setOnClickListener(
                v -> saveBudget()
        );
    }

    private void setCurrentMonth() {

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.getDefault()
                );

        etMonth.setText(
                formatter.format(
                        selectedMonth.getTime()
                )
        );
    }

    private void showMonthPicker() {

        int year =
                selectedMonth.get(
                        Calendar.YEAR
                );

        int month =
                selectedMonth.get(
                        Calendar.MONTH
                );

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, selectedYear, selectedMonthValue, day) -> {

                            selectedMonth.set(
                                    Calendar.YEAR,
                                    selectedYear
                            );

                            selectedMonth.set(
                                    Calendar.MONTH,
                                    selectedMonthValue
                            );

                            SimpleDateFormat formatter =
                                    new SimpleDateFormat(
                                            "MMMM yyyy",
                                            Locale.getDefault()
                                    );

                            etMonth.setText(
                                    formatter.format(
                                            selectedMonth.getTime()
                                    )
                            );
                        },
                        year,
                        month,
                        1
                );

        dialog.show();
    }

    private void addCategoryRow() {

        View categoryView =
                LayoutInflater.from(this).inflate(
                        R.layout.item_budget_category,
                        categoryContainer,
                        false
                );

        AutoCompleteTextView actvCategory =
                categoryView.findViewById(
                        R.id.actvCategory
                );

        ImageButton btnRemoveCategory =
                categoryView.findViewById(
                        R.id.btnRemoveCategory
                );

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        categories
                );

        actvCategory.setAdapter(adapter);

        actvCategory.setOnClickListener(
                v -> actvCategory.showDropDown()
        );

        btnRemoveCategory.setOnClickListener(
                v -> categoryContainer.removeView(
                        categoryView
                )
        );

        categoryContainer.addView(
                categoryView
        );
    }

    private void saveBudget() {

        String month =
                etMonth.getText()
                        .toString()
                        .trim();

        String totalBudgetText =
                etTotalBudget.getText()
                        .toString()
                        .trim();

        if (month.isEmpty()) {

            etMonth.setError(
                    "Select budget month"
            );

            return;
        }

        if (totalBudgetText.isEmpty()) {

            etTotalBudget.setError(
                    "Enter total budget"
            );

            return;
        }

        double totalBudget;

        try {

            totalBudget =
                    Double.parseDouble(
                            totalBudgetText
                    );

        } catch (NumberFormatException e) {

            etTotalBudget.setError(
                    "Enter a valid amount"
            );

            return;
        }

        if (totalBudget <= 0) {

            etTotalBudget.setError(
                    "Budget must be greater than zero"
            );

            return;
        }

        if (!validateCategoryBudgets(
                totalBudget)) {

            return;
        }

        boolean carryForward =
                switchCarryForward.isChecked();

        // Database saving will be added
        // after connecting your DatabaseHelper.

        Toast.makeText(
                this,
                "Budget details validated successfully",
                Toast.LENGTH_SHORT
        ).show();
    }

    private boolean validateCategoryBudgets(
            double totalBudget) {

        double categoryTotal = 0;

        Set<String> selectedCategories =
                new HashSet<>();

        for (int i = 0;
             i < categoryContainer.getChildCount();
             i++) {

            View categoryView =
                    categoryContainer.getChildAt(i);

            AutoCompleteTextView category =
                    categoryView.findViewById(
                            R.id.actvCategory
                    );

            TextInputEditText amount =
                    categoryView.findViewById(
                            R.id.etCategoryBudget
                    );

            String categoryName =
                    category.getText()
                            .toString()
                            .trim();

            String amountText =
                    amount.getText()
                            .toString()
                            .trim();

            if (categoryName.isEmpty()) {

                category.setError(
                        "Select a category"
                );

                return false;
            }

            if (amountText.isEmpty()) {

                amount.setError(
                        "Enter amount"
                );

                return false;
            }

            if (selectedCategories.contains(
                    categoryName)) {

                category.setError(
                        "Category already added"
                );

                return false;
            }

            double categoryAmount;

            try {

                categoryAmount =
                        Double.parseDouble(
                                amountText
                        );

            } catch (NumberFormatException e) {

                amount.setError(
                        "Enter a valid amount"
                );

                return false;
            }

            if (categoryAmount <= 0) {

                amount.setError(
                        "Amount must be greater than zero"
                );

                return false;
            }

            selectedCategories.add(
                    categoryName
            );

            categoryTotal +=
                    categoryAmount;
        }

        if (categoryTotal > totalBudget) {

            Toast.makeText(
                    this,
                    "Category budgets cannot exceed the total monthly budget",
                    Toast.LENGTH_LONG
            ).show();

            return false;
        }

        return true;
    }
}