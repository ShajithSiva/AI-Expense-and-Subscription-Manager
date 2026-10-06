from fastapi import FastAPI
from pydantic import BaseModel
import joblib
import pandas as pd


# Load trained model
model = joblib.load("best_subscription_model.pkl")

# Load label encoder
label_encoder = joblib.load("label_encoder.pkl")


# Create FastAPI application
app = FastAPI(
    title="Subscription Usage Prediction API",
    version="1.0"
)


# Request data
class SubscriptionUsageRequest(BaseModel):
    Monthly_Cost_LKR: float
    Usage_Minutes_30D: int


# Test API
@app.get("/")
def home():
    return {
        "message": "Subscription Usage Prediction API is running"
    }


# Prediction API
@app.post("/predict-subscription-usage")
def predict_subscription_usage(
        request: SubscriptionUsageRequest
):

    input_data = pd.DataFrame([
        {
            "Monthly_Cost_LKR": request.Monthly_Cost_LKR,
            "Usage_Minutes_30D": request.Usage_Minutes_30D
        }
    ])

    prediction_encoded = model.predict(input_data)

    prediction_label = label_encoder.inverse_transform(
        prediction_encoded
    )[0]

    return {
        "prediction": prediction_label
    }