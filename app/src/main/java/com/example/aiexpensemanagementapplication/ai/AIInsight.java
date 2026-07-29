package com.example.aiexpensemanagementapplication.ai;

public class AIInsight {

    private String title;
    private String message;
    private int icon;

    public AIInsight(String title, String message, int icon) {
        this.title = title;
        this.message = message;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public int getIcon() {
        return icon;
    }
}