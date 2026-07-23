package com.example.aiexpensemanagementapplication.ui.profile;

import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BudgetPeriodActivity extends AppCompatActivity {

    private MaterialToolbar toolbarBudgetPeriod;

    private TextView tvCurrentBudgetPeriod;
    private TextView tvBudgetPeriodDescription;

    private RadioGroup radioGroupBudgetPeriod;

    private MaterialRadioButton radioWeekly;
    private MaterialRadioButton radioMonthly;
    private MaterialRadioButton radioQuarterly;
    private MaterialRadioButton radioYearly;

    private MaterialButton btnSaveBudgetPeriod;

    private DatabaseHelper databaseHelper;

    private FirebaseUser currentUser;

    private int currentUserId = -1;

    private String savedBudgetPeriod = "Monthly";
    private String selectedBudgetPeriod = "Monthly";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_period);

        initializeViews();

        initializeServices();

        loadCurrentUser();

        setupToolbar();

        loadSavedBudgetPeriod();

        setupPeriodSelection();

        setupSaveButton();
    }

    private void initializeViews() {

        toolbarBudgetPeriod =
                findViewById(R.id.toolbarBudgetPeriod);

        tvCurrentBudgetPeriod =
                findViewById(R.id.tvCurrentBudgetPeriod);

        tvBudgetPeriodDescription =
                findViewById(R.id.tvBudgetPeriodDescription);

        radioGroupBudgetPeriod =
                findViewById(R.id.radioGroupBudgetPeriod);

        radioWeekly =
                findViewById(R.id.radioWeekly);

        radioMonthly =
                findViewById(R.id.radioMonthly);

        radioQuarterly =
                findViewById(R.id.radioQuarterly);

        radioYearly =
                findViewById(R.id.radioYearly);

        btnSaveBudgetPeriod =
                findViewById(R.id.btnSaveBudgetPeriod);
    }

    private void initializeServices() {

        databaseHelper = new DatabaseHelper(this);

        currentUser =
                FirebaseAuth.getInstance().getCurrentUser();
    }

    private void loadCurrentUser() {

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "User session not found. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        currentUserId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid()
                );

        if (currentUserId == -1) {

            Toast.makeText(
                    this,
                    "Local user profile not found.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
        }
    }

    private void setupToolbar() {

        toolbarBudgetPeriod.setNavigationOnClickListener(
                view -> finish()
        );
    }

    private void loadSavedBudgetPeriod() {

        if (currentUserId == -1) {
            return;
        }

        savedBudgetPeriod =
                databaseHelper.getSavedBudgetPeriod(
                        currentUserId
                );

        if (savedBudgetPeriod == null
                || savedBudgetPeriod.trim().isEmpty()) {

            savedBudgetPeriod = "Monthly";
        }

        selectedBudgetPeriod = savedBudgetPeriod;

        tvCurrentBudgetPeriod.setText(
                savedBudgetPeriod
        );

        checkSavedPeriod(savedBudgetPeriod);

        updateDescription(savedBudgetPeriod);

        btnSaveBudgetPeriod.setEnabled(false);
    }

    private void checkSavedPeriod(String budgetPeriod) {

        if ("Weekly".equalsIgnoreCase(budgetPeriod)) {

            radioWeekly.setChecked(true);

        } else if ("Quarterly".equalsIgnoreCase(budgetPeriod)) {

            radioQuarterly.setChecked(true);

        } else if ("Yearly".equalsIgnoreCase(budgetPeriod)) {

            radioYearly.setChecked(true);

        } else {

            radioMonthly.setChecked(true);
        }
    }

    private void setupPeriodSelection() {

        radioGroupBudgetPeriod.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (checkedId == R.id.radioWeekly) {

                        selectedBudgetPeriod = "Weekly";

                    } else if (checkedId == R.id.radioMonthly) {

                        selectedBudgetPeriod = "Monthly";

                    } else if (checkedId == R.id.radioQuarterly) {

                        selectedBudgetPeriod = "Quarterly";

                    } else if (checkedId == R.id.radioYearly) {

                        selectedBudgetPeriod = "Yearly";
                    }

                    tvCurrentBudgetPeriod.setText(
                            selectedBudgetPeriod
                    );

                    updateDescription(
                            selectedBudgetPeriod
                    );

                    btnSaveBudgetPeriod.setEnabled(
                            !selectedBudgetPeriod.equalsIgnoreCase(
                                    savedBudgetPeriod
                            )
                    );
                }
        );
    }

    private void updateDescription(String budgetPeriod) {

        String description;

        switch (budgetPeriod) {

            case "Weekly":
                description =
                        "Your budgets and spending summaries will reset every week.";
                break;

            case "Quarterly":
                description =
                        "Your budgets and spending summaries will be organized every three months.";
                break;

            case "Yearly":
                description =
                        "Your budgets and spending summaries will be organized for the full year.";
                break;

            case "Monthly":
            default:
                description =
                        "Your budgets and spending summaries will reset every month.";
                break;
        }

        tvBudgetPeriodDescription.setText(description);
    }

    private void setupSaveButton() {

        btnSaveBudgetPeriod.setOnClickListener(
                view -> saveBudgetPeriod()
        );
    }

    private void saveBudgetPeriod() {

        if (currentUserId == -1) {

            Toast.makeText(
                    this,
                    "User profile not available.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (selectedBudgetPeriod == null
                || selectedBudgetPeriod.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select a budget period.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnSaveBudgetPeriod.setEnabled(false);

        boolean saved =
                databaseHelper.saveBudgetPeriod(
                        currentUserId,
                        selectedBudgetPeriod
                );

        if (saved) {

            savedBudgetPeriod =
                    selectedBudgetPeriod;

            Toast.makeText(
                    this,
                    "Budget period updated successfully.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            btnSaveBudgetPeriod.setEnabled(true);

            Toast.makeText(
                    this,
                    "Failed to save budget period.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}