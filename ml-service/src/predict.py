import joblib
import pandas as pd
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent.parent

MODEL_PATH = BASE_DIR / "models" / "employee_attrition_model.pkl"

FINAL_THRESHOLD = 0.55


# Load the trained model
model = joblib.load(MODEL_PATH)


def predict_attrition(employee_data):
    """
    Predict employee attrition risk.

    Parameters:
        employee_data (dict): Employee information.

    Returns:
        dict: Prediction and attrition probability.
    """

    employee_df = pd.DataFrame([employee_data])

    probabilities = model.predict_proba(employee_df)[0]

    # Find the probability corresponding to "Yes"
    yes_class_index = list(model.classes_).index("Yes")

    probability = float(probabilities[yes_class_index])

    prediction = (
        "Yes"
        if probability >= FINAL_THRESHOLD
        else "No"
    )

    return {
        "attrition_prediction": prediction,
        "attrition_probability": round(probability, 4)
    }