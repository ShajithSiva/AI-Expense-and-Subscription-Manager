package com.example.aiexpensemanagementapplication.ui.smsreview;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;

import com.example.aiexpensemanagementapplication.data.remote.FamilyFirestoreService;

import com.example.aiexpensemanagementapplication.ai.SmsTransactionParser;

import java.util.ArrayList;

public class ReviewSmsTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_PENDING_SMS_ID =
            "pending_sms_id";

    private TextView tvSmsSender;
    private TextView tvSmsBody;
    private TextView tvSmsAmount;

    private Spinner spinnerCategory;
    private Spinner spinnerFamily;

    private CheckBox checkShareFamily;

    private Button btnSaveReviewedSms;

    private DatabaseHelper databaseHelper;

    private FamilyFirestoreService familyFirestoreService;
    private final ArrayList<Integer> familyIds = new ArrayList<>();
    private final ArrayList<String> familyNames = new ArrayList<>();
    private long pendingSmsId = -1;

    private int pendingUserId = -1;
    private double pendingAmount = 0;
    private String pendingSmsBody = "";
    private String pendingTransactionDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_review_sms_transaction
        );

        databaseHelper =
                new DatabaseHelper(this);

        familyFirestoreService = new FamilyFirestoreService();

        initializeViews();

        setupCategorySpinner();

        setupFamilyCheckbox();

        setupSaveButton();

        getPendingSmsIdFromIntent();
    }


    // =========================================================
    // INITIALIZE XML VIEWS
    // =========================================================

    private void initializeViews() {

        tvSmsSender =
                findViewById(R.id.tvSmsSender);

        tvSmsBody =
                findViewById(R.id.tvSmsBody);

        tvSmsAmount =
                findViewById(R.id.tvSmsAmount);

        spinnerCategory =
                findViewById(R.id.spinnerCategory);

        spinnerFamily =
                findViewById(R.id.spinnerFamily);

        checkShareFamily =
                findViewById(R.id.checkShareFamily);

        btnSaveReviewedSms =
                findViewById(R.id.btnSaveReviewedSms);
    }


    // =========================================================
    // CATEGORY SPINNER
    // =========================================================

    private void setupCategorySpinner() {

        ArrayList<String> categoryNames =
                new ArrayList<>();

        Cursor cursor =
                databaseHelper.getAllCategories();

        try {

            while (cursor.moveToNext()) {

                String categoryName =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.CATEGORY_NAME
                                )
                        );

                // OTP_REJECT must never appear to the user
                if (!"OTP_REJECT".equals(categoryName)) {

                    categoryNames.add(
                            categoryName
                    );
                }
            }

        } finally {

            cursor.close();
        }


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categoryNames
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerCategory.setAdapter(adapter);
    }


    // =========================================================
    // FAMILY SHARE CHECKBOX
    // =========================================================

    private void setupFamilyCheckbox() {

        spinnerFamily.setVisibility(View.GONE);

        checkShareFamily.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        spinnerFamily.setVisibility(
                                View.VISIBLE
                        );

                    } else {

                        spinnerFamily.setVisibility(
                                View.GONE
                        );
                    }
                }
        );
    }


    // =========================================================
    // GET PENDING SMS ID
    // =========================================================

    private void getPendingSmsIdFromIntent() {

        pendingSmsId =
                getIntent().getLongExtra(
                        EXTRA_PENDING_SMS_ID,
                        -1
                );

        if (pendingSmsId == -1) {

            Toast.makeText(
                    this,
                    "Pending SMS transaction not found",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }

        loadPendingSmsTransaction();
    }


    // =========================================================
    // LOAD PENDING SMS FROM DATABASE
    // =========================================================

    private void loadPendingSmsTransaction() {

        Cursor cursor =
                databaseHelper
                        .getPendingSmsTransaction(
                                pendingSmsId
                        );

        try {

            if (!cursor.moveToFirst()) {

                Toast.makeText(
                        this,
                        "SMS transaction not found",
                        Toast.LENGTH_LONG
                ).show();

                finish();

                return;
            }


            pendingUserId =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.PENDING_SMS_USER_ID
                            )
                    );


            String sender =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.PENDING_SMS_SENDER
                            )
                    );


            pendingSmsBody =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.PENDING_SMS_BODY
                            )
                    );


            pendingAmount =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.PENDING_SMS_AMOUNT
                            )
                    );


            pendingTransactionDate =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.PENDING_SMS_TRANSACTION_DATE
                            )
                    );


            String status =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.PENDING_SMS_STATUS
                            )
                    );


            if (!"PENDING".equals(status)) {

                Toast.makeText(
                        this,
                        "This SMS transaction is already reviewed",
                        Toast.LENGTH_LONG
                ).show();

                finish();

                return;
            }


            tvSmsSender.setText(
                    sender == null ? "-" : sender
            );

            tvSmsBody.setText(
                    pendingSmsBody == null
                            ? "-"
                            : pendingSmsBody
            );

            tvSmsAmount.setText(
                    String.format(
                            java.util.Locale.getDefault(),
                            "LKR %.2f",
                            pendingAmount
                    )
            );


            selectOthersCategory();

            loadUserFamilies();

        } finally {

            cursor.close();
        }
    }


    // =========================================================
    // DEFAULT CATEGORY = OTHERS
    // =========================================================

    private void selectOthersCategory() {

        ArrayAdapter adapter =
                (ArrayAdapter)
                        spinnerCategory.getAdapter();

        if (adapter == null) {
            return;
        }

        for (int i = 0;
             i < adapter.getCount();
             i++) {

            Object item =
                    adapter.getItem(i);

            if (item != null &&
                    "Others".equals(
                            item.toString()
                    )) {

                spinnerCategory.setSelection(i);

                break;
            }
        }
    }

    // =========================================================
// LOAD USER FAMILIES
// =========================================================

    private void loadUserFamilies() {

        familyIds.clear();
        familyNames.clear();

        ArrayList<Integer> userFamilyIds =
                databaseHelper.getFamilyIdsForUser(
                        pendingUserId
                );

        if (userFamilyIds != null) {

            for (Integer familyId : userFamilyIds) {

                if (familyId == null) {
                    continue;
                }

                String familyName =
                        databaseHelper.getFamilyNameById(
                                familyId
                        );

                if (familyName != null &&
                        !familyName.trim().isEmpty()) {

                    familyIds.add(familyId);

                    familyNames.add(familyName);
                }
            }
        }


        ArrayAdapter<String> familyAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        familyNames
                );

        familyAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerFamily.setAdapter(
                familyAdapter
        );


        // User does not belong to any family
        if (familyIds.isEmpty()) {

            checkShareFamily.setChecked(false);

            checkShareFamily.setEnabled(false);

            spinnerFamily.setVisibility(
                    View.GONE
            );

            checkShareFamily.setText(
                    "Share with family (No family available)"
            );

        } else {

            checkShareFamily.setEnabled(true);

            checkShareFamily.setText(
                    "Share this transaction with family"
            );
        }
    }

    // =========================================================
// SAVE BUTTON
// =========================================================

    private void setupSaveButton() {

        btnSaveReviewedSms.setOnClickListener(
                view -> saveReviewedTransaction()
        );
    }

    // =========================================================
// SAVE REVIEWED SMS AS NORMAL TRANSACTION
// =========================================================

    private void saveReviewedTransaction() {

        // =========================================================
        // 1. VALIDATE PENDING SMS
        // =========================================================

        if (pendingSmsId == -1 || pendingUserId == -1) {

            Toast.makeText(
                    this,
                    "Invalid pending SMS transaction",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (pendingAmount <= 0) {

            Toast.makeText(
                    this,
                    "Invalid transaction amount",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =========================================================
        // 2. GET SELECTED CATEGORY
        // =========================================================

        Object selectedItem =
                spinnerCategory.getSelectedItem();

        if (selectedItem == null) {

            Toast.makeText(
                    this,
                    "Please select a category",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String selectedCategory =
                selectedItem.toString();


        // OTP_REJECT must never be stored
        if ("OTP_REJECT".equals(selectedCategory)) {

            Toast.makeText(
                    this,
                    "Invalid category",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =========================================================
        // 3. DETERMINE TRANSACTION TYPE
        // =========================================================

        String transactionType =
                SmsTransactionParser.getTransactionType(
                        selectedCategory
                );

        if (!"Expense".equals(transactionType) &&
                !"Income".equals(transactionType) &&
                !"Others".equals(transactionType)) {

            Toast.makeText(
                    this,
                    "Unable to determine transaction type",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // User is allowed to keep Others.
        // Others is stored as an Expense transaction.
        if ("Others".equals(transactionType)) {

            transactionType = "Expense";
        }


        // =========================================================
        // 4. CATEGORY ID
        // =========================================================

        int categoryId =
                databaseHelper.getCategoryIdByName(
                        selectedCategory
                );

        if (categoryId == -1) {

            Toast.makeText(
                    this,
                    "Category not found",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =========================================================
        // 5. PAYMENT METHOD
        // =========================================================

        String paymentMethodName =
                "Bank Transfer";

        int paymentMethodId =
                databaseHelper.getPaymentMethodIdByName(
                        paymentMethodName
                );

        if (paymentMethodId == -1) {

            Toast.makeText(
                    this,
                    "Bank Transfer payment method not found",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =========================================================
        // 6. VALIDATE FAMILY SELECTION
        // =========================================================

        boolean shareWithFamily =
                checkShareFamily.isChecked();

        int selectedFamilyId = -1;

        if (shareWithFamily) {

            if (familyIds.isEmpty()) {

                Toast.makeText(
                        this,
                        "No family available for sharing",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            int selectedPosition =
                    spinnerFamily.getSelectedItemPosition();

            if (selectedPosition < 0 ||
                    selectedPosition >= familyIds.size()) {

                Toast.makeText(
                        this,
                        "Please select a family",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            selectedFamilyId =
                    familyIds.get(selectedPosition);
        }


        // =========================================================
        // 7. FIREBASE USER
        // =========================================================

        com.google.firebase.auth.FirebaseUser currentUser =
                com.google.firebase.auth.FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "User not logged in",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =========================================================
        // 8. SAVE NORMAL PERSONAL TRANSACTION
        // =========================================================

        btnSaveReviewedSms.setEnabled(false);

        long transactionId =
                databaseHelper.insertTransaction(
                        pendingUserId,
                        paymentMethodId,
                        categoryId,
                        pendingAmount,
                        transactionType,
                        pendingTransactionDate,
                        "SMS",
                        "Personal"
                );

        if (transactionId == -1) {

            btnSaveReviewedSms.setEnabled(true);

            Toast.makeText(
                    this,
                    "Failed to save transaction",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =========================================================
        // 9. PERSONAL ONLY
        // =========================================================

        if (!shareWithFamily) {

            completePendingSmsAndFinish(
                    "Transaction saved successfully"
            );

            return;
        }


        // =========================================================
        // 10. SAVE LOCAL FAMILY SHARE
        // =========================================================

        boolean localShareSuccess;

        if ("Income".equals(transactionType)) {

            localShareSuccess =
                    databaseHelper.shareIncomeWithFamily(
                            (int) transactionId,
                            selectedFamilyId,
                            pendingUserId
                    );

        } else {

            localShareSuccess =
                    databaseHelper.shareExpenseWithFamily(
                            (int) transactionId,
                            selectedFamilyId,
                            pendingUserId
                    );
        }

        if (!localShareSuccess) {

            btnSaveReviewedSms.setEnabled(true);

            Toast.makeText(
                    this,
                    "Transaction saved, but local family sharing failed",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =========================================================
        // 11. GET FIRESTORE FAMILY ID
        // =========================================================

        String firestoreFamilyId =
                databaseHelper.getFirestoreFamilyId(
                        selectedFamilyId
                );

        if (firestoreFamilyId == null ||
                firestoreFamilyId.trim().isEmpty()) {

            btnSaveReviewedSms.setEnabled(true);

            Toast.makeText(
                    this,
                    "Transaction saved locally, but family Firestore ID was not found",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =========================================================
        // 12. OWNER DETAILS
        // =========================================================

        String ownerName =
                currentUser.getDisplayName();

        if (ownerName == null ||
                ownerName.trim().isEmpty()) {

            ownerName = "Family Member";
        }


        // Values used inside asynchronous callback
        final String finalOwnerName =
                ownerName;

        final String finalTransactionType =
                transactionType;


        // =========================================================
        // 13. FIRESTORE FAMILY SYNC
        // =========================================================

        if ("Income".equals(finalTransactionType)) {

            familyFirestoreService.addFamilyIncome(

                    firestoreFamilyId,

                    String.valueOf(transactionId),

                    currentUser.getUid(),

                    finalOwnerName,

                    pendingAmount,

                    selectedCategory,

                    categoryId,

                    paymentMethodName,

                    paymentMethodId,

                    pendingTransactionDate,

                    "SMS",

                    new FamilyFirestoreService.FamilyIncomeCallback() {

                        @Override
                        public void onSuccess() {

                            completePendingSmsAndFinish(
                                    "Income shared with family successfully"
                            );
                        }

                        @Override
                        public void onFailure(String message) {

                            completePendingSmsAndFinish(
                                    "Transaction saved, but family sync failed: "
                                            + message
                            );
                        }
                    }
            );

        } else {

            familyFirestoreService.addFamilyExpense(

                    firestoreFamilyId,

                    String.valueOf(transactionId),

                    currentUser.getUid(),

                    finalOwnerName,

                    pendingAmount,

                    selectedCategory,

                    categoryId,

                    paymentMethodName,

                    paymentMethodId,

                    pendingTransactionDate,

                    "SMS",

                    new FamilyFirestoreService.FamilyTransactionCallback() {

                        @Override
                        public void onSuccess() {

                            completePendingSmsAndFinish(
                                    "Expense shared with family successfully"
                            );
                        }

                        @Override
                        public void onFailure(String message) {

                            completePendingSmsAndFinish(
                                    "Transaction saved, but family sync failed: "
                                            + message
                            );
                        }
                    }
            );
        }
    }

    private void completePendingSmsAndFinish(
            String message
    ) {

        int updatedRows =
                databaseHelper.markPendingSmsAsCompleted(
                        pendingSmsId
                );

        if (updatedRows <= 0) {

            btnSaveReviewedSms.setEnabled(true);

            Toast.makeText(
                    this,
                    "Transaction saved, but review status could not be updated",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        databaseHelper.deleteSmsReviewNotification(pendingSmsId);

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();

        finish();
    }
}