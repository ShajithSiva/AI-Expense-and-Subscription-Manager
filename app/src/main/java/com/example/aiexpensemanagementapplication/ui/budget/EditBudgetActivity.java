package com.example.aiexpensemanagementapplication.ui.budget;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;

public class EditBudgetActivity extends AppCompatActivity {

    private TextView btnBack, btnSaveTop, btnInfo;
    private TextView tvCategoryIcon, tvCategoryName;
    private EditText etBudgetLimit;

    private TextView tvSafeZone, tvUsedPercent, tvSpentAmount, tvRemainingAmount;
    private ProgressBar progressSpending;

    private TextView tvSmartSuggestion, btnApplySuggestion;
    private TextView btnSaveChanges, btnRemoveCategoryBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_budget);

        bindViews();
        loadPlaceholderData();
        setupClicks();
        setupBudgetLimitWatcher();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSaveTop = findViewById(R.id.btnSaveTop);
        btnInfo = findViewById(R.id.btnInfo);

        tvCategoryIcon = findViewById(R.id.tvCategoryIcon);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        etBudgetLimit = findViewById(R.id.etBudgetLimit);

        tvSafeZone = findViewById(R.id.tvSafeZone);
        tvUsedPercent = findViewById(R.id.tvUsedPercent);
        tvSpentAmount = findViewById(R.id.tvSpentAmount);
        tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
        progressSpending = findViewById(R.id.progressSpending);

        tvSmartSuggestion = findViewById(R.id.tvSmartSuggestion);
        btnApplySuggestion = findViewById(R.id.btnApplySuggestion);

        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnRemoveCategoryBudget = findViewById(R.id.btnRemoveCategoryBudget);
    }

    private void loadPlaceholderData() {
        /*
         * Later Room DB can replace these values using budgetId/categoryId.
         */

        tvCategoryIcon.setText("🍴");
        tvCategoryName.setText("Food & Dining");

        etBudgetLimit.setText("8000");

        tvSafeZone.setText("○ Safe Zone");
        tvUsedPercent.setText("68%");
        tvSpentAmount.setText("Rs 5,440");
        tvRemainingAmount.setText("Rs 2,560");
        progressSpending.setProgress(68);

        tvSmartSuggestion.setText("Based on your spending patterns from the last 3 months, your suggested limit is Rs 9,500.");
    }

    private void setupClicks() {
        btnBack.setOnClickListener(v -> finish());

        btnSaveTop.setOnClickListener(v -> validateAndSaveBudget());
        btnSaveChanges.setOnClickListener(v -> validateAndSaveBudget());

        btnInfo.setOnClickListener(v ->
                showToast("Budget limit is your monthly spending cap for this category.")
        );

        btnApplySuggestion.setOnClickListener(v -> {
            etBudgetLimit.setText("9500");
            showToast("Suggested limit applied");
        });

        btnRemoveCategoryBudget.setOnClickListener(v ->
                showToast("Open delete budget confirmation screen")
        );
    }

    private void setupBudgetLimitWatcher() {
        etBudgetLimit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Later: enable save button / recalculate remaining amount.
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void validateAndSaveBudget() {
        String limit = etBudgetLimit.getText().toString().trim();

        if (limit.isEmpty()) {
            showToast("Please enter budget limit");
            return;
        }

        showToast("Budget limit ready to save using Room DB later.");
    }

    private void showToast(String message) {
        Toast.makeText(EditBudgetActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}