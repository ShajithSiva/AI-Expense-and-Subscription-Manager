package com.example.aiexpensemanagementapplication.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    private static final String BASE_URL =
            "https://api.frankfurter.dev/";

    private static Retrofit retrofit;

    private RetrofitClient() {
        // Prevent object creation
    }

    public static Retrofit getClient() {

        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();
        }

        return retrofit;
    }

    public static CurrencyApiService getCurrencyApiService() {

        return getClient().create(
                CurrencyApiService.class
        );
    }
}