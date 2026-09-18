const aiService = require("../services/aiService");
const config = require("../config/config");


// =====================================================
// GENERATE PROACTIVE FINANCIAL INSIGHTS
// =====================================================

async function generateFinancialInsights(req, res) {

    let detectedInsightsFromRequest = [];

    try {

        console.log(
            "======================================"
        );

        console.log(
            "PROACTIVE FINANCIAL INSIGHTS REQUEST"
        );

        console.log(
            "======================================"
        );


        // =================================================
        // READ REQUEST
        // =================================================

        const {
            financialData,
            insights
        } = req.body;


        // =================================================
        // VALIDATE FINANCIAL DATA
        // =================================================

        if (
            !financialData ||
            typeof financialData !== "object" ||
            Array.isArray(financialData)
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "Financial data is required."
            });
        }


        // =================================================
        // VALIDATE INSIGHTS
        // =================================================

        if (
            !Array.isArray(insights)
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "Insights must be an array."
            });
        }


        // =================================================
        // NO INSIGHTS
        // =================================================

        if (
            insights.length === 0
        ) {

            console.log(
                "No proactive insights detected."
            );

            return res.json({

                success: true,

                insights: []
            });
        }


        // =================================================
        // LIMIT INSIGHTS
        // =================================================

        const limitedInsights =
            insights
                .filter(
                    insight =>
                        insight &&
                        typeof insight === "object"
                )
                .slice(
                    0,
                    config.limits.maxInsights
                );


        console.log(
            "Detected insights:",
            limitedInsights.length
        );


        // =================================================
        // NORMALIZE SEVERITY
        // =================================================

        const normalizedInsights =
            limitedInsights.map(
                insight => {

                    const severity =
                        normalizeSeverity(
                            insight.severity
                        );


                    return {

                        type:
                            String(
                                insight.type ||
                                "UNKNOWN"
                            ),

                        severity,

                        title:
                            String(
                                insight.title ||
                                "Financial Insight"
                            ).trim(),

                        message:
                            String(
                                insight.message ||
                                ""
                            ).trim()
                    };
                }
            );


        // =================================================
        // CREATE FALLBACK INSIGHTS
        // =================================================

        const fallbackInsights =
            normalizedInsights
                .map(
                    insight => {

                        if (
                            insight.message.length === 0
                        ) {

                            return null;
                        }


                        return {

                            title:
                                insight.title.length > 0
                                    ? insight.title
                                    : "Financial Insight",

                            message:
                                insight.message,

                            severity:
                                insight.severity
                        };
                    }
                )
                .filter(
                    insight =>
                        insight !== null
                );


        detectedInsightsFromRequest =
            fallbackInsights;


        // =================================================
        // FINANCIAL VALUES
        // =================================================

        const totalIncome =
            Number(
                financialData.totalIncome
            ) || 0;


        const totalExpense =
            Number(
                financialData.totalExpense
            ) || 0;


        const savings =
            Number(
                financialData.savings
            ) || 0;


        const highestCategory =
            String(
                financialData.highestCategory ||
                "Unknown"
            );


        // =================================================
        // DETECTED INSIGHTS TEXT
        // =================================================

        const detectedInsightsText =
            normalizedInsights
                .map(
                    (insight, index) => {

                        return (
                            `Insight ${index + 1}:\n`
                            + `Type: ${insight.type}\n`
                            + `Severity: ${insight.severity}\n`
                            + `Title: ${insight.title}\n`
                            + `Detected message: ${insight.message}`
                        );
                    }
                )
                .join("\n\n");


        // =================================================
        // SYSTEM PROMPT
        // =================================================

        const systemPrompt = `
You are a financial insight formatter.

The application has ALREADY detected the financial
insights and assigned their severity.

Your ONLY job is to rewrite those detected messages
into short, clear and useful messages.

STRICT RULES:

1. Do NOT create new insights.

2. Do NOT remove detected insights.

3. Keep exactly the same number of insights.

4. Do NOT change financial numbers.

5. Do NOT invent financial information.

6. Use only the supplied information.

7. Use Sri Lankan Rupees (Rs).

8. Keep each message short.

9. Return ONLY JSON.

10. Do not explain your reasoning.

11. Do not write anything outside the JSON object.

12. Do not use markdown.

13. Keep the title semantically equivalent to the
    detected insight title.

14. Preserve the meaning of every detected insight.

15. The severity is assigned by the application.

16. NEVER change the supplied severity.

17. Return the exact supplied severity for each
    corresponding insight.

18. Preserve the order of the insights.

Required format:

{
  "insights": [
    {
      "title": "title",
      "message": "short message",
      "severity": "CRITICAL"
    }
  ]
}

Allowed severity values:

CRITICAL
HIGH
MEDIUM
LOW
POSITIVE
`;


        // =================================================
        // USER PROMPT
        // =================================================

        const userPrompt = `
FINANCIAL FACTS

Income:
Rs ${totalIncome}

Expenses:
Rs ${totalExpense}

Savings:
Rs ${savings}

Highest spending category:
${highestCategory}


DETECTED INSIGHTS

${detectedInsightsText}


TASK

Rewrite ONLY the detected insights.

Keep exactly ${normalizedInsights.length} insights.

Preserve all financial numbers.

Do not create any new insight.

Do not remove any detected insight.

Do not change severity.

Return the exact severity supplied for
each insight.

Preserve the order.

Keep each message concise.

Return ONLY the JSON object.
`;


        // =================================================
        // JSON FORMAT
        // =================================================

        const responseFormat = {

            type: "object",

            properties: {

                insights: {

                    type: "array",

                    items: {

                        type: "object",

                        properties: {

                            title: {
                                type: "string"
                            },

                            message: {
                                type: "string"
                            },

                            severity: {

                                type: "string",

                                enum: [
                                    "CRITICAL",
                                    "HIGH",
                                    "MEDIUM",
                                    "LOW",
                                    "POSITIVE"
                                ]
                            }
                        },

                        required: [
                            "title",
                            "message",
                            "severity"
                        ]
                    }
                }
            },

            required: [
                "insights"
            ]
        };


        // =================================================
        // SEND TO AI SERVICE
        // =================================================

        console.log(
            "Sending proactive insight request to AI..."
        );


        const result =
            await aiService.generateFinancialInsights({

                systemPrompt,

                userPrompt,

                format:
                    responseFormat
            });


        // =================================================
        // CHECK TRUNCATION
        // =================================================

        if (
            result.metadata &&
            result.metadata.doneReason === "length"
        ) {

            console.warn(
                "AI response was truncated."
            );

            return res.json({

                success: true,

                insights:
                    fallbackInsights,

                source:
                    "detected_insights_fallback"
            });
        }


        // =================================================
        // CLEAN JSON
        // =================================================

        const cleanedResponse =
            cleanAIJsonResponse(
                result.response
            );


        // =================================================
        // PARSE JSON
        // =================================================

        let parsed;


        try {

            parsed =
                JSON.parse(
                    cleanedResponse
                );

        } catch (jsonError) {

            console.error(
                "AI JSON parsing failed:",
                jsonError.message
            );


            return res.json({

                success: true,

                insights:
                    fallbackInsights,

                source:
                    "detected_insights_fallback"
            });
        }


        // =================================================
        // VALIDATE STRUCTURE
        // =================================================

        if (
            !parsed ||
            !Array.isArray(
                parsed.insights
            )
        ) {

            console.warn(
                "AI response does not contain a valid insights array."
            );


            return res.json({

                success: true,

                insights:
                    fallbackInsights,

                source:
                    "detected_insights_fallback"
            });
        }


        // =================================================
        // VERIFY COUNT
        // =================================================

        if (
            parsed.insights.length !==
            normalizedInsights.length
        ) {

            console.warn(
                "AI returned "
                + parsed.insights.length
                + " insights, but the application detected "
                + normalizedInsights.length
                + "."
            );


            return res.json({

                success: true,

                insights:
                    fallbackInsights,

                source:
                    "detected_insights_fallback"
            });
        }


        // =================================================
        // SANITIZE AI RESULTS
        // =================================================

        const results = [];


        for (
            let i = 0;
            i < parsed.insights.length;
            i++
        ) {

            const aiInsight =
                parsed.insights[i];


            if (
                !aiInsight ||
                typeof aiInsight !== "object"
            ) {

                console.warn(
                    "Invalid AI insight at index "
                    + i
                );


                return res.json({

                    success: true,

                    insights:
                        fallbackInsights,

                    source:
                        "detected_insights_fallback"
                });
            }


            const originalInsight =
                normalizedInsights[i];


            const title =
                String(
                    aiInsight.title ||
                    originalInsight.title ||
                    "Financial Insight"
                ).trim();


            const message =
                String(
                    aiInsight.message ||
                    ""
                ).trim();


            if (
                message.length === 0
            ) {

                console.warn(
                    "AI returned an empty message at index "
                    + i
                );


                return res.json({

                    success: true,

                    insights:
                        fallbackInsights,

                    source:
                        "detected_insights_fallback"
                });
            }


            /*
             * IMPORTANT:
             *
             * Severity is controlled by the
             * Android ProactiveInsightEngine.
             *
             * We intentionally ignore the severity
             * returned by the AI.
             */

            const severity =
                originalInsight.severity;


            results.push({

                title:
                    title.length > 0
                        ? title
                        : "Financial Insight",

                message,

                severity
            });
        }


        // =================================================
        // SUCCESS
        // =================================================

        console.log(
            "Generated proactive insights:"
        );


        console.log(
            JSON.stringify(
                results,
                null,
                2
            )
        );


        return res.json({

            success: true,

            insights:
                results,

            source:
                "ollama"
        });


    } catch (error) {

        console.error(
            "======================================"
        );

        console.error(
            "Financial Insights Error:"
        );

        console.error(
            error
        );

        console.error(
            "======================================"
        );


        // =================================================
        // SAFE FALLBACK
        // =================================================

        if (
            Array.isArray(
                detectedInsightsFromRequest
            ) &&
            detectedInsightsFromRequest.length > 0
        ) {

            console.warn(
                "Returning detected insights because AI failed."
            );


            return res.json({

                success: true,

                insights:
                    detectedInsightsFromRequest,

                source:
                    "detected_insights_fallback"
            });
        }


        return res.status(500).json({

            success: false,

            message:
                "Unable to generate financial insights.",

            error:
                error.message
        });
    }
}


// =====================================================
// NORMALIZE SEVERITY
// =====================================================

function normalizeSeverity(severity) {

    const value =
        String(
            severity || "LOW"
        )
            .trim()
            .toUpperCase();


    const allowed = [

        "CRITICAL",
        "HIGH",
        "MEDIUM",
        "LOW",
        "POSITIVE"
    ];


    if (
        allowed.includes(value)
    ) {

        return value;
    }


    console.warn(
        "Unknown severity received: "
        + value
        + ". Defaulting to LOW."
    );


    return "LOW";
}


// =====================================================
// CLEAN AI JSON RESPONSE
// =====================================================

function cleanAIJsonResponse(response) {

    if (
        !response ||
        typeof response !== "string"
    ) {

        return "";
    }


    let cleaned =
        response.trim();


    // Remove ```json
    cleaned =
        cleaned.replace(
            /^```json\s*/i,
            ""
        );


    // Remove ```
    cleaned =
        cleaned.replace(
            /^```\s*/i,
            ""
        );


    // Remove closing ```
    cleaned =
        cleaned.replace(
            /\s*```$/i,
            ""
        );


    cleaned =
        cleaned.trim();


    // =================================================
    // EXTRACT JSON OBJECT
    // =================================================

    const firstBrace =
        cleaned.indexOf("{");


    const lastBrace =
        cleaned.lastIndexOf("}");


    if (
        firstBrace >= 0 &&
        lastBrace > firstBrace
    ) {

        cleaned =
            cleaned.substring(
                firstBrace,
                lastBrace + 1
            );
    }


    return cleaned.trim();
}


// =====================================================
// EXPORT
// =====================================================

module.exports = {

    generateFinancialInsights
};