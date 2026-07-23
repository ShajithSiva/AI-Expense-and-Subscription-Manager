package com.example.aiexpensemanagementapplication.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.ui.auth.LoginActivity;
import com.example.aiexpensemanagementapplication.ui.budget.BudgetActivity;
import com.example.aiexpensemanagementapplication.ui.dashboard.PersonalDashboardActivity;
import com.example.aiexpensemanagementapplication.ui.expense.ExpenseListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ProfileActivity extends AppCompatActivity{
    private TextView tvName;
    private TextView tvEmail;

    private TextView tvExpense;
    private TextView tvBalance;
    private TextView tvBudget;

    private ImageView imgProfile;
    private ImageButton btnChangePhoto;

    private MaterialButton btnLogout;

    private BottomNavigationView bottomNavigation;

    private LinearLayout layoutEditProfile;
    private LinearLayout layoutChangePassword;
    private LinearLayout layoutNotifications;

    private LinearLayout layoutCurrency;
    private TextView tvCurrency;

    private LinearLayout layoutBudgetPeriod;
    private TextView tvBudgetPeriod;

    private LinearLayout layoutBudget;
    private LinearLayout layoutSubscription;

    private DatabaseHelper databaseHelper;

    private FirebaseAuth mAuth;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initialize();

        loadProfile();

        loadStatistics();

        loadCurrencyPreference();

        loadBudgetPeriodPreference();

        listeners();

        setupBottomNavigation();
    }
    private void initialize() {

        databaseHelper = new DatabaseHelper(this);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        imgProfile = findViewById(R.id.imgProfile);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);

        tvExpense = findViewById(R.id.tvExpense);
        tvBalance = findViewById(R.id.tvBalance);
        tvBudget = findViewById(R.id.tvBudget);

        btnLogout = findViewById(R.id.btnLogout);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        layoutEditProfile = findViewById(R.id.layoutEditProfile);

        layoutChangePassword = findViewById(R.id.layoutChangePassword);

        layoutNotifications = findViewById(R.id.layoutNotifications);

        layoutCurrency = findViewById(R.id.layoutCurrency);

        tvCurrency = findViewById(R.id.tvCurrency);

        layoutBudgetPeriod = findViewById(R.id.layoutBudgetPeriod);

        tvBudgetPeriod = findViewById(R.id.tvBudgetPeriod);

        layoutBudget = findViewById(R.id.layoutBudget);

        layoutSubscription = findViewById(R.id.layoutSubscription);

    }
    private void loadProfile() {

        if (currentUser == null) return;

        int userId = databaseHelper.getUserIdByFirebaseUid(currentUser.getUid());

        if (userId == -1) return;

        Cursor cursor = databaseHelper.getUserById(userId);

        if (cursor.moveToFirst()) {

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.USER_NAME));

            String email = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.USER_EMAIL));

            tvName.setText(name);

            tvEmail.setText(email);
        }
        cursor.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadProfile();

        loadStatistics();

        loadCurrencyPreference();

        loadBudgetPeriodPreference();
    }

    private void loadStatistics() {

        if (currentUser == null) return;

        int userId = databaseHelper.getUserIdByFirebaseUid(currentUser.getUid());

        if (userId == -1) return;

        double totalExpense = databaseHelper.getTotalExpense(userId);

        double totalBalance = databaseHelper.getTotalBalance(userId);

        double totalBudget = databaseHelper.getTotalBudget(userId);

        tvExpense.setText(String.format("Rs. %.2f", totalExpense));

        tvBalance.setText(String.format("Rs. %.2f", totalBalance));

        tvBudget.setText(String.format("Rs. %.2f", totalBudget));
    }

    private void setupBottomNavigation() {

        bottomNavigation.setSelectedItemId(R.id.nav_profile);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {

                startActivity(new Intent(this, PersonalDashboardActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_expenses) {

                startActivity(new Intent(this, ExpenseListActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_profile) {

                return true;
            }

            return false;
        });
    }

    private void listeners() {

        btnChangePhoto.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Profile picture update coming soon",
                    Toast.LENGTH_SHORT
            ).show();

        });

        btnLogout.setOnClickListener(v -> {

            mAuth.signOut();

            Intent intent = new Intent(
                    ProfileActivity.this,
                    LoginActivity.class);

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            finish();

        });

        layoutEditProfile.setOnClickListener(v -> {

            startActivity(new Intent(
                    this,
                    EditProfileActivity.class));

        });

        layoutChangePassword.setOnClickListener(v -> {

            startActivity(new Intent(
                    this,
                    ChangePasswordActivity.class));

        });

        layoutNotifications.setOnClickListener(v -> {

            startActivity(new Intent(
                    this,
                    NotificationPreferancesActivity.class));

        });

        layoutCurrency.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ProfileActivity.this,
                    CurrencySettingsActivity.class
            );

            startActivity(intent);
        });

        layoutBudgetPeriod.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ProfileActivity.this,
                    BudgetPeriodActivity.class
            );

            startActivity(intent);
        });

        layoutBudget.setOnClickListener(v -> {

            startActivity(new Intent(
                    this,
                    BudgetActivity.class));

        });

    }

    private void loadCurrencyPreference() {

        if (currentUser == null) {
            return;
        }

        int userId = databaseHelper.getUserIdByFirebaseUid(
                currentUser.getUid()
        );

        if (userId == -1) {
            return;
        }

        String currencyDisplay =
                databaseHelper.getSavedCurrencyDisplay(userId);

        tvCurrency.setText(currencyDisplay);
    }

    private void loadBudgetPeriodPreference() {

        if (currentUser == null) {
            return;
        }

        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid()
                );

        if (userId == -1) {
            return;
        }

        String budgetPeriod =
                databaseHelper.getSavedBudgetPeriod(
                        userId
                );

        tvBudgetPeriod.setText(budgetPeriod);
    }



}