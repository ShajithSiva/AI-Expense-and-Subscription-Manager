package com.example.aiexpensemanagementapplication.ui.budget;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class BudgetActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageButton btnAddBudget;

    private TextView tvCurrentMonth;
    private TextView tvTotalBudget;
    private TextView tvSpent;
    private TextView tvRemaining;
    private TextView tvBudgetPercentage;

    private TextView tvBudgetStatus;
    private TextView tvBudgetStatusMessage;

    private TextView tvCarryForwardAmount;

    private LinearProgressIndicator budgetProgress;

    private RecyclerView recyclerCategoryBudgets;

    private SwitchMaterial switchCarryForward;

    private CategoryBudgetAdapter adapter;

    private List<CategoryBudget> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_budget
        );

        initializeViews();

        setupRecyclerView();

        setupListeners();

        loadBudgetData();
    }

    private void initializeViews() {

        btnBack =
                findViewById(R.id.btnBack);

        btnAddBudget =
                findViewById(R.id.btnAddBudget);

        tvCurrentMonth =
                findViewById(R.id.tvCurrentMonth);

        tvTotalBudget =
                findViewById(R.id.tvTotalBudget);

        tvSpent =
                findViewById(R.id.tvSpent);

        tvRemaining =
                findViewById(R.id.tvRemaining);

        tvBudgetPercentage =
                findViewById(R.id.tvBudgetPercentage);

        tvBudgetStatus =
                findViewById(R.id.tvBudgetStatus);

        tvBudgetStatusMessage =
                findViewById(
                        R.id.tvBudgetStatusMessage
                );

        tvCarryForwardAmount =
                findViewById(
                        R.id.tvCarryForwardAmount
                );

        budgetProgress =
                findViewById(
                        R.id.budgetProgress
                );

        recyclerCategoryBudgets =
                findViewById(
                        R.id.recyclerCategoryBudgets
                );

        switchCarryForward =
                findViewById(
                        R.id.switchCarryForward
                );
    }

    private void setupRecyclerView() {

        categoryList =
                new ArrayList<>();

        adapter =
                new CategoryBudgetAdapter(
                        categoryList
                );

        recyclerCategoryBudgets.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerCategoryBudgets.setAdapter(
                adapter
        );
    }

    private void setupListeners() {

        btnBack.setOnClickListener(
                v -> finish()
        );

        btnAddBudget.setOnClickListener(v -> {

            Intent intent = new Intent(
                    BudgetActivity.this,
                    AddBudgetActivity.class
            );

            startActivity(intent);
        });

        switchCarryForward.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        Toast.makeText(
                                this,
                                "Unused budget will be carried forward",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                "Carry forward disabled",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void loadBudgetData() {

        // Temporary data for UI testing.
        // This will be replaced with DatabaseHelper data.

        tvTotalBudget.setText(
                "Rs. 50,000.00"
        );

        tvSpent.setText(
                "Rs. 25,000.00"
        );

        tvRemaining.setText(
                "Rs. 25,000.00"
        );

        budgetProgress.setProgress(
                50
        );

        tvBudgetPercentage.setText(
                "50% used"
        );

        tvBudgetStatus.setText(
                "You're within your budget"
        );

        tvBudgetStatusMessage.setText(
                "You have Rs. 25,000.00 remaining this month."
        );

        tvCarryForwardAmount.setText(
                "Unused budget: Rs. 25,000.00"
        );

        categoryList.clear();

        categoryList.add(
                new CategoryBudget(
                        "Food",
                        10000,
                        5000
                )
        );

        categoryList.add(
                new CategoryBudget(
                        "Transport",
                        5000,
                        2500
                )
        );

        categoryList.add(
                new CategoryBudget(
                        "Shopping",
                        10000,
                        8000
                )
        );

        adapter.notifyDataSetChanged();
    }
}