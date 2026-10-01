package com.example.aiexpensemanagementapplication.ml;

import com.google.gson.annotations.SerializedName;

public class ExpensePredictionResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("prediction")
    private Prediction prediction;

    @SerializedName("error")
    private String error;


    public boolean isSuccess() {
        return success;
    }


    public Prediction getPrediction() {
        return prediction;
    }


    public String getError() {
        return error;
    }


    public static class Prediction {

        @SerializedName("target")
        private String target;

        @SerializedName("predictedNextMonthExpense")
        private double predictedNextMonthExpense;


        public String getTarget() {
            return target;
        }


        public double getPredictedNextMonthExpense() {
            return predictedNextMonthExpense;
        }
    }
}