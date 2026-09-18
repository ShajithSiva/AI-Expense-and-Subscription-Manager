const express = require("express");

const {
    generateFinancialInsights
} = require("../controllers/insightController");

const router = express.Router();


// =====================================================
// FINANCIAL INSIGHTS
// =====================================================

router.post(
    "/financial-insights",
    generateFinancialInsights
);


module.exports = router;