package com.example.aiexpensemanagementapplication.ui.subscription;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.model.PendingSubscription;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;


public class PendingSubscriptionsActivity extends AppCompatActivity {

    // =========================================================
    // UI
    // =========================================================

    private MaterialToolbar toolbar;

    private RecyclerView rvPendingSubscriptions;

    private LinearLayout layoutEmpty;

    private TextView tvPendingCount;


    // =========================================================
    // DATABASE
    // =========================================================

    private DatabaseHelper databaseHelper;


    // =========================================================
    // DATA
    // =========================================================

    private final ArrayList<PendingSubscription> pendingList =
            new ArrayList<>();

    private PendingSubscriptionAdapter adapter;


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
                R.layout.activity_pending_subscriptions
        );


        initializeViews();

        setupToolbar();


        databaseHelper =
                new DatabaseHelper(this);


        if (!initializeUser()) {

            return;
        }


        setupRecyclerView();

        loadPendingSubscriptions();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        toolbar =
                findViewById(
                        R.id.toolbar
                );


        rvPendingSubscriptions =
                findViewById(
                        R.id.rvPendingSubscriptions
                );


        layoutEmpty =
                findViewById(
                        R.id.layoutEmpty
                );


        tvPendingCount =
                findViewById(
                        R.id.tvPendingCount
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
                    "Local user record not found.",
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
                new PendingSubscriptionAdapter(

                        this,

                        pendingList,

                        subscription -> {

                            if (subscription == null) {

                                return;
                            }


                            openReviewScreen(
                                    subscription
                            );
                        }
                );


        rvPendingSubscriptions.setLayoutManager(
                new LinearLayoutManager(
                        this
                )
        );


        rvPendingSubscriptions.setAdapter(
                adapter
        );
    }


    // =========================================================
    // LOAD PENDING SUBSCRIPTIONS
    // =========================================================

    private void loadPendingSubscriptions() {

        if (databaseHelper == null ||
                userId <= 0) {

            return;
        }


        pendingList.clear();


        Cursor cursor = null;


        try {

            cursor =
                    databaseHelper
                            .getPendingSubscriptions(
                                    userId
                            );


            if (cursor != null) {

                while (cursor.moveToNext()) {

                    PendingSubscription subscription =
                            new PendingSubscription();


                    // =================================================
                    // ID
                    // =================================================

                    subscription.setPendingId(
                            cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            DatabaseHelper.PENDING_ID
                                    )
                            )
                    );


                    // =================================================
                    // USER ID
                    // =================================================

                    subscription.setUserId(
                            cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            DatabaseHelper.PENDING_USER_ID
                                    )
                            )
                    );


                    // =================================================
                    // GMAIL MESSAGE ID
                    // =================================================

                    subscription.setGmailMessageId(
                            getNullableString(
                                    cursor,
                                    DatabaseHelper.PENDING_GMAIL_MESSAGE_ID
                            )
                    );


                    // =================================================
                    // SUGGESTION ID
                    // =================================================

                    subscription.setSuggestionId(
                            getNullableString(
                                    cursor,
                                    DatabaseHelper.PENDING_SUGGESTION_ID
                            )
                    );


                    // =================================================
                    // SERVICE
                    // =================================================

                    subscription.setServiceName(
                            getNullableString(
                                    cursor,
                                    DatabaseHelper.PENDING_SERVICE_NAME
                            )
                    );


                    // =================================================
                    // AMOUNT
                    // =================================================

                    int amountIndex =
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.PENDING_AMOUNT
                            );


                    if (!cursor.isNull(amountIndex)) {

                        subscription.setAmount(
                                cursor.getDouble(
                                        amountIndex
                                )
                        );


                    } else {

                        subscription.setAmount(
                                0.0
                        );
                    }


                    // =================================================
                    // CURRENCY
                    // =================================================

                    subscription.setCurrency(
                            getNullableString(
                                    cursor,
                                    DatabaseHelper.PENDING_CURRENCY
                            )
                    );


                    // =================================================
                    // BILLING CYCLE
                    // =================================================

                    subscription.setBillingCycle(
                            getNullableString(
                                    cursor,
                                    DatabaseHelper.PENDING_BILLING_CYCLE
                            )
                    );


                    // =================================================
                    // NEXT BILLING DATE
                    // =================================================

                    subscription.setNextBillingDate(
                            getNullableString(
                                    cursor,
                                    DatabaseHelper.PENDING_NEXT_BILLING_DATE
                            )
                    );


                    // =================================================
                    // CONFIDENCE
                    // =================================================

                    int confidenceIndex =
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.PENDING_CONFIDENCE
                            );


                    if (!cursor.isNull(confidenceIndex)) {

                        subscription.setConfidence(
                                cursor.getDouble(
                                        confidenceIndex
                                )
                        );


                    } else {

                        subscription.setConfidence(
                                0.0
                        );
                    }


                    // =================================================
                    // EMAIL SUBJECT
                    // =================================================

                    subscription.setEmailSubject(
                            getNullableString(
                                    cursor,
                                    DatabaseHelper.PENDING_EMAIL_SUBJECT
                            )
                    );


                    // =================================================
                    // EMAIL SENDER
                    // =================================================

                    subscription.setEmailSender(
                            getNullableString(
                                    cursor,
                                    DatabaseHelper.PENDING_EMAIL_SENDER
                            )
                    );


                    // =================================================
                    // EMAIL BODY
                    // =================================================

                    subscription.setEmailBody(
                            getNullableString(
                                    cursor,
                                    DatabaseHelper.PENDING_EMAIL_BODY
                            )
                    );


                    // =================================================
                    // STATUS
                    // =================================================

                    subscription.setStatus(
                            getNullableString(
                                    cursor,
                                    DatabaseHelper.PENDING_STATUS
                            )
                    );


                    // =================================================
                    // CREATED AT
                    // =================================================

                    subscription.setCreatedAt(
                            getNullableString(
                                    cursor,
                                    DatabaseHelper.PENDING_CREATED_AT
                            )
                    );


                    pendingList.add(
                            subscription
                    );
                }
            }


        } catch (Exception e) {

            e.printStackTrace();


            Toast.makeText(
                    this,
                    "Failed to load detected subscriptions.",
                    Toast.LENGTH_LONG
            ).show();


        } finally {

            if (cursor != null) {

                cursor.close();
            }
        }


        if (adapter != null) {

            adapter.notifyDataSetChanged();
        }


        updateScreenState();
    }


    // =========================================================
    // SCREEN STATE
    // =========================================================

    private void updateScreenState() {

        int count =
                pendingList.size();


        if (count == 1) {

            tvPendingCount.setText(
                    "1 subscription waiting for review"
            );


        } else {

            tvPendingCount.setText(
                    count
                            + " subscriptions waiting for review"
            );
        }


        if (count == 0) {

            layoutEmpty.setVisibility(
                    View.VISIBLE
            );


            rvPendingSubscriptions.setVisibility(
                    View.GONE
            );


        } else {

            layoutEmpty.setVisibility(
                    View.GONE
            );


            rvPendingSubscriptions.setVisibility(
                    View.VISIBLE
            );
        }
    }


    // =========================================================
    // OPEN REVIEW
    // =========================================================

    private void openReviewScreen(
            PendingSubscription subscription
    ) {

        Intent intent =
                new Intent(
                        PendingSubscriptionsActivity.this,
                        ReviewSubscriptionActivity.class
                );


        // =====================================================
        // IMPORTANT
        // =====================================================

        intent.putExtra(
                "pendingId",
                subscription.getPendingId()
        );


        // =====================================================
        // DETAILS
        // =====================================================

        intent.putExtra(
                "serviceName",
                subscription.getServiceName()
        );


        intent.putExtra(
                "amount",
                subscription.getAmount()
        );


        intent.putExtra(
                "currency",
                subscription.getCurrency()
        );


        intent.putExtra(
                "billingCycle",
                subscription.getBillingCycle()
        );


        intent.putExtra(
                "nextBillingDate",
                subscription.getNextBillingDate()
        );


        // =====================================================
        // SOURCE
        // =====================================================

        intent.putExtra(
                "sourceType",
                "GMAIL"
        );


        intent.putExtra(
                "messageId",
                subscription.getGmailMessageId()
        );


        intent.putExtra(
                "suggestionId",
                subscription.getSuggestionId()
        );


        intent.putExtra(
                "emailSubject",
                subscription.getEmailSubject()
        );


        intent.putExtra(
                "emailSender",
                subscription.getEmailSender()
        );


        intent.putExtra(
                "emailBody",
                subscription.getEmailBody()
        );


        // =====================================================
        // API CONFIDENCE
        // =====================================================

        intent.putExtra(
                "subscriptionProbability",
                subscription.getConfidence()
        );


        startActivity(
                intent
        );
    }


    // =========================================================
    // NULLABLE STRING
    // =========================================================

    private String getNullableString(
            Cursor cursor,
            String columnName
    ) {

        int index =
                cursor.getColumnIndex(
                        columnName
                );


        if (index == -1 ||
                cursor.isNull(index)) {

            return "";
        }


        String value =
                cursor.getString(
                        index
                );


        if (value == null) {

            return "";
        }


        return value.trim();
    }


    // =========================================================
    // ON RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();


        /*
         * Review screen:
         *
         * Confirm & Save
         * OR
         * Do not save
         *
         * முடிந்ததும் list refresh ஆகும்.
         */

        if (databaseHelper != null &&
                userId > 0) {

            loadPendingSubscriptions();
        }
    }
}