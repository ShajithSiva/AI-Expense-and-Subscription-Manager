package com.example.aiexpensemanagementapplication.ui.subscription;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.ai.SubscriptionDetectionAI;
import com.example.aiexpensemanagementapplication.ai.SubscriptionEmailExtractor;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.data.remote.gmail.GmailAuthManager;
import com.example.aiexpensemanagementapplication.data.remote.gmail.GmailServiceManager;
import com.example.aiexpensemanagementapplication.model.Subscription;
import com.example.aiexpensemanagementapplication.ui.dashboard.DashboardActivity;
import com.example.aiexpensemanagementapplication.ui.expense.ExpenseListActivity;
import com.example.aiexpensemanagementapplication.ui.income.IncomeListActivity;
import com.example.aiexpensemanagementapplication.ui.profile.ProfileActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

public class SubscriptionActivity extends AppCompatActivity {

    // =========================================================
    // UI
    // =========================================================

    private MaterialToolbar toolbar;

    private RecyclerView rvSubscriptions;

    private FloatingActionButton fabAddSubscription;

    private BottomNavigationView bottomNavigation;

    private LinearLayout layoutEmpty;

    private TextView tvMonthlySpend;
    private TextView tvActiveSubscriptions;
    private TextView tvNextDue;
    private TextView tvOptimization;
    private TextView tvGmailStatus;

    private Button btnConnectGmail;


    // =========================================================
    // DATABASE
    // =========================================================

    private DatabaseHelper databaseHelper;


    // =========================================================
    // GMAIL
    // =========================================================

    private GmailAuthManager gmailAuthManager;

    private GmailServiceManager gmailServiceManager;


    // =========================================================
    // AI
    // =========================================================

    private SubscriptionDetectionAI subscriptionDetectionAI;


    // =========================================================
    // SUBSCRIPTION LIST
    // =========================================================

    private final ArrayList<Subscription> subscriptionList =
            new ArrayList<>();

    private SubscriptionAdapter adapter;


    // =========================================================
    // USER
    // =========================================================

    private int userId = -1;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_subscription
        );

        initializeViews();

        setupToolbar();

        databaseHelper =
                new DatabaseHelper(this);

        gmailAuthManager =
                new GmailAuthManager(this);

        gmailServiceManager =
                new GmailServiceManager(this);

        initializeAI();


        // INITIALIZE LOCAL USER

        boolean userInitialized =
                initializeUser();

        if (!userInitialized) {
            return;
        }

        // CONTINUE ONLY WITH VALID USER

        setupRecyclerView();

        loadSubscriptions();

        setupListeners();

        setupBottomNavigation();
    }

    // INITIALIZE VIEWS

    private void initializeViews() {

        toolbar =
                findViewById(R.id.toolbar);

        rvSubscriptions =
                findViewById(R.id.rvSubscriptions);

        fabAddSubscription =
                findViewById(R.id.fabAddSubscription);

        bottomNavigation =
                findViewById(R.id.bottomNavigation);

        layoutEmpty =
                findViewById(R.id.layoutEmpty);

        tvMonthlySpend =
                findViewById(R.id.tvMonthlySpend);

        tvActiveSubscriptions =
                findViewById(R.id.tvActiveSubscriptions);

        tvNextDue =
                findViewById(R.id.tvNextDue);

        tvOptimization =
                findViewById(R.id.tvOptimization);

        btnConnectGmail =
                findViewById(R.id.btnConnectGmail);

        tvGmailStatus =
                findViewById(R.id.tvGmailStatus);
    }


    // =========================================================
    // INITIALIZE AI
    // =========================================================

    private void initializeAI() {

        try {

            subscriptionDetectionAI =
                    new SubscriptionDetectionAI(this);

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "AI MODEL INITIALIZED SUCCESSFULLY"
            );

            System.out.println(
                    "========================================"
            );

        } catch (Exception e) {

            subscriptionDetectionAI = null;

            Toast.makeText(
                    this,
                    "AI model failed to load.",
                    Toast.LENGTH_LONG
            ).show();

            e.printStackTrace();
        }
    }

    // TOOLBAR

    private void setupToolbar() {

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {

            getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(true);

            getSupportActionBar()
                    .setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private void setupBottomNavigation() {

        bottomNavigation.setSelectedItemId(
                R.id.nav_subscriptions
        );

        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int itemId =
                            item.getItemId();


                    if (itemId ==
                            R.id.nav_dashboard) {

                        startActivity(
                                new Intent(
                                        this,
                                        DashboardActivity.class
                                )
                        );

                        finish();

                        return true;
                    }


                    else if (itemId ==
                            R.id.nav_expenses) {

                        startActivity(
                                new Intent(
                                        this,
                                        ExpenseListActivity.class
                                )
                        );

                        finish();

                        return true;
                    }


                    else if (itemId ==
                            R.id.nav_subscriptions) {

                        return true;
                    }


                    else if (itemId ==
                            R.id.nav_income) {

                        startActivity(
                                new Intent(
                                        this,
                                        IncomeListActivity.class
                                )
                        );

                        finish();

                        return true;
                    }


                    else if (itemId ==
                            R.id.nav_profile) {

                        startActivity(
                                new Intent(
                                        this,
                                        ProfileActivity.class
                                )
                        );

                        finish();

                        return true;
                    }


                    return false;
                }
        );
    }


    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {

        adapter =
                new SubscriptionAdapter(
                        this,
                        subscriptionList,
                        subscription -> {

                            Intent intent =
                                    new Intent(
                                            SubscriptionActivity.this,
                                            EditSubscriptionActivity.class
                                    );

                            intent.putExtra(
                                    "subscriptionId",
                                    subscription.getSubscriptionId()
                            );

                            startActivity(intent);
                        }
                );


        rvSubscriptions.setLayoutManager(
                new LinearLayoutManager(this)
        );


        rvSubscriptions.setAdapter(
                adapter
        );
    }


    // =========================================================
    // INITIALIZE USER
    // =========================================================

    private boolean initializeUser() {

        FirebaseUser firebaseUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        // Firebase user not logged in
        if (firebaseUser == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return false;
        }

        String firebaseUid =
                firebaseUser.getUid();

        String email =
                firebaseUser.getEmail();

        System.out.println(
                "========================================"
        );

        System.out.println(
                "FIREBASE UID: " + firebaseUid
        );

        System.out.println(
                "FIREBASE EMAIL: " + email
        );


        // --------------------------------------------
        // Find local SQLite user
        // --------------------------------------------

        userId =
                databaseHelper
                        .getUserIdByFirebaseUid(
                                firebaseUid
                        );


        System.out.println(
                "LOCAL SQLITE USER ID: " + userId
        );


        // --------------------------------------------
        // IMPORTANT
        // --------------------------------------------

        if (userId <= 0) {

            Toast.makeText(
                    this,
                    "Local user record not found. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            System.out.println(
                    "ERROR: Firebase user exists, "
                            + "but SQLite User record does not exist."
            );

            return false;
        }


        System.out.println(
                "VALID LOCAL USER FOUND: " + userId
        );

        System.out.println(
                "========================================"
        );

        return true;
    }

    // =========================================================
    // LOAD SUBSCRIPTIONS
    // =========================================================

    private void loadSubscriptions() {

        if (databaseHelper == null) {
            return;
        }


        if (userId <= 0) {
            return;
        }


        subscriptionList.clear();


        Cursor cursor =
                null;


        try {

            cursor =
                    databaseHelper
                            .getSubscriptions(userId);


            if (cursor != null) {

                while (cursor.moveToNext()) {

                    Subscription subscription =
                            new Subscription();


                    subscription.setSubscriptionId(
                            cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            DatabaseHelper
                                                    .SUBSCRIPTION_ID
                                    )
                            )
                    );


                    subscription.setServiceName(
                            cursor.getString(
                                    cursor.getColumnIndexOrThrow(
                                            DatabaseHelper
                                                    .SERVICE_NAME
                                    )
                            )
                    );


                    subscription.setAmount(
                            cursor.getDouble(
                                    cursor.getColumnIndexOrThrow(
                                            DatabaseHelper
                                                    .AMOUNT
                                    )
                            )
                    );


                    subscription.setBillingCycle(
                            cursor.getString(
                                    cursor.getColumnIndexOrThrow(
                                            DatabaseHelper
                                                    .BILLING_CYCLE
                                    )
                            )
                    );


                    subscription.setNextBillingDate(
                            cursor.getString(
                                    cursor.getColumnIndexOrThrow(
                                            DatabaseHelper
                                                    .NEXT_BILLING_DATE
                                    )
                            )
                    );


                    subscriptionList.add(
                            subscription
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }


        if (adapter != null) {

            adapter.notifyDataSetChanged();
        }


        updateSummary();


        updateEmptyState();
    }


    // =========================================================
    // EMPTY STATE
    // =========================================================

    private void updateEmptyState() {

        if (subscriptionList.isEmpty()) {

            layoutEmpty.setVisibility(
                    View.VISIBLE
            );

            rvSubscriptions.setVisibility(
                    View.GONE
            );

        } else {

            layoutEmpty.setVisibility(
                    View.GONE
            );

            rvSubscriptions.setVisibility(
                    View.VISIBLE
            );
        }
    }


    // =========================================================
    // GMAIL + AI SCAN
    // =========================================================

    private void readGmailSubscriptions() {

        if (userId <= 0) {

            Toast.makeText(
                    this,
                    "Local user not found. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            System.out.println(
                    "SUBSCRIPTION SCAN STOPPED: userId = "
                            + userId
            );

            return;
        }

        Toast.makeText(
                this,
                "AI is scanning Gmail...",
                Toast.LENGTH_SHORT
        ).show();


        if (subscriptionDetectionAI == null) {

            Toast.makeText(
                    this,
                    "AI model is not available.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        if (gmailServiceManager == null) {

            Toast.makeText(
                    this,
                    "Gmail service is not available.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        gmailServiceManager
                .readSubscriptionEmails(

                        new GmailServiceManager.GmailCallback() {

                            @Override
                            public void onSuccess(
                                    ArrayList<GmailServiceManager.GmailMessageData> messages
                            ) {

                                int detectedCount = 0;

                                int savedCount = 0;

                                int duplicateCount = 0;


                                if (messages == null ||
                                        messages.isEmpty()) {

                                    runOnUiThread(
                                            () -> Toast.makeText(
                                                    SubscriptionActivity.this,
                                                    "No matching Gmail emails found.",
                                                    Toast.LENGTH_LONG
                                            ).show()
                                    );

                                    return;
                                }


                                // =================================================
                                // PROCESS EVERY EMAIL
                                // =================================================

                                for (
                                        GmailServiceManager.GmailMessageData message :
                                        messages
                                ) {

                                    if (message == null) {
                                        continue;
                                    }


                                    String subject =
                                            safeString(
                                                    message.getSubject()
                                            );


                                    String sender =
                                            safeString(
                                                    message.getSender()
                                            );


                                    String snippet =
                                            safeString(
                                                    message.getSnippet()
                                            );


                                    // =================================================
                                    // AI PREDICTION
                                    // =================================================

                                    SubscriptionDetectionAI
                                            .PredictionResult result =
                                            subscriptionDetectionAI.predict(
                                                    subject,
                                                    snippet
                                            );


                                    String label =
                                            result.getLabel();


                                    float confidence =
                                            result.getConfidence();


                                    // =================================================
                                    // LOG
                                    // =================================================

                                    System.out.println(
                                            "========================================"
                                    );


                                    System.out.println(
                                            "GMAIL SUBJECT: "
                                                    + subject
                                    );


                                    System.out.println(
                                            "GMAIL SENDER: "
                                                    + sender
                                    );


                                    System.out.println(
                                            "GMAIL CONTENT: "
                                                    + snippet
                                    );


                                    System.out.println(
                                            "AI RESULT: "
                                                    + label
                                    );


                                    System.out.println(
                                            "AI CONFIDENCE: "
                                                    + String.format(
                                                    Locale.US,
                                                    "%.2f%%",
                                                    confidence * 100
                                            )
                                    );


                                    // =================================================
                                    // ONLY SUBSCRIPTIONS
                                    // =================================================

                                    if (!result.isSubscription()) {

                                        System.out.println(
                                                "NOT A SUBSCRIPTION"
                                        );

                                        continue;
                                    }


                                    detectedCount++;


                                    System.out.println(
                                            "SUBSCRIPTION DETECTED"
                                    );


                                    // =================================================
                                    // EXTRACT DETAILS
                                    // =================================================

                                    SubscriptionEmailExtractor
                                            .SubscriptionDetails details =
                                            SubscriptionEmailExtractor.extract(
                                                    sender,
                                                    subject,
                                                    snippet
                                            );


                                    if (details == null) {

                                        System.out.println(
                                                "EXTRACTION FAILED"
                                        );

                                        continue;
                                    }


                                    String serviceName =
                                            safeString(
                                                    details.serviceName
                                            );


                                    String billingCycle =
                                            safeString(
                                                    details.billingCycle
                                            );


                                    String nextBillingDate =
                                            safeString(
                                                    details.nextBillingDate
                                            );


                                    double amount =
                                            details.amount;


                                    // =================================================
                                    // LOG EXTRACTED DATA
                                    // =================================================

                                    System.out.println(
                                            "SERVICE NAME: "
                                                    + serviceName
                                    );


                                    System.out.println(
                                            "AMOUNT: "
                                                    + details.currency
                                                    + " "
                                                    + amount
                                    );


                                    System.out.println(
                                            "BILLING CYCLE: "
                                                    + billingCycle
                                    );


                                    System.out.println(
                                            "NEXT BILLING DATE: "
                                                    + nextBillingDate
                                    );


                                    // =================================================
                                    // VALIDATE SERVICE NAME
                                    // =================================================

                                    if (serviceName.isEmpty()) {

                                        serviceName =
                                                extractServiceNameFromSender(
                                                        sender
                                                );
                                    }


                                    if (serviceName.isEmpty()) {

                                        serviceName =
                                                "Unknown Service";
                                    }


                                    // =================================================
                                    // DUPLICATE CHECK
                                    // =================================================

                                    boolean exists =
                                            databaseHelper
                                                    .subscriptionExists(
                                                            userId,
                                                            serviceName
                                                    );


                                    if (exists) {

                                        duplicateCount++;


                                        System.out.println(
                                                "DUPLICATE SUBSCRIPTION - NOT SAVED"
                                        );

                                        continue;
                                    }


                                    // =================================================
                                    // SAVE TO SQLITE DATABASE
                                    // =================================================

                                    long insertedId =
                                            databaseHelper
                                                    .insertSubscription(
                                                            userId,
                                                            serviceName,
                                                            amount,
                                                            billingCycle,
                                                            nextBillingDate
                                                    );


                                    if (insertedId != -1) {

                                        savedCount++;


                                        System.out.println(
                                                "************************************"
                                        );

                                        System.out.println(
                                                "SUBSCRIPTION SAVED SUCCESSFULLY"
                                        );

                                        System.out.println(
                                                "DATABASE ID: "
                                                        + insertedId
                                        );

                                        System.out.println(
                                                "SERVICE: "
                                                        + serviceName
                                        );

                                        System.out.println(
                                                "AMOUNT: "
                                                        + amount
                                        );

                                        System.out.println(
                                                "CYCLE: "
                                                        + billingCycle
                                        );

                                        System.out.println(
                                                "NEXT BILLING: "
                                                        + nextBillingDate
                                        );

                                        System.out.println(
                                                "************************************"
                                        );


                                    } else {

                                        System.out.println(
                                                "FAILED TO SAVE SUBSCRIPTION"
                                        );
                                    }
                                }


                                // =================================================
                                // REFRESH UI
                                // =================================================

                                final int finalDetectedCount =
                                        detectedCount;

                                final int finalSavedCount =
                                        savedCount;

                                final int finalDuplicateCount =
                                        duplicateCount;


                                runOnUiThread(
                                        () -> {

                                            // Reload database
                                            loadSubscriptions();


                                            String message =
                                                    "AI Scan Complete\n"
                                                            + "Detected: "
                                                            + finalDetectedCount
                                                            + "\n"
                                                            + "Saved: "
                                                            + finalSavedCount
                                                            + "\n"
                                                            + "Already exists: "
                                                            + finalDuplicateCount;


                                            Toast.makeText(
                                                    SubscriptionActivity.this,
                                                    message,
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        }
                                );
                            }


                            @Override
                            public void onError(
                                    String error
                            ) {

                                runOnUiThread(
                                        () -> Toast.makeText(
                                                SubscriptionActivity.this,
                                                "Gmail scan failed:\n"
                                                        + error,
                                                Toast.LENGTH_LONG
                                        ).show()
                                );
                            }
                        }
                );
    }


    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safeString(String value) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }


    // =========================================================
    // FALLBACK SERVICE NAME
    // =========================================================

    private String extractServiceNameFromSender(
            String sender
    ) {

        if (sender == null ||
                sender.trim().isEmpty()) {

            return "";
        }


        String value =
                sender.trim();


        // Example:
        // Netflix <info@netflix.com>
        // Coursera <coursera@learn.coursera.org>

        int lessThan =
                value.indexOf("<");


        if (lessThan > 0) {

            value =
                    value.substring(
                            0,
                            lessThan
                    ).trim();
        }


        // Remove quotation marks

        value =
                value.replace(
                        "\"",
                        ""
                ).trim();


        return value;
    }

    private void updateSummary() {

        if (databaseHelper == null) {
            return;
        }


        if (userId <= 0) {
            return;
        }


        double total =
                databaseHelper
                        .getTotalSubscriptionAmount(
                                userId
                        );

        int count =
                databaseHelper
                        .getSubscriptionCount(
                                userId
                        );


        tvMonthlySpend.setText(
                String.format(
                        Locale.US,
                        "Rs %.2f",
                        total
                )
        );


        tvActiveSubscriptions.setText(
                count + " Services"
        );


        // -----------------------------------------------------
        // Next billing date
        // -----------------------------------------------------

        Cursor cursor =
                null;


        try {

            cursor =
                    databaseHelper
                            .getNextSubscription(
                                    userId
                            );


            if (
                    cursor != null &&
                            cursor.moveToFirst()
            ) {

                String nextDate =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper
                                                .NEXT_BILLING_DATE
                                )
                        );


                if (nextDate == null ||
                        nextDate.trim().isEmpty()) {

                    tvNextDue.setText(
                            "No Due"
                    );

                } else {

                    tvNextDue.setText(
                            nextDate
                    );
                }

            } else {

                tvNextDue.setText(
                        "No Due"
                );
            }

        } catch (Exception e) {

            tvNextDue.setText(
                    "No Due"
            );

            e.printStackTrace();

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }


        // -----------------------------------------------------
        // AI Insight
        // -----------------------------------------------------

        try {

            String insight =
                    databaseHelper
                            .generateAIInsight(
                                    userId
                            );


            if (insight == null ||
                    insight.trim().isEmpty()) {

                tvOptimization.setText(
                        "No optimization tips available."
                );

            } else {

                tvOptimization.setText(
                        insight
                );
            }

        } catch (Exception e) {

            tvOptimization.setText(
                    "No optimization tips available."
            );
        }
    }


    // =========================================================
    // BUTTON LISTENERS
    // =========================================================

    private void setupListeners() {

        // -----------------------------------------------------
        // ADD SUBSCRIPTION
        // -----------------------------------------------------

        fabAddSubscription.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    this,
                                    AddSubscriptionActivity.class
                            );

                    startActivity(intent);
                }
        );


        // -----------------------------------------------------
        // CONNECT GMAIL
        // -----------------------------------------------------

        btnConnectGmail.setOnClickListener(
                v -> {

                    if (gmailAuthManager == null) {

                        Toast.makeText(
                                this,
                                "Gmail manager unavailable.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    gmailAuthManager.connectGmail(
                            SubscriptionActivity.this
                    );
                }
        );
    }


    // =========================================================
    // ACTIVITY RESULT
    // =========================================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );


        if (gmailAuthManager == null) {
            return;
        }


        boolean handled =
                gmailAuthManager
                        .handleActivityResult(
                                requestCode,
                                resultCode,
                                data
                        );


        if (
                !handled
                        || !gmailAuthManager.isConnected()
        ) {

            return;
        }


        // =====================================================
        // GMAIL CONNECTED
        // =====================================================

        tvGmailStatus.setText(
                "Gmail connected"
        );


        Toast.makeText(
                this,
                "Gmail connected successfully!",
                Toast.LENGTH_SHORT
        ).show();


        // =====================================================
        // GET GOOGLE ACCOUNT
        // =====================================================

        GoogleSignInAccount account =
                gmailAuthManager
                        .getSignedInAccount();


        if (account == null) {

            Toast.makeText(
                    this,
                    "Google account is not available.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =====================================================
        // INITIALIZE GMAIL SERVICE
        // =====================================================

        try {

            gmailServiceManager.initialize(
                    account
            );


            System.out.println(
                    "GMAIL SERVICE INITIALIZED SUCCESSFULLY"
            );


            // =================================================
            // START AI SCAN
            // =================================================

            readGmailSubscriptions();


        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Gmail setup failed:\n"
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();


        if (databaseHelper != null &&
                userId > 0) {

            loadSubscriptions();
        }
    }


    @Override
    protected void onDestroy() {

        if (subscriptionDetectionAI != null) {

            subscriptionDetectionAI.close();

            subscriptionDetectionAI = null;
        }


        if (gmailServiceManager != null) {

            gmailServiceManager.shutdown();

            gmailServiceManager = null;
        }


        super.onDestroy();
    }
}