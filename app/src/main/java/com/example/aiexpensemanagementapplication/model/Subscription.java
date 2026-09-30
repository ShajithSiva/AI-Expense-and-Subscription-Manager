package com.example.aiexpensemanagementapplication.model;

public class Subscription {

    private int subscriptionId;
    private int userId;

    private String serviceName;
    private double amount;
    private String currency;
    private String billingCycle;
    private String nextBillingDate;

    // ==========================
    // Empty Constructor
    // ==========================
    public Subscription() {
    }

    // ==========================
    // Old Constructor
    // Keep this for compatibility
    // ==========================
    public Subscription(int subscriptionId,
                        int userId,
                        String serviceName,
                        double amount,
                        String billingCycle,
                        String nextBillingDate) {

        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.serviceName = serviceName;
        this.amount = amount;
        this.currency = null;
        this.billingCycle = billingCycle;
        this.nextBillingDate = nextBillingDate;
    }

    // ==========================
    // New Constructor
    // With Currency
    // ==========================
    public Subscription(int subscriptionId,
                        int userId,
                        String serviceName,
                        double amount,
                        String currency,
                        String billingCycle,
                        String nextBillingDate) {

        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.serviceName = serviceName;
        this.amount = amount;
        this.currency = currency;
        this.billingCycle = billingCycle;
        this.nextBillingDate = nextBillingDate;
    }

    // ==========================
    // Getters
    // ==========================

    public int getSubscriptionId() {
        return subscriptionId;
    }

    public int getUserId() {
        return userId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double getAmount() {
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

    // ==========================
    // Setters
    // ==========================

    public void setSubscriptionId(int subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {

        if (currency == null || currency.trim().isEmpty()) {
            this.currency = null;
        } else {
            this.currency = currency.trim().toUpperCase();
        }
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public void setNextBillingDate(String nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }
}