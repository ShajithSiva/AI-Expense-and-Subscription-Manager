package com.example.aiexpensemanagementapplication.ai;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubscriptionEmailExtractor {

    public static class SubscriptionDetails {

        public String serviceName = "";
        public double amount = 0.0;
        public String currency = "";
        public String billingCycle = "";
        public String nextBillingDate = "";

        public boolean isValid() {
            return !serviceName.isEmpty()
                    || amount > 0
                    || !billingCycle.isEmpty();
        }
    }

    public static SubscriptionDetails extract(
            String sender,
            String subject,
            String content
    ) {

        SubscriptionDetails details =
                new SubscriptionDetails();

        String text =
                ((sender == null ? "" : sender) + " "
                        + (subject == null ? "" : subject) + " "
                        + (content == null ? "" : content))
                        .toLowerCase();

        // =========================================
        // 1. SERVICE NAME
        // =========================================

        details.serviceName =
                extractServiceName(
                        sender,
                        subject,
                        content
                );

        // =========================================
        // 2. AMOUNT
        // =========================================

        Pattern amountPattern =
                Pattern.compile(
                        "(rs\\.?|lkr|usd|\\$|€|£)\\s*([0-9,]+(?:\\.\\d{1,2})?)",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher amountMatcher =
                amountPattern.matcher(text);

        if (amountMatcher.find()) {

            details.currency =
                    amountMatcher.group(1)
                            .replace(".", "")
                            .toUpperCase();

            String amountText =
                    amountMatcher.group(2)
                            .replace(",", "");

            try {
                details.amount =
                        Double.parseDouble(amountText);
            } catch (Exception ignored) {
                details.amount = 0.0;
            }
        }

        // =========================================
        // 3. BILLING CYCLE
        // =========================================

        if (text.contains("monthly")
                || text.contains("every month")
                || text.contains("per month")
                || text.contains("monthly subscription")) {

            details.billingCycle = "Monthly";

        } else if (text.contains("annual")
                || text.contains("yearly")
                || text.contains("every year")
                || text.contains("per year")) {

            details.billingCycle = "Yearly";

        } else if (text.contains("weekly")
                || text.contains("every week")
                || text.contains("per week")) {

            details.billingCycle = "Weekly";

        } else if (text.contains("daily")
                || text.contains("every day")
                || text.contains("per day")) {

            details.billingCycle = "Daily";
        }

        // =========================================
        // 4. NEXT BILLING DATE
        // =========================================

        details.nextBillingDate =
                extractDate(text);

        return details;
    }

    // ==================================================
    // SERVICE NAME
    // ==================================================

    private static String extractServiceName(
            String sender,
            String subject,
            String content
    ) {

        String combined =
                ((sender == null ? "" : sender) + " "
                        + (subject == null ? "" : subject) + " "
                        + (content == null ? "" : content));

        String lower =
                combined.toLowerCase();

        // Common subscription services

        String[] services = {
                "netflix",
                "spotify",
                "youtube",
                "youtube premium",
                "amazon prime",
                "google one",
                "google",
                "microsoft 365",
                "linkedin",
                "dropbox",
                "adobe",
                "canva",
                "grammarly",
                "chatgpt",
                "notion",
                "zoom",
                "disney+",
                "apple music",
                "icloud",
                "github",
                "coursera",
                "duolingo",
                "daraz",
                "uber",
                "uber eats",
                "pickme"
        };

        for (String service : services) {

            if (lower.contains(service)) {

                return service;
            }
        }

        // ==========================================
        // Try extracting from sender
        // ==========================================

        if (sender != null && !sender.isEmpty()) {

            int atIndex =
                    sender.indexOf("@");

            if (atIndex > 0) {

                String emailPart =
                        sender.substring(0, atIndex);

                emailPart =
                        emailPart
                                .replaceAll(
                                        "[<>\"']",
                                        ""
                                )
                                .trim();

                if (!emailPart.isEmpty()) {
                    return emailPart;
                }
            }
        }

        return "Unknown Service";
    }

    // ==================================================
    // DATE
    // ==================================================

    private static String extractDate(
            String text
    ) {

        // YYYY-MM-DD

        Pattern isoPattern =
                Pattern.compile(
                        "\\b(20\\d{2})[-/](\\d{1,2})[-/](\\d{1,2})\\b"
                );

        Matcher isoMatcher =
                isoPattern.matcher(text);

        if (isoMatcher.find()) {

            return isoMatcher.group();
        }

        // DD/MM/YYYY

        Pattern normalPattern =
                Pattern.compile(
                        "\\b(\\d{1,2})[/.-](\\d{1,2})[/.-](20\\d{2})\\b"
                );

        Matcher normalMatcher =
                normalPattern.matcher(text);

        if (normalMatcher.find()) {

            return normalMatcher.group();
        }

        return "";
    }
}