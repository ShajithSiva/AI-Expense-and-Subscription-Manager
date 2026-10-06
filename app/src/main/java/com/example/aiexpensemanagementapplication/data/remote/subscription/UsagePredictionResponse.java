package com.example.aiexpensemanagementapplication.data.remote.subscription;

import com.google.gson.annotations.SerializedName;

public class UsagePredictionResponse {

    @SerializedName("prediction")
    private String prediction;

    public String getPrediction() {
        return prediction;
    }
}