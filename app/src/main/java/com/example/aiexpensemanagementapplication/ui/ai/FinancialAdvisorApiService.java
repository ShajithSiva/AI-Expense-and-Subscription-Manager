package com.example.aiexpensemanagementapplication.ui.ai;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FinancialAdvisorApiService {

    // =====================================================
    // BACKEND URL
    // =====================================================

    /*
     * Android Emulator -> Computer
     *
     * 10.0.2.2 means the host computer.
     */

    private static final String API_URL =
            "http://10.0.2.2:3000/api/financial-advisor";


    // =====================================================
    // TIMEOUT
    // =====================================================

    private static final int CONNECT_TIMEOUT =
            15000;

    private static final int READ_TIMEOUT =
            180000;


    // =====================================================
    // EXECUTOR
    // =====================================================

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();


    // =====================================================
    // CALLBACK
    // =====================================================

    public interface AdvisorCallback {

        void onSuccess(
                String response
        );

        void onFailure(
                String message
        );
    }


    // =====================================================
    // ASK ADVISOR
    // =====================================================

    public void askAdvisor(
            FinancialAnalysis analysis,
            String question,
            ArrayList<AdvisorMessage> conversation,
            AdvisorCallback callback
    ) {

        executor.execute(() -> {

            HttpURLConnection connection =
                    null;

            try {

                // =================================================
                // VALIDATION
                // =================================================

                if (analysis == null) {

                    callback.onFailure(
                            "Financial analysis is not available."
                    );

                    return;
                }

                if (question == null ||
                        question.trim().isEmpty()) {

                    callback.onFailure(
                            "Please enter a question."
                    );

                    return;
                }


                // =================================================
                // CREATE URL
                // =================================================

                URL url =
                        new URL(API_URL);


                connection =
                        (HttpURLConnection)
                                url.openConnection();


                // =================================================
                // CONNECTION SETTINGS
                // =================================================

                connection.setRequestMethod(
                        "POST"
                );

                connection.setConnectTimeout(
                        CONNECT_TIMEOUT
                );

                connection.setReadTimeout(
                        READ_TIMEOUT
                );

                connection.setDoInput(
                        true
                );

                connection.setDoOutput(
                        true
                );

                connection.setUseCaches(
                        false
                );

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );


                // =================================================
                // CREATE FINANCIAL DATA JSON
                // =================================================

                JSONObject financialData =
                        new JSONObject();


                financialData.put(
                        "totalIncome",
                        analysis.getTotalIncome()
                );


                financialData.put(
                        "totalExpense",
                        analysis.getTotalExpense()
                );


                financialData.put(
                        "savings",
                        analysis.getSavings()
                );


                financialData.put(
                        "savingsRate",
                        analysis.getSavingsRate()
                );


                financialData.put(
                        "expenseRate",
                        analysis.getExpenseRate()
                );


                financialData.put(
                        "budget",
                        analysis.getBudget()
                );


                financialData.put(
                        "budgetUsed",
                        analysis.getBudgetUsed()
                );


                financialData.put(
                        "remainingBudget",
                        analysis.getRemainingBudget()
                );


                financialData.put(
                        "highestCategory",
                        analysis.getHighestCategory()
                );


                financialData.put(
                        "highestCategoryAmount",
                        analysis.getHighestCategoryAmount()
                );


                financialData.put(
                        "currentMonthExpense",
                        analysis.getCurrentMonthExpense()
                );


                financialData.put(
                        "previousMonthExpense",
                        analysis.getPreviousMonthExpense()
                );


                financialData.put(
                        "expenseChangePercentage",
                        analysis.getExpenseChangePercentage()
                );


                financialData.put(
                        "financialHealthScore",
                        analysis.getFinancialHealthScore()
                );


                // =================================================
                // CREATE CONVERSATION ARRAY
                // =================================================

                JSONArray conversationArray =
                        new JSONArray();


                if (conversation != null &&
                        !conversation.isEmpty()) {

                    /*
                     * Only send the latest 10 messages.
                     *
                     * This prevents the prompt from becoming
                     * unnecessarily large and helps reduce
                     * Ollama response time.
                     */

                    int startIndex =
                            Math.max(
                                    0,
                                    conversation.size() - 10
                            );


                    for (
                            int i = startIndex;
                            i < conversation.size();
                            i++
                    ) {

                        AdvisorMessage message =
                                conversation.get(i);


                        if (message == null) {
                            continue;
                        }


                        JSONObject messageJson =
                                new JSONObject();


                        // -------------------------------------------------
                        // ROLE
                        // -------------------------------------------------

                        String role;


                        if (
                                message.getType()
                                        == AdvisorMessage.TYPE_USER
                        ) {

                            role =
                                    "user";

                        } else {

                            role =
                                    "assistant";
                        }


                        messageJson.put(
                                "role",
                                role
                        );


                        // -------------------------------------------------
                        // CONTENT
                        // -------------------------------------------------

                        String content =
                                message.getMessage();


                        if (content == null) {
                            content = "";
                        }


                        if (
                                content.trim().isEmpty()
                        ) {

                            continue;
                        }


                        messageJson.put(
                                "content",
                                content.trim()
                        );


                        conversationArray.put(
                                messageJson
                        );
                    }
                }


                // =================================================
                // CREATE MAIN REQUEST
                // =================================================

                JSONObject request =
                        new JSONObject();


                request.put(
                        "question",
                        question.trim()
                );


                request.put(
                        "financialData",
                        financialData
                );


                request.put(
                        "conversation",
                        conversationArray
                );


                // =================================================
                // REQUEST BODY
                // =================================================

                String requestBody =
                        request.toString();


                // =================================================
                // SEND REQUEST
                // =================================================

                OutputStream outputStream =
                        connection.getOutputStream();


                outputStream.write(
                        requestBody.getBytes(
                                StandardCharsets.UTF_8
                        )
                );


                outputStream.flush();

                outputStream.close();


                // =================================================
                // GET RESPONSE CODE
                // =================================================

                int responseCode =
                        connection.getResponseCode();


                // =================================================
                // SELECT INPUT STREAM
                // =================================================

                InputStream inputStream;


                if (
                        responseCode >= 200 &&
                                responseCode < 300
                ) {

                    inputStream =
                            connection.getInputStream();

                } else {

                    inputStream =
                            connection.getErrorStream();
                }


                // =================================================
                // NO RESPONSE STREAM
                // =================================================

                if (inputStream == null) {

                    callback.onFailure(
                            "The Financial Advisor server "
                                    + "returned no response."
                    );

                    return;
                }


                // =================================================
                // READ RESPONSE
                // =================================================

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        inputStream,
                                        StandardCharsets.UTF_8
                                )
                        );


                StringBuilder responseBuilder =
                        new StringBuilder();


                String line;


                while (
                        (line = reader.readLine())
                                != null
                ) {

                    responseBuilder.append(
                            line
                    );
                }


                reader.close();


                String responseText =
                        responseBuilder.toString();


                // =================================================
                // EMPTY SERVER RESPONSE
                // =================================================

                if (
                        responseText.trim().isEmpty()
                ) {

                    callback.onFailure(
                            "The Financial Advisor server "
                                    + "returned an empty response."
                    );

                    return;
                }


                // =================================================
                // HTTP ERROR
                // =================================================

                if (
                        responseCode < 200 ||
                                responseCode >= 300
                ) {

                    try {

                        JSONObject errorJson =
                                new JSONObject(
                                        responseText
                                );


                        String errorMessage =
                                errorJson.optString(
                                        "message",
                                        ""
                                );


                        String detailedError =
                                errorJson.optString(
                                        "error",
                                        ""
                                );


                        if (
                                !errorMessage
                                        .trim()
                                        .isEmpty()
                        ) {

                            if (
                                    !detailedError
                                            .trim()
                                            .isEmpty()
                            ) {

                                errorMessage +=
                                        "\n"
                                                + detailedError;
                            }


                            callback.onFailure(
                                    errorMessage
                            );

                        } else {

                            callback.onFailure(
                                    "Server returned HTTP "
                                            + responseCode
                            );
                        }

                    } catch (Exception parseException) {

                        callback.onFailure(
                                "Server returned HTTP "
                                        + responseCode
                                        + "\n\n"
                                        + responseText
                        );
                    }


                    return;
                }


                // =================================================
                // PARSE SUCCESS RESPONSE
                // =================================================

                JSONObject responseJson =
                        new JSONObject(
                                responseText
                        );


                boolean success =
                        responseJson.optBoolean(
                                "success",
                                false
                        );


                // =================================================
                // SERVER REPORTED FAILURE
                // =================================================

                if (!success) {

                    callback.onFailure(
                            responseJson.optString(
                                    "message",
                                    "Unable to generate financial advice."
                            )
                    );

                    return;
                }


                // =================================================
                // GET AI RESPONSE
                // =================================================

                String response =
                        responseJson.optString(
                                "response",
                                ""
                        );


                // =================================================
                // EMPTY AI RESPONSE
                // =================================================

                if (
                        response.trim().isEmpty()
                ) {

                    callback.onFailure(
                            "The AI returned an empty response."
                    );

                    return;
                }


                // =================================================
                // SUCCESS
                // =================================================

                callback.onSuccess(
                        response.trim()
                );


            } catch (Exception e) {

                e.printStackTrace();


                String errorMessage;


                if (
                        e.getMessage() != null &&
                                !e.getMessage()
                                        .trim()
                                        .isEmpty()
                ) {

                    errorMessage =
                            e.getMessage();

                } else {

                    errorMessage =
                            "Unknown connection error.";
                }


                callback.onFailure(
                        "Unable to connect to the "
                                + "Financial Advisor server.\n\n"
                                + errorMessage
                );


            } finally {

                // =================================================
                // DISCONNECT
                // =================================================

                if (connection != null) {

                    connection.disconnect();
                }
            }
        });
    }


    // =====================================================
    // SHUTDOWN
    // =====================================================

    public void shutdown() {

        executor.shutdownNow();
    }
}