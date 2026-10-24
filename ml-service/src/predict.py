import pandas as pd
from pathlib import Path
from functools import lru_cache

if __package__:
    from .json_models import load_artifact
else:
    from json_models import load_artifact

BASE_DIR = Path(__file__).resolve().parent.parent

MODEL_PATH = BASE_DIR / "models" / "employee_attrition_model.json"


class ModelUnavailable(RuntimeError):
    """Raised when a prediction artifact cannot be safely loaded."""


@lru_cache(maxsize=4)
def _load_attrition_model(path):
    """Load the attrition artifact on first use, never during module import."""
    try:
        artifact = load_artifact(Path(path))
        threshold = float(artifact["metadata"]["threshold"])
        if not 0 <= threshold <= 1:
            raise ValueError("invalid threshold")
        metadata = artifact["metadata"]
        required = ("model_version", "dataset", "training_provenance", "limitations")
        if (any(not isinstance(metadata.get(key), str) or not metadata[key].strip()
                for key in required[:3])
                or not isinstance(metadata.get("limitations"), list)
                or any(not isinstance(item, str) or not item.strip() for item in metadata["limitations"])):
            raise ValueError("invalid model metadata")
        return artifact["model"], threshold, metadata
    except Exception as exc:
        raise ModelUnavailable("Attrition model unavailable.") from exc


def load_attrition_model():
    return _load_attrition_model(str(MODEL_PATH))


def predict_attrition(employee_data):
    """
    Predict employee attrition risk.

    Parameters:
        employee_data (dict): Employee information.

    Returns:
        dict: Prediction and attrition probability.
    """

    model, final_threshold, metadata = load_attrition_model()
    employee_df = pd.DataFrame([employee_data])
    try:
        probabilities = model.predict_proba(employee_df)[0]
        yes_class_index = list(model.classes_).index("Yes")
    except (ValueError, IndexError, KeyError) as exc:
        raise ModelUnavailable("Attrition model could not score this request.") from exc

    probability = float(probabilities[yes_class_index])

    prediction = (
        "Yes"
        if probability >= final_threshold
        else "No"
    )

    return {
        "attrition_prediction": prediction,
        "attrition_probability": round(probability, 4),
        "model_version": metadata.get("model_version"),
        "decision_threshold": final_threshold,
        "dataset": metadata.get("dataset"),
        "training_provenance": metadata.get("training_provenance"),
        "limitations": metadata.get("limitations", [])
    }
