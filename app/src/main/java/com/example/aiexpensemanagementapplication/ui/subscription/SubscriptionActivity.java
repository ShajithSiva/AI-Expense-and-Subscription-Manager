package com.example.aiexpensemanagementapplication.ui.subscription;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.database.Cursor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.model.Subscription;
import com.example.aiexpensemanagementapplication.ui.dashboard.PersonalDashboardActivity;
import com.example.aiexpensemanagementapplication.ui.expense.ExpenseListActivity;
import com.example.aiexpensemanagementapplication.ui.income.IncomeListActivity;
import com.example.aiexpensemanagementapplication.ui.profile.ProfileActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;


public class SubscriptionActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;

    private RecyclerView rvSubscriptions;

    private FloatingActionButton fabAddSubscription;

    private BottomNavigationView bottomNavigation;

    private LinearLayout layoutEmpty;

    private TextView tvMonthlySpend;
    private TextView tvActiveSubscriptions;
    private TextView tvNextDue;
    private TextView tvOptimization;

    private DatabaseHelper databaseHelper;

    private ArrayList<Subscription> subscriptionList = new ArrayList<>();
    private SubscriptionAdapter adapter;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_subscription);

        initializeViews();

        setupToolbar();

        databaseHelper = new DatabaseHelper(this);

        initializeUser();

        setupRecyclerView();

        loadSubscriptions();

        setupListeners();

        setupBottomNavigation();
    }

    private void initializeViews() {

        toolbar = findViewById(R.id.toolbar);

        rvSubscriptions = findViewById(R.id.rvSubscriptions);

        fabAddSubscription = findViewById(R.id.fabAddSubscription);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        layoutEmpty = findViewById(R.id.layoutEmpty);

        tvMonthlySpend = findViewById(R.id.tvMonthlySpend);

        tvActiveSubscriptions = findViewById(R.id.tvActiveSubscriptions);

        tvNextDue = findViewById(R.id.tvNextDue);

        tvOptimization = findViewById(R.id.tvOptimization);
    }

    private void setupToolbar() {

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupBottomNavigation() {

        bottomNavigation.setSelectedItemId(R.id.nav_subscriptions);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {

                startActivity(new Intent(this, PersonalDashboardActivity.class));
                finish();
                return true;

            } else if (itemId == R.id.nav_expenses) {

                startActivity(new Intent(this, ExpenseListActivity.class));
                finish();
                return true;

            } else if (itemId == R.id.nav_subscriptions) {

                return true;

            } else if (itemId == R.id.nav_income) {

                startActivity(new Intent(this, IncomeListActivity.class));
                finish();
                return true;

            } else if (itemId == R.id.nav_profile) {

                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void setupRecyclerView() {

        adapter = new SubscriptionAdapter(this, subscriptionList);

        rvSubscriptions.setLayoutManager(new LinearLayoutManager(this));

        rvSubscriptions.setAdapter(adapter);
    }

    private void initializeUser() {

        FirebaseUser firebaseUser =
                FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {

            finish();
            return;
        }

        userId = databaseHelper.getUserIdByFirebaseUid(
                firebaseUser.getUid()
        );
    }

    private void loadSubscriptions() {

        subscriptionList.clear();

        Cursor cursor = databaseHelper.getSubscriptions(userId);

        while (cursor.moveToNext()) {

            Subscription subscription = new Subscription();

            subscription.setSubscriptionId(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SUBSCRIPTION_ID)));

            subscription.setServiceName(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SERVICE_NAME)));

            subscription.setAmount(
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.AMOUNT)));

            subscription.setBillingCycle(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BILLING_CYCLE)));

            subscription.setNextBillingDate(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NEXT_BILLING_DATE)));

            subscriptionList.add(subscription);
        }

        cursor.close();

        adapter.notifyDataSetChanged();

        updateSummary();

        if (subscriptionList.isEmpty()) {

            layoutEmpty.setVisibility(View.VISIBLE);

            rvSubscriptions.setVisibility(View.GONE);

        } else {

            layoutEmpty.setVisibility(View.GONE);

            rvSubscriptions.setVisibility(View.VISIBLE);
        }
    }

    private void updateSummary() {

        double total =
                databaseHelper.getTotalSubscriptionAmount(userId);

        int count =
                databaseHelper.getSubscriptionCount(userId);

        tvMonthlySpend.setText(
                String.format("Rs %.2f", total));

        tvActiveSubscriptions.setText(
                count + " Services");

        Cursor cursor = databaseHelper.getNextSubscription(userId);

        if (cursor.moveToFirst()) {

            String nextDate =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(DatabaseHelper.NEXT_BILLING_DATE));

            tvNextDue.setText(nextDate);

        } else {

            tvNextDue.setText("No Due");
        }

        cursor.close();

        tvOptimization.setText(
                databaseHelper.generateAIInsight(userId));
    }

    private void setupListeners() {

        fabAddSubscription.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            this,
                            AddSubscriptionActivity.class
                    );

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadSubscriptions();
    }

}
