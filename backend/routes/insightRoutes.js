const express = require("express");
const {
    generateFinancialInsights
} = require("../controllers/insightController");
const apiKeyMiddleware = require("../middleware/apiKeyMiddleware");

const router = express.Router();

router.post(
    "/financial-insights",
    apiKeyMiddleware,
    generateFinancialInsights
);

module.exports = router;