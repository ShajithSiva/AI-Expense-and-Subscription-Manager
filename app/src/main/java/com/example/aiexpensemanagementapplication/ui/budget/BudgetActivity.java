package com.example.aiexpensemanagementapplication.ui.budget;

import android.os.Bundle;

import androidx.annotation.Nullable;
import android.view.View;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.aiexpensemanagementapplication.model.Budget;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;

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

    private DatabaseHelper databaseHelper;
    private int userId;

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

        databaseHelper = new DatabaseHelper(this);

        initializeUser();

        loadBudget();
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

        showLoading(true);

        //boolean success = databaseHelper.saveBudgetSettings(userId, budget);
        boolean success = true;

        showLoading(false);

        if (success) {

            updateSummary(budget);

            Toast.makeText(
                    this,
                    "Budget Saved Successfully",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "Failed to Save Budget",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private double getValue(TextInputEditText editText) {

        String value = editText.getText().toString().trim();

        if (value.isEmpty()) {
            return 0;
        }

        return Double.parseDouble(value);
    }

    private boolean validateInputs() {

        double monthlyBudget = getValue(etMonthlyBudget);

        if (monthlyBudget <= 0) {

            etMonthlyBudget.setError("Enter a valid monthly budget");
            etMonthlyBudget.requestFocus();
            return false;
        }

        double totalCategories =
                getValue(etFoodBudget)
                        + getValue(etTransportBudget)
                        + getValue(etShoppingBudget)
                        + getValue(etBillsBudget)
                        + getValue(etHealthBudget)
                        + getValue(etEducationBudget)
                        + getValue(etEntertainmentBudget)
                        + getValue(etOthersBudget);

        if (totalCategories > monthlyBudget) {

            Toast.makeText(
                    this,
                    "Category budgets exceed Monthly Budget",
                    Toast.LENGTH_LONG
            ).show();

            return false;
        }

        return true;
    }

    private void updateSummary(Budget budget) {

        double monthlyBudget = budget.getMonthlyBudget();

        double spent = databaseHelper.getTotalExpense(userId);

        double remaining = monthlyBudget - spent;

        if (remaining < 0)
            remaining = 0;

        tvMonthlyBudget.setText(
                "Budget : Rs. " + String.format("%.2f", monthlyBudget));

        tvSpentAmount.setText(
                "Spent : Rs. " + String.format("%.2f", spent));

        tvRemainingAmount.setText(
                "Remaining : Rs. " + String.format("%.2f", remaining));

        int percentage = 0;

        if (monthlyBudget > 0) {

            percentage = (int) ((spent / monthlyBudget) * 100);

            if (percentage > 100)
                percentage = 100;
        }

        progressBudget.setProgress(percentage);
    }

    private void loadBudget() {

        //Budget budget = databaseHelper.getBudgetSettings(userId);
        Budget budget = null;

        if (budget == null) {
            return;
        }

        etMonthlyBudget.setText(String.valueOf(budget.getMonthlyBudget()));

        etFoodBudget.setText(String.valueOf(budget.getFoodBudget()));
        etTransportBudget.setText(String.valueOf(budget.getTransportBudget()));
        etShoppingBudget.setText(String.valueOf(budget.getShoppingBudget()));
        etBillsBudget.setText(String.valueOf(budget.getBillsBudget()));
        etHealthBudget.setText(String.valueOf(budget.getHealthBudget()));
        etEducationBudget.setText(String.valueOf(budget.getEducationBudget()));
        etEntertainmentBudget.setText(String.valueOf(budget.getEntertainmentBudget()));
        etOthersBudget.setText(String.valueOf(budget.getOthersBudget()));

        updateSummary(budget);
    }

    private void initializeUser() {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {

            Toast.makeText(this,
                    "User not logged in",
                    Toast.LENGTH_SHORT).show();

            finish();
            return;
        }

        userId = databaseHelper.getUserIdByFirebaseUid(firebaseUser.getUid());

        if (userId == -1) {

            Toast.makeText(this,
                    "User not found",
                    Toast.LENGTH_SHORT).show();

            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadBudget();
    }

    private void showLoading(boolean show) {

        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);

        btnSaveBudget.setEnabled(!show);

    }


}

