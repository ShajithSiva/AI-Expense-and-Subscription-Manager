package com.example.aiexpensemanagementapplication.data.remote.subscription;

import com.google.gson.annotations.SerializedName;

public class PredictionResponse {

    @SerializedName("suggestion_id")
    private String suggestionId;

    @SerializedName("predicted_label")
    private String predictedLabel;

    @SerializedName("subscription_probability")
    private double subscriptionProbability;

    @SerializedName("binary_threshold")
    private double binaryThreshold;

    @SerializedName("model_version")
    private String modelVersion;

    @SerializedName("input_truncated")
    private boolean inputTruncated;

    @SerializedName("extracted_details")
    private ExtractedDetails extractedDetails;

    @SerializedName("decision")
    private String decision;

    @SerializedName("saved")
    private boolean saved;

    @SerializedName("message")
    private String message;


    public String getSuggestionId() {
        return suggestionId;
    }

    public String getPredictedLabel() {
        return predictedLabel;
    }

    public double getSubscriptionProbability() {
        return subscriptionProbability;
    }

    public double getBinaryThreshold() {
        return binaryThreshold;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public boolean isInputTruncated() {
        return inputTruncated;
    }

    public ExtractedDetails getExtractedDetails() {
        return extractedDetails;
    }

    public String getDecision() {
        return decision;
    }

    public boolean isSaved() {
        return saved;
    }

    public String getMessage() {
        return message;
    }


    // =========================================================
    // HELPER
    // =========================================================

    public boolean isSubscription() {

        return predictedLabel != null &&
                predictedLabel.equalsIgnoreCase("Subscription");
    }


    // =========================================================
    // EXTRACTED DETAILS
    // =========================================================

    public static class ExtractedDetails {

        @SerializedName("service")
        private String service;

        /*
         * API currently returns amount as a String:
         *
         * "amount": "9.99"
         *
         * So keep it as String here.
         */
        @SerializedName("amount")
        private String amount;

        @SerializedName("currency")
        private String currency;

        @SerializedName("billing_cycle")
        private String billingCycle;

        @SerializedName("next_billing_date")
        private String nextBillingDate;


        public String getService() {
            return service;
        }

        public String getAmount() {
            return amount;
        }

        public String getCurrency() {
            return currency;
        }

        public String getBillingCycle() {
            return billingCycle;
        }

        public String getNextBillingDate() {
            return nextBillingDate;
        }


        public double getAmountAsDouble() {

            if (amount == null ||
                    amount.trim().isEmpty()) {

                return 0.0;
            }

            try {

                return Double.parseDouble(
                        amount.trim()
                );

            } catch (NumberFormatException e) {

                return 0.0;
            }
        }
    }
}