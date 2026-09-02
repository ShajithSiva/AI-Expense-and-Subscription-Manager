package com.example.aiexpensemanagementapplication.ui.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class AIInsightCacheManager {

    private static final String TAG =
            "AI_INSIGHT_CACHE";

    private static final String PREF_NAME =
            "ai_insights_cache";

    private static final String KEY_FINGERPRINT_PREFIX =
            "fingerprint_";

    private static final String KEY_RESULTS_PREFIX =
            "results_";


    // =====================================================
    // PRIVATE CONSTRUCTOR
    // =====================================================

    private AIInsightCacheManager() {
        // Utility class.
    }


    // =====================================================
    // GET PREFERENCES
    // =====================================================

    private static SharedPreferences getPreferences(
            Context context
    ) {

        return context.getApplicationContext()
                .getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );
    }


    // =====================================================
    // FINGERPRINT KEY
    // =====================================================

    private static String getFingerprintKey(
            int userId
    ) {

        return KEY_FINGERPRINT_PREFIX
                + userId;
    }


    // =====================================================
    // RESULTS KEY
    // =====================================================

    private static String getResultsKey(
            int userId
    ) {

        return KEY_RESULTS_PREFIX
                + userId;
    }


    // =====================================================
    // SAVE INSIGHTS
    // =====================================================

    public static void saveInsights(
            Context context,
            int userId,
            String fingerprint,
            List<AIInsightResult> insights
    ) {

        if (
                context == null ||
                        userId == -1 ||
                        fingerprint == null ||
                        fingerprint.trim().isEmpty() ||
                        insights == null ||
                        insights.isEmpty()
        ) {

            return;
        }


        try {

            JSONArray array =
                    new JSONArray();


            for (
                    AIInsightResult insight
                    : insights
            ) {

                if (
                        insight == null
                ) {

                    continue;
                }


                JSONObject object =
                        new JSONObject();


                // =============================================
                // TITLE
                // =============================================

                String title =
                        insight.getTitle();


                object.put(
                        "title",
                        title == null
                                ? ""
                                : title
                );


                // =============================================
                // MESSAGE
                // =============================================

                String message =
                        insight.getMessage();


                object.put(
                        "message",
                        message == null
                                ? ""
                                : message
                );


                // =============================================
                // SEVERITY
                // =============================================

                FinancialInsight.Severity severity =
                        insight.getSeverity();


                if (
                        severity == null
                ) {

                    severity =
                            FinancialInsight.Severity.LOW;
                }


                object.put(
                        "severity",
                        severity.name()
                );


                // =============================================
                // ADD RESULT
                // =============================================

                array.put(
                        object
                );
            }


            if (
                    array.length() == 0
            ) {

                return;
            }


            // =============================================
            // SAVE
            // =============================================

            getPreferences(context)
                    .edit()
                    .putString(
                            getFingerprintKey(
                                    userId
                            ),
                            fingerprint
                    )
                    .putString(
                            getResultsKey(
                                    userId
                            ),
                            array.toString()
                    )
                    .apply();


            Log.d(
                    TAG,
                    "AI insights cached for user "
                            + userId
                            + " with severity."
            );

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Failed to save AI insight cache.",
                    e
            );
        }
    }


    // =====================================================
    // GET CACHED INSIGHTS
    // =====================================================

    public static List<AIInsightResult> getCachedInsights(
            Context context,
            int userId,
            String currentFingerprint
    ) {

        if (
                context == null ||
                        userId == -1 ||
                        currentFingerprint == null ||
                        currentFingerprint.trim().isEmpty()
        ) {

            return null;
        }


        try {

            SharedPreferences preferences =
                    getPreferences(
                            context
                    );


            // =============================================
            // GET SAVED FINGERPRINT
            // =============================================

            String savedFingerprint =
                    preferences.getString(
                            getFingerprintKey(
                                    userId
                            ),
                            ""
                    );


            // =============================================
            // FINGERPRINT DOES NOT MATCH
            // =============================================

            if (
                    savedFingerprint.isEmpty() ||
                            !savedFingerprint.equals(
                                    currentFingerprint
                            )
            ) {

                return null;
            }


            // =============================================
            // GET CACHED JSON
            // =============================================

            String cachedJson =
                    preferences.getString(
                            getResultsKey(
                                    userId
                            ),
                            ""
                    );


            if (
                    cachedJson.isEmpty()
            ) {

                return null;
            }


            JSONArray array =
                    new JSONArray(
                            cachedJson
                    );


            List<AIInsightResult> results =
                    new ArrayList<>();


            // =============================================
            // READ EACH INSIGHT
            // =============================================

            for (
                    int i = 0;
                    i < array.length();
                    i++
            ) {

                JSONObject object =
                        array.optJSONObject(
                                i
                        );


                if (
                        object == null
                ) {

                    continue;
                }


                // =========================================
                // TITLE
                // =========================================

                String title =
                        object.optString(
                                "title",
                                "Financial Insight"
                        );


                // =========================================
                // MESSAGE
                // =========================================

                String message =
                        object.optString(
                                "message",
                                ""
                        );


                if (
                        message.trim().isEmpty()
                ) {

                    continue;
                }


                // =========================================
                // SEVERITY
                // =========================================

                String severityString =
                        object.optString(
                                "severity",
                                "LOW"
                        );


                FinancialInsight.Severity severity =
                        parseSeverity(
                                severityString
                        );


                // =========================================
                // CREATE RESULT
                // =========================================

                results.add(
                        new AIInsightResult(
                                title,
                                message,
                                severity
                        )
                );
            }


            // =============================================
            // NO VALID RESULTS
            // =============================================

            if (
                    results.isEmpty()
            ) {

                return null;
            }


            Log.d(
                    TAG,
                    "Using cached AI insights for user "
                            + userId
                            + " with severity."
            );


            return results;

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Failed to read AI insight cache.",
                    e
            );


            return null;
        }
    }


    // =====================================================
    // PARSE SEVERITY
    // =====================================================

    private static FinancialInsight.Severity parseSeverity(
            String severity
    ) {

        if (
                severity == null ||
                        severity.trim().isEmpty()
        ) {

            return FinancialInsight.Severity.LOW;
        }


        try {

            return FinancialInsight.Severity.valueOf(
                    severity
                            .trim()
                            .toUpperCase()
            );

        } catch (IllegalArgumentException e) {

            Log.w(
                    TAG,
                    "Unknown severity: "
                            + severity
                            + ". Using LOW."
            );


            return FinancialInsight.Severity.LOW;
        }
    }


    // =====================================================
    // INVALIDATE USER CACHE
    // =====================================================

    public static void invalidate(
            Context context,
            int userId
    ) {

        if (
                context == null ||
                        userId == -1
        ) {

            return;
        }


        getPreferences(context)
                .edit()
                .remove(
                        getFingerprintKey(
                                userId
                        )
                )
                .remove(
                        getResultsKey(
                                userId
                        )
                )
                .apply();


        Log.d(
                TAG,
                "AI insight cache invalidated for user "
                        + userId
        );
    }


    // =====================================================
    // INVALIDATE STRING USER ID
    // =====================================================

    public static void invalidate(
            Context context,
            String userId
    ) {

        if (
                context == null ||
                        userId == null ||
                        userId.trim().isEmpty()
        ) {

            return;
        }


        try {

            int id =
                    Integer.parseInt(
                            userId.trim()
                    );


            invalidate(
                    context,
                    id
            );

        } catch (NumberFormatException e) {

            Log.e(
                    TAG,
                    "Invalid user ID: "
                            + userId,
                    e
            );
        }
    }


    // =====================================================
    // CLEAR ALL CACHE
    // =====================================================

    public static void clearAll(
            Context context
    ) {

        if (
                context == null
        ) {

            return;
        }


        getPreferences(context)
                .edit()
                .clear()
                .apply();


        Log.d(
                TAG,
                "All AI insight cache cleared."
        );
    }
}