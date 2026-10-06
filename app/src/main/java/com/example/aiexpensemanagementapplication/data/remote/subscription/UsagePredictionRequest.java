package com.example.aiexpensemanagementapplication.data.remote.subscription;

import com.google.gson.annotations.SerializedName;

public class UsagePredictionRequest {

    @SerializedName("Monthly_Cost_LKR")
    private double monthlyCostLkr;

    @SerializedName("Usage_Minutes_30D")
    private int usageMinutes30D;

    public UsagePredictionRequest(
            double monthlyCostLkr,
            int usageMinutes30D
    ) {
        this.monthlyCostLkr = monthlyCostLkr;
        this.usageMinutes30D = usageMinutes30D;
    }

    public double getMonthlyCostLkr() {
        return monthlyCostLkr;
    }

    public int getUsageMinutes30D() {
        return usageMinutes30D;
    }
}