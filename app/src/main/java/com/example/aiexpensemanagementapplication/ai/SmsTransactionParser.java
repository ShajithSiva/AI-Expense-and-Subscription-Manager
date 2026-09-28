package com.example.aiexpensemanagementapplication.ai;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsTransactionParser {

    // =========================================================
    // AMOUNT PATTERNS
    // Supports:
    // LKR 5,000.00
    // LKR 5000
    // Rs. 2,450.00
    // Rs 2450
    // 5000 LKR
    // =========================================================

    private static final Pattern CURRENCY_BEFORE_AMOUNT =
            Pattern.compile(
                    "(?i)(?:LKR|Rs\\.?|රු\\.?)\\s*([0-9][0-9,]*(?:\\.\\d{1,2})?)"
            );

    private static final Pattern CURRENCY_AFTER_AMOUNT =
            Pattern.compile(
                    "(?i)([0-9][0-9,]*(?:\\.\\d{1,2})?)\\s*(?:LKR|Rs\\.?)"
            );

    // =========================================================
    // EXTRACT AMOUNT
    // =========================================================

    public static double extractAmount(String smsText) {

        if (smsText == null || smsText.trim().isEmpty()) {
            return -1;
        }

        // First try:
        // LKR 5000 / Rs. 5000

        Matcher matcher =
                CURRENCY_BEFORE_AMOUNT.matcher(smsText);

        if (matcher.find()) {

            return parseAmount(
                    matcher.group(1)
            );
        }

        // Second try:
        // 5000 LKR

        matcher =
                CURRENCY_AFTER_AMOUNT.matcher(smsText);

        if (matcher.find()) {

            return parseAmount(
                    matcher.group(1)
            );
        }

        // Amount could not be identified safely
        return -1;
    }

    // =========================================================
    // CONVERT STRING AMOUNT TO DOUBLE
    // =========================================================

    private static double parseAmount(String amountText) {

        try {

            if (amountText == null) {
                return -1;
            }

            String cleaned =
                    amountText.replace(",", "").trim();

            return Double.parseDouble(cleaned);

        } catch (Exception e) {

            return -1;
        }
    }

    // =========================================================
    // CHECK WHETHER AMOUNT WAS FOUND
    // =========================================================

    public static boolean hasValidAmount(double amount) {

        return amount > 0;
    }

    // =========================================================
    // DETERMINE TRANSACTION TYPE FROM MODEL CATEGORY
    // =========================================================

    public static String getTransactionType(String category) {

        if (category == null) {
            return "Unknown";
        }

        switch (category) {

            // -------------------------
            // INCOME
            // -------------------------

            case "Salary":
            case "Business":
            case "Investment":
            case "Gift":

                return "Income";

            // -------------------------
            // EXPENSE
            // -------------------------

            case "Food":
            case "Transport":
            case "Shopping":
            case "Bills":
            case "Health":
            case "Education":
            case "Entertainment":
            case "Travel":

                return "Expense";

            // -------------------------
            // NEEDS USER REVIEW
            // -------------------------

            case "Others":

                return "Others";

            // -------------------------
            // SECURITY
            // -------------------------

            case "OTP_REJECT":

                return "Ignore";

            default:

                return "Unknown";
        }
    }
}