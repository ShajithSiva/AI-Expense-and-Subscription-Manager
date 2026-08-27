package com.example.aiexpensemanagementapplication.ui.ai;

public class AdvisorMessage {

    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;

    private final String message;
    private final int type;

    public AdvisorMessage(String message, int type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }
}