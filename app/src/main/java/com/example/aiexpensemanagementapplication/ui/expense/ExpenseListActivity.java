package com.example.aiexpensemanagementapplication.ui.expense;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;

import com.example.aiexpensemanagementapplication.model.Subscription;
import com.example.aiexpensemanagementapplication.ui.dashboard.FamilyDashboardActivity;
import com.example.aiexpensemanagementapplication.ui.dashboard.PersonalDashboardActivity;
import com.example.aiexpensemanagementapplication.ui.profile.ProfileActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ExpenseListActivity extends AppCompatActivity {

    LinearLayout navDashboard, navExpenses, navSubs, navFamily, navProfile;
    FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_list); // your XML file name

        // Initialize Bottom Navigation
        navDashboard = findViewById(R.id.navDashboard);
        navExpenses = findViewById(R.id.navExpenses);
        navSubs = findViewById(R.id.navSubs);
        navFamily = findViewById(R.id.navFamily);
        navProfile = findViewById(R.id.navProfile);

        fabAdd = findViewById(R.id.fabAdd);

        // Dashboard Navigation
        navDashboard.setOnClickListener(v -> {
            startActivity(new Intent(ExpenseListActivity.this, PersonalDashboardActivity.class));
            finish();
        });

        // Expenses (current screen)
        navExpenses.setOnClickListener(v -> {
            // already here, do nothing or refresh
        });

        // Subscriptions Navigation
        navSubs.setOnClickListener(v -> {
            startActivity(new Intent(ExpenseListActivity.this, Subscription.class));
        });

        // Family Navigation
        navFamily.setOnClickListener(v -> {
            startActivity(new Intent(ExpenseListActivity.this, FamilyDashboardActivity.class));
        });

        // Profile Navigation
        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(ExpenseListActivity.this, ProfileActivity.class));
        });

        // Floating Action Button (Add Expense)
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(ExpenseListActivity.this, AddExpenseActivity.class));
        });
    }
}