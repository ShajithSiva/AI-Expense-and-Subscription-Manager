const config = require("../config/config");

function apiKeyMiddleware(req, res, next) {
    const clientApiKey = req.header("x-api-key");

    if (!clientApiKey || clientApiKey !== config.api.clientKey) {
        return res.status(401).json({
            success: false,
            message: "Unauthorized request."
        });
    }

    next();
}

module.exports = apiKeyMiddleware;