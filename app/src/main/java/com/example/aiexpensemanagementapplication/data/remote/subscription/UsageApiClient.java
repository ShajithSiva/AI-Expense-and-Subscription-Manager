package com.example.aiexpensemanagementapplication.data.remote.subscription;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UsageApiClient {

    private static final String BASE_URL =
            "https://subscription-usage-ml-api.onrender.com/";

    private static Retrofit retrofit = null;

    private UsageApiClient() {
        // Prevent object creation
    }

    public static Retrofit getClient() {

        if (retrofit == null) {

            OkHttpClient okHttpClient =
                    new OkHttpClient.Builder()
                            .connectTimeout(
                                    30,
                                    TimeUnit.SECONDS
                            )
                            .readTimeout(
                                    90,
                                    TimeUnit.SECONDS
                            )
                            .writeTimeout(
                                    30,
                                    TimeUnit.SECONDS
                            )
                            .callTimeout(
                                    120,
                                    TimeUnit.SECONDS
                            )
                            .build();

            retrofit =
                    new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(okHttpClient)
                            .addConverterFactory(
                                    GsonConverterFactory.create()
                            )
                            .build();
        }

        return retrofit;

    }

    public static UsageApiService getApiService() {

        return getClient()
                .create(UsageApiService.class);
    }
}