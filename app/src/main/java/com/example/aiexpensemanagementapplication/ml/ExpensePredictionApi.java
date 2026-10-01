package com.example.aiexpensemanagementapplication.ml;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ExpensePredictionApi {

    @POST("predict-expense")
    Call<ExpensePredictionResponse> predictExpense(
            @Body ExpensePredictionRequest request
    );
}