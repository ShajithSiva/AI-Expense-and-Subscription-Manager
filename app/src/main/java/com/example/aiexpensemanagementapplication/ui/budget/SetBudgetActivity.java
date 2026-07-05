package com.example.aiexpensemanagementapplication.ui.budget;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;

public class SetBudgetActivity extends AppCompatActivity {

    private TextView btnBack, btnSaveTop, btnAddCategory;
    private TextView tvBudgetPeriod;
    private View tvStartDate;
    private EditText etTotalBudgetAmount;

    private TextView tvCategoryGroceries, tvCategoryRentUtilities, tvCategoryEntertainment;
    private EditText etGroceriesLimit, etRentUtilitiesLimit, etEntertainmentLimit;

    private TextView btnDeleteGroceries, btnDeleteRentUtilities, btnDeleteEntertainment;
    private TextView tvBudgetError, btnPersonalScope, btnFamilySharedScope;
    private TextView tvBudgetSuggestion, btnApplySuggestion, btnCreateBudget, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        bindViews();
        loadDefaultUiValues();
        setupClickEvents();
        setupAmountWatcher();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSaveTop = findViewById(R.id.btnSaveTop);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        tvBudgetPeriod = findViewById(R.id.tvBudgetPeriod);
        tvStartDate = findViewById(R.id.tvStartDate);

        etTotalBudgetAmount = findViewById(R.id.etTotalBudgetAmount);

        tvCategoryGroceries = findViewById(R.id.tvCategoryGroceries);
        tvCategoryRentUtilities = findViewById(R.id.tvCategoryRentUtilities);
        tvCategoryEntertainment = findViewById(R.id.tvCategoryEntertainment);

        etGroceriesLimit = findViewById(R.id.etGroceriesLimit);
        etRentUtilitiesLimit = findViewById(R.id.etRentUtilitiesLimit);
        etEntertainmentLimit = findViewById(R.id.etEntertainmentLimit);

        btnDeleteGroceries = findViewById(R.id.btnDeleteGroceries);
        btnDeleteRentUtilities = findViewById(R.id.btnDeleteRentUtilities);
        btnDeleteEntertainment = findViewById(R.id.btnDeleteEntertainment);

        tvBudgetError = findViewById(R.id.tvBudgetError);

        btnPersonalScope = findViewById(R.id.btnPersonalScope);
        btnFamilySharedScope = findViewById(R.id.btnFamilySharedScope);

        tvBudgetSuggestion = findViewById(R.id.tvBudgetSuggestion);
        btnApplySuggestion = findViewById(R.id.btnApplySuggestion);

        btnCreateBudget = findViewById(R.id.btnCreateBudget);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void loadDefaultUiValues() {

        // Placeholder values for UI phase only
        etTotalBudgetAmount.setText("");

        tvBudgetPeriod.setText("Monthly        ˅");

        tvCategoryGroceries.setText("Groceries &\nFood        ˅");
        tvCategoryRentUtilities.setText("Rent & Utilities        ˅");
        tvCategoryEntertainment.setText("Entertainment        ˅");

        etGroceriesLimit.setText("Rs 15000");
        etRentUtilitiesLimit.setText("Rs 25000");
        etEntertainmentLimit.setText("Rs 8000");

        tvBudgetError.setText(
                "Category limits exceed total budget (Rs 48,000 > Rs 45,000)"
        );

        tvBudgetSuggestion.setText(
                "Based on your past spending, suggested monthly budget is Rs 45,000."
        );
    }

    private void setupClickEvents() {

        btnBack.setOnClickListener(v -> finish());

        btnCancel.setOnClickListener(v -> finish());

        btnSaveTop.setOnClickListener(v -> validateBudget());

        btnCreateBudget.setOnClickListener(v -> validateBudget());

        btnAddCategory.setOnClickListener(v ->
                showToast("Add category clicked")
        );

        btnApplySuggestion.setOnClickListener(v -> {
            etTotalBudgetAmount.setText("45000");
            showToast("Suggestion applied");
        });

        btnDeleteGroceries.setOnClickListener(v ->
                showToast("Delete Groceries & Food"));

        btnDeleteRentUtilities.setOnClickListener(v ->
                showToast("Delete Rent & Utilities"));

        btnDeleteEntertainment.setOnClickListener(v ->
                showToast("Delete Entertainment"));

        tvBudgetPeriod.setOnClickListener(v ->
                showToast("Open period selector"));

        tvStartDate.setOnClickListener(v ->
                showToast("Open date picker"));

        btnPersonalScope.setOnClickListener(v ->
                showToast("Personal scope selected"));

        btnFamilySharedScope.setOnClickListener(v ->
                showToast("Family shared scope selected"));
    }

    private void setupAmountWatcher() {

        etTotalBudgetAmount.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                // Future implementation:
                // Validate category allocations here
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void validateBudget() {

        String totalBudget =
                etTotalBudgetAmount.getText().toString().trim();

        if (totalBudget.isEmpty()) {
            showToast("Please enter total budget amount");
            return;
        }

        showToast(
                "Budget UI validated. Ready for database integration later."
        );
    }

    private void showToast(String message) {
        Toast.makeText(
                SetBudgetActivity.this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}