package com.example.aiexpensemanagementapplication.model;

public class PendingSubscription {

    // =========================================================
    // FIELDS
    // =========================================================

    private int pendingId;

    private int userId;

    private String gmailMessageId;

    private String suggestionId;

    private String serviceName;

    private double amount;

    private String currency;

    private String billingCycle;

    private String nextBillingDate;

    private double confidence;

    private String emailSubject;

    private String emailSender;

    private String emailBody;

    private String status;

    private String createdAt;


    // =========================================================
    // EMPTY CONSTRUCTOR
    // =========================================================

    public PendingSubscription() {
    }


    // =========================================================
    // FULL CONSTRUCTOR
    // =========================================================

    public PendingSubscription(
            int pendingId,
            int userId,
            String gmailMessageId,
            String suggestionId,
            String serviceName,
            double amount,
            String currency,
            String billingCycle,
            String nextBillingDate,
            double confidence,
            String emailSubject,
            String emailSender,
            String emailBody,
            String status,
            String createdAt
    ) {

        this.pendingId = pendingId;

        this.userId = userId;

        this.gmailMessageId = gmailMessageId;

        this.suggestionId = suggestionId;

        this.serviceName = serviceName;

        this.amount = amount;

        this.currency = currency;

        this.billingCycle = billingCycle;

        this.nextBillingDate = nextBillingDate;

        this.confidence = confidence;

        this.emailSubject = emailSubject;

        this.emailSender = emailSender;

        this.emailBody = emailBody;

        this.status = status;

        this.createdAt = createdAt;
    }


    // =========================================================
    // GETTERS + SETTERS
    // =========================================================

    public int getPendingId() {

        return pendingId;
    }


    public void setPendingId(
            int pendingId
    ) {

        this.pendingId = pendingId;
    }


    public int getUserId() {

        return userId;
    }


    public void setUserId(
            int userId
    ) {

        this.userId = userId;
    }


    public String getGmailMessageId() {

        return gmailMessageId;
    }


    public void setGmailMessageId(
            String gmailMessageId
    ) {

        this.gmailMessageId =
                cleanString(
                        gmailMessageId
                );
    }


    public String getSuggestionId() {

        return suggestionId;
    }


    public void setSuggestionId(
            String suggestionId
    ) {

        this.suggestionId =
                cleanString(
                        suggestionId
                );
    }


    public String getServiceName() {

        return serviceName;
    }


    public void setServiceName(
            String serviceName
    ) {

        this.serviceName =
                cleanString(
                        serviceName
                );
    }


    public double getAmount() {

        return amount;
    }


    public void setAmount(
            double amount
    ) {

        this.amount = amount;
    }


    public String getCurrency() {

        return currency;
    }


    public void setCurrency(
            String currency
    ) {

        this.currency =
                cleanString(
                        currency
                );
    }


    public String getBillingCycle() {

        return billingCycle;
    }


    public void setBillingCycle(
            String billingCycle
    ) {

        this.billingCycle =
                cleanString(
                        billingCycle
                );
    }


    public String getNextBillingDate() {

        return nextBillingDate;
    }


    public void setNextBillingDate(
            String nextBillingDate
    ) {

        this.nextBillingDate =
                cleanString(
                        nextBillingDate
                );
    }


    public double getConfidence() {

        return confidence;
    }


    public void setConfidence(
            double confidence
    ) {

        this.confidence = confidence;
    }


    public String getEmailSubject() {

        return emailSubject;
    }


    public void setEmailSubject(
            String emailSubject
    ) {

        this.emailSubject =
                cleanString(
                        emailSubject
                );
    }


    public String getEmailSender() {

        return emailSender;
    }


    public void setEmailSender(
            String emailSender
    ) {

        this.emailSender =
                cleanString(
                        emailSender
                );
    }


    public String getEmailBody() {

        return emailBody;
    }


    public void setEmailBody(
            String emailBody
    ) {

        this.emailBody =
                cleanString(
                        emailBody
                );
    }


    public String getStatus() {

        return status;
    }


    public void setStatus(
            String status
    ) {

        this.status =
                cleanString(
                        status
                );
    }


    public String getCreatedAt() {

        return createdAt;
    }


    public void setCreatedAt(
            String createdAt
    ) {

        this.createdAt =
                cleanString(
                        createdAt
                );
    }


    // =========================================================
    // HELPER
    // =========================================================

    private String cleanString(
            String value
    ) {

        if (value == null) {

            return "";
        }


        return value.trim();
    }


    // =========================================================
    // DISPLAY HELPERS
    // =========================================================

    public String getDisplayAmount() {

        String safeCurrency =
                currency == null
                        ? ""
                        : currency.trim();


        if (safeCurrency.isEmpty()) {

            if (amount > 0) {

                return String.format(
                        java.util.Locale.US,
                        "%.2f",
                        amount
                );
            }


            return "Amount not detected";
        }


        if (amount <= 0) {

            return safeCurrency
                    + " —";
        }


        return String.format(
                java.util.Locale.US,
                "%s %.2f",
                safeCurrency,
                amount
        );
    }


    public String getDisplayBillingCycle() {

        if (billingCycle == null ||
                billingCycle.trim().isEmpty()) {

            return "Billing cycle not detected";
        }


        return billingCycle.trim();
    }


    public String getDisplayNextBillingDate() {

        if (nextBillingDate == null ||
                nextBillingDate.trim().isEmpty()) {

            return "Date not detected";
        }


        return nextBillingDate.trim();
    }


    public String getDisplayConfidence() {

        return String.format(
                java.util.Locale.US,
                "%.1f%%",
                confidence * 100.0
        );
    }
}