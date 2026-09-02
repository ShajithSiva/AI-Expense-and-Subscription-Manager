package com.example.aiexpensemanagementapplication.ui.ai;

public class FinancialInsight {

    // =====================================================
    // INSIGHT TYPE
    // =====================================================

    public enum Type {

        BUDGET_WARNING,

        BUDGET_EXCEEDED,

        EXPENSES_EXCEED_INCOME,

        HIGH_SPENDING_CATEGORY,

        EXPENSE_INCREASE,

        EXPENSE_DECREASE,

        LOW_SAVINGS,

        GOOD_SAVINGS,

        POSITIVE_TREND
    }


    // =====================================================
    // INSIGHT SEVERITY
    // =====================================================

    public enum Severity {

        CRITICAL(4),

        HIGH(3),

        MEDIUM(2),

        LOW(1),

        POSITIVE(0);


        private final int priority;


        Severity(
                int priority
        ) {

            this.priority =
                    priority;
        }


        public int getPriority() {

            return priority;
        }
    }


    // =====================================================
    // FIELDS
    // =====================================================

    private final Type type;

    private final Severity severity;

    private final String title;

    private final String message;

    private final double value;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FinancialInsight(
            Type type,
            Severity severity,
            String title,
            String message,
            double value
    ) {

        this.type =
                type;

        this.severity =
                severity;

        this.title =
                title;

        this.message =
                message;

        this.value =
                value;
    }


    // =====================================================
    // TYPE
    // =====================================================

    public Type getType() {

        return type;
    }


    // =====================================================
    // SEVERITY
    // =====================================================

    public Severity getSeverity() {

        return severity;
    }


    // =====================================================
    // TITLE
    // =====================================================

    public String getTitle() {

        return title;
    }


    // =====================================================
    // MESSAGE
    // =====================================================

    public String getMessage() {

        return message;
    }


    // =====================================================
    // VALUE
    // =====================================================

    public double getValue() {

        return value;
    }
}
