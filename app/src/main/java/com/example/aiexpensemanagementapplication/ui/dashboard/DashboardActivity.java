package com.example.aiexpensemanagementapplication.ui.dashboard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.notification.ReminderScheduler;

import com.example.aiexpensemanagementapplication.ui.expense.AddExpenseActivity;
import com.example.aiexpensemanagementapplication.ui.expense.ExpenseListActivity;
import com.example.aiexpensemanagementapplication.ui.income.IncomeListActivity;
import com.example.aiexpensemanagementapplication.ui.notification.NotificationActivity;
import com.example.aiexpensemanagementapplication.ui.profile.ProfileActivity;
import com.example.aiexpensemanagementapplication.ui.subscription.SubscriptionActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;


public class DashboardActivity extends AppCompatActivity {

    // =========================================================
    // DATABASE
    // =========================================================

    private DatabaseHelper databaseHelper;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;


    // =========================================================
    // HEADER
    // =========================================================

    private TextView tvGreeting;
    private TextView tvUserName;

    private ImageView imgNotification;
    private TextView tvNotificationBadge;

    private ShapeableImageView imgProfile;


    // =========================================================
    // PERSONAL / FAMILY SWITCH
    // =========================================================

    private MaterialButton btnPersonal;
    private MaterialButton btnFamily;


    // =========================================================
    // FAB
    // =========================================================

    private FloatingActionButton fabAddExpense;


    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private BottomNavigationView bottomNavigation;


    // =========================================================
    // DASHBOARD MODE
    // =========================================================

    private boolean isPersonalMode = true;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);


        // Initialize views, Firebase and database
        initialize();


        // Load user name + greeting
        loadUser();


        // Personal / Family switch
        setupDashboardMode();


        // Notification, Profile and FAB
        setupClickListeners();


        // Bottom navigation
        setupBottomNavigation();


        // Android 13+ notification permission
        requestNotificationPermission();


        // Budget / Subscription reminders
        setupReminders();


        // -----------------------------------------------------
        // DEFAULT DASHBOARD = PERSONAL
        // -----------------------------------------------------

        if (savedInstanceState == null) {

            showPersonalDashboard();

        } else {

            isPersonalMode =
                    savedInstanceState.getBoolean(
                            "PERSONAL_MODE",
                            true
                    );

            updateDashboardModeUI();
        }
    }


    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initialize() {

        // -----------------------------------------------------
        // DATABASE
        // -----------------------------------------------------

        databaseHelper =
                new DatabaseHelper(this);


        // -----------------------------------------------------
        // FIREBASE
        // -----------------------------------------------------

        mAuth =
                FirebaseAuth.getInstance();

        firestore =
                FirebaseFirestore.getInstance();

        currentUser =
                mAuth.getCurrentUser();


        // -----------------------------------------------------
        // HEADER
        // -----------------------------------------------------

        tvGreeting =
                findViewById(R.id.tvGreeting);

        tvUserName =
                findViewById(R.id.tvUserName);

        imgNotification =
                findViewById(R.id.imgNotification);

        tvNotificationBadge =
                findViewById(R.id.tvNotificationBadge);

        imgProfile =
                findViewById(R.id.imgProfile);


        // -----------------------------------------------------
        // PERSONAL / FAMILY BUTTONS
        // -----------------------------------------------------

        btnPersonal =
                findViewById(R.id.btnPersonal);

        btnFamily =
                findViewById(R.id.btnFamily);


        // -----------------------------------------------------
        // FAB
        // -----------------------------------------------------

        fabAddExpense =
                findViewById(R.id.fabAddExpense);


        // -----------------------------------------------------
        // BOTTOM NAVIGATION
        // -----------------------------------------------------

        bottomNavigation =
                findViewById(R.id.bottomNavigation);
    }


    // =========================================================
    // LOAD USER
    // =========================================================

    private void loadUser() {

        updateGreeting();


        if (currentUser == null) {

            tvUserName.setText("User");

            return;
        }


        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {

                    String name =
                            document.getString("fullName");


                    if (name == null ||
                            name.trim().isEmpty()) {

                        name =
                                currentUser.getDisplayName();
                    }


                    if (name == null ||
                            name.trim().isEmpty()) {

                        name = "User";
                    }


                    tvUserName.setText(name);

                })
                .addOnFailureListener(e -> {

                    String name =
                            currentUser.getDisplayName();


                    if (name == null ||
                            name.trim().isEmpty()) {

                        name = "User";
                    }


                    tvUserName.setText(name);
                });
    }


    // =========================================================
    // GREETING
    // =========================================================

    private void updateGreeting() {

        int hour =
                Calendar.getInstance()
                        .get(Calendar.HOUR_OF_DAY);


        if (hour < 12) {

            tvGreeting.setText(
                    "Good Morning,"
            );

        } else if (hour < 17) {

            tvGreeting.setText(
                    "Good Afternoon,"
            );

        } else {

            tvGreeting.setText(
                    "Good Evening,"
            );
        }
    }


    // =========================================================
    // PERSONAL / FAMILY SWITCH
    // =========================================================

    private void setupDashboardMode() {

        // -----------------------------------------------------
        // PERSONAL
        // -----------------------------------------------------

        btnPersonal.setOnClickListener(v -> {

            if (!isPersonalMode) {

                showPersonalDashboard();
            }
        });


        // -----------------------------------------------------
        // FAMILY
        // -----------------------------------------------------

        btnFamily.setOnClickListener(v -> {

            if (isPersonalMode) {

                showFamilyDashboard();
            }
        });
    }


    // =========================================================
    // SHOW PERSONAL DASHBOARD
    // =========================================================

    private void showPersonalDashboard() {

        isPersonalMode = true;


        replaceDashboardFragment(
                new PersonalDashboardFragment()
        );


        updateDashboardModeUI();
    }


    // =========================================================
    // SHOW FAMILY DASHBOARD
    // =========================================================

    private void showFamilyDashboard() {

        isPersonalMode = false;


        replaceDashboardFragment(
                new FamilyDashboardFragment()
        );


        updateDashboardModeUI();
    }


    // =========================================================
    // REPLACE DASHBOARD FRAGMENT
    // =========================================================

    private void replaceDashboardFragment(
            Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.dashboardContainer,
                        fragment
                )
                .commit();
    }


    // =========================================================
    // UPDATE PERSONAL / FAMILY BUTTON DESIGN
    // =========================================================

    private void updateDashboardModeUI() {

        if (isPersonalMode) {

            // =================================================
            // PERSONAL SELECTED
            // =================================================

            btnPersonal.setBackgroundTintList(
                    ContextCompat.getColorStateList(
                            this,
                            R.color.dashboard_selected
                    )
            );


            btnPersonal.setTextColor(
                    ContextCompat.getColor(
                            this,
                            android.R.color.white
                    )
            );


            // =================================================
            // FAMILY UNSELECTED
            // =================================================

            btnFamily.setBackgroundTintList(
                    ContextCompat.getColorStateList(
                            this,
                            R.color.dashboard_unselected
                    )
            );


            btnFamily.setTextColor(
                    ContextCompat.getColor(
                            this,
                            R.color.dashboard_unselected_text
                    )
            );

        } else {

            // =================================================
            // FAMILY SELECTED
            // =================================================

            btnFamily.setBackgroundTintList(
                    ContextCompat.getColorStateList(
                            this,
                            R.color.dashboard_selected
                    )
            );


            btnFamily.setTextColor(
                    ContextCompat.getColor(
                            this,
                            android.R.color.white
                    )
            );


            // =================================================
            // PERSONAL UNSELECTED
            // =================================================

            btnPersonal.setBackgroundTintList(
                    ContextCompat.getColorStateList(
                            this,
                            R.color.dashboard_unselected
                    )
            );


            btnPersonal.setTextColor(
                    ContextCompat.getColor(
                            this,
                            R.color.dashboard_unselected_text
                    )
            );
        }
    }


    // =========================================================
    // COMMON CLICK LISTENERS
    // =========================================================

    private void setupClickListeners() {

        // -----------------------------------------------------
        // NOTIFICATION
        // -----------------------------------------------------

        imgNotification.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            DashboardActivity.this,
                            NotificationActivity.class
                    );

            startActivity(intent);
        });


        // -----------------------------------------------------
        // PROFILE
        // -----------------------------------------------------

        imgProfile.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            DashboardActivity.this,
                            ProfileActivity.class
                    );

            startActivity(intent);
        });


        // -----------------------------------------------------
        // ADD EXPENSE FAB
        // -----------------------------------------------------

        fabAddExpense.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            DashboardActivity.this,
                            AddExpenseActivity.class
                    );

            startActivity(intent);
        });
    }


    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private void setupBottomNavigation() {

        bottomNavigation.setSelectedItemId(
                R.id.nav_dashboard
        );


        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int id =
                            item.getItemId();


                    // -------------------------------------------------
                    // DASHBOARD
                    // -------------------------------------------------

                    if (id == R.id.nav_dashboard) {

                        return true;
                    }


                    // -------------------------------------------------
                    // EXPENSES
                    // -------------------------------------------------

                    else if (id == R.id.nav_expenses) {

                        Intent intent =
                                new Intent(
                                        DashboardActivity.this,
                                        ExpenseListActivity.class
                                );


                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        );


                        startActivity(intent);

                        return true;
                    }


                    // -------------------------------------------------
                    // SUBSCRIPTIONS
                    // -------------------------------------------------

                    else if (id == R.id.nav_subscriptions) {

                        Intent intent =
                                new Intent(
                                        DashboardActivity.this,
                                        SubscriptionActivity.class
                                );


                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        );


                        startActivity(intent);

                        return true;
                    }


                    // -------------------------------------------------
                    // INCOME
                    // -------------------------------------------------

                    else if (id == R.id.nav_income) {

                        Intent intent =
                                new Intent(
                                        DashboardActivity.this,
                                        IncomeListActivity.class
                                );


                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        );


                        startActivity(intent);

                        return true;
                    }


                    // -------------------------------------------------
                    // PROFILE
                    // -------------------------------------------------

                    else if (id == R.id.nav_profile) {

                        Intent intent =
                                new Intent(
                                        DashboardActivity.this,
                                        ProfileActivity.class
                                );


                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        );


                        startActivity(intent);

                        return true;
                    }


                    return false;
                }
        );
    }


    // =========================================================
    // NOTIFICATION PERMISSION
    // =========================================================

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.TIRAMISU) {


            if (checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {


                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        101
                );
            }
        }
    }


    // =========================================================
    // NOTIFICATION BADGE
    // =========================================================

    private void updateNotificationBadge() {

        int unread =
                databaseHelper
                        .getUnreadNotificationCount();


        if (unread > 0) {

            tvNotificationBadge.setVisibility(
                    View.VISIBLE
            );


            tvNotificationBadge.setText(
                    String.valueOf(unread)
            );

        } else {

            tvNotificationBadge.setVisibility(
                    View.GONE
            );
        }
    }


    // =========================================================
    // REMINDERS
    // =========================================================

    private void setupReminders() {

        if (currentUser == null) {

            return;
        }


        String email =
                currentUser.getEmail();


        if (email == null ||
                email.trim().isEmpty()) {

            return;
        }


        int userId =
                databaseHelper.getUserIdByEmail(
                        email
                );


        if (userId == -1) {

            return;
        }


        ReminderScheduler scheduler =
                new ReminderScheduler(this);


        scheduler.checkBudgetReminder(
                databaseHelper,
                userId
        );


        scheduler.checkSubscriptionReminder(
                databaseHelper,
                userId
        );


        Calendar calendar =
                Calendar.getInstance();


        if (calendar.get(
                Calendar.DAY_OF_MONTH
        ) == 1) {

            scheduler.showMonthlyReportReminder();
        }
    }


    // =========================================================
    // ON RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();


        // Update greeting
        updateGreeting();


        // Update notification badge
        updateNotificationBadge();


        // Keep Dashboard selected
        if (bottomNavigation != null) {

            bottomNavigation.setSelectedItemId(
                    R.id.nav_dashboard
            );
        }
    }


    // =========================================================
    // SAVE SELECTED DASHBOARD MODE
    // =========================================================

    @Override
    protected void onSaveInstanceState(
            Bundle outState) {

        outState.putBoolean(
                "PERSONAL_MODE",
                isPersonalMode
        );


        super.onSaveInstanceState(
                outState
        );
    }
}