package com.example.aiexpensemanagementapplication.ui.ai;

import android.util.Log;

import com.example.aiexpensemanagementapplication.BuildConfig;

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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FinancialAdvisorApiService {

    // =====================================================
    // TAG
    // =====================================================

    private static final String TAG =
            "FINANCIAL_ADVISOR_API";


    // =====================================================
    // BACKEND URL
    // =====================================================

    /*
     * Android Emulator -> Windows PC
     *
     * 10.0.2.2 points to the host computer.
     *
     * Your Node.js backend must be running on:
     *
     * http://localhost:3000
     */

    private static final String BACKEND_BASE_URL =
            BuildConfig.BACKEND_BASE_URL;

    private static final String API_URL =
            BACKEND_BASE_URL + "/api/financial-advisor";

    private static final String INSIGHTS_API_URL =
            BACKEND_BASE_URL + "/api/financial-insights";


    // =====================================================
    // TIMEOUT
    // =====================================================

    private static final int CONNECT_TIMEOUT =
            15000;

    /*
     * Qwen can take some time on a local PC.
     *
     * 180 seconds = 3 minutes.
     */

    private static final int READ_TIMEOUT =
            180000;


    // =====================================================
    // EXECUTOR
    // =====================================================

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();


    // =====================================================
    // ADVISOR CALLBACK
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
    // PROACTIVE INSIGHT CALLBACK
    // =====================================================

    public interface InsightCallback {

        void onSuccess(
                List<AIInsightResult> insights
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

                if (
                        question == null ||
                                question.trim().isEmpty()
                ) {

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
                // FINANCIAL DATA
                // =================================================

                JSONObject financialData =
                        createFinancialData(
                                analysis
                        );


                // =================================================
                // CONVERSATION
                // =================================================

                JSONArray conversationArray =
                        createConversation(
                                conversation
                        );


                // =================================================
                // REQUEST
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
                // SEND REQUEST
                // =================================================

                String responseText =
                        executeRequest(
                                connection,
                                request
                        );


                // =================================================
                // PROCESS RESPONSE
                // =================================================

                String aiResponse =
                        parseAdvisorResponse(
                                responseText
                        );


                if (
                        aiResponse == null ||
                                aiResponse.trim().isEmpty()
                ) {

                    callback.onFailure(
                            "The AI returned an empty response."
                    );

                    return;
                }

                callback.onSuccess(
                        aiResponse.trim()
                );


            } catch (Exception e) {

                Log.e(
                        TAG,
                        "askAdvisor failed",
                        e
                );

                callback.onFailure(
                        getReadableError(e)
                );


            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }


    // =====================================================
    // GENERATE PROACTIVE INSIGHTS
    // =====================================================

    public void generateProactiveInsights(
            FinancialAnalysis analysis,
            List<FinancialInsight> insights,
            InsightCallback callback
    ) {

        executor.execute(() -> {

            HttpURLConnection connection =
                    null;

            try {

                // =================================================
                // VALIDATE ANALYSIS
                // =================================================

                if (analysis == null) {

                    callback.onFailure(
                            "Financial analysis is unavailable."
                    );

                    return;
                }


                // =================================================
                // VALIDATE INSIGHTS
                // =================================================

                if (
                        insights == null ||
                                insights.isEmpty()
                ) {

                    callback.onSuccess(
                            new ArrayList<>()
                    );

                    return;
                }


                Log.d(
                        TAG,
                        "Generating proactive insights..."
                );


                // =================================================
                // CREATE URL
                // =================================================

                URL url =
                        new URL(
                                INSIGHTS_API_URL
                        );

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


                // =================================================
                // HEADERS
                // =================================================

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );


                // =================================================
                // FINANCIAL DATA
                // =================================================

                JSONObject financialData =
                        createFinancialData(
                                analysis
                        );


                // =================================================
                // DETECTED INSIGHTS
                // =================================================

                JSONArray insightsArray =
                        new JSONArray();

                int count = 0;


                for (
                        FinancialInsight insight
                        : insights
                ) {

                    if (
                            insight == null ||
                                    count >= 5
                    ) {
                        continue;
                    }


                    JSONObject insightJson =
                            new JSONObject();


                    // -------------------------------------------------
                    // TYPE
                    // -------------------------------------------------

                    insightJson.put(
                            "type",
                            insight.getType() != null
                                    ? insight.getType().name()
                                    : ""
                    );


                    // -------------------------------------------------
                    // SEVERITY
                    // -------------------------------------------------

                    /*
                     * IMPORTANT:
                     *
                     * Severity is generated by the local
                     * ProactiveInsightEngine.
                     *
                     * The backend should preserve this
                     * severity instead of inventing a new one.
                     */

                    insightJson.put(
                            "severity",
                            insight.getSeverity() != null
                                    ? insight.getSeverity().name()
                                    : FinancialInsight.Severity.LOW.name()
                    );


                    // -------------------------------------------------
                    // TITLE
                    // -------------------------------------------------

                    insightJson.put(
                            "title",
                            safeString(
                                    insight.getTitle()
                            )
                    );


                    // -------------------------------------------------
                    // MESSAGE
                    // -------------------------------------------------

                    insightJson.put(
                            "message",
                            safeString(
                                    insight.getMessage()
                            )
                    );


                    insightsArray.put(
                            insightJson
                    );


                    count++;
                }


                // =================================================
                // REQUEST JSON
                // =================================================

                JSONObject request =
                        new JSONObject();

                request.put(
                        "financialData",
                        financialData
                );

                request.put(
                        "insights",
                        insightsArray
                );


                // =================================================
                // DEBUG
                // =================================================

                Log.d(
                        TAG,
                        "Insights endpoint: "
                                + INSIGHTS_API_URL
                );

                Log.d(
                        TAG,
                        "Detected insight count: "
                                + insightsArray.length()
                );


                // =================================================
                // SEND REQUEST
                // =================================================

                OutputStream outputStream =
                        connection.getOutputStream();

                outputStream.write(
                        request
                                .toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );

                outputStream.flush();
                outputStream.close();


                // =================================================
                // RESPONSE CODE
                // =================================================

                int responseCode =
                        connection.getResponseCode();

                Log.d(
                        TAG,
                        "Insights HTTP response: "
                                + responseCode
                );


                // =================================================
                // RESPONSE STREAM
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


                if (inputStream == null) {

                    throw new Exception(
                            "The Financial Insights server "
                                    + "returned no response."
                    );
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

                    responseBuilder.append(line);
                }

                reader.close();


                String responseText =
                        responseBuilder.toString();


                // =================================================
                // HTTP ERROR
                // =================================================

                if (
                        responseCode < 200 ||
                                responseCode >= 300
                ) {

                    String errorMessage =
                            "Financial Insights server error.";

                    try {

                        JSONObject errorJson =
                                new JSONObject(
                                        responseText
                                );

                        String message =
                                errorJson.optString(
                                        "message",
                                        ""
                                );

                        String error =
                                errorJson.optString(
                                        "error",
                                        ""
                                );


                        if (
                                !message.trim().isEmpty()
                        ) {

                            errorMessage =
                                    message;
                        }

                        if (
                                !error.trim().isEmpty()
                        ) {

                            errorMessage +=
                                    "\n" + error;
                        }

                    } catch (Exception ignored) {
                    }


                    throw new Exception(
                            errorMessage
                    );
                }


                // =================================================
                // EMPTY RESPONSE
                // =================================================

                if (
                        responseText.trim().isEmpty()
                ) {

                    throw new Exception(
                            "The Financial Insights server "
                                    + "returned an empty response."
                    );
                }


                // =================================================
                // PARSE RESPONSE
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


                if (!success) {

                    String message =
                            responseJson.optString(
                                    "message",
                                    "Unable to generate financial insights."
                            );

                    String error =
                            responseJson.optString(
                                    "error",
                                    ""
                            );


                    if (
                            !error.trim().isEmpty()
                    ) {

                        message +=
                                "\n" + error;
                    }


                    throw new Exception(
                            message
                    );
                }


                // =================================================
                // GET INSIGHTS ARRAY
                // =================================================

                JSONArray resultArray =
                        responseJson.optJSONArray(
                                "insights"
                        );


                if (resultArray == null) {

                    callback.onSuccess(
                            new ArrayList<>()
                    );

                    return;
                }


                // =================================================
                // CREATE RESULT LIST
                // =================================================

                List<AIInsightResult> results =
                        new ArrayList<>();


                // =================================================
                // PARSE EACH INSIGHT
                // =================================================

                for (
                        int i = 0;
                        i < resultArray.length();
                        i++
                ) {

                    JSONObject insightJson =
                            resultArray.optJSONObject(i);


                    if (insightJson == null) {
                        continue;
                    }


                    String title =
                            insightJson.optString(
                                    "title",
                                    "Financial Insight"
                            );

                    String message =
                            insightJson.optString(
                                    "message",
                                    ""
                            );

                    String severityString =
                            insightJson.optString(
                                    "severity",
                                    "LOW"
                            );


                    title =
                            title.trim();

                    message =
                            message.trim();

                    severityString =
                            severityString
                                    .trim()
                                    .toUpperCase();


                    // =================================================
                    // PARSE SEVERITY SAFELY
                    // =================================================

                    FinancialInsight.Severity severity =
                            parseSeverity(
                                    severityString
                            );


                    // =================================================
                    // VALID RESULT
                    // =================================================

                    if (
                            message.isEmpty()
                    ) {

                        continue;
                    }


                    results.add(
                            new AIInsightResult(
                                    title,
                                    message,
                                    severity
                            )
                    );
                }


                // =================================================
                // DEBUG
                // =================================================

                Log.d(
                        TAG,
                        "AI insights received: "
                                + results.size()
                );


                // =================================================
                // SUCCESS
                // =================================================

                callback.onSuccess(
                        results
                );


            } catch (Exception e) {

                Log.e(
                        TAG,
                        "generateProactiveInsights failed",
                        e
                );

                callback.onFailure(
                        getReadableError(e)
                );


            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }


    // =====================================================
    // PARSE SEVERITY
    // =====================================================

    private FinancialInsight.Severity parseSeverity(
            String severityString
    ) {

        if (
                severityString == null ||
                        severityString.trim().isEmpty()
        ) {

            return FinancialInsight.Severity.LOW;
        }


        try {

            return FinancialInsight.Severity.valueOf(
                    severityString
            );

        } catch (IllegalArgumentException e) {

            Log.w(
                    TAG,
                    "Unknown insight severity: "
                            + severityString
                            + ". Defaulting to LOW."
            );

            return FinancialInsight.Severity.LOW;
        }
    }


    // =====================================================
    // CREATE FINANCIAL DATA
    // =====================================================

    private JSONObject createFinancialData(
            FinancialAnalysis analysis
    ) throws Exception {

        JSONObject financialData =
                new JSONObject();


        // -------------------------------------------------
        // BASIC FINANCIAL VALUES
        // -------------------------------------------------

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


        // -------------------------------------------------
        // BUDGET
        // -------------------------------------------------

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


        // -------------------------------------------------
        // CATEGORY
        // -------------------------------------------------

        financialData.put(
                "highestCategory",
                safeString(
                        analysis.getHighestCategory()
                )
        );

        financialData.put(
                "highestCategoryAmount",
                analysis.getHighestCategoryAmount()
        );


        // -------------------------------------------------
        // CURRENT / PREVIOUS MONTH
        // -------------------------------------------------

        financialData.put(
                "currentMonthExpense",
                analysis.getCurrentMonthExpense()
        );

        financialData.put(
                "previousMonthExpense",
                analysis.getPreviousMonthExpense()
        );

        financialData.put(
                "currentMonthIncome",
                analysis.getCurrentMonthIncome()
        );

        financialData.put(
                "previousMonthIncome",
                analysis.getPreviousMonthIncome()
        );

        financialData.put(
                "expenseChangePercentage",
                analysis.getExpenseChangePercentage()
        );


        // -------------------------------------------------
        // HEALTH SCORE
        // -------------------------------------------------

        financialData.put(
                "financialHealthScore",
                analysis.getFinancialHealthScore()
        );


        // =================================================
        // CATEGORY TOTALS
        // =================================================

        JSONObject categoryTotals =
                new JSONObject();

        Map<String, Double> categories =
                analysis.getCategoryTotals();


        if (categories != null) {

            for (
                    Map.Entry<String, Double> entry
                    : categories.entrySet()
            ) {

                String category =
                        entry.getKey();


                if (
                        category == null ||
                                category.trim().isEmpty()
                ) {

                    continue;
                }


                Double amount =
                        entry.getValue();


                if (amount == null) {
                    amount = 0.0;
                }


                categoryTotals.put(
                        category,
                        amount
                );
            }
        }


        financialData.put(
                "categoryTotals",
                categoryTotals
        );


        // =================================================
        // DEBUG
        // =================================================

        Log.d(
                TAG,
                "Category totals = "
                        + categoryTotals
        );


        return financialData;
    }


    // =====================================================
    // CREATE CONVERSATION
    // =====================================================

    private JSONArray createConversation(
            ArrayList<AdvisorMessage> conversation
    ) throws Exception {

        JSONArray conversationArray =
                new JSONArray();


        if (
                conversation == null ||
                        conversation.isEmpty()
        ) {

            return conversationArray;
        }


        /*
         * Only send the latest 10 messages.
         *
         * This keeps the request smaller and helps
         * reduce unnecessary processing time.
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


            String content =
                    message.getMessage();


            if (
                    content == null ||
                            content.trim().isEmpty()
            ) {

                continue;
            }


            JSONObject messageJson =
                    new JSONObject();


            String role;


            if (
                    message.getType()
                            == AdvisorMessage.TYPE_USER
            ) {

                role = "user";

            } else {

                role = "assistant";
            }


            messageJson.put(
                    "role",
                    role
            );


            messageJson.put(
                    "content",
                    content.trim()
            );


            conversationArray.put(
                    messageJson
            );
        }


        return conversationArray;
    }


    // =====================================================
    // EXECUTE HTTP REQUEST
    // =====================================================

    private String executeRequest(
            HttpURLConnection connection,
            JSONObject request
    ) throws Exception {

        String requestBody =
                request.toString();


        Log.d(
                TAG,
                "Sending request to: "
                        + API_URL
        );


        Log.d(
                TAG,
                "Request size = "
                        + requestBody.length()
                        + " characters"
        );


        // =================================================
        // WRITE REQUEST
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
        // RESPONSE CODE
        // =================================================

        int responseCode =
                connection.getResponseCode();


        Log.d(
                TAG,
                "HTTP response code = "
                        + responseCode
        );


        // =================================================
        // INPUT STREAM
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


        if (inputStream == null) {

            throw new Exception(
                    "The Financial Advisor server "
                            + "returned no response."
            );
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

            responseBuilder.append(line);
        }


        reader.close();


        String responseText =
                responseBuilder.toString();


        // =================================================
        // EMPTY RESPONSE
        // =================================================

        if (
                responseText.trim().isEmpty()
        ) {

            throw new Exception(
                    "The Financial Advisor server "
                            + "returned an empty response."
            );
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


                String message =
                        errorJson.optString(
                                "message",
                                ""
                        );


                String error =
                        errorJson.optString(
                                "error",
                                ""
                        );


                if (
                        !message.trim().isEmpty()
                ) {

                    if (
                            !error.trim().isEmpty()
                    ) {

                        message +=
                                "\n" + error;
                    }


                    throw new Exception(
                            message
                    );
                }


            } catch (Exception jsonException) {

                if (
                        jsonException.getMessage() != null &&
                                !jsonException
                                        .getMessage()
                                        .trim()
                                        .isEmpty()
                ) {

                    throw jsonException;
                }
            }


            throw new Exception(
                    "Server returned HTTP "
                            + responseCode
            );
        }


        return responseText;
    }


    // =====================================================
    // PARSE ADVISOR RESPONSE
    // =====================================================

    private String parseAdvisorResponse(
            String responseText
    ) throws Exception {

        if (
                responseText == null ||
                        responseText.trim().isEmpty()
        ) {

            return "";
        }


        JSONObject responseJson =
                new JSONObject(
                        responseText
                );


        boolean success =
                responseJson.optBoolean(
                        "success",
                        false
                );


        if (!success) {

            String message =
                    responseJson.optString(
                            "message",
                            ""
                    );


            String error =
                    responseJson.optString(
                            "error",
                            ""
                    );


            if (
                    !message.trim().isEmpty()
            ) {

                if (
                        !error.trim().isEmpty()
                ) {

                    message +=
                            "\n" + error;
                }


                throw new Exception(
                        message
                );
            }


            throw new Exception(
                    "The Financial Advisor "
                            + "server reported a failure."
            );
        }


        return responseJson.optString(
                "response",
                ""
        );
    }


    // =====================================================
    // SAFE STRING
    // =====================================================

    private String safeString(
            String value
    ) {

        if (
                value == null ||
                        value.trim().isEmpty()
        ) {

            return "";
        }


        return value.trim();
    }


    // =====================================================
    // READABLE ERROR
    // =====================================================

    private String getReadableError(
            Exception e
    ) {

        if (e == null) {

            return "Unknown connection error.";
        }


        String message =
                e.getMessage();


        if (
                message == null ||
                        message.trim().isEmpty()
        ) {

            return "Unknown connection error.";
        }


        String lower =
                message.toLowerCase();


        // -------------------------------------------------
        // TIMEOUT
        // -------------------------------------------------

        if (
                lower.contains("timeout") ||
                        lower.contains("timed out")
        ) {

            return "The AI took too long to respond. "
                    + "Your local Qwen model may need more "
                    + "time to process the request.";
        }


        // -------------------------------------------------
        // CONNECTION REFUSED
        // -------------------------------------------------

        if (
                lower.contains(
                        "connection refused"
                )
        ) {

            return "Unable to connect to the "
                    + "Financial Advisor server. "
                    + "Make sure the Node.js backend "
                    + "is running on port 3000.";
        }


        // -------------------------------------------------
        // UNKNOWN HOST
        // -------------------------------------------------

        if (
                lower.contains(
                        "unable to resolve host"
                ) ||
                        lower.contains(
                                "unknownhost"
                        )
        ) {

            return "The device cannot reach the "
                    + "Financial Advisor server.";
        }


        return message;
    }


    // =====================================================
    // SHUTDOWN
    // =====================================================

    public void shutdown() {

        try {

            executor.shutdownNow();

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Error shutting down executor.",
                    e
            );
        }
    }
}
