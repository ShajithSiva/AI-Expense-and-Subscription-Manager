package com.example.aiexpensemanagementapplication.model;

public class Notification {

    private int id;
    private String title;
    private String message;
    private String subtitle;
    private String type;
    private long timestamp;
    private boolean isRead;

    public Notification() {
    }

    public Notification(int id,
                        String title,
                        String message,
                        String subtitle,
                        String type,
                        long timestamp,
                        boolean isRead) {

        this.id = id;
        this.title = title;
        this.message = message;
        this.subtitle = subtitle;
        this.type = type;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}