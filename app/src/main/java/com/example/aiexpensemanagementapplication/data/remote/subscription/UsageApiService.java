package com.example.aiexpensemanagementapplication.data.remote.subscription;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UsageApiService {

    @POST("predict-subscription-usage")
    Call<UsagePredictionResponse> predictSubscriptionUsage(
            @Body UsagePredictionRequest request
    );
}