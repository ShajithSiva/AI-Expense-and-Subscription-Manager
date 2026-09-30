package com.example.aiexpensemanagementapplication.ui.subscription;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Locale;


public class ReviewSubscriptionActivity extends AppCompatActivity {

    // =========================================================
    // UI
    // =========================================================

    private MaterialToolbar toolbar;

    private TextView tvDetectionStatus;

    private TextView tvLkrEstimate;

    private TextView tvRateInfo;

    private TextInputEditText etServiceName;

    private TextInputEditText etAmount;

    private AutoCompleteTextView actCurrency;

    private AutoCompleteTextView actBillingCycle;

    private TextInputEditText etNextBillingDate;

    private MaterialCheckBox checkConfirmed;

    private MaterialButton btnConfirmSave;

    private MaterialButton btnDoNotSave;


    // =========================================================
    // DATABASE
    // =========================================================

    private DatabaseHelper databaseHelper;


    // =========================================================
    // USER
    // =========================================================

    private int userId = -1;


    // =========================================================
    // PENDING RECORD
    // =========================================================

    private int pendingId = -1;


    // =========================================================
    // SOURCE DATA
    // =========================================================

    private String sourceType = "";

    private String gmailMessageId = "";

    private String suggestionId = "";

    private String emailSubject = "";

    private String emailSender = "";

    private String emailBody = "";

    private double subscriptionProbability = 0.0;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_review_subscription
        );


        initializeViews();

        setupToolbar();


        databaseHelper =
                new DatabaseHelper(this);


        if (!initializeUser()) {

            return;
        }


        setupCurrencyDropdown();

        setupBillingCycleDropdown();

        setupDatePicker();

        setupListeners();


        // =====================================================
        // READ PENDING ID
        // =====================================================

        pendingId =
                getIntent()
                        .getIntExtra(
                                "pendingId",
                                -1
                        );


        // =====================================================
        // IF OPENED FROM PENDING LIST
        // LOAD DATABASE RECORD
        // =====================================================

        if (pendingId > 0) {

            loadPendingSubscription(
                    pendingId
            );


        } else {

            // Fallback for direct API review
            loadIntentData();
        }


        updateLkrEstimate();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        toolbar =
                findViewById(
                        R.id.toolbar
                );


        tvDetectionStatus =
                findViewById(
                        R.id.tvDetectionStatus
                );


        etServiceName =
                findViewById(
                        R.id.etServiceName
                );


        etAmount =
                findViewById(
                        R.id.etAmount
                );


        actCurrency =
                findViewById(
                        R.id.actCurrency
                );


        tvLkrEstimate =
                findViewById(
                        R.id.tvLkrEstimate
                );


        tvRateInfo =
                findViewById(
                        R.id.tvRateInfo
                );


        actBillingCycle =
                findViewById(
                        R.id.actBillingCycle
                );


        etNextBillingDate =
                findViewById(
                        R.id.etNextBillingDate
                );


        checkConfirmed =
                findViewById(
                        R.id.checkConfirmed
                );


        btnConfirmSave =
                findViewById(
                        R.id.btnConfirmSave
                );


        btnDoNotSave =
                findViewById(
                        R.id.btnDoNotSave
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
    // LOAD PENDING RECORD FROM SQLITE
    // =========================================================

    private void loadPendingSubscription(
            int pendingId
    ) {

        Cursor cursor = null;


        try {

            cursor =
                    databaseHelper
                            .getPendingSubscription(
                                    pendingId
                            );


            if (cursor == null ||
                    !cursor.moveToFirst()) {

                Toast.makeText(
                        this,
                        "Pending subscription not found.",
                        Toast.LENGTH_LONG
                ).show();


                finish();

                return;
            }


            // =================================================
            // CHECK RECORD BELONGS TO CURRENT USER
            // =================================================

            int recordUserId =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.PENDING_USER_ID
                            )
                    );


            if (recordUserId != userId) {

                Toast.makeText(
                        this,
                        "This subscription does not belong to the current user.",
                        Toast.LENGTH_LONG
                ).show();


                finish();

                return;
            }


            // =================================================
            // SERVICE
            // =================================================

            String serviceName =
                    getNullableString(
                            cursor,
                            DatabaseHelper.PENDING_SERVICE_NAME
                    );


            // =================================================
            // AMOUNT
            // =================================================

            double amount = 0.0;


            int amountIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.PENDING_AMOUNT
                    );


            if (amountIndex != -1 &&
                    !cursor.isNull(amountIndex)) {

                amount =
                        cursor.getDouble(
                                amountIndex
                        );
            }


            // =================================================
            // CURRENCY
            // =================================================

            String currency =
                    normalizeCurrency(
                            getNullableString(
                                    cursor,
                                    DatabaseHelper.PENDING_CURRENCY
                            )
                    );


            // =================================================
            // BILLING CYCLE
            // =================================================

            String billingCycle =
                    getNullableString(
                            cursor,
                            DatabaseHelper.PENDING_BILLING_CYCLE
                    );


            // =================================================
            // NEXT BILL
            // =================================================

            String nextBillingDate =
                    getNullableString(
                            cursor,
                            DatabaseHelper.PENDING_NEXT_BILLING_DATE
                    );


            // =================================================
            // SOURCE INFORMATION
            // =================================================

            gmailMessageId =
                    getNullableString(
                            cursor,
                            DatabaseHelper.PENDING_GMAIL_MESSAGE_ID
                    );


            suggestionId =
                    getNullableString(
                            cursor,
                            DatabaseHelper.PENDING_SUGGESTION_ID
                    );


            emailSubject =
                    getNullableString(
                            cursor,
                            DatabaseHelper.PENDING_EMAIL_SUBJECT
                    );


            emailSender =
                    getNullableString(
                            cursor,
                            DatabaseHelper.PENDING_EMAIL_SENDER
                    );


            emailBody =
                    getNullableString(
                            cursor,
                            DatabaseHelper.PENDING_EMAIL_BODY
                    );


            int confidenceIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.PENDING_CONFIDENCE
                    );


            if (confidenceIndex != -1 &&
                    !cursor.isNull(confidenceIndex)) {

                subscriptionProbability =
                        cursor.getDouble(
                                confidenceIndex
                        );
            }


            sourceType =
                    "GMAIL";


            // =================================================
            // POPULATE SCREEN
            // =================================================

            populateFields(
                    serviceName,
                    amount,
                    currency,
                    billingCycle,
                    nextBillingDate
            );


            updateDetectionStatus();


        } catch (Exception e) {

            e.printStackTrace();


            Toast.makeText(
                    this,
                    "Failed to load subscription details.",
                    Toast.LENGTH_LONG
            ).show();


        } finally {

            if (cursor != null) {

                cursor.close();
            }
        }
    }


    // =========================================================
    // LOAD DIRECT INTENT DATA
    // =========================================================

    private void loadIntentData() {

        String serviceName =
                safeString(
                        getIntent()
                                .getStringExtra(
                                        "serviceName"
                                )
                );


        double amount =
                getIntent()
                        .getDoubleExtra(
                                "amount",
                                0.0
                        );


        String currency =
                normalizeCurrency(
                        getIntent()
                                .getStringExtra(
                                        "currency"
                                )
                );


        String billingCycle =
                safeString(
                        getIntent()
                                .getStringExtra(
                                        "billingCycle"
                                )
                );


        String nextBillingDate =
                safeString(
                        getIntent()
                                .getStringExtra(
                                        "nextBillingDate"
                                )
                );


        sourceType =
                safeString(
                        getIntent()
                                .getStringExtra(
                                        "sourceType"
                                )
                );


        gmailMessageId =
                safeString(
                        getIntent()
                                .getStringExtra(
                                        "messageId"
                                )
                );


        suggestionId =
                safeString(
                        getIntent()
                                .getStringExtra(
                                        "suggestionId"
                                )
                );


        emailSubject =
                safeString(
                        getIntent()
                                .getStringExtra(
                                        "emailSubject"
                                )
                );


        emailSender =
                safeString(
                        getIntent()
                                .getStringExtra(
                                        "emailSender"
                                )
                );


        emailBody =
                safeString(
                        getIntent()
                                .getStringExtra(
                                        "emailBody"
                                )
                );


        subscriptionProbability =
                getIntent()
                        .getDoubleExtra(
                                "subscriptionProbability",
                                0.0
                        );


        populateFields(
                serviceName,
                amount,
                currency,
                billingCycle,
                nextBillingDate
        );


        updateDetectionStatus();
    }


    // =========================================================
    // POPULATE FIELDS
    // =========================================================

    private void populateFields(
            String serviceName,
            double amount,
            String currency,
            String billingCycle,
            String nextBillingDate
    ) {

        etServiceName.setText(
                safeString(
                        serviceName
                )
        );


        if (amount > 0) {

            etAmount.setText(
                    String.format(
                            Locale.US,
                            "%.2f",
                            amount
                    )
            );


        } else {

            etAmount.setText(
                    ""
            );
        }


        // =====================================================
        // AMBIGUOUS $ MUST NOT AUTO SELECT USD
        // =====================================================

        if (currency.equals("$")) {

            actCurrency.setText(
                    "",
                    false
            );


        } else {

            actCurrency.setText(
                    currency,
                    false
            );
        }


        actBillingCycle.setText(
                safeString(
                        billingCycle
                ),
                false
        );


        etNextBillingDate.setText(
                safeString(
                        nextBillingDate
                )
        );
    }


    // =========================================================
    // DETECTION STATUS
    // =========================================================

    private void updateDetectionStatus() {

        if (subscriptionProbability > 0) {

            tvDetectionStatus.setText(
                    String.format(
                            Locale.US,
                            "Subscription detected • %.1f%% confidence",
                            subscriptionProbability * 100.0
                    )
            );


        } else {

            tvDetectionStatus.setText(
                    "Subscription detected"
            );
        }
    }


    // =========================================================
    // CURRENCY
    // =========================================================

    private void setupCurrencyDropdown() {

        String[] currencies = {

                "LKR",
                "USD",
                "EUR",
                "GBP",
                "AUD",
                "CAD",
                "SGD",
                "INR",
                "JPY"
        };


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(

                        this,

                        android.R.layout
                                .simple_dropdown_item_1line,

                        currencies
                );


        actCurrency.setAdapter(
                adapter
        );


        actCurrency.setOnItemClickListener(
                (parent, view, position, id) ->
                        updateLkrEstimate()
        );
    }


    // =========================================================
    // BILLING CYCLE
    // =========================================================

    private void setupBillingCycleDropdown() {

        String[] billingCycles = {

                "Monthly",
                "Weekly",
                "Quarterly",
                "Yearly"
        };


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(

                        this,

                        android.R.layout
                                .simple_dropdown_item_1line,

                        billingCycles
                );


        actBillingCycle.setAdapter(
                adapter
        );
    }


    // =========================================================
    // DATE PICKER
    // =========================================================

    private void setupDatePicker() {

        etNextBillingDate.setOnClickListener(
                v -> {

                    Calendar calendar =
                            Calendar.getInstance();


                    String currentDate =
                            getEditTextValue(
                                    etNextBillingDate
                            );


                    if (!currentDate.isEmpty()) {

                        try {

                            String[] parts =
                                    currentDate.split(
                                            "-"
                                    );


                            if (parts.length == 3) {

                                calendar.set(

                                        Integer.parseInt(
                                                parts[0]
                                        ),

                                        Integer.parseInt(
                                                parts[1]
                                        ) - 1,

                                        Integer.parseInt(
                                                parts[2]
                                        )
                                );
                            }


                        } catch (Exception ignored) {

                        }
                    }


                    DatePickerDialog dialog =
                            new DatePickerDialog(

                                    ReviewSubscriptionActivity.this,

                                    (view,
                                     year,
                                     month,
                                     dayOfMonth) -> {

                                        String selectedDate =
                                                String.format(

                                                        Locale.US,

                                                        "%04d-%02d-%02d",

                                                        year,

                                                        month + 1,

                                                        dayOfMonth
                                                );


                                        etNextBillingDate.setText(
                                                selectedDate
                                        );
                                    },

                                    calendar.get(
                                            Calendar.YEAR
                                    ),

                                    calendar.get(
                                            Calendar.MONTH
                                    ),

                                    calendar.get(
                                            Calendar.DAY_OF_MONTH
                                    )
                            );


                    dialog.show();
                }
        );
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnConfirmSave.setEnabled(
                false
        );


        checkConfirmed.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    btnConfirmSave.setEnabled(
                            isChecked
                    );
                }
        );


        btnConfirmSave.setOnClickListener(
                v -> saveReviewedSubscription()
        );


        btnDoNotSave.setOnClickListener(
                v -> ignorePendingSubscription()
        );


        etAmount.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (!hasFocus) {

                        updateLkrEstimate();
                    }
                }
        );
    }


    // =========================================================
    // SAVE REVIEWED SUBSCRIPTION
    // =========================================================

    private void saveReviewedSubscription() {

        // =====================================================
        // VALIDATION
        // =====================================================

        String serviceName =
                getEditTextValue(
                        etServiceName
                );


        String amountText =
                getEditTextValue(
                        etAmount
                );


        String currency =
                normalizeCurrency(
                        safeString(
                                actCurrency
                                        .getText()
                                        .toString()
                        )
                );


        String billingCycle =
                safeString(
                        actBillingCycle
                                .getText()
                                .toString()
                );


        String nextBillingDate =
                getEditTextValue(
                        etNextBillingDate
                );


        if (serviceName.isEmpty()) {

            etServiceName.setError(
                    "Service name is required"
            );

            etServiceName.requestFocus();

            return;
        }


        if (amountText.isEmpty()) {

            etAmount.setError(
                    "Amount is required"
            );

            etAmount.requestFocus();

            return;
        }


        double amount;


        try {

            amount =
                    Double.parseDouble(
                            amountText
                    );


        } catch (NumberFormatException e) {

            etAmount.setError(
                    "Enter a valid amount"
            );

            etAmount.requestFocus();

            return;
        }


        if (amount <= 0) {

            etAmount.setError(
                    "Amount must be greater than 0"
            );

            return;
        }


        if (currency.isEmpty() ||
                currency.equals("$")) {

            actCurrency.setError(
                    "Please select the exact currency"
            );

            return;
        }


        if (billingCycle.isEmpty()) {

            actBillingCycle.setError(
                    "Please select billing cycle"
            );

            return;
        }


        if (nextBillingDate.isEmpty()) {

            etNextBillingDate.setError(
                    "Next billing date is required"
            );

            return;
        }


        if (!checkConfirmed.isChecked()) {

            Toast.makeText(
                    this,
                    "Please confirm that you checked the details.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // DUPLICATE CHECK AGAIN
        //
        // Important because user may edit the service name.
        // =====================================================

        boolean exists =
                databaseHelper
                        .subscriptionExists(
                                userId,
                                serviceName
                        );


        if (exists) {

            Toast.makeText(
                    this,
                    serviceName
                            + " already exists in active subscriptions.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =====================================================
        // INSERT INTO ACTIVE SUBSCRIPTION TABLE
        // =====================================================

        long insertedId =
                databaseHelper
                        .insertSubscription(

                                userId,

                                serviceName,

                                amount,

                                currency,

                                billingCycle,

                                nextBillingDate
                        );


        if (insertedId == -1) {

            Toast.makeText(
                    this,
                    "Failed to save subscription.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =====================================================
        // MARK PENDING RECORD AS SAVED
        // =====================================================

        if (pendingId > 0) {

            databaseHelper
                    .markPendingSubscriptionSaved(
                            pendingId
                    );
        }


        System.out.println(
                "========================================"
        );

        System.out.println(
                "SUBSCRIPTION REVIEW CONFIRMED"
        );

        System.out.println(
                "ACTIVE SUBSCRIPTION ID: "
                        + insertedId
        );

        System.out.println(
                "PENDING ID: "
                        + pendingId
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
                "BILLING CYCLE: "
                        + billingCycle
        );

        System.out.println(
                "NEXT BILLING DATE: "
                        + nextBillingDate
        );

        System.out.println(
                "GMAIL MESSAGE ID: "
                        + gmailMessageId
        );

        System.out.println(
                "SUGGESTION ID: "
                        + suggestionId
        );

        System.out.println(
                "========================================"
        );


        Toast.makeText(
                this,
                "Subscription added successfully.",
                Toast.LENGTH_SHORT
        ).show();


        /*
         * We just finish this screen.
         *
         * PendingSubscriptionsActivity.onResume()
         * will reload its list automatically.
         */

        setResult(
                RESULT_OK
        );


        finish();
    }


    // =========================================================
    // DO NOT SAVE / IGNORE
    // =========================================================

    private void ignorePendingSubscription() {

        if (pendingId > 0) {

            int result =
                    databaseHelper
                            .markPendingSubscriptionIgnored(
                                    pendingId
                            );


            if (result > 0) {

                Toast.makeText(
                        this,
                        "Subscription message ignored.",
                        Toast.LENGTH_SHORT
                ).show();


            } else {

                Toast.makeText(
                        this,
                        "Could not update subscription message.",
                        Toast.LENGTH_LONG
                ).show();
            }


        } else {

            Toast.makeText(
                    this,
                    "Subscription was not saved.",
                    Toast.LENGTH_SHORT
            ).show();
        }


        setResult(
                RESULT_CANCELED
        );


        finish();
    }


    // =========================================================
    // LKR ESTIMATE
    //
    // Proper /convert API integration will be added later.
    // Do not invent exchange rates.
    // =========================================================

    private void updateLkrEstimate() {

        String amountText =
                getEditTextValue(
                        etAmount
                );


        String currency =
                normalizeCurrency(
                        safeString(
                                actCurrency
                                        .getText()
                                        .toString()
                        )
                );


        if (amountText.isEmpty()) {

            tvLkrEstimate.setText(
                    "Rs. —"
            );


            tvRateInfo.setText(
                    "Enter an amount first."
            );


            return;
        }


        double amount;


        try {

            amount =
                    Double.parseDouble(
                            amountText
                    );


        } catch (NumberFormatException e) {

            tvLkrEstimate.setText(
                    "Rs. —"
            );


            tvRateInfo.setText(
                    "Enter a valid amount."
            );


            return;
        }


        if (currency.equals("LKR")) {

            tvLkrEstimate.setText(
                    String.format(
                            Locale.US,
                            "Rs. %,.2f",
                            amount
                    )
            );


            tvRateInfo.setText(
                    "Original amount is already in LKR."
            );


            return;
        }


        tvLkrEstimate.setText(
                "Rs. —"
        );


        if (currency.isEmpty() ||
                currency.equals("$")) {

            tvRateInfo.setText(
                    "Select the exact original currency."
            );


        } else {

            tvRateInfo.setText(
                    "LKR conversion will be added using the exchange-rate API."
            );
        }
    }


    // =========================================================
    // CURRENCY NORMALIZATION
    // =========================================================

    private String normalizeCurrency(
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


        /*
         * "$" alone is ambiguous.
         */

        if (currency.equals("$")) {

            return "$";
        }


        return upper;
    }


    // =========================================================
    // GET EDIT TEXT VALUE
    // =========================================================

    private String getEditTextValue(
            TextInputEditText editText
    ) {

        if (editText == null ||
                editText.getText() == null) {

            return "";
        }


        return editText
                .getText()
                .toString()
                .trim();
    }


    // =========================================================
    // NULLABLE CURSOR STRING
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


        return safeString(
                value
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
}