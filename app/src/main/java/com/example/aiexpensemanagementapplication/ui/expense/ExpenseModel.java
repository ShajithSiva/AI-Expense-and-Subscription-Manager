package com.example.aiexpensemanagementapplication.ui.expense;

public class ExpenseModel {

    // =========================================================
    // TRANSACTION DATA
    // =========================================================

    private int transactionId;

    private int categoryId;

    private int paymentMethodId;


    // =========================================================
    // DISPLAY DATA
    // =========================================================

    private String categoryName;

    private String paymentMethod;

    private double amount;

    private String transactionDate;

    private String note;


    // =========================================================
    // FAMILY SHARING
    // =========================================================

    /*
     * -1 means the expense is not shared.
     *
     * Otherwise this contains the FamilyID
     * that the expense is shared with.
     */
    private int sharedFamilyId = -1;


    /*
     * Example:
     *
     * My Family
     * Parents
     * Shared House
     *
     * null means not shared / not loaded.
     */
    private String sharedFamilyName;


    // =========================================================
    // LEGACY FIELD
    // =========================================================

    /*
     * OLD ARCHITECTURE:
     *
     * ExpenseMode = Personal / Family
     *
     * NEW ARCHITECTURE:
     *
     * Every expense is owned by the individual user.
     * Family visibility is stored separately using
     * ExpenseFamilyShare.
     *
     * This field is temporarily retained so existing
     * DatabaseHelper methods do not break.
     *
     * Do NOT use this field to determine whether an
     * expense is shared.
     */
    private String expenseMode;


    // =========================================================
    // EMPTY CONSTRUCTOR
    // =========================================================

    public ExpenseModel() {

    }


    // =========================================================
    // EXISTING CONSTRUCTOR
    // =========================================================

    public ExpenseModel(

            int transactionId,

            int categoryId,

            int paymentMethodId,

            String categoryName,

            String paymentMethod,

            double amount,

            String transactionDate,

            String note,

            String expenseMode

    ) {

        this.transactionId =
                transactionId;

        this.categoryId =
                categoryId;

        this.paymentMethodId =
                paymentMethodId;

        this.categoryName =
                categoryName;

        this.paymentMethod =
                paymentMethod;

        this.amount =
                amount;

        this.transactionDate =
                transactionDate;

        this.note =
                note;

        this.expenseMode =
                expenseMode;
    }


    // =========================================================
    // NEW CONSTRUCTOR WITH FAMILY SHARING
    // =========================================================

    public ExpenseModel(

            int transactionId,

            int categoryId,

            int paymentMethodId,

            String categoryName,

            String paymentMethod,

            double amount,

            String transactionDate,

            String note,

            String expenseMode,

            int sharedFamilyId,

            String sharedFamilyName

    ) {

        this.transactionId =
                transactionId;

        this.categoryId =
                categoryId;

        this.paymentMethodId =
                paymentMethodId;

        this.categoryName =
                categoryName;

        this.paymentMethod =
                paymentMethod;

        this.amount =
                amount;

        this.transactionDate =
                transactionDate;

        this.note =
                note;

        this.expenseMode =
                expenseMode;

        this.sharedFamilyId =
                sharedFamilyId;

        this.sharedFamilyName =
                sharedFamilyName;
    }


    // =========================================================
    // TRANSACTION ID
    // =========================================================

    public int getTransactionId() {

        return transactionId;
    }


    public void setTransactionId(
            int transactionId
    ) {

        this.transactionId =
                transactionId;
    }


    // =========================================================
    // CATEGORY ID
    // =========================================================

    public int getCategoryId() {

        return categoryId;
    }


    public void setCategoryId(
            int categoryId
    ) {

        this.categoryId =
                categoryId;
    }


    // =========================================================
    // PAYMENT METHOD ID
    // =========================================================

    public int getPaymentMethodId() {

        return paymentMethodId;
    }


    public void setPaymentMethodId(
            int paymentMethodId
    ) {

        this.paymentMethodId =
                paymentMethodId;
    }


    // =========================================================
    // CATEGORY NAME
    // =========================================================

    public String getCategoryName() {

        return categoryName;
    }


    public void setCategoryName(
            String categoryName
    ) {

        this.categoryName =
                categoryName;
    }


    // =========================================================
    // PAYMENT METHOD
    // =========================================================

    public String getPaymentMethod() {

        return paymentMethod;
    }


    public void setPaymentMethod(
            String paymentMethod
    ) {

        this.paymentMethod =
                paymentMethod;
    }


    // =========================================================
    // AMOUNT
    // =========================================================

    public double getAmount() {

        return amount;
    }


    public void setAmount(
            double amount
    ) {

        this.amount =
                amount;
    }


    // =========================================================
    // TRANSACTION DATE
    // =========================================================

    public String getTransactionDate() {

        return transactionDate;
    }


    public void setTransactionDate(
            String transactionDate
    ) {

        this.transactionDate =
                transactionDate;
    }


    // =========================================================
    // NOTE
    // =========================================================

    public String getNote() {

        return note;
    }


    public void setNote(
            String note
    ) {

        this.note =
                note;
    }


    // =========================================================
    // FAMILY ID
    // =========================================================

    public int getSharedFamilyId() {

        return sharedFamilyId;
    }


    public void setSharedFamilyId(
            int sharedFamilyId
    ) {

        this.sharedFamilyId =
                sharedFamilyId;
    }


    // =========================================================
    // FAMILY NAME
    // =========================================================

    public String getSharedFamilyName() {

        return sharedFamilyName;
    }


    public void setSharedFamilyName(
            String sharedFamilyName
    ) {

        this.sharedFamilyName =
                sharedFamilyName;
    }


    // =========================================================
    // SHARING HELPER
    // =========================================================

    public boolean isSharedWithFamily() {

        return sharedFamilyId != -1;
    }


    // =========================================================
    // LEGACY EXPENSE MODE
    // =========================================================

    public String getExpenseMode() {

        return expenseMode;
    }


    public void setExpenseMode(
            String expenseMode
    ) {

        this.expenseMode =
                expenseMode;
    }
}