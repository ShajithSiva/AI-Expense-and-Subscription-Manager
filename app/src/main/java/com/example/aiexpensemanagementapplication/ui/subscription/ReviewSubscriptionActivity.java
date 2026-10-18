package com.example.aiexpensemanagementapplication.ui.subscription;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
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

    private ImageView ivServiceIcon;

    private TextView tvReviewServiceName;
    private TextView tvReviewEmailSubject;
    private TextView tvEmailSubject;
    private TextView tvEmailFrom;
    private TextView tvEmailDate;
    private TextView tvEmailPreview;
    private TextView tvLkrEstimate;
    private TextView tvRateInfo;

    private TextInputEditText etServiceName;
    private TextInputEditText etAmount;
    private TextInputEditText etNextBillingDate;

    private AutoCompleteTextView actCurrency;
    private AutoCompleteTextView actBillingCycle;

    private MaterialCheckBox checkConfirmed;

    private MaterialButton btnConfirmSave;
    private MaterialButton btnDoNotSave;

    // =========================================================
    // DATABASE AND USER
    // =========================================================

    private DatabaseHelper databaseHelper;

    private int userId = -1;
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

    // Retained for compatibility with existing intent/database data.
    // It is not displayed in the UI.
    private double subscriptionProbability = 0.0;

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_review_subscription);

        initializeViews();
        setupToolbar();

        databaseHelper = new DatabaseHelper(this);

        if (!initializeUser()) {
            return;
        }

        setupCurrencyDropdown();
        setupBillingCycleDropdown();
        setupDatePicker();
        setupListeners();

        pendingId = getIntent().getIntExtra("pendingId", -1);

        if (pendingId > 0) {
            loadPendingSubscription(pendingId);
        } else {
            loadIntentData();
        }

        updateLkrEstimate();
    }

    // =========================================================
    // INITIALIZE VIEWS
    // Optional views are checked because XML versions may differ.
    // =========================================================

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);

        ivServiceIcon = findViewById(R.id.ivServiceIcon);


        tvReviewServiceName = findViewById(R.id.tvReviewServiceName);
        tvReviewEmailSubject = findViewById(R.id.tvReviewEmailSubject);
        tvEmailSubject = findViewById(R.id.tvEmailSubject);
        tvEmailFrom = findViewById(R.id.tvEmailFrom);
        tvEmailDate = findViewById(R.id.tvEmailDate);

        tvEmailPreview = findViewById(R.id.tvEmailPreview);
        tvLkrEstimate = findViewById(R.id.tvLkrEstimate);
        tvRateInfo = findViewById(R.id.tvRateInfo);

        etServiceName = findViewById(R.id.etServiceName);
        etAmount = findViewById(R.id.etAmount);
        actCurrency = findViewById(R.id.actCurrency);
        actBillingCycle = findViewById(R.id.actBillingCycle);
        etNextBillingDate = findViewById(R.id.etNextBillingDate);


        btnConfirmSave = findViewById(R.id.btnConfirmSave);
        btnDoNotSave = findViewById(R.id.btnDoNotSave);
    }

    // =========================================================
    // TOOLBAR
    // =========================================================

    private void setupToolbar() {
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // =========================================================
    // USER VALIDATION
    // =========================================================

    private boolean initializeUser() {
        FirebaseUser firebaseUser =
                FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return false;
        }

        userId = databaseHelper.getUserIdByFirebaseUid(
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
    // LOAD PENDING SUBSCRIPTION FROM SQLITE
    // =========================================================

    private void loadPendingSubscription(int id) {
        Cursor cursor = null;

        try {
            cursor = databaseHelper.getPendingSubscription(id);

            if (cursor == null || !cursor.moveToFirst()) {
                Toast.makeText(
                        this,
                        "Pending subscription not found.",
                        Toast.LENGTH_LONG
                ).show();

                finish();
                return;
            }

            int userIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.PENDING_USER_ID
            );

            int recordUserId = cursor.getInt(userIndex);

            if (recordUserId != userId) {
                Toast.makeText(
                        this,
                        "This subscription does not belong to the current user.",
                        Toast.LENGTH_LONG
                ).show();

                finish();
                return;
            }

            String serviceName = getNullableString(
                    cursor,
                    DatabaseHelper.PENDING_SERVICE_NAME
            );

            double amount = 0.0;

            int amountIndex = cursor.getColumnIndex(
                    DatabaseHelper.PENDING_AMOUNT
            );

            if (amountIndex != -1 && !cursor.isNull(amountIndex)) {
                amount = cursor.getDouble(amountIndex);
            }

            String currency = normalizeCurrency(
                    getNullableString(
                            cursor,
                            DatabaseHelper.PENDING_CURRENCY
                    )
            );

            String billingCycle = getNullableString(
                    cursor,
                    DatabaseHelper.PENDING_BILLING_CYCLE
            );

            String nextBillingDate = getNullableString(
                    cursor,
                    DatabaseHelper.PENDING_NEXT_BILLING_DATE
            );

            gmailMessageId = getNullableString(
                    cursor,
                    DatabaseHelper.PENDING_GMAIL_MESSAGE_ID
            );

            suggestionId = getNullableString(
                    cursor,
                    DatabaseHelper.PENDING_SUGGESTION_ID
            );

            emailSubject = getNullableString(
                    cursor,
                    DatabaseHelper.PENDING_EMAIL_SUBJECT
            );

            emailSender = getNullableString(
                    cursor,
                    DatabaseHelper.PENDING_EMAIL_SENDER
            );

            emailBody = getNullableString(
                    cursor,
                    DatabaseHelper.PENDING_EMAIL_BODY
            );

            int confidenceIndex = cursor.getColumnIndex(
                    DatabaseHelper.PENDING_CONFIDENCE
            );

            if (confidenceIndex != -1 && !cursor.isNull(confidenceIndex)) {
                subscriptionProbability = cursor.getDouble(confidenceIndex);
            }

            sourceType = "GMAIL";

            populateFields(
                    serviceName,
                    amount,
                    currency,
                    billingCycle,
                    nextBillingDate
            );

            populateEmailInformation();
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
        String serviceName = safeString(
                getIntent().getStringExtra("serviceName")
        );

        double amount = getIntent().getDoubleExtra("amount", 0.0);

        String currency = normalizeCurrency(
                getIntent().getStringExtra("currency")
        );

        String billingCycle = safeString(
                getIntent().getStringExtra("billingCycle")
        );

        String nextBillingDate = safeString(
                getIntent().getStringExtra("nextBillingDate")
        );

        sourceType = safeString(
                getIntent().getStringExtra("sourceType")
        );

        gmailMessageId = safeString(
                getIntent().getStringExtra("messageId")
        );

        suggestionId = safeString(
                getIntent().getStringExtra("suggestionId")
        );

        emailSubject = safeString(
                getIntent().getStringExtra("emailSubject")
        );

        emailSender = safeString(
                getIntent().getStringExtra("emailSender")
        );

        emailBody = safeString(
                getIntent().getStringExtra("emailBody")
        );

        subscriptionProbability = getIntent().getDoubleExtra(
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

        populateEmailInformation();
        updateDetectionStatus();
    }

    // =========================================================
    // POPULATE SUBSCRIPTION FIELDS
    // =========================================================

    private void populateFields(
            String serviceName,
            double amount,
            String currency,
            String billingCycle,
            String nextBillingDate
    ) {
        etServiceName.setText(safeString(serviceName));

        if (amount > 0) {
            etAmount.setText(
                    String.format(Locale.US, "%.2f", amount)
            );
        } else {
            etAmount.setText("");
        }

        // Do not guess USD when the source currency is only "$".
        if ("$".equals(currency)) {
            actCurrency.setText("", false);
        } else {
            actCurrency.setText(safeString(currency), false);
        }

        actBillingCycle.setText(
                safeString(billingCycle),
                false
        );

        etNextBillingDate.setText(
                safeString(nextBillingDate)
        );

        populateEmailInformation();
    }

    // =========================================================
    // POPULATE EMAIL INFORMATION
    // =========================================================

    private void populateEmailInformation() {
        String serviceName = getEditTextValue(etServiceName);

        if (tvReviewServiceName != null) {
            tvReviewServiceName.setText(
                    serviceName.isEmpty()
                            ? "Subscription"
                            : serviceName
            );
        }

        String subjectText = emailSubject.isEmpty()
                ? "Email subject unavailable"
                : emailSubject;

        if (tvReviewEmailSubject != null) {
            tvReviewEmailSubject.setText(subjectText);
        }

        if (tvEmailSubject != null) {
            tvEmailSubject.setText(
                    emailSubject.isEmpty()
                            ? "Not available"
                            : emailSubject
            );
        }

        if (tvEmailFrom != null) {
            tvEmailFrom.setText(
                    emailSender.isEmpty()
                            ? "Not available"
                            : emailSender
            );
        }

        if (tvEmailPreview != null) {
            tvEmailPreview.setText(
                    emailBody.isEmpty()
                            ? "Email content unavailable"
                            : emailBody
            );
        }

        // There is no dedicated email-date field in the supplied
        // database schema. Do not present CreatedAt as email date.
        if (tvEmailDate != null) {
            tvEmailDate.setText("Date unavailable");
        }
    }

    // =========================================================
    // DETECTION STATUS
    // =========================================================

    private void updateDetectionStatus() {
        // Some versions of the XML do not include this TextView.

    }

    // =========================================================
    // CURRENCY DROPDOWN
    // =========================================================

    private void setupCurrencyDropdown() {
        String[] currencies = {
                "LKR", "USD", "EUR", "GBP", "AUD",
                "CAD", "SGD", "INR", "JPY"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                currencies
        );

        actCurrency.setAdapter(adapter);

        actCurrency.setOnItemClickListener(
                (parent, view, position, id) -> updateLkrEstimate()
        );
    }

    // =========================================================
    // BILLING CYCLE DROPDOWN
    // =========================================================

    private void setupBillingCycleDropdown() {
        String[] billingCycles = {
                "Monthly", "Weekly", "Quarterly", "Yearly"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                billingCycles
        );

        actBillingCycle.setAdapter(adapter);
    }

    // =========================================================
    // DATE PICKER
    // =========================================================

    private void setupDatePicker() {
        etNextBillingDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            String currentDate = getEditTextValue(etNextBillingDate);

            if (!currentDate.isEmpty()) {
                try {
                    String[] parts = currentDate.split("-");

                    if (parts.length == 3) {
                        calendar.set(
                                Integer.parseInt(parts[0]),
                                Integer.parseInt(parts[1]) - 1,
                                Integer.parseInt(parts[2])
                        );
                    }
                } catch (Exception ignored) {
                    // Use today's date if the stored date is invalid.
                }
            }

            DatePickerDialog dialog = new DatePickerDialog(
                    ReviewSubscriptionActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String selectedDate = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                year,
                                month + 1,
                                dayOfMonth
                        );

                        etNextBillingDate.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            dialog.show();
        });
    }

    // =========================================================
    // BUTTON LISTENERS
    // =========================================================

    private void setupListeners() {
        // If the XML contains a confirmation checkbox, require it.
        // If it does not, do not crash or permanently disable saving.
        if (checkConfirmed != null) {
            btnConfirmSave.setEnabled(false);

            checkConfirmed.setOnCheckedChangeListener(
                    (buttonView, isChecked) ->
                            btnConfirmSave.setEnabled(isChecked)
            );
        } else {
            btnConfirmSave.setEnabled(true);
        }

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
        String serviceName = getEditTextValue(etServiceName);
        String amountText = getEditTextValue(etAmount);

        String currency = normalizeCurrency(
                actCurrency.getText().toString()
        );

        String billingCycle = safeString(
                actBillingCycle.getText().toString()
        );

        String nextBillingDate = getEditTextValue(etNextBillingDate);

        if (serviceName.isEmpty()) {
            etServiceName.setError("Service name is required");
            etServiceName.requestFocus();
            return;
        }

        if (amountText.isEmpty()) {
            etAmount.setError("Amount is required");
            etAmount.requestFocus();
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            etAmount.setError("Enter a valid amount");
            etAmount.requestFocus();
            return;
        }

        if (amount <= 0) {
            etAmount.setError("Amount must be greater than 0");
            etAmount.requestFocus();
            return;
        }

        if (currency.isEmpty() || "$".equals(currency)) {
            actCurrency.setError("Please select the exact currency");
            return;
        }

        if (billingCycle.isEmpty()) {
            actBillingCycle.setError("Please select billing cycle");
            return;
        }

        if (nextBillingDate.isEmpty()) {
            etNextBillingDate.setError("Next billing date is required");
            return;
        }

        if (checkConfirmed != null && !checkConfirmed.isChecked()) {
            Toast.makeText(
                    this,
                    "Please confirm that you checked the details.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Prevent duplicate active subscriptions.
        boolean exists = databaseHelper.subscriptionExists(
                userId,
                serviceName
        );

        if (exists) {
            Toast.makeText(
                    this,
                    serviceName + " already exists in active subscriptions.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        // Save the reviewed subscription.
        long insertedId = databaseHelper.insertSubscription(
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

        // Mark the source pending record as saved.
        if (pendingId > 0) {
            databaseHelper.markPendingSubscriptionSaved(pendingId);
        }

        Toast.makeText(
                this,
                "Subscription added successfully.",
                Toast.LENGTH_SHORT
        ).show();

        setResult(RESULT_OK);
        finish();
    }

    // =========================================================
    // REJECT / DO NOT SAVE
    // =========================================================

    private void ignorePendingSubscription() {
        if (pendingId > 0) {
            int result =
                    databaseHelper.markPendingSubscriptionIgnored(pendingId);

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

        setResult(RESULT_CANCELED);
        finish();
    }

    // =========================================================
    // LKR ESTIMATE
    // No invented exchange rate is used.
    // =========================================================

    private void updateLkrEstimate() {
        String amountText = getEditTextValue(etAmount);

        String currency = normalizeCurrency(
                actCurrency.getText().toString()
        );

        if (amountText.isEmpty()) {
            tvLkrEstimate.setText("Rs. —");
            tvRateInfo.setText("Enter an amount first.");
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            tvLkrEstimate.setText("Rs. —");
            tvRateInfo.setText("Enter a valid amount.");
            return;
        }

        if ("LKR".equals(currency)) {
            tvLkrEstimate.setText(
                    String.format(Locale.US, "Rs. %,.2f", amount)
            );

            tvRateInfo.setText(
                    "Original amount is already in LKR."
            );
            return;
        }

        tvLkrEstimate.setText("Rs. —");

        if (currency.isEmpty() || "$".equals(currency)) {
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

    private String normalizeCurrency(String value) {
        String currency = safeString(value);

        if (currency.isEmpty()) {
            return "";
        }

        String upper = currency.toUpperCase(Locale.US);

        if (upper.equals("RS") ||
                upper.equals("RS.") ||
                upper.equals("LKR")) {
            return "LKR";
        }

        if (currency.equals("€") || upper.equals("EUR")) {
            return "EUR";
        }

        if (currency.equals("£") || upper.equals("GBP")) {
            return "GBP";
        }

        if (currency.equals("₹") || upper.equals("INR")) {
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

        // A bare dollar sign is ambiguous.
        if (currency.equals("$")) {
            return "$";
        }

        return upper;
    }

    // =========================================================
    // EDIT TEXT HELPER
    // =========================================================

    private String getEditTextValue(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }

    // =========================================================
    // NULLABLE CURSOR STRING
    // =========================================================

    private String getNullableString(
            Cursor cursor,
            String columnName
    ) {
        int index = cursor.getColumnIndex(columnName);

        if (index == -1 || cursor.isNull(index)) {
            return "";
        }

        return safeString(cursor.getString(index));
    }

    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }
}
