package com.example.aiexpensemanagementapplication.data.remote;

import com.example.aiexpensemanagementapplication.model.CurrencyModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CurrencyApiService {

    @GET("v2/currencies")
    Call<List<CurrencyModel>> getCurrencies();
}