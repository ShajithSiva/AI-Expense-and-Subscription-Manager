package com.example.aiexpensemanagementapplication.ui.budget;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;

public class BudgetActivity extends AppCompatActivity {

    private TextView tvMonthlyBudget, tvBudgetUsedPercent, tvBudgetStatus;
    private TextView tvSpentAmount, tvRemainingAmount, tvBudgetInsight;
    private ProgressBar progressMonthlyBudget;

    private View cardFoodBudget, cardTransportBudget, cardUtilitiesBudget, cardShoppingBudget, cardHousingBudget;
    private TextView btnBack, btnViewAllCategories, btnAddBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        bindViews();
        loadPlaceholderBudgetData();
        setupClicks();
    }

    private void bindViews() {
        tvMonthlyBudget = findViewById(R.id.tvMonthlyBudget);
        tvBudgetUsedPercent = findViewById(R.id.tvBudgetUsedPercent);
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus);
        tvSpentAmount = findViewById(R.id.tvSpentAmount);
        tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
        tvBudgetInsight = findViewById(R.id.tvBudgetInsight);
        progressMonthlyBudget = findViewById(R.id.progressMonthlyBudget);

        cardFoodBudget = findViewById(R.id.cardFoodBudget);
        cardTransportBudget = findViewById(R.id.cardTransportBudget);
        cardUtilitiesBudget = findViewById(R.id.cardUtilitiesBudget);
        cardShoppingBudget = findViewById(R.id.cardShoppingBudget);
        cardHousingBudget = findViewById(R.id.cardHousingBudget);

        btnBack = findViewById(R.id.btnBack);
        btnViewAllCategories = findViewById(R.id.btnViewAllCategories);
        btnAddBudget = findViewById(R.id.btnAddBudget);
    }

    private void loadPlaceholderBudgetData() {
        /*
         * Later your teammate can replace this method with Room DB values.
         * Required Room data:
         * monthlyBudget, spentAmount, remainingAmount, usedPercent, budgetStatus,
         * budgetInsight, and category budget list.
         */

        tvMonthlyBudget.setText("Rs. 45,000");
        tvBudgetUsedPercent.setText("68% Used");
        tvBudgetStatus.setText("On Track");
        tvSpentAmount.setText("Rs. 30,600");
        tvRemainingAmount.setText("Rs. 14,400");
        tvBudgetInsight.setText("You are likely to exceed your Food budget by Rs 1,200 this month.\nConsider reducing weekend dining.");
        progressMonthlyBudget.setProgress(68);

        setCategoryData(
                cardFoodBudget,
                "🍴",
                "Food & Dining",
                "Limit: Rs. 8,000",
                "Safe",
                "RS. 5,440 SPENT",
                "68%",
                68,
                false
        );

        setCategoryData(
                cardTransportBudget,
                "🚗",
                "Transport",
                "Limit: Rs. 3,500",
                "Near Limit",
                "RS. 3,100 SPENT",
                "89%",
                89,
                false
        );

        setCategoryData(
                cardUtilitiesBudget,
                "⚡",
                "Utilities",
                "Limit: Rs. 5,000",
                "Safe",
                "RS. 1,200 SPENT",
                "24%",
                24,
                false
        );

        setCategoryData(
                cardShoppingBudget,
                "▣",
                "Shopping",
                "Limit: Rs. 4,000",
                "Exceeded",
                "RS. 4,500 SPENT",
                "113%",
                100,
                true
        );

        setCategoryData(
                cardHousingBudget,
                "⌂",
                "Housing",
                "Limit: Rs. 15,000",
                "Safe",
                "RS. 15,000 SPENT",
                "100%",
                100,
                false
        );
    }

    private void setCategoryData(
            View card,
            String icon,
            String name,
            String limit,
            String status,
            String spent,
            String percent,
            int progress,
            boolean exceeded
    ) {
        TextView tvCategoryIcon = card.findViewById(R.id.tvCategoryIcon);
        TextView tvCategoryName = card.findViewById(R.id.tvCategoryName);
        TextView tvCategoryLimit = card.findViewById(R.id.tvCategoryLimit);
        TextView tvCategoryStatus = card.findViewById(R.id.tvCategoryStatus);
        TextView tvCategorySpent = card.findViewById(R.id.tvCategorySpent);
        TextView tvCategoryPercent = card.findViewById(R.id.tvCategoryPercent);
        ProgressBar progressCategoryBudget = card.findViewById(R.id.progressCategoryBudget);
        TextView btnEditCategoryBudget = card.findViewById(R.id.btnEditCategoryBudget);

        tvCategoryIcon.setText(icon);
        tvCategoryName.setText(name);
        tvCategoryLimit.setText(limit);
        tvCategoryStatus.setText(status);
        tvCategorySpent.setText(spent);
        tvCategoryPercent.setText(percent);
        progressCategoryBudget.setProgress(progress);

        if (exceeded) {
            tvCategoryStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            progressCategoryBudget.setProgressDrawable(getResources().getDrawable(R.drawable.progress_red));
        } else {
            tvCategoryStatus.setTextColor(getResources().getColor(android.R.color.black));
            progressCategoryBudget.setProgressDrawable(getResources().getDrawable(R.drawable.progress_dark));
        }

        btnEditCategoryBudget.setOnClickListener(v ->
                Toast.makeText(this, "Edit " + name + " budget", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupClicks() {
        btnBack.setOnClickListener(v -> finish());

        btnViewAllCategories.setOnClickListener(v ->
                Toast.makeText(this, "View all categories clicked", Toast.LENGTH_SHORT).show()
        );

        btnAddBudget.setOnClickListener(v ->
                Toast.makeText(this, "Add budget clicked", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.navDashboard).setOnClickListener(v ->
                Toast.makeText(this, "Dashboard clicked", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.navExpenses).setOnClickListener(v ->
                Toast.makeText(this, "Expenses clicked", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.navSubs).setOnClickListener(v ->
                Toast.makeText(this, "Subscriptions clicked", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.navFamily).setOnClickListener(v ->
                Toast.makeText(this, "Family clicked", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.navProfile).setOnClickListener(v ->
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
        );
    }
}