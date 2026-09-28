package com.example.aiexpensemanagementapplication.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.ui.smsreview.ReviewSmsTransactionActivity;

import com.example.aiexpensemanagementapplication.ai.RobustSMSCategoryClassifier;
import com.example.aiexpensemanagementapplication.ai.SMSMNBClassifier;
import com.example.aiexpensemanagementapplication.ai.SmsTransactionParser;
import com.example.aiexpensemanagementapplication.ai.SmsTransactionStorage;

import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SMSReceiver extends BroadcastReceiver {

    private static final String TAG = "SMSReceiver";

    // =========================================================
    // TRUSTED FINANCIAL SENDERS
    // =========================================================

    private static final Set<String> TRUSTED_SENDERS =
            new HashSet<>(Arrays.asList(

                    "COMBANK",
                    "BOC",
                    "SAMPATH",
                    "HNB",
                    "NDB",
                    "DFCC",
                    "NSB",
                    "PEOPLES"

            ));

    @Override
    public void onReceive(Context context, Intent intent) {

        // =====================================================
        // CHECK SMS BROADCAST
        // =====================================================

        if (!"android.provider.Telephony.SMS_RECEIVED"
                .equals(intent.getAction())) {

            return;
        }

        try {

            Bundle bundle = intent.getExtras();

            if (bundle == null) {
                return;
            }

            Object[] pdus =
                    (Object[]) bundle.get("pdus");

            if (pdus == null || pdus.length == 0) {
                return;
            }

            String format =
                    bundle.getString("format");

            // =================================================
            // LOAD MODEL 1
            // =================================================

            SMSMNBClassifier model1Classifier =
                    new SMSMNBClassifier(context);

            // =================================================
            // LOAD MODEL 2
            // =================================================

            RobustSMSCategoryClassifier model2Classifier =
                    new RobustSMSCategoryClassifier(context);

            // =================================================
            // LOAD TRANSACTION STORAGE
            // =================================================

            SmsTransactionStorage transactionStorage =
                    new SmsTransactionStorage(context);

            // =================================================
            // PROCESS EACH SMS
            // =================================================

            for (Object pdu : pdus) {

                SmsMessage smsMessage;

                if (android.os.Build.VERSION.SDK_INT >=
                        android.os.Build.VERSION_CODES.M) {

                    smsMessage =
                            SmsMessage.createFromPdu(
                                    (byte[]) pdu,
                                    format
                            );

                } else {

                    smsMessage =
                            SmsMessage.createFromPdu(
                                    (byte[]) pdu
                            );
                }

                if (smsMessage == null) {
                    continue;
                }

                // =================================================
                // GET SMS INFORMATION
                // =================================================

                String sender =
                        smsMessage.getOriginatingAddress();

                String message =
                        smsMessage.getMessageBody();

                if (message == null ||
                        message.trim().isEmpty()) {

                    continue;
                }

                Log.d(
                        TAG,
                        "SMS received from: " + sender
                );

                Log.d(
                        TAG,
                        "SMS message: " + message
                );

                // =================================================
                // CHECK TRUSTED FINANCIAL SENDER
                // =================================================

                if (!isTrustedFinancialSender(sender)) {

                    Log.d(
                            TAG,
                            "IGNORED - Untrusted sender: "
                                    + sender
                    );

                    continue;
                }

                Log.d(
                        TAG,
                        "TRUSTED FINANCIAL SENDER: "
                                + sender
                );

                // =================================================
                // MODEL 1 - TRANSACTION DETECTION
                // =================================================

                SMSMNBClassifier.Prediction prediction =
                        model1Classifier.predict(message);

                Log.d(
                        TAG,
                        "MODEL 1 PREDICTION: "
                                + prediction.getLabel()
                );

                Log.d(
                        TAG,
                        "MODEL 1 CONFIDENCE: "
                                + prediction.getConfidence()
                                + "%"
                );

                // =================================================
                // IF MODEL 1 SAYS NON-TRANSACTION
                // =================================================

                if (!prediction.isTransaction()) {

                    Log.d(
                            TAG,
                            "RESULT: NON-TRANSACTION SMS"
                    );

                    continue;
                }

                Log.d(
                        TAG,
                        "RESULT: TRANSACTION SMS"
                );

                // =================================================
                // MODEL 2 - CATEGORY CLASSIFICATION
                // =================================================

                String category =
                        model2Classifier.predict(message);

                Log.d(
                        TAG,
                        "MODEL 2 CATEGORY: "
                                + category
                );

                // =================================================
                // OTP_REJECT
                // =================================================

                if ("OTP_REJECT".equals(category)) {

                    Log.d(
                            TAG,
                            "OTP_REJECT - SMS ignored"
                    );

                    continue;
                }

                // =================================================
                // EXTRACT TRANSACTION AMOUNT
                // =================================================

                double amount =
                        SmsTransactionParser
                                .extractAmount(message);

                Log.d(
                        TAG,
                        "EXTRACTED AMOUNT: "
                                + amount
                );

                // =================================================
                // AMOUNT NOT FOUND
                // =================================================

                if (!SmsTransactionParser
                        .hasValidAmount(amount)) {

                    Log.d(
                            TAG,
                            "Amount could not be extracted. "
                                    + "Transaction not automatically saved."
                    );

                    /*
                     * Later this can also be sent to
                     * the review screen.
                     */

                    continue;
                }

                // =================================================
                // OTHERS
                //
                // Do NOT automatically store this as a normal
                // transaction.
                //
                // In the next step, this will be stored as a
                // pending SMS transaction and the user will be
                // asked to choose the correct category.
                // =================================================

                if ("Others".equals(category)) {

                    Log.d(
                            TAG,
                            "OTHERS detected."
                                    + " Amount=" + amount
                                    + ". Saving for user review."
                    );

                    try {

                        // ---------------------------------------------
                        // GET CURRENT FIREBASE USER
                        // ---------------------------------------------

                        FirebaseUser firebaseUser =
                                FirebaseAuth
                                        .getInstance()
                                        .getCurrentUser();

                        if (firebaseUser == null) {

                            Log.e(
                                    TAG,
                                    "Cannot save pending SMS - user not logged in"
                            );

                            continue;
                        }

                        // ---------------------------------------------
                        // GET LOCAL USER ID
                        // ---------------------------------------------

                        DatabaseHelper databaseHelper =
                                new DatabaseHelper(context);

                        int userId =
                                databaseHelper
                                        .getUserIdByFirebaseUid(
                                                firebaseUser.getUid()
                                        );

                        if (userId == -1) {

                            Log.e(
                                    TAG,
                                    "Cannot save pending SMS - local user not found"
                            );

                            continue;
                        }

                        // ---------------------------------------------
                        // SMS TRANSACTION DATE
                        // ---------------------------------------------

                        String transactionDate =
                                new SimpleDateFormat(
                                        "yyyy-MM-dd",
                                        Locale.getDefault()
                                ).format(new Date());

                        // ---------------------------------------------
                        // SAVE AS PENDING
                        // ---------------------------------------------

                        long pendingSmsId =
                                databaseHelper
                                        .insertPendingSmsTransaction(
                                                userId,
                                                sender,
                                                message,
                                                amount,
                                                category,
                                                transactionDate
                                        );

                        if (pendingSmsId != -1) {

                            Log.d(
                                    TAG,
                                    "PENDING SMS SAVED SUCCESSFULLY"
                                            + " | PendingSmsID="
                                            + pendingSmsId
                                            + " | Amount="
                                            + amount
                            );

                            showPendingSmsReviewNotification(
                                    context,
                                    pendingSmsId,
                                    amount
                            );

                        } else {

                            Log.e(
                                    TAG,
                                    "FAILED TO SAVE PENDING SMS"
                            );
                        }

                    } catch (Exception e) {

                        Log.e(
                                TAG,
                                "Error saving pending SMS",
                                e
                        );
                    }

                    // IMPORTANT:
                    // Others must NOT continue into automatic transaction save.
                    continue;
                }

                // =================================================
                // KNOWN CATEGORY
                //
                // Food, Shopping, Bills, Health, etc.
                // Salary, Business, Investment, Gift, etc.
                // =================================================

                String transactionType =
                        SmsTransactionParser
                                .getTransactionType(category);

                Log.d(
                        TAG,
                        "TRANSACTION TYPE: "
                                + transactionType
                );

                // =================================================
                // AUTOMATICALLY SAVE KNOWN CATEGORY
                // =================================================

                boolean saved =
                        transactionStorage.saveTransaction(
                                message,
                                category,
                                amount
                        );

                if (saved) {

                    Log.d(
                            TAG,
                            "SUCCESS: SMS transaction saved"
                                    + " | Category=" + category
                                    + " | Amount=" + amount
                    );

                } else {

                    Log.e(
                            TAG,
                            "FAILED: SMS transaction was not saved"
                                    + " | Category=" + category
                                    + " | Amount=" + amount
                    );
                }
            }

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Error processing SMS",
                    e
            );
        }
    }

    // =========================================================
    // TRUSTED SENDER CHECK
    // =========================================================

    // =========================================================
// SHOW PENDING SMS REVIEW NOTIFICATION
// =========================================================

    private void showPendingSmsReviewNotification(
            Context context,
            long pendingSmsId,
            double amount
    ) {

        final String channelId =
                "sms_transaction_review";

        // -----------------------------------------------------
        // CREATE NOTIFICATION CHANNEL - ANDROID 8+
        // -----------------------------------------------------

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            channelId,
                            "SMS Transaction Review",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Notifications for SMS transactions that need category review"
            );

            NotificationManager notificationManager =
                    (NotificationManager)
                            context.getSystemService(
                                    Context.NOTIFICATION_SERVICE
                            );

            if (notificationManager != null) {

                notificationManager.createNotificationChannel(
                        channel
                );
            }
        }


        // -----------------------------------------------------
        // OPEN REVIEW ACTIVITY WHEN NOTIFICATION IS TAPPED
        // -----------------------------------------------------

        Intent reviewIntent =
                new Intent(
                        context,
                        ReviewSmsTransactionActivity.class
                );

        reviewIntent.putExtra(
                ReviewSmsTransactionActivity.EXTRA_PENDING_SMS_ID,
                pendingSmsId
        );

        reviewIntent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        );


        int requestCode =
                (int) (pendingSmsId & 0x7fffffff);


        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        requestCode,
                        reviewIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );


        // -----------------------------------------------------
        // BUILD NOTIFICATION
        // -----------------------------------------------------

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        channelId
                )
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(
                                "Review SMS Transaction"
                        )
                        .setContentText(
                                String.format(
                                        Locale.getDefault(),
                                        "LKR %.2f needs a category",
                                        amount
                                )
                        )
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);


        // -----------------------------------------------------
        // SHOW NOTIFICATION
        // -----------------------------------------------------

        try {

            NotificationManagerCompat
                    .from(context)
                    .notify(
                            requestCode,
                            builder.build()
                    );

        } catch (SecurityException e) {

            Log.e(
                    TAG,
                    "Notification permission not granted",
                    e
            );
        }
    }
    private boolean isTrustedFinancialSender(String sender) {

        if (sender == null ||
                sender.trim().isEmpty()) {

            return false;
        }

        String normalizedSender =
                sender.trim().toUpperCase();

        return TRUSTED_SENDERS.contains(
                normalizedSender
        );
    }
}