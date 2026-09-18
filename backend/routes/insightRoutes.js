const express = require("express");
const {
    generateFinancialInsights
} = require("../controllers/insightController");
const firebaseAuthMiddleware = require("../middleware/firebaseAuthMiddleware");

const router = express.Router();

router.post(
    "/financial-insights",
    firebaseAuthMiddleware,
    generateFinancialInsights
);

module.exports = router;