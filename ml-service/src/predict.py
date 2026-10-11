import pandas as pd
from pathlib import Path

if __package__:
    from .json_models import load_artifact
else:
    from json_models import load_artifact

BASE_DIR = Path(__file__).resolve().parent.parent

MODEL_PATH = BASE_DIR / "models" / "employee_attrition_model.json"

# Load both weights and decision threshold from the single JSON artifact.
artifact = load_artifact(MODEL_PATH)
FINAL_THRESHOLD = float(artifact["metadata"]["threshold"])
model = artifact["model"]


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
