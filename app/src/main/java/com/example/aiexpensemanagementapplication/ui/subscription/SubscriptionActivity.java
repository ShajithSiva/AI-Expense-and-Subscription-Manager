package com.example.aiexpensemanagementapplication.ui.subscription;

import android.content.Intent;
import com.example.aiexpensemanagementapplication.data.remote.subscription.UsagePredictionRequest;
import com.example.aiexpensemanagementapplication.data.remote.subscription.UsagePredictionResponse;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import com.example.aiexpensemanagementapplication.data.remote.subscription.UsagePredictionRequest;
import com.example.aiexpensemanagementapplication.data.remote.subscription.UsagePredictionResponse;
import com.example.aiexpensemanagementapplication.data.remote.subscription.UsageApiClient;
import com.example.aiexpensemanagementapplication.data.remote.subscription.UsageApiService;
import com.example.aiexpensemanagementapplication.data.remote.subscription.UsageApiClient;
import com.example.aiexpensemanagementapplication.data.remote.subscription.UsageApiService;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.data.remote.gmail.GmailAuthManager;
import com.example.aiexpensemanagementapplication.data.remote.gmail.GmailServiceManager;
import com.example.aiexpensemanagementapplication.data.remote.subscription.ApiClient;
import com.example.aiexpensemanagementapplication.data.remote.subscription.ApiService;
import com.example.aiexpensemanagementapplication.data.remote.subscription.PredictionRequest;
import com.example.aiexpensemanagementapplication.data.remote.subscription.PredictionResponse;
import com.example.aiexpensemanagementapplication.model.Subscription;
import com.example.aiexpensemanagementapplication.ui.dashboard.DashboardActivity;
import com.example.aiexpensemanagementapplication.ui.expense.ExpenseListActivity;
import com.example.aiexpensemanagementapplication.ui.income.IncomeListActivity;
import com.example.aiexpensemanagementapplication.ui.profile.ProfileActivity;
import com.example.aiexpensemanagementapplication.data.UsageStatsHelper;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.aiexpensemanagementapplication.notification.NotificationConstants;
import com.example.aiexpensemanagementapplication.notification.NotificationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
    private Button btnViewDetectedSubscriptions;


    // =========================================================
    // DATABASE
    // =========================================================

    private DatabaseHelper databaseHelper;

    // =========================================================
// USAGE STATS
// =========================================================

    private UsageStatsHelper usageStatsHelper;


    // =========================================================
    // GMAIL
    // =========================================================

    private GmailAuthManager gmailAuthManager;

    private GmailServiceManager gmailServiceManager;


    // =========================================================
    // V3 FASTAPI
    // =========================================================

    private ApiService apiService;

    private UsageApiService usageApiService;

    private Call<PredictionResponse> activePredictionCall;


    private Call<UsagePredictionResponse> activeUsagePredictionCall;


    // =========================================================
    // SCAN STATE
    // =========================================================

    private boolean gmailScanInProgress = false;


    // =========================================================
    // SUBSCRIPTIONS
    // =========================================================

    private final ArrayList<Subscription> subscriptionList =
            new ArrayList<>();

    private SubscriptionAdapter adapter;


    // =========================================================
    // USER
    // =========================================================

    private int userId = -1;

    private NotificationHelper notificationHelper;


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

        notificationHelper =
                new NotificationHelper(this);

        usageStatsHelper =
                new UsageStatsHelper(this);

        gmailAuthManager =
                new GmailAuthManager(this);


        gmailServiceManager =
                new GmailServiceManager(this);


        try {

            apiService =
                    ApiClient.getApiService();

        } catch (Exception e) {

            apiService = null;

            e.printStackTrace();
        }

        try {

            usageApiService =
                    UsageApiClient.getApiService();

        } catch (Exception e) {

            usageApiService = null;

            e.printStackTrace();
        }


        if (!initializeUser()) {

            return;
        }


        setupRecyclerView();

        setupListeners();

        setupBottomNavigation();
        testFacebookDailyUsage();
        loadSubscriptions();

        checkUsageAccessPermission();

        saveAllSubscriptionUsage();

        testAllSubscriptionUsage();

        updateGmailUi();

        updatePendingReviewButton();
    }

    private void saveAllSubscriptionUsage() {

        if (usageStatsHelper == null ||
                databaseHelper == null) {

            return;
        }

        if (!usageStatsHelper.hasUsageAccessPermission()) {

            Log.d(
                    "USAGE_SAVE",
                    "Usage Access permission is not enabled."
            );

            return;
        }

        if (subscriptionList.isEmpty()) {

            Log.d(
                    "USAGE_SAVE",
                    "No subscriptions found."
            );

            return;
        }

        for (Subscription subscription :
                subscriptionList) {

            if (subscription == null) {
                continue;
            }

            int subscriptionId =
                    subscription.getSubscriptionId();

            String appName =
                    subscription.getServiceName();

            // Find installed app package
            String packageName =
                    findMatchingPackage(appName);

            if (packageName == null) {

                Log.d(
                        "USAGE_SAVE",
                        "App not installed: " + appName
                );

                continue;
            }

            // Get 30-day usage
            long usageMinutes =
                    usageStatsHelper
                            .testWeeklyUsageLast30Days(
                                    packageName
                            );

            // Convert subscription cost to monthly cost
            double monthlyCost =
                    convertToMonthlyAmount(
                            subscription.getAmount(),
                            subscription.getBillingCycle()
                    );

            // Check whether usage record already exists
            Cursor cursor =
                    databaseHelper
                            .getUsageBySubscription(
                                    subscriptionId
                            );

            try {

                if (cursor != null &&
                        cursor.moveToFirst()) {

                    // Existing record → UPDATE

                    int usageId =
                            cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            DatabaseHelper.USAGE_ID
                                    )
                            );

                    databaseHelper.updateUsageData(
                            usageId,
                            (int) usageMinutes,
                            monthlyCost,
                            "",
                            0
                    );

                    Log.d(
                            "USAGE_SAVE",
                            "Usage updated: " + appName
                    );

                } else {

                    // No record → INSERT

                    databaseHelper.insertUsageData(
                            subscriptionId,
                            appName,
                            (int) usageMinutes,
                            monthlyCost,
                            "",
                            0
                    );

                    Log.d(
                            "USAGE_SAVE",
                            "Usage inserted: " + appName
                    );
                }

            } finally {

                if (cursor != null) {
                    cursor.close();
                }
            }

            Log.d(
                    "USAGE_SAVE",
                    "SubscriptionID: " +
                            subscriptionId
            );

            Log.d(
                    "USAGE_SAVE",
                    "AppName: " +
                            appName
            );

            Log.d(
                    "USAGE_SAVE",
                    "UsageMinutes: " +
                            usageMinutes
            );

            Log.d(
                    "USAGE_SAVE",
                    "MonthlyCost: " +
                            monthlyCost
            );
            predictSubscriptionUsage(
                    appName,
                    monthlyCost,
                    usageMinutes
            );
        }
    }

    private void predictSubscriptionUsage(
            String appName,
            double monthlyCost,
            long usageMinutes
    ) {

        if (usageApiService == null) {
            Log.d(
                    "USAGE_PREDICTION",
                    "ApiService is null."
            );
            return;
        }

        UsagePredictionRequest request =
                new UsagePredictionRequest(
                        monthlyCost,
                        (int) usageMinutes
                );

        Log.d(
                "USAGE_PREDICTION",
                "Sending prediction request..."
        );

        Log.d(
                "USAGE_PREDICTION",
                "AppName: " + appName
        );

        Log.d(
                "USAGE_PREDICTION",
                "MonthlyCost: " + monthlyCost
        );

        Log.d(
                "USAGE_PREDICTION",
                "UsageMinutes: " + usageMinutes
        );

        usageApiService
                .predictSubscriptionUsage(request)
                .enqueue(new Callback<UsagePredictionResponse>() {

                    @Override
                    public void onResponse(
                            Call<UsagePredictionResponse> call,
                            Response<UsagePredictionResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            String prediction =
                                    response.body().getPrediction();

                            Log.d(
                                    "USAGE_PREDICTION",
                                    "================================"
                            );

                            Log.d(
                                    "USAGE_PREDICTION",
                                    "AppName: " + appName
                            );

                            Log.d(
                                    "USAGE_PREDICTION",
                                    "Prediction: " + prediction
                            );

                            Log.d(
                                    "USAGE_PREDICTION",
                                    "================================"
                            );

                            showSubscriptionUsageNotification(
                                    appName,
                                    monthlyCost,
                                    usageMinutes,
                                    prediction
                            );

                        } else {

                            Log.d(
                                    "USAGE_PREDICTION",
                                    "Prediction failed. HTTP Code: "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<UsagePredictionResponse> call,
                            Throwable t
                    ) {

                        Log.e(
                                "USAGE_PREDICTION",
                                "API call failed: "
                                        + t.getMessage(),
                                t
                        );
                    }
                });
    }

    private void showSubscriptionUsageNotification(
            String appName,
            double monthlyCost,
            long usageMinutes,
            String prediction
    ) {

        if (notificationHelper == null) {
            return;
        }

        if (prediction == null) {
            return;
        }

        String status;
        String message;

        if (prediction.equalsIgnoreCase("Useful")) {

            status = "Useful";

            message =
                    "You spend LKR "
                            + String.format(
                            Locale.getDefault(),
                            "%.2f",
                            monthlyCost
                    )
                            + " per month on "
                            + appName
                            + ". You used it for "
                            + usageMinutes
                            + " minutes in the last 30 days. "
                            + "This subscription is Useful based on your usage.";

        } else if (prediction.equalsIgnoreCase("Low_Usage")) {

            status = "Low Usage";

            message =
                    "You spend LKR "
                            + String.format(
                            Locale.getDefault(),
                            "%.2f",
                            monthlyCost
                    )
                            + " per month on "
                            + appName
                            + ". You used it for "
                            + usageMinutes
                            + " minutes in the last 30 days. "
                            + "This subscription has Low Usage. "
                            + "Consider reviewing whether it is worth the monthly cost.";

        } else if (prediction.equalsIgnoreCase("Unused")) {

            status = "Unused";

            message =
                    "You spend LKR "
                            + String.format(
                            Locale.getDefault(),
                            "%.2f",
                            monthlyCost
                    )
                            + " per month on "
                            + appName
                            + ", but you used it for only "
                            + usageMinutes
                            + " minutes in the last 30 days. "
                            + "This subscription is Unused based on your usage. "
                            + "Consider cancelling it to save money.";

        } else {

            status = prediction;

            message =
                    "You spend LKR "
                            + String.format(
                            Locale.getDefault(),
                            "%.2f",
                            monthlyCost
                    )
                            + " per month on "
                            + appName
                            + ". Your 30-day usage is "
                            + usageMinutes
                            + " minutes. "
                            + "Subscription status: "
                            + prediction
                            + ".";

        }

        int notificationId =
                NotificationConstants.SUBSCRIPTION_USAGE_ANALYSIS_BASE_ID;

        notificationId += Math.abs(
                appName.hashCode()
        );

        notificationHelper.showNotification(
                notificationId,
                appName + " Subscription Analysis",
                message,
                status
        );
    }

    // =========================================================
// FIND INSTALLED APP PACKAGE
// =========================================================

    private String findMatchingPackage(String serviceName) {

        if (serviceName == null ||
                serviceName.trim().isEmpty()) {

            return null;
        }

        String targetName =
                serviceName.trim()
                        .toLowerCase(Locale.ROOT);

        PackageManager packageManager =
                getPackageManager();

        Intent launchIntent =
                new Intent(Intent.ACTION_MAIN);

        launchIntent.addCategory(
                Intent.CATEGORY_LAUNCHER
        );

        List<ResolveInfo> installedApps =
                packageManager.queryIntentActivities(
                        launchIntent,
                        PackageManager.MATCH_ALL
                );

        for (ResolveInfo resolveInfo : installedApps) {

            if (resolveInfo == null ||
                    resolveInfo.activityInfo == null) {

                continue;
            }

            CharSequence appLabel =
                    resolveInfo.loadLabel(
                            packageManager
                    );

            if (appLabel == null) {
                continue;
            }

            String installedAppName =
                    appLabel.toString()
                            .trim()
                            .toLowerCase(Locale.ROOT);

            if (installedAppName.equals(targetName)) {

                return resolveInfo.activityInfo.packageName;
            }
        }

        return null;
    }

    // =========================================================
// TEST SUBSCRIPTION APP MATCHING
// =========================================================

    // =========================================================
// TEST SUBSCRIPTION APP MATCHING
// =========================================================

    private void testSubscriptionAppMatching() {

        if (subscriptionList == null ||
                subscriptionList.isEmpty()) {

            Log.d(
                    "SUBSCRIPTION_MATCH",
                    "No subscriptions found."
            );

            return;
        }

        for (Subscription subscription : subscriptionList) {

            if (subscription == null) {
                continue;
            }

            String serviceName =
                    subscription.getServiceName();

            String packageName =
                    findMatchingPackage(serviceName);

            Log.d(
                    "SUBSCRIPTION_MATCH",
                    "Service Name: " + serviceName
            );

            Log.d(
                    "SUBSCRIPTION_MATCH",
                    "Package Name: " + packageName
            );

            Log.d(
                    "SUBSCRIPTION_MATCH",
                    "--------------------------------"
            );
        }
    }

    private void testFacebookDailyUsage() {

        if (usageStatsHelper == null) {
            Log.d(
                    "FACEBOOK_DAILY",
                    "UsageStatsHelper is null."
            );
            return;
        }

        if (!usageStatsHelper.hasUsageAccessPermission()) {
            Log.d(
                    "FACEBOOK_DAILY",
                    "Usage Access permission is not enabled."
            );
            return;
        }

        String packageName =
                findMatchingPackage("Facebook");

        if (packageName == null) {
            Log.d(
                    "FACEBOOK_DAILY",
                    "Facebook package not found."
            );
            return;
        }

        Log.d(
                "FACEBOOK_DAILY",
                "Package: " + packageName
        );

        android.app.usage.UsageStatsManager usageStatsManager =
                (android.app.usage.UsageStatsManager)
                        getSystemService(
                                android.content.Context.USAGE_STATS_SERVICE
                        );

        java.util.Calendar calendar =
                java.util.Calendar.getInstance();

        long endTime =
                calendar.getTimeInMillis();

        calendar.add(
                java.util.Calendar.DAY_OF_YEAR,
                -30
        );

        long startTime =
                calendar.getTimeInMillis();

        java.util.List<android.app.usage.UsageStats> usageStatsList =
                usageStatsManager.queryUsageStats(
                        android.app.usage.UsageStatsManager.INTERVAL_DAILY,
                        startTime,
                        endTime
                );

        long totalMinutes = 0;

        if (usageStatsList != null) {

            for (android.app.usage.UsageStats usageStats :
                    usageStatsList) {

                if (usageStats == null) {
                    continue;
                }

                if (!packageName.equals(
                        usageStats.getPackageName()
                )) {
                    continue;
                }

                long foregroundMillis =
                        usageStats.getTotalTimeInForeground();

                long minutes =
                        foregroundMillis / (1000 * 60);

                java.text.SimpleDateFormat dateFormat =
                        new java.text.SimpleDateFormat(
                                "yyyy-MM-dd",
                                java.util.Locale.getDefault()
                        );

                String date =
                        dateFormat.format(
                                new java.util.Date(
                                        usageStats.getFirstTimeStamp()
                                )
                        );

                Log.d(
                        "FACEBOOK_DAILY",
                        "Date: " + date +
                                " | Minutes: " + minutes
                );

                totalMinutes += minutes;
            }
        }

        Log.d(
                "FACEBOOK_DAILY",
                "================================"
        );

        Log.d(
                "FACEBOOK_DAILY",
                "TOTAL DAILY USAGE: " +
                        totalMinutes +
                        " minutes"
        );

        Log.d(
                "FACEBOOK_DAILY",
                "TOTAL HOURS: " +
                        (totalMinutes / 60.0)
        );

        Log.d(
                "FACEBOOK_DAILY",
                "================================"
        );
    }
    private void testAllSubscriptionUsage() {

        if (usageStatsHelper == null) {

            Log.d(
                    "SUB_USAGE",
                    "UsageStatsHelper is null."
            );

            return;
        }

        if (!usageStatsHelper.hasUsageAccessPermission()) {

            Log.d(
                    "SUB_USAGE",
                    "Usage Access permission is not enabled."
            );

            return;
        }

        if (subscriptionList == null ||
                subscriptionList.isEmpty()) {

            Log.d(
                    "SUB_USAGE",
                    "No subscriptions found."
            );

            return;
        }

        for (Subscription subscription :
                subscriptionList) {

            if (subscription == null) {
                continue;
            }

            String serviceName =
                    subscription.getServiceName();

            String packageName =
                    findMatchingPackage(serviceName);

            Log.d(
                    "SUB_USAGE",
                    "Service: " + serviceName
            );

            Log.d(
                    "SUB_USAGE",
                    "Package: " + packageName
            );

            if (packageName == null) {

                Log.d(
                        "SUB_USAGE",
                        "App not installed or package not found."
                );

                continue;
            }

            long usageMinutes =
                    usageStatsHelper
                            .getUsageMinutesLast30DaysWeekly(
                                    packageName
                            );

            double usageHours =
                    usageMinutes / 60.0;

            Log.d(
                    "SUB_USAGE",
                    "30-Day Usage Minutes: "
                            + usageMinutes
            );

            Log.d(
                    "SUB_USAGE",
                    "30-Day Usage Hours: "
                            + usageHours
            );

            Log.d(
                    "SUB_USAGE",
                    "--------------------------------"
            );
        }
    }
    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        toolbar =
                findViewById(
                        R.id.toolbar
                );


        rvSubscriptions =
                findViewById(
                        R.id.rvSubscriptions
                );


        fabAddSubscription =
                findViewById(
                        R.id.fabAddSubscription
                );


        bottomNavigation =
                findViewById(
                        R.id.bottomNavigation
                );


        layoutEmpty =
                findViewById(
                        R.id.layoutEmpty
                );


        tvMonthlySpend =
                findViewById(
                        R.id.tvMonthlySpend
                );


        tvActiveSubscriptions =
                findViewById(
                        R.id.tvActiveSubscriptions
                );


        tvNextDue =
                findViewById(
                        R.id.tvNextDue
                );


        tvOptimization =
                findViewById(
                        R.id.tvOptimization
                );


        tvGmailStatus =
                findViewById(
                        R.id.tvGmailStatus
                );


        btnConnectGmail =
                findViewById(
                        R.id.btnConnectGmail
                );

        btnViewDetectedSubscriptions =
                findViewById(
                        R.id.btnViewDetectedSubscriptions
                );
    }


    // =========================================================
    // TOOLBAR
    // =========================================================

    private void setupToolbar() {

        setSupportActionBar(
                toolbar
        );


        if (getSupportActionBar() != null) {

            getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(
                            true
                    );


            getSupportActionBar()
                    .setDisplayShowHomeEnabled(
                            true
                    );
        }


        toolbar.setNavigationOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // USER
    // =========================================================

    private boolean initializeUser() {

        FirebaseUser firebaseUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();


        if (firebaseUser == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_LONG
            ).show();


            finish();

            return false;
        }


        userId =
                databaseHelper
                        .getUserIdByFirebaseUid(
                                firebaseUser.getUid()
                        );


        if (userId <= 0) {

            Toast.makeText(
                    this,
                    "Local user record not found. Please login again.",
                    Toast.LENGTH_LONG
            ).show();


            finish();

            return false;
        }


        return true;
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

                            if (subscription == null) {

                                return;
                            }


                            Intent intent =
                                    new Intent(
                                            SubscriptionActivity.this,
                                            EditSubscriptionActivity.class
                                    );


                            intent.putExtra(
                                    "subscriptionId",
                                    subscription.getSubscriptionId()
                            );


                            startActivity(
                                    intent
                            );
                        }
                );


        rvSubscriptions.setLayoutManager(
                new LinearLayoutManager(
                        this
                )
        );


        rvSubscriptions.setAdapter(
                adapter
        );
    }


    // =========================================================
    // LOAD ACTIVE SUBSCRIPTIONS
    // =========================================================

    private void loadSubscriptions() {

        if (databaseHelper == null ||
                userId <= 0) {

            return;
        }


        subscriptionList.clear();


        Cursor cursor = null;


        try {

            cursor =
                    databaseHelper
                            .getSubscriptions(
                                    userId
                            );


            if (cursor != null) {

                while (cursor.moveToNext()) {

                    Subscription subscription =
                            new Subscription();


                    subscription.setSubscriptionId(
                            cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            DatabaseHelper.SUBSCRIPTION_ID
                                    )
                            )
                    );


                    int userIdIndex =
                            cursor.getColumnIndex(
                                    DatabaseHelper.USER_ID
                            );


                    if (userIdIndex != -1) {

                        subscription.setUserId(
                                cursor.getInt(
                                        userIdIndex
                                )
                        );
                    }


                    subscription.setServiceName(
                            cursor.getString(
                                    cursor.getColumnIndexOrThrow(
                                            DatabaseHelper.SERVICE_NAME
                                    )
                            )
                    );


                    subscription.setAmount(
                            cursor.getDouble(
                                    cursor.getColumnIndexOrThrow(
                                            DatabaseHelper.AMOUNT
                                    )
                            )
                    );


                    int currencyIndex =
                            cursor.getColumnIndex(
                                    DatabaseHelper.SUBSCRIPTION_CURRENCY
                            );


                    if (currencyIndex != -1 &&
                            !cursor.isNull(currencyIndex)) {

                        subscription.setCurrency(
                                cursor.getString(
                                        currencyIndex
                                )
                        );


                    } else {

                        subscription.setCurrency(
                                null
                        );
                    }


                    subscription.setBillingCycle(
                            cursor.getString(
                                    cursor.getColumnIndexOrThrow(
                                            DatabaseHelper.BILLING_CYCLE
                                    )
                            )
                    );


                    subscription.setNextBillingDate(
                            cursor.getString(
                                    cursor.getColumnIndexOrThrow(
                                            DatabaseHelper.NEXT_BILLING_DATE
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


        updateEmptyState();

        updateSummary();
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
    // BUTTON LISTENERS
    // =========================================================

    private void setupListeners() {

        // =====================================================
        // MANUAL ADD
        // =====================================================

        fabAddSubscription.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    SubscriptionActivity.this,
                                    AddSubscriptionActivity.class
                            );


                    startActivity(
                            intent
                    );
                }
        );


        // =====================================================
        // CONNECT / SCAN GMAIL
        // =====================================================

        btnConnectGmail.setOnClickListener(
                v -> {

                    if (gmailScanInProgress) {

                        return;
                    }


                    if (gmailAuthManager == null) {

                        Toast.makeText(
                                this,
                                "Gmail manager unavailable.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    // Already connected → scan directly

                    if (gmailAuthManager.isConnected()) {

                        startGmailScan();

                        return;
                    }


                    // Not connected → connect first

                    gmailAuthManager.connectGmail(
                            SubscriptionActivity.this
                    );
                }
        );
        btnViewDetectedSubscriptions.setOnClickListener(
                v -> {

                    int pendingCount =
                            databaseHelper
                                    .getPendingSubscriptionCount(
                                            userId
                                    );


                    if (pendingCount <= 0) {

                        Toast.makeText(
                                SubscriptionActivity.this,
                                "No detected subscriptions waiting for review.",
                                Toast.LENGTH_SHORT
                        ).show();

                        updatePendingReviewButton();

                        return;
                    }


                    Intent intent =
                            new Intent(
                                    SubscriptionActivity.this,
                                    PendingSubscriptionsActivity.class
                            );


                    startActivity(
                            intent
                    );
                }
        );
    }


    // =========================================================
    // START GMAIL SCAN
    // =========================================================

    private void startGmailScan() {

        if (gmailScanInProgress) {

            return;
        }


        if (apiService == null) {

            Toast.makeText(
                    this,
                    "Subscription API is unavailable.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        if (gmailServiceManager == null) {

            Toast.makeText(
                    this,
                    "Gmail service is unavailable.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        GoogleSignInAccount account =
                gmailAuthManager
                        .getSignedInAccount();


        if (account == null) {

            Toast.makeText(
                    this,
                    "Google account is unavailable.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        try {

            gmailServiceManager.initialize(
                    account
            );


        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Gmail initialization failed:\n"
                            + safeString(
                            e.getMessage()
                    ),
                    Toast.LENGTH_LONG
            ).show();


            return;
        }


        gmailScanInProgress = true;


        btnConnectGmail.setEnabled(
                false
        );


        btnConnectGmail.setText(
                "Scanning..."
        );


        tvGmailStatus.setText(
                "Reading subscription emails..."
        );


        Toast.makeText(
                this,
                "Scanning Gmail...",
                Toast.LENGTH_SHORT
        ).show();


        gmailServiceManager
                .readSubscriptionEmails(

                        new GmailServiceManager.GmailCallback() {

                            @Override
                            public void onSuccess(
                                    ArrayList<GmailServiceManager.GmailMessageData> messages
                            ) {

                                if (messages == null ||
                                        messages.isEmpty()) {

                                    finishScan(
                                            0,
                                            0,
                                            0,
                                            "No matching emails found."
                                    );

                                    return;
                                }


                                System.out.println(
                                        "========================================"
                                );

                                System.out.println(
                                        "GMAIL MESSAGES FOUND: "
                                                + messages.size()
                                );

                                System.out.println(
                                        "========================================"
                                );


                                processNextEmail(

                                        messages,

                                        0,

                                        0,

                                        0,

                                        0
                                );
                            }


                            @Override
                            public void onError(
                                    String error
                            ) {

                                finishScan(
                                        0,
                                        0,
                                        0,
                                        "Gmail scan failed:\n"
                                                + safeString(error)
                                );
                            }
                        }
                );
    }


    // =========================================================
    // PROCESS EMAILS SEQUENTIALLY
    // =========================================================

    private void processNextEmail(
            ArrayList<GmailServiceManager.GmailMessageData> messages,
            int index,
            int detectedCount,
            int newPendingCount,
            int skippedCount
    ) {

        // =====================================================
        // ALL EMAILS FINISHED
        // =====================================================

        if (messages == null ||
                index >= messages.size()) {

            finishScan(
                    detectedCount,
                    newPendingCount,
                    skippedCount,
                    null
            );

            return;
        }


        GmailServiceManager.GmailMessageData gmailMessage =
                messages.get(
                        index
                );


        if (gmailMessage == null) {

            processNextEmail(

                    messages,

                    index + 1,

                    detectedCount,

                    newPendingCount,

                    skippedCount
            );

            return;
        }


        String messageId =
                safeString(
                        gmailMessage.getId()
                );


        String sender =
                safeString(
                        gmailMessage.getSender()
                );


        String subject =
                safeString(
                        gmailMessage.getSubject()
                );


        String snippet =
                safeString(
                        gmailMessage.getSnippet()
                );


        String fullBody =
                safeString(
                        gmailMessage.getFullBody()
                );

        // =====================================================
// ALREADY PROCESSED?
//
// PENDING / SAVED / IGNORED subscription messages
// and Non-Subscription messages can all be skipped.
// =====================================================

        if (!messageId.isEmpty()) {

            boolean alreadyProcessed =
                    databaseHelper
                            .isGmailMessageProcessed(
                                    userId,
                                    messageId
                            );


            boolean alreadyPendingOrReviewed =
                    databaseHelper
                            .pendingSubscriptionExistsByMessageId(
                                    userId,
                                    messageId
                            );


            if (alreadyProcessed ||
                    alreadyPendingOrReviewed) {

                System.out.println(
                        "SKIPPING ALREADY PROCESSED EMAIL: "
                                + messageId
                );


                processNextEmail(

                        messages,

                        index + 1,

                        detectedCount,

                        newPendingCount,

                        skippedCount + 1
                );


                return;
            }
        }


        // =====================================================
        // FULL EMAIL BODY PREFERRED
        // =====================================================

        String bodyForModel;


        if (!fullBody.isEmpty()) {

            bodyForModel =
                    fullBody;


        } else {

            bodyForModel =
                    snippet;
        }


        String modelText =
                buildModelText(
                        sender,
                        subject,
                        bodyForModel
                );


        if (modelText.isEmpty()) {

            processNextEmail(

                    messages,

                    index + 1,

                    detectedCount,

                    newPendingCount,

                    skippedCount
            );

            return;
        }


        System.out.println(
                "========================================"
        );

        System.out.println(
                "EMAIL "
                        + (index + 1)
                        + " / "
                        + messages.size()
        );

        System.out.println(
                "MESSAGE ID: "
                        + messageId
        );

        System.out.println(
                "SUBJECT: "
                        + subject
        );

        System.out.println(
                "FULL TEXT SENT TO V3 API:"
        );

        System.out.println(
                modelText
        );

        System.out.println(
                "========================================"
        );


        tvGmailStatus.setText(
                "Analyzing email "
                        + (index + 1)
                        + " of "
                        + messages.size()
        );


        PredictionRequest request =
                new PredictionRequest(
                        modelText
                );


        activePredictionCall =
                apiService.predict(
                        request
                );


        activePredictionCall.enqueue(

                new Callback<PredictionResponse>() {

                    @Override
                    public void onResponse(
                            Call<PredictionResponse> call,
                            Response<PredictionResponse> response
                    ) {

                        activePredictionCall = null;


                        if (isFinishing() ||
                                isDestroyed()) {

                            return;
                        }


                        // =================================================
                        // API HTTP ERROR
                        // =================================================

                        if (!response.isSuccessful()) {

                            System.out.println(
                                    "API ERROR HTTP "
                                            + response.code()
                            );


                            processNextEmail(

                                    messages,

                                    index + 1,

                                    detectedCount,

                                    newPendingCount,

                                    skippedCount
                            );


                            return;
                        }


                        PredictionResponse prediction =
                                response.body();


                        if (prediction == null) {

                            processNextEmail(

                                    messages,

                                    index + 1,

                                    detectedCount,

                                    newPendingCount,

                                    skippedCount
                            );


                            return;
                        }


                        logPrediction(
                                prediction
                        );


                        // =================================================
                        // NOT SUBSCRIPTION
                        // =================================================

                        if (!prediction.isSubscription()) {

                            System.out.println(
                                    "V3 RESULT: NON-SUBSCRIPTION"
                            );


                            // =================================================
                            // API SUCCESSFUL → REMEMBER THIS EMAIL
                            // =================================================

                            if (!messageId.isEmpty()) {

                                databaseHelper
                                        .markGmailMessageProcessed(

                                                userId,

                                                messageId,

                                                safeString(
                                                        prediction.getPredictedLabel()
                                                ),

                                                prediction
                                                        .getSubscriptionProbability()
                                        );
                            }


                            processNextEmail(

                                    messages,

                                    index + 1,

                                    detectedCount,

                                    newPendingCount,

                                    skippedCount
                            );


                            return;
                        }


                        int updatedDetectedCount =
                                detectedCount + 1;


                        PredictionResponse.ExtractedDetails details =
                                prediction.getExtractedDetails();


                        // =================================================
                        // DETAILS MAY BE PARTIAL
                        //
                        // Even if amount/cycle/date missing,
                        // save it as pending.
                        // User can correct in Review screen.
                        // =================================================

                        String serviceName = "";

                        double amount = 0.0;

                        String currency = "";

                        String billingCycle = "";

                        String nextBillingDate = "";


                        if (details != null) {

                            serviceName =
                                    safeString(
                                            details.getService()
                                    );


                            amount =
                                    details.getAmountAsDouble();


                            currency =
                                    normalizeSubscriptionCurrency(
                                            details.getCurrency()
                                    );


                            billingCycle =
                                    safeString(
                                            details.getBillingCycle()
                                    );


                            nextBillingDate =
                                    safeString(
                                            details.getNextBillingDate()
                                    );
                        }


                        // =================================================
                        // SERVICE FALLBACK
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


                        System.out.println(
                                "SUBSCRIPTION DETECTED"
                        );

                        System.out.println(
                                "SERVICE: "
                                        + serviceName
                        );

                        System.out.println(
                                "AMOUNT: "
                                        + currency
                                        + " "
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


                        // =================================================
                        // ALREADY ACTIVE SUBSCRIPTION
                        //
                        // Don't create another "new subscription"
                        // entry for already-saved services.
                        // =================================================

                        if (!serviceName.equalsIgnoreCase(
                                "Unknown Service"
                        )) {

                            boolean activeExists =
                                    databaseHelper
                                            .subscriptionExists(
                                                    userId,
                                                    serviceName
                                            );


                            if (activeExists) {

                                System.out.println(
                                        "ACTIVE SUBSCRIPTION ALREADY EXISTS: "
                                                + serviceName
                                );


                                // API succeeded, so mark Gmail message processed.

                                if (!messageId.isEmpty()) {

                                    databaseHelper
                                            .markGmailMessageProcessed(

                                                    userId,

                                                    messageId,

                                                    safeString(
                                                            prediction.getPredictedLabel()
                                                    ),

                                                    prediction
                                                            .getSubscriptionProbability()
                                            );
                                }


                                processNextEmail(

                                        messages,

                                        index + 1,

                                        updatedDetectedCount,

                                        newPendingCount,

                                        skippedCount + 1
                                );


                                return;
                            }
                        }


                        // =================================================
                        // MESSAGE ID REQUIRED FOR DUPLICATE TRACKING
                        // =================================================

                        String safeMessageId =
                                messageId;


                        if (safeMessageId.isEmpty()) {

                            /*
                             * Extremely unlikely with Gmail API.
                             *
                             * Use suggestion ID as fallback only if
                             * Gmail ID isn't available.
                             */

                            safeMessageId =
                                    safeString(
                                            prediction.getSuggestionId()
                                    );
                        }


                        if (safeMessageId.isEmpty()) {

                            System.out.println(
                                    "NO MESSAGE ID - PENDING RECORD SKIPPED"
                            );


                            processNextEmail(

                                    messages,

                                    index + 1,

                                    updatedDetectedCount,

                                    newPendingCount,

                                    skippedCount + 1
                            );


                            return;
                        }


                        // =================================================
                        // SAVE AS PENDING
                        //
                        // NOT ACTIVE SUBSCRIPTION.
                        // =================================================

                        long pendingId =
                                databaseHelper
                                        .insertPendingSubscription(

                                                userId,

                                                safeMessageId,

                                                safeString(
                                                        prediction
                                                                .getSuggestionId()
                                                ),

                                                serviceName,

                                                amount,

                                                currency,

                                                billingCycle,

                                                nextBillingDate,

                                                prediction
                                                        .getSubscriptionProbability(),

                                                subject,

                                                sender,

                                                bodyForModel
                                        );


                        int updatedNewPendingCount =
                                newPendingCount;


                        int updatedSkippedCount =
                                skippedCount;


                        if (pendingId != -1) {

                            updatedNewPendingCount++;


                            System.out.println(
                                    "PENDING SUBSCRIPTION SAVED: "
                                            + pendingId
                            );


                            // =================================================
                            // NOW MARK GMAIL EMAIL AS PROCESSED
                            //
                            // Pending record was safely created first.
                            // =================================================

                            if (!safeMessageId.isEmpty()) {

                                long processedResult =
                                        databaseHelper
                                                .markGmailMessageProcessed(

                                                        userId,

                                                        safeMessageId,

                                                        safeString(
                                                                prediction.getPredictedLabel()
                                                        ),

                                                        prediction.getSubscriptionProbability()
                                                );


                                System.out.println(
                                        "PROCESSED MESSAGE RESULT: "
                                                + processedResult
                                );
                            }


                        } else {

                            // =================================================
                            // INSERT FAILED / DUPLICATE
                            // =================================================

                            boolean pendingExists =
                                    !safeMessageId.isEmpty()
                                            && databaseHelper
                                            .pendingSubscriptionExistsByMessageId(
                                                    userId,
                                                    safeMessageId
                                            );


                            if (pendingExists) {

                                // Duplicate already exists safely.
                                // We can mark this Gmail email processed.

                                databaseHelper
                                        .markGmailMessageProcessed(
                                                userId,
                                                safeMessageId,
                                                safeString(
                                                        prediction.getPredictedLabel()
                                                ),
                                                prediction.getSubscriptionProbability()
                                        );


                                updatedSkippedCount++;


                                System.out.println(
                                        "PENDING RECORD ALREADY EXISTS"
                                );


                            } else {

                                /*
                                 * Real DB insertion problem.
                                 *
                                 * IMPORTANT:
                                 * Do NOT mark processed.
                                 *
                                 * Next Gmail scan can retry it.
                                 */

                                System.out.println(
                                        "PENDING INSERT FAILED - EMAIL NOT MARKED PROCESSED"
                                );
                            }
                        }


                        // =================================================
                        // CONTINUE TO NEXT EMAIL
                        // =================================================

                        processNextEmail(

                                messages,

                                index + 1,

                                updatedDetectedCount,

                                updatedNewPendingCount,

                                updatedSkippedCount
                        );
                    }


                    @Override
                    public void onFailure(
                            Call<PredictionResponse> call,
                            Throwable throwable
                    ) {

                        activePredictionCall = null;


                        if (call.isCanceled()) {

                            return;
                        }


                        if (isFinishing() ||
                                isDestroyed()) {

                            return;
                        }


                        String error =
                                throwable == null
                                        ? ""
                                        : safeString(
                                        throwable.getMessage()
                                );


                        System.out.println(
                                "V3 API REQUEST FAILED: "
                                        + error
                        );


                        /*
                         * One email network failure shouldn't stop
                         * the entire Gmail scan.
                         */

                        processNextEmail(

                                messages,

                                index + 1,

                                detectedCount,

                                newPendingCount,

                                skippedCount
                        );
                    }
                }
        );
    }


    // =========================================================
    // FINISH SCAN
    // =========================================================

    private void finishScan(
            int detectedCount,
            int newPendingCount,
            int skippedCount,
            String customMessage
    ) {

        gmailScanInProgress =
                false;


        runOnUiThread(
                () -> {

                    btnConnectGmail.setEnabled(
                            true
                    );


                    btnConnectGmail.setText(
                            "Scan"
                    );


                    int pendingCount =
                            databaseHelper
                                    .getPendingSubscriptionCount(
                                            userId
                                    );


                    tvGmailStatus.setText(
                            pendingCount
                                    + " subscriptions waiting for review"
                    );


                    String message;


                    if (customMessage != null &&
                            !customMessage.trim().isEmpty()) {

                        message =
                                customMessage;


                    } else {

                        message =
                                "Gmail scan complete\n"
                                        + "Detected: "
                                        + detectedCount
                                        + "\n"
                                        + "New to review: "
                                        + newPendingCount
                                        + "\n"
                                        + "Skipped: "
                                        + skippedCount;
                    }


                    Toast.makeText(
                            SubscriptionActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();


                    // =================================================
                    // OPEN PENDING LIST
                    // =================================================

                    updatePendingReviewButton();
                }
        );
    }


    // =========================================================
    // BUILD FULL MODEL TEXT
    // =========================================================

    private String buildModelText(
            String sender,
            String subject,
            String body
    ) {

        String cleanSender = safeString(sender);
        String cleanSubject = safeString(subject);
        String cleanBody = safeString(body);

        // Keep body reasonably bounded.
        // DistilBERT backend will still perform the final token truncation.
        final int MAX_BODY_CHARS = 4000;

        if (cleanBody.length() > MAX_BODY_CHARS) {
            cleanBody = cleanBody.substring(0, MAX_BODY_CHARS);
        }

        StringBuilder builder = new StringBuilder();

        if (!cleanSender.isEmpty()) {
            builder.append("Sender: ")
                    .append(cleanSender)
                    .append("\n");
        }

        if (!cleanSubject.isEmpty()) {
            builder.append("Subject: ")
                    .append(cleanSubject)
                    .append("\n");
        }

        if (!cleanBody.isEmpty()) {
            builder.append("Message:\n")
                    .append(cleanBody);
        }

        return builder.toString().trim();
    }


    // =========================================================
    // LOG PREDICTION
    // =========================================================

    private void logPrediction(
            PredictionResponse prediction
    ) {

        System.out.println(
                "----------------------------------------"
        );

        System.out.println(
                "V3 API RESULT"
        );

        System.out.println(
                "LABEL: "
                        + safeString(
                        prediction.getPredictedLabel()
                )
        );

        System.out.println(
                "PROBABILITY: "
                        + prediction.getSubscriptionProbability()
        );

        System.out.println(
                "THRESHOLD: "
                        + prediction.getBinaryThreshold()
        );

        System.out.println(
                "MODEL: "
                        + safeString(
                        prediction.getModelVersion()
                )
        );

        System.out.println(
                "DECISION: "
                        + safeString(
                        prediction.getDecision()
                )
        );

        System.out.println(
                "----------------------------------------"
        );
    }


    // =========================================================
    // GMAIL LOGIN RESULT
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


        if (!handled ||
                !gmailAuthManager.isConnected()) {

            return;
        }


        Toast.makeText(
                this,
                "Gmail connected successfully.",
                Toast.LENGTH_SHORT
        ).show();


        startGmailScan();
    }


    // =========================================================
    // UPDATE GMAIL UI
    // =========================================================

    private void updateGmailUi() {

        if (btnConnectGmail == null ||
                tvGmailStatus == null) {

            return;
        }


        if (gmailAuthManager != null &&
                gmailAuthManager.isConnected()) {

            btnConnectGmail.setText(
                    "Scan"
            );


            int pendingCount =
                    databaseHelper
                            .getPendingSubscriptionCount(
                                    userId
                            );


            if (pendingCount > 0) {

                tvGmailStatus.setText(
                        pendingCount
                                + " subscriptions waiting for review"
                );


            } else {

                tvGmailStatus.setText(
                        "Gmail connected — ready to scan"
                );
            }


        } else {

            btnConnectGmail.setText(
                    "Connect"
            );


            tvGmailStatus.setText(
                    "Connect Gmail to detect subscriptions"
            );
        }
    }


    // =========================================================
    // CURRENCY NORMALIZATION
    // =========================================================

    private String normalizeSubscriptionCurrency(
            String value
    ) {

        String currency =
                safeString(
                        value
                );


        if (currency.isEmpty()) {

            return "";
        }


        String upper =
                currency.toUpperCase(
                        Locale.US
                );


        if (upper.equals("RS") ||
                upper.equals("RS.") ||
                upper.equals("LKR")) {

            return "LKR";
        }


        if (currency.equals("€") ||
                upper.equals("EUR")) {

            return "EUR";
        }


        if (currency.equals("£") ||
                upper.equals("GBP")) {

            return "GBP";
        }


        if (currency.equals("₹") ||
                upper.equals("INR")) {

            return "INR";
        }


        if (upper.equals("US$")) {

            return "USD";
        }


        if (upper.equals("A$")) {

            return "AUD";
        }


        if (upper.equals("C$")) {

            return "CAD";
        }


        if (upper.equals("S$")) {

            return "SGD";
        }


        // $ alone is ambiguous

        if (currency.equals("$")) {

            return "$";
        }


        return upper;
    }


    // =========================================================
    // SERVICE FALLBACK
    // =========================================================

    private String extractServiceNameFromSender(
            String sender
    ) {

        String value =
                safeString(
                        sender
                );


        if (value.isEmpty()) {

            return "";
        }


        int lessThan =
                value.indexOf(
                        "<"
                );


        if (lessThan > 0) {

            value =
                    value.substring(
                            0,
                            lessThan
                    ).trim();
        }


        value =
                value.replace(
                        "\"",
                        ""
                ).trim();


        return value;
    }


    // =========================================================
    // MONTHLY NORMALIZATION
    // =========================================================

    private double convertToMonthlyAmount(
            double amount,
            String billingCycle
    ) {

        String cycle =
                safeString(
                        billingCycle
                )
                        .toLowerCase(
                                Locale.US
                        );


        if (cycle.contains("year") ||
                cycle.contains("annual")) {

            return amount / 12.0;
        }


        if (cycle.contains("quarter")) {

            return amount / 3.0;
        }


        if (cycle.contains("week")) {

            return amount
                    * 52.0
                    / 12.0;
        }


        return amount;
    }


    // =========================================================
    // SUMMARY
    // =========================================================

    private void updateSummary() {

        if (databaseHelper == null ||
                userId <= 0) {

            return;
        }


        int count =
                databaseHelper
                        .getSubscriptionCount(
                                userId
                        );


        tvActiveSubscriptions.setText(
                count
                        + " Services"
        );


        // =====================================================
        // MONTHLY TOTAL
        // =====================================================

        if (subscriptionList.isEmpty()) {

            tvMonthlySpend.setText(
                    "No subscriptions"
            );


        } else {

            String commonCurrency =
                    null;


            boolean mixedCurrencies =
                    false;


            boolean unknownCurrency =
                    false;


            double monthlyTotal =
                    0.0;


            for (
                    Subscription subscription :
                    subscriptionList
            ) {

                if (subscription == null) {

                    continue;
                }


                String currency =
                        safeString(
                                subscription.getCurrency()
                        );


                if (currency.isEmpty() ||
                        currency.equals("$")) {

                    unknownCurrency =
                            true;

                    continue;
                }


                if (commonCurrency == null) {

                    commonCurrency =
                            currency;


                } else if (
                        !commonCurrency
                                .equalsIgnoreCase(
                                        currency
                                )
                ) {

                    mixedCurrencies =
                            true;
                }


                monthlyTotal +=
                        convertToMonthlyAmount(

                                subscription.getAmount(),

                                subscription.getBillingCycle()
                        );
            }


            if (mixedCurrencies) {

                tvMonthlySpend.setText(
                        "Mixed currencies"
                );


            } else if (unknownCurrency) {

                tvMonthlySpend.setText(
                        "Review currency"
                );


            } else if (commonCurrency != null) {

                tvMonthlySpend.setText(
                        String.format(
                                Locale.US,
                                "%s %,.2f",
                                commonCurrency,
                                monthlyTotal
                        )
                );


            } else {

                tvMonthlySpend.setText(
                        "Review currency"
                );
            }
        }


        // =====================================================
        // NEXT DUE
        // =====================================================

        Cursor cursor = null;


        try {

            cursor =
                    databaseHelper
                            .getNextSubscription(
                                    userId
                            );


            if (cursor != null &&
                    cursor.moveToFirst()) {

                String date =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.NEXT_BILLING_DATE
                                )
                        );


                if (safeString(date).isEmpty()) {

                    tvNextDue.setText(
                            "No Due"
                    );


                } else {

                    tvNextDue.setText(
                            date
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


        } finally {

            if (cursor != null) {

                cursor.close();
            }
        }


        // =====================================================
        // INSIGHT
        // =====================================================

        try {

            String insight =
                    databaseHelper
                            .generateAIInsight(
                                    userId
                            );


            if (safeString(insight).isEmpty()) {

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


                    if (itemId ==
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


                    if (itemId ==
                            R.id.nav_subscriptions) {

                        return true;
                    }


                    if (itemId ==
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


                    if (itemId ==
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
    // SAFE STRING
    // =========================================================

    private String safeString(
            String value
    ) {

        if (value == null) {

            return "";
        }


        return value.trim();
    }


    // =========================================================
// UPDATE DETECTED SUBSCRIPTIONS BUTTON
// =========================================================

    private void updatePendingReviewButton() {

        if (btnViewDetectedSubscriptions == null ||
                databaseHelper == null ||
                userId <= 0) {

            return;
        }


        int pendingCount =
                databaseHelper
                        .getPendingSubscriptionCount(
                                userId
                        );


        if (pendingCount > 0) {

            btnViewDetectedSubscriptions.setVisibility(
                    View.VISIBLE
            );


            if (pendingCount == 1) {

                btnViewDetectedSubscriptions.setText(
                        "View detected subscription (1)"
                );


            } else {

                btnViewDetectedSubscriptions.setText(
                        "View detected subscriptions ("
                                + pendingCount
                                + ")"
                );
            }


        } else {

            btnViewDetectedSubscriptions.setVisibility(
                    View.GONE
            );
        }
    }

    // =========================================================
    // ON RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_subscriptions);
        }


        if (databaseHelper != null &&
                userId > 0) {

            loadSubscriptions();



            updateGmailUi();

            updatePendingReviewButton();
        }
    }


    // =========================================================
    // ON DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        if (activePredictionCall != null) {

            activePredictionCall.cancel();

            activePredictionCall = null;
        }


        if (gmailServiceManager != null) {

            gmailServiceManager.shutdown();

            gmailServiceManager = null;
        }


        super.onDestroy();
    }
    // =========================================================
// USAGE ACCESS PERMISSION
// =========================================================

    private void checkUsageAccessPermission() {

        if (usageStatsHelper == null) {
            return;
        }

        if (usageStatsHelper.hasUsageAccessPermission()) {
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Usage Access Required")
                .setMessage(
                        "To analyze subscription usage, " +
                                "please allow Usage Access for this app."
                )
                .setPositiveButton(
                        "Enable",
                        (dialog, which) -> {
                            usageStatsHelper.openUsageAccessSettings();
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }
}