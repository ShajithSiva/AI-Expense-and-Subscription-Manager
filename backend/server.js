// =====================================================
// ENVIRONMENT
// =====================================================

require("dotenv").config();


// =====================================================
// IMPORTS
// =====================================================

const express = require("express");
const cors = require("cors");

const config = require("./config/config");

const aiService = require("./services/aiService");

const advisorRoutes = require("./routes/advisorRoutes");
const insightRoutes = require("./routes/insightRoutes");


// =====================================================
// APP
// =====================================================

const app = express();


// =====================================================
// MIDDLEWARE
// =====================================================

app.use(cors());

app.use(
    express.json({
        limit: "1mb"
    })
);


// =====================================================
// HEALTH CHECK
// =====================================================

app.get(
    "/",
    (req, res) => {

        res.json({

            success: true,

            message:
                "AI Financial Advisor backend is running",

            provider:
                "Ollama",

            model:
                config.ollama.model,

            endpoints: {

                advisor:
                    "/api/financial-advisor",

                insights:
                    "/api/financial-insights",

                test:
                    "/test-ai"
            }
        });
    }
);


// =====================================================
// TEST AI
// =====================================================

app.get(
    "/test-ai",
    async (req, res) => {

        try {

            console.log(
                "======================================"
            );

            console.log(
                "TEST AI REQUEST"
            );

            console.log(
                "======================================"
            );


            const response =
                await aiService.testAI();


            console.log(
                "Test AI response:"
            );

            console.log(
                response
            );


            return res.json({

                success:
                    true,

                response
            });


        } catch (error) {

            console.error(
                "Test AI error:",
                error
            );


            return res.status(500).json({

                success:
                    false,

                message:
                    "Unable to connect to AI service.",

                error:
                    error.message
            });
        }
    }
);


// =====================================================
// API ROUTES
// =====================================================

app.use(
    "/api",
    advisorRoutes
);

app.use(
    "/api",
    insightRoutes
);


// =====================================================
// GLOBAL ERROR HANDLER
// =====================================================

app.use(
    (
        err,
        req,
        res,
        next
    ) => {

        console.error(
            "Unhandled server error:",
            err
        );


        if (
            res.headersSent
        ) {

            return next(err);
        }


        return res.status(500).json({

            success:
                false,

            message:
                "Internal server error.",

            error:
                err.message
        });
    }
);


// =====================================================
// START SERVER
// =====================================================

app.listen(
    config.port,
    "0.0.0.0",
    () => {

        console.log(
            "======================================"
        );

        console.log(
            "AI Financial Advisor Backend"
        );

        console.log(
            "======================================"
        );

        console.log(
            `Backend: http://localhost:${config.port}`
        );

        console.log(
            `AI Provider: ${config.aiProvider}`
        );

        console.log(
            `Model: ${config.ollama.model}`
        );

        console.log(
            `Keep Alive: ${config.ollama.keepAlive}`
        );

        console.log(
            `Context: ${config.ai.contextSize} tokens`
        );

        console.log(
            `Advisor Max Output: ${config.ai.advisorMaxOutput} tokens`
        );

        console.log(
            `Insight Max Output: ${config.ai.insightMaxOutput} tokens`
        );

        console.log(
            "Thinking: Disabled"
        );

        console.log(
            "======================================"
        );
    }
);