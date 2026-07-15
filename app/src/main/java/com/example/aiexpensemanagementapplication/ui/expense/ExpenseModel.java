package com.example.aiexpensemanagementapplication.ui.expense;

public class ExpenseModel {

    private int transactionId;
    private int categoryId;
    private int paymentMethodId;

    private String categoryName;
    private String paymentMethod;

    private double amount;

    private String transactionDate;

    private String note;

    private String expenseMode;

    public ExpenseModel() {
    }

    public ExpenseModel(int transactionId,
                        int categoryId,
                        int paymentMethodId,
                        String categoryName,
                        String paymentMethod,
                        double amount,
                        String transactionDate,
                        String note,
                        String expenseMode) {

        this.transactionId = transactionId;
        this.categoryId = categoryId;
        this.paymentMethodId = paymentMethodId;
        this.categoryName = categoryName;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.note = note;
        this.expenseMode = expenseMode;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
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

    public String getExpenseMode() {
        return expenseMode;
    }

    public void setExpenseMode(String expenseMode) {
        this.expenseMode = expenseMode;
    }

}