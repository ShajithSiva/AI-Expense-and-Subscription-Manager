package com.example.aiexpensemanagementapplication.ui.income;

public class IncomeModel {

    private int transactionId;
    private int categoryId;
    private int paymentMethodId;

    private String categoryName;
    private String incomeSource;

    private double amount;

    private String transactionDate;

    private String note;

    private String incomeMode;

    public IncomeModel() {
    }

    public IncomeModel(int transactionId,
                       int categoryId,
                       int paymentMethodId,
                       String categoryName,
                       String incomeSource,
                       double amount,
                       String transactionDate,
                       String note,
                       String incomeMode) {

        this.transactionId = transactionId;
        this.categoryId = categoryId;
        this.paymentMethodId = paymentMethodId;
        this.categoryName = categoryName;
        this.incomeSource = incomeSource;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.note = note;
        this.incomeMode = incomeMode;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(int paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getIncomeSource() {
        return incomeSource;
    }

    public void setIncomeSource(String incomeSource) {
        this.incomeSource = incomeSource;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getIncomeMode() {
        return incomeMode;
    }

    public void setIncomeMode(String incomeMode) {
        this.incomeMode = incomeMode;
    }
}