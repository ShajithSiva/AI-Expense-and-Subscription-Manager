require("dotenv").config();

const config = {

    // =================================================
    // SERVER
    // =================================================

    port:
        Number(process.env.PORT) || 3000,

    // =================================================
    // AI PROVIDER
    // =================================================

    aiProvider:
        (
            process.env.AI_PROVIDER ||
            "ollama"
        ).toLowerCase(),


    // =================================================
    // OLLAMA
    // =================================================

    ollama: {

        url:
            process.env.OLLAMA_URL ||
            "http://localhost:11434/api/generate",

        model:
            process.env.MODEL ||
            "qwen3:4b",

        keepAlive:
            process.env.KEEP_ALIVE ||
            "10m"
    },


    // =================================================
    // GEMINI
    // =================================================

    gemini: {

        apiKey:
            process.env.GEMINI_API_KEY ||
            "",

        model:
            process.env.GEMINI_MODEL ||
            "gemini-2.5-flash-lite"
    },


    // =================================================
    // AI SETTINGS
    // =================================================

    ai: {

        advisorMaxOutput:
            200,

        insightMaxOutput:
            180,

        contextSize:
            2048
    },


    // =================================================
    // REQUEST LIMITS
    // =================================================

    limits: {

        maxInsights:
            5,

        maxConversationMessages:
            10
    }
};


module.exports = config;