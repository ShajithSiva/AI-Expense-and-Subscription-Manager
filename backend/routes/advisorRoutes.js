const express = require("express");
const { askFinancialAdvisor } = require("../controllers/advisorController");
const firebaseAuthMiddleware = require("../middleware/firebaseAuthMiddleware");

const router = express.Router();

router.post(
    "/financial-advisor",
    firebaseAuthMiddleware,
    askFinancialAdvisor
);

module.exports = router;