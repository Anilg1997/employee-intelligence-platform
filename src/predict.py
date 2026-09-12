import joblib
import pandas as pd
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent
MODEL_PATH = BASE_DIR / "models" / "employee_attrition_model.pkl"

FINAL_THRESHOLD = 0.55

def load_model():
    return joblib.load(MODEL_PATH)


def predict_attrition(employee_data):
    model = load_model()

    employee_df = pd.DataFrame([employee_data])

    probability = model.predict_proba(employee_df)[0, 1]

    prediction = "Yes" if probability >= FINAL_THRESHOLD else "No"

    return {
        "attrition_prediction": prediction,
        "attrition_probability": probability
    }