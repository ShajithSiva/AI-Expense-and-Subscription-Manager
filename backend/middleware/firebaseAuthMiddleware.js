const { getAuth } = require("firebase-admin/auth");
const firebaseApp = require("../config/firebaseAdmin");

const auth = getAuth(firebaseApp);

async function firebaseAuthMiddleware(req, res, next) {
    try {
        const authHeader = req.headers.authorization;

        if (!authHeader || !authHeader.startsWith("Bearer ")) {
            return res.status(401).json({
                success: false,
                message: "Authentication required."
            });
        }

        const idToken = authHeader.substring(7);

        const decodedToken = await auth.verifyIdToken(idToken);

        req.user = decodedToken;

        next();
    } catch (error) {
        console.error(
            "Firebase authentication error:",
            error.message
        );

        return res.status(401).json({
            success: false,
            message: "Invalid or expired authentication token."
        });
    }
}

module.exports = firebaseAuthMiddleware;