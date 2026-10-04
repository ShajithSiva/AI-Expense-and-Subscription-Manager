package com.example.aiexpensemanagementapplication.data.remote.subscription;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL =
            "https://subscription-api-v3.onrender.com/";

    private static Retrofit retrofit = null;

    private ApiClient() {
        // Prevent object creation
    }

    public static Retrofit getClient() {

        if (retrofit == null) {

            // Longer timeout for Render + DistilBERT inference
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .callTimeout(90, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();
        }

        return retrofit;
    }

    public static ApiService getApiService() {

        return getClient()
                .create(ApiService.class);
    }
}