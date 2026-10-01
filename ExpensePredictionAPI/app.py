from flask import Flask, request, jsonify
import pandas as pd
import joblib

app = Flask(__name__)

MODEL_PATH = "ExpensePrediction_LightGBM.pkl"

# Load trained LightGBM model
model = joblib.load(MODEL_PATH)

FEATURES = [
    "Year",
    "Month",
    "Monthly_Income",
    "Bills",
    "Education",
    "Entertainment",
    "Food",
    "Health",
    "Shopping",
    "Transport",
    "Travel",
    "Total_Expense",
    "Savings",
    "Savings_Rate",
    "Expense_Ratio",
    "Average_Daily_Expense",
    "Weekend_Spending_Percentage",
    "Transaction_Count",
    "Spending_Profile"
]


@app.route("/", methods=["GET"])
def home():

    return jsonify({
        "status": "success",
        "service": "Expense Prediction API",
        "model": "LightGBM",
        "target": "NextMonthExpense"
    })


@app.route("/predict-expense", methods=["POST"])
def predict_expense():

    try:

        data = request.get_json()

        if data is None:

            return jsonify({
                "success": False,
                "error": "Request body is empty."
            }), 400


        # Check required features
        missing_features = [
            feature
            for feature in FEATURES
            if feature not in data
        ]


        if missing_features:

            return jsonify({
                "success": False,
                "error": "Missing required features.",
                "missingFeatures": missing_features
            }), 400


        # Create dataframe
        input_data = {
            feature: [data[feature]]
            for feature in FEATURES
        }

        input_df = pd.DataFrame(input_data)


        # Convert categorical feature
        input_df["Spending_Profile"] = (
            input_df["Spending_Profile"].astype("category")
        )


        # Make prediction
        prediction = model.predict(input_df)

        predicted_expense = float(prediction[0])


        return jsonify({

            "success": True,

            "prediction": {

                "target": "NextMonthExpense",

                "predictedNextMonthExpense": predicted_expense

            }

        })


    except Exception as e:

        return jsonify({

            "success": False,

            "error": str(e)

        }), 500


if __name__ == "__main__":
    import os

    port = int(os.environ.get("PORT", 5000))

    app.run(
        host="0.0.0.0",
        port=port
    )