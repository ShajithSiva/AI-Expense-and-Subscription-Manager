package com.example.aiexpensemanagementapplication.ml;

import com.example.aiexpensemanagementapplication.BuildConfig;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ExpensePredictionApiService {

    private final ExpensePredictionApi api;


    public ExpensePredictionApiService() {

        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(
                                BuildConfig.EXPENSE_PREDICTION_BASE_URL
                        )
                        .addConverterFactory(
                                GsonConverterFactory.create()
                        )
                        .build();

        api =
                retrofit.create(
                        ExpensePredictionApi.class
                );
    }


    public interface PredictionCallback {

        void onSuccess(
                double predictedExpense
        );

        void onFailure(
                String message
        );
    }


    public void predict(
            ExpensePredictionRequest request,
            PredictionCallback callback
    ) {

        api.predictExpense(request)
                .enqueue(
                        new Callback<ExpensePredictionResponse>() {

                            @Override
                            public void onResponse(
                                    Call<ExpensePredictionResponse> call,
                                    Response<ExpensePredictionResponse> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onFailure(
                                            "Prediction server returned HTTP "
                                                    + response.code()
                                    );

                                    return;
                                }


                                ExpensePredictionResponse body =
                                        response.body();


                                if (
                                        body == null ||
                                                !body.isSuccess() ||
                                                body.getPrediction() == null
                                ) {

                                    String error =
                                            body != null
                                                    ? body.getError()
                                                    : null;

                                    callback.onFailure(
                                            error != null
                                                    ? error
                                                    : "Invalid prediction response."
                                    );

                                    return;
                                }


                                callback.onSuccess(
                                        body.getPrediction()
                                                .getPredictedNextMonthExpense()
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<ExpensePredictionResponse> call,
                                    Throwable t
                            ) {

                                callback.onFailure(
                                        t.getMessage() != null
                                                ? t.getMessage()
                                                : "Unable to connect to prediction server."
                                );
                            }
                        }
                );
    }
}