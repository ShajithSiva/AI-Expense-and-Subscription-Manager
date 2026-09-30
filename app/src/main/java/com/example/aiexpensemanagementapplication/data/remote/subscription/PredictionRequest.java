package com.example.aiexpensemanagementapplication.data.remote.subscription;

import com.google.gson.annotations.SerializedName;

public class PredictionRequest {

    @SerializedName("text")
    private String text;

    public PredictionRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}