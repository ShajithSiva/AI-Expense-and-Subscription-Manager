const config = require("../config/config");


// =====================================================
// PROVIDER SELECTION
// =====================================================

function getAIProvider() {

    const provider =
        config.aiProvider.toLowerCase();


    if (
        provider !== "ollama" &&
        provider !== "gemini"
    ) {

        throw new Error(
            `Unsupported AI provider: ${provider}`
        );
    }


    return provider;
}


// =====================================================
// OLLAMA REQUEST
// =====================================================

async function generateWithOllama({
    system,
    prompt,
    temperature = 0.3,
    numPredict = 200,
    format = undefined
}) {

    const requestBody = {

        model:
            config.ollama.model,

        system,

        prompt,

        stream:
            false,

        think:
            false,

        keep_alive:
            config.ollama.keepAlive,

        options: {

            temperature,

            num_predict:
                numPredict,

            num_ctx:
                config.ai.contextSize
        }
    };


    if (
        format !== undefined
    ) {

        requestBody.format =
            format;
    }


    const startTime =
        Date.now();


    const response =
        await fetch(
            config.ollama.url,
            {
                method:
                    "POST",

                headers: {

                    "Content-Type":
                        "application/json",

                    "Accept":
                        "application/json"
                },

                body:
                    JSON.stringify(
                        requestBody
                    )
            }
        );


    const responseTime =
        (
            Date.now()
            - startTime
        ) / 1000;


    console.log(
        "Ollama response time:",
        responseTime.toFixed(2),
        "seconds"
    );


    if (
        !response.ok
    ) {

        const errorText =
            await response.text();


        throw new Error(
            "Ollama returned HTTP "
            + response.status
            + ": "
            + errorText
        );
    }


    return await response.json();
}


// =====================================================
// GEMINI REQUEST
// =====================================================

async function generateWithGemini({
    system,
    prompt,
    temperature = 0.3,
    numPredict = 200,
    format = undefined
}) {

    if (
        !config.gemini.apiKey
    ) {

        throw new Error(
            "GEMINI_API_KEY is not configured."
        );
    }


    const model =
        config.gemini.model;


    const url =
         `https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(model)}:generateContent`;


    const generationConfig = {

        temperature,

        maxOutputTokens:
            numPredict
    };


    /*
     * Gemini structured JSON output.
     *
     * The insight controller already supplies
     * a JSON schema. We translate the required
     * Ollama format into Gemini's response format.
     */

    if (
        format &&
        format.type === "object"
    ) {

        generationConfig.responseMimeType =
            "application/json";


        if (
            format.properties
        ) {

            generationConfig.responseSchema =
                convertGeminiSchema(
                    format
                );
        }
    }


    const body = {

        systemInstruction: {

            parts: [

                {
                    text:
                        system || ""
                }

            ]
        },

        contents: [

            {

                role:
                    "user",

                parts: [

                    {
                        text:
                            prompt
                    }

                ]
            }

        ],

        generationConfig
    };


    const startTime =
        Date.now();


    const response =
        await fetch(
            url,
            {
                method:
                    "POST",

                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json",
                    "x-goog-api-key": config.gemini.apiKey
                },

                body:
                    JSON.stringify(
                        body
                    )
            }
        );


    const responseTime =
        (
            Date.now()
            - startTime
        ) / 1000;


    console.log(
        "Gemini response time:",
        responseTime.toFixed(2),
        "seconds"
    );


    if (
        !response.ok
    ) {

        const errorText =
            await response.text();


        throw new Error(
            "Gemini returned HTTP "
            + response.status
            + ": "
            + errorText
        );
    }


    const data =
        await response.json();


    const candidate =
        data.candidates &&
        data.candidates[0];


    if (
        !candidate
    ) {

        throw new Error(
            "Gemini returned no candidate."
        );
    }


    const parts =
        candidate.content &&
        candidate.content.parts;


    if (
        !Array.isArray(parts)
    ) {

        throw new Error(
            "Gemini returned no response content."
        );
    }


    const text =
        parts
            .map(
                part =>
                    part.text || ""
            )
            .join("")
            .trim();


    if (
        text.length === 0
    ) {

        throw new Error(
            "Gemini returned an empty response."
        );
    }


    return {

        response:
            text,

        model:
            model,

        done:
            true,

        done_reason:
            candidate.finishReason,

        eval_count:
            undefined
    };
}


// =====================================================
// GEMINI SCHEMA CONVERSION
// =====================================================

function convertGeminiSchema(
    schema
) {

    if (
        !schema ||
        typeof schema !== "object"
    ) {

        return undefined;
    }


    const result = {};


    if (
        schema.type
    ) {

        result.type =
            schema.type.toUpperCase();
    }


    if (
        schema.description
    ) {

        result.description =
            schema.description;
    }


    if (
        Array.isArray(
            schema.enum
        )
    ) {

        result.enum =
            schema.enum;
    }


    if (
        schema.properties
    ) {

        result.properties = {};


        for (
            const [
                key,
                value
            ]
            of Object.entries(
                schema.properties
            )
        ) {

            result.properties[key] =
                convertGeminiSchema(
                    value
                );
        }
    }


    if (
        schema.items
    ) {

        result.items =
            convertGeminiSchema(
                schema.items
            );
    }


    if (
        Array.isArray(
            schema.required
        )
    ) {

        result.required =
            schema.required;
    }


    return result;
}


// =====================================================
// GENERIC AI REQUEST
// =====================================================

async function generateAI({
    system,
    prompt,
    temperature = 0.3,
    numPredict = 200,
    format = undefined
}) {

    const provider =
        getAIProvider();


    console.log(
        "AI provider:",
        provider
    );


    if (
        provider === "gemini"
    ) {

        return await generateWithGemini({

            system,

            prompt,

            temperature,

            numPredict,

            format
        });
    }


    return await generateWithOllama({

        system,

        prompt,

        temperature,

        numPredict,

        format
    });
}


// =====================================================
// TEST AI
// =====================================================

async function testAI() {

    const result =
        await generateAI({

            prompt:
                "Explain what a monthly budget is "
                + "in one short sentence.",

            temperature:
                0.3,

            numPredict:
                100
        });


    return result.response.trim();
}


// =====================================================
// FINANCIAL ADVISOR
// =====================================================

async function generateFinancialAdvice({
    systemPrompt,
    userPrompt
}) {

    const result =
        await generateAI({

            system:
                systemPrompt,

            prompt:
                userPrompt,

            temperature:
                0.3,

            numPredict:
                config.ai.advisorMaxOutput
        });


    return {

        response:
            result.response.trim(),

        metadata: {

            model:
                result.model,

            done:
                result.done,

            doneReason:
                result.done_reason,

            evalCount:
                result.eval_count
        }
    };
}


// =====================================================
// FINANCIAL INSIGHTS
// =====================================================

async function generateFinancialInsights({
    systemPrompt,
    userPrompt,
    format
}) {

    const result =
        await generateAI({

            system:
                systemPrompt,

            prompt:
                userPrompt,

            temperature:
                0.0,

            numPredict:
                config.ai.insightMaxOutput,

            format
        });


    return {

        response:
            result.response.trim(),

        metadata: {

            model:
                result.model,

            done:
                result.done,

            doneReason:
                result.done_reason,

            evalCount:
                result.eval_count
        }
    };
}


// =====================================================
// EXPORTS
// =====================================================

module.exports = {

    generateWithOllama,

    generateWithGemini,

    generateAI,

    testAI,

    generateFinancialAdvice,

    generateFinancialInsights
};