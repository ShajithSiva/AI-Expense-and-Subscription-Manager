const express = require("express");

const {
    askFinancialAdvisor
} = require("../controllers/advisorController");

const router = express.Router();


// =====================================================
// FINANCIAL ADVISOR
// =====================================================

router.post(
    "/financial-advisor",
    askFinancialAdvisor
);


module.exports = router;