package com.example.aiexpensemanagementapplication.ui.budget;

import android.os.Bundle;

import androidx.annotation.Nullable;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.example.aiexpensemanagementapplication.model.Budget;

import com.example.aiexpensemanagementapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import android.widget.TextView;

public class BudgetActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;

    private TextView tvMonthlyBudget;
    private TextView tvSpentAmount;
    private TextView tvRemainingAmount;

    private LinearProgressIndicator progressBudget;
    private CircularProgressIndicator progressIndicator;

    private TextInputEditText etMonthlyBudget;

    private TextInputEditText etFoodBudget;
    private TextInputEditText etTransportBudget;
    private TextInputEditText etShoppingBudget;
    private TextInputEditText etBillsBudget;
    private TextInputEditText etHealthBudget;
    private TextInputEditText etEducationBudget;
    private TextInputEditText etEntertainmentBudget;
    private TextInputEditText etOthersBudget;

    private MaterialButton btnSaveBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_budget);

        initializeViews();

        setupToolbar();

        btnSaveBudget.setOnClickListener(v -> saveBudget());
    }

    private void initializeViews() {

        toolbar = findViewById(R.id.toolbar);

        tvMonthlyBudget = findViewById(R.id.tvMonthlyBudget);
        tvSpentAmount = findViewById(R.id.tvSpentAmount);
        tvRemainingAmount = findViewById(R.id.tvRemainingAmount);

        progressBudget = findViewById(R.id.progressBudget);
        progressIndicator = findViewById(R.id.progressIndicator);

        etMonthlyBudget = findViewById(R.id.etMonthlyBudget);

        etFoodBudget = findViewById(R.id.etFoodBudget);
        etTransportBudget = findViewById(R.id.etTransportBudget);
        etShoppingBudget = findViewById(R.id.etShoppingBudget);
        etBillsBudget = findViewById(R.id.etBillsBudget);
        etHealthBudget = findViewById(R.id.etHealthBudget);
        etEducationBudget = findViewById(R.id.etEducationBudget);
        etEntertainmentBudget = findViewById(R.id.etEntertainmentBudget);
        etOthersBudget = findViewById(R.id.etOthersBudget);

        btnSaveBudget = findViewById(R.id.btnSaveBudget);
    }

    private void setupToolbar() {

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void saveBudget() {

        if (!validateInputs()) {
            return;
        }

        Budget budget = new Budget();

        budget.setMonthlyBudget(getValue(etMonthlyBudget));

        budget.setFoodBudget(getValue(etFoodBudget));
        budget.setTransportBudget(getValue(etTransportBudget));
        budget.setShoppingBudget(getValue(etShoppingBudget));
        budget.setBillsBudget(getValue(etBillsBudget));
        budget.setHealthBudget(getValue(etHealthBudget));
        budget.setEducationBudget(getValue(etEducationBudget));
        budget.setEntertainmentBudget(getValue(etEntertainmentBudget));
        budget.setOthersBudget(getValue(etOthersBudget));

        updateSummary(budget);

        Toast.makeText(this,
                "Budget Ready",
                Toast.LENGTH_SHORT).show();
    }

    private double getValue(TextInputEditText editText) {

        String value = editText.getText().toString().trim();

        if (value.isEmpty()) {
            return 0;
        }

        return Double.parseDouble(value);
    }

    private boolean validateInputs() {

        if (etMonthlyBudget.getText().toString().trim().isEmpty()) {

            etMonthlyBudget.setError("Enter Monthly Budget");

            etMonthlyBudget.requestFocus();

            return false;
        }

        return true;
    }

    private void updateSummary(Budget budget) {

        double monthly = budget.getMonthlyBudget();

        double spent = 0;

        double remaining = monthly - spent;

        tvMonthlyBudget.setText(
                "Budget : Rs. " + monthly);

        tvSpentAmount.setText(
                "Spent : Rs. " + spent);

        tvRemainingAmount.setText(
                "Remaining : Rs. " + remaining);

        progressBudget.setProgress(0);
    }


}

