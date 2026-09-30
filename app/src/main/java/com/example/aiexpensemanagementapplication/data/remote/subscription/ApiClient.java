package com.example.aiexpensemanagementapplication.data.remote.subscription;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // =========================================================
    // LOCAL FASTAPI
    //
    // Android Emulator -> Mac localhost
    // =========================================================

    private static final String BASE_URL =
            "http://10.0.2.2:8000/";


    private static Retrofit retrofit = null;


    private ApiClient() {
        // Prevent object creation
    }


    public static Retrofit getClient() {

        if (retrofit == null) {

            retrofit =
                    new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(
                                    GsonConverterFactory.create()
                            )
                            .build();
        }


        return retrofit;
    }


    public static ApiService getApiService() {

        return getClient()
                .create(
                        ApiService.class
                );
    }
}