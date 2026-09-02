package com.example.aiexpensemanagementapplication.ui.ai;

public class AIInsightResult {

    private String title;
    private String message;
    private FinancialInsight.Severity severity;

    // Required empty constructor
    public AIInsightResult() {
    }

    // Backward-compatible constructor
    public AIInsightResult(String title, String message) {
        this.title = title;
        this.message = message;
        this.severity = FinancialInsight.Severity.LOW;
    }

    // New constructor with severity
    public AIInsightResult(
            String title,
            String message,
            FinancialInsight.Severity severity
    ) {
        this.title = title;
        this.message = message;
        this.severity = severity;
    }

    // ==============================
    // GETTERS
    // ==============================

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public FinancialInsight.Severity getSeverity() {
        return severity;
    }

    // ==============================
    // SETTERS
    // ==============================

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSeverity(FinancialInsight.Severity severity) {
        this.severity = severity;
    }
}
