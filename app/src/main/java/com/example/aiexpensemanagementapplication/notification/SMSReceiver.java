package com.example.aiexpensemanagementapplication.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.aiexpensemanagementapplication.ai.SMSMNBClassifier;

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

                    // Add verified bank/payment sender IDs here
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

            SMSMNBClassifier classifier =
                    new SMSMNBClassifier(context);

            // =================================================
            // PROCESS EACH SMS PART
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
                // GET SENDER AND MESSAGE
                // =================================================

                String sender =
                        smsMessage.getOriginatingAddress();

                String message =
                        smsMessage.getMessageBody();

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
                            "IGNORED — Untrusted sender: "
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
                // MODEL 1 — TRANSACTION DETECTION
                // =================================================

                SMSMNBClassifier.Prediction prediction =
                        classifier.predict(message);

                Log.d(
                        TAG,
                        "Prediction: "
                                + prediction.getLabel()
                );

                Log.d(
                        TAG,
                        "Confidence: "
                                + prediction.getConfidence()
                                + "%"
                );

                // =================================================
                // MODEL 1 RESULT
                // =================================================

                if (prediction.isTransaction()) {

                    Log.d(
                            TAG,
                            "RESULT: TRANSACTION SMS"
                    );

                    // =================================================
                    // NEXT STEP:
                    // MODEL 2 WILL BE CALLED HERE
                    // =================================================

                } else {

                    Log.d(
                            TAG,
                            "RESULT: NON-TRANSACTION SMS"
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