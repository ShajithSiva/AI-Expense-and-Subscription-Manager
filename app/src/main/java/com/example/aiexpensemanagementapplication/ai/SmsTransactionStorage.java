package com.example.aiexpensemanagementapplication.ai;

import android.content.Context;
import android.util.Log;

import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsTransactionStorage {

    private static final String TAG = "SmsTransactionStorage";

    private final DatabaseHelper databaseHelper;

    public SmsTransactionStorage(Context context) {

        databaseHelper =
                new DatabaseHelper(
                        context.getApplicationContext()
                );
    }

    // =========================================================
    // SAVE MODEL 2 RESULT
    // =========================================================

    public boolean saveTransaction(
            String smsText,
            String category,
            double amount
    ) {

        try {

            // =================================================
            // VALIDATE AMOUNT
            // =================================================

            if (!SmsTransactionParser.hasValidAmount(amount)) {

                Log.e(
                        TAG,
                        "Transaction not saved - invalid amount"
                );

                return false;
            }

            // =================================================
            // DETERMINE TRANSACTION TYPE
            // =================================================

            String transactionType =
                    SmsTransactionParser
                            .getTransactionType(category);

            // Others must NOT be automatically stored.
            // It will be handled by the review flow later.

            if ("Others".equals(transactionType)) {

                Log.d(
                        TAG,
                        "OTHERS category - waiting for user review"
                );

                return false;
            }

            // OTP must never be stored

            if ("Ignore".equals(transactionType)) {

                Log.d(
                        TAG,
                        "OTP/security message ignored"
                );

                return false;
            }

            if ("Unknown".equals(transactionType)) {

                Log.e(
                        TAG,
                        "Unknown transaction category: "
                                + category
                );

                return false;
            }

            // =================================================
            // GET CURRENT LOGGED-IN USER
            // =================================================

            FirebaseUser firebaseUser =
                    FirebaseAuth
                            .getInstance()
                            .getCurrentUser();

            if (firebaseUser == null) {

                Log.e(
                        TAG,
                        "Transaction not saved - user not logged in"
                );

                return false;
            }

            int userId =
                    databaseHelper
                            .getUserIdByFirebaseUid(
                                    firebaseUser.getUid()
                            );

            if (userId == -1) {

                Log.e(
                        TAG,
                        "Transaction not saved - local user not found"
                );

                return false;
            }

            // =================================================
            // GET CATEGORY ID
            // =================================================

            int categoryId =
                    databaseHelper
                            .getCategoryIdByName(category);

            if (categoryId == -1) {

                Log.e(
                        TAG,
                        "Category not found in database: "
                                + category
                );

                return false;
            }

            // =================================================
            // PAYMENT METHOD
            //
            // SMS transaction came from a financial institution.
            // Use Bank Transfer as the default SMS payment method.
            // =================================================

            int paymentMethodId =
                    databaseHelper
                            .getPaymentMethodIdByName(
                                    "Bank Transfer"
                            );

            if (paymentMethodId == -1) {

                Log.e(
                        TAG,
                        "Bank Transfer payment method not found"
                );

                return false;
            }

            // =================================================
            // CURRENT TRANSACTION DATE
            // Database queries expect yyyy-MM-dd compatible date.
            // =================================================

            String transactionDate =
                    new SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                    ).format(new Date());

            // =================================================
            // SOURCE
            // =================================================

            String source =
                    "SMS";

            // =================================================
            // EXPENSE MODE
            // Existing transaction schema requires this value.
            // Personal is the default.
            // Family sharing will be handled separately.
            // =================================================

            String expenseMode =
                    "Personal";

            // =================================================
            // INSERT INTO EXISTING TRANSACTIONS TABLE
            // =================================================

            long result =
                    databaseHelper.insertTransaction(
                            userId,
                            paymentMethodId,
                            categoryId,
                            amount,
                            transactionType,
                            transactionDate,
                            source,
                            expenseMode
                    );

            if (result == -1) {

                Log.e(
                        TAG,
                        "Failed to insert SMS transaction"
                );

                return false;
            }

            Log.d(
                    TAG,
                    "SMS TRANSACTION SAVED"
                            + " | ID=" + result
                            + " | Type=" + transactionType
                            + " | Category=" + category
                            + " | Amount=" + amount
            );

            return true;

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Error saving SMS transaction",
                    e
            );

            return false;
        }
    }
}