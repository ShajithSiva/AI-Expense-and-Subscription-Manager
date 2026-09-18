const express = require("express");
const { askFinancialAdvisor } = require("../controllers/advisorController");
const apiKeyMiddleware = require("../middleware/apiKeyMiddleware");

const router = express.Router();

router.post(
    "/financial-advisor",
    apiKeyMiddleware,
    askFinancialAdvisor
);

module.exports = router;