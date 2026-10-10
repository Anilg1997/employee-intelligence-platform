"""Deterministic, leakage-safe performance-rating classification model."""
from datetime import datetime, timezone
from pathlib import Path
import hashlib
import json
import joblib
import numpy as np
import pandas as pd
import sklearn
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, balanced_accuracy_score, f1_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder

BASE_DIR = Path(__file__).resolve().parent.parent
DATA_PATH = BASE_DIR / "data" / "raw" / "employee_data.csv"
MODEL_PATH = BASE_DIR / "models" / "employee_performance_model.pkl"
MODEL_VERSION = "performance-classifier-v1"
ALGORITHM = "random-forest-classifier"
PERFORMANCE_FEATURES = ["Age", "BusinessTravel", "DailyRate", "Department", "DistanceFromHome", "Education", "EducationField", "EnvironmentSatisfaction", "Gender", "HourlyRate", "JobInvolvement", "JobLevel", "JobRole", "JobSatisfaction", "MaritalStatus", "MonthlyIncome", "MonthlyRate", "NumCompaniesWorked", "OverTime", "PercentSalaryHike", "RelationshipSatisfaction", "StockOptionLevel", "TotalWorkingYears", "TrainingTimesLastYear", "WorkLifeBalance", "YearsAtCompany", "YearsInCurrentRole", "YearsSinceLastPromotion", "YearsWithCurrManager"]
CATEGORICAL_FEATURES = ["BusinessTravel", "Department", "EducationField", "Gender", "JobRole", "MaritalStatus", "OverTime"]
NUMERIC_FEATURES = [name for name in PERFORMANCE_FEATURES if name not in CATEGORICAL_FEATURES]
HYPERPARAMETERS = dict(n_estimators=200, random_state=42, min_samples_leaf=2, n_jobs=1, class_weight="balanced")
LIMITATIONS = [
    "Synthetic IBM HR data; not representative of a real employer or performance process.",
    "The rating labels are ordinal in meaning but this model treats them as classes.",
    "Held-out metrics describe this dataset only; probabilities are not calibrated confidence.",
    "This estimate must not be used as a standalone employment or performance decision.",
]


class PerformanceModelUnavailable(RuntimeError):
    pass


def split_performance_data(data):
    train, held_out = train_test_split(data, test_size=0.4, random_state=42, stratify=data["PerformanceRating"])
    validation, test = train_test_split(held_out, test_size=0.5, random_state=43, stratify=held_out["PerformanceRating"])
    return train, validation, test


def train_performance_model(output_path: Path = MODEL_PATH):
    data = pd.read_csv(DATA_PATH)
    train, validation, test = split_performance_data(data)
    preprocessor = ColumnTransformer([
        ("categorical", OneHotEncoder(handle_unknown="ignore"), CATEGORICAL_FEATURES),
        ("numeric", "passthrough", NUMERIC_FEATURES),
    ])
    model = Pipeline([("preprocessor", preprocessor), ("classifier", RandomForestClassifier(**HYPERPARAMETERS))])
    model.fit(train[PERFORMANCE_FEATURES], train["PerformanceRating"])

    def metrics(rows):
        actual, predicted = rows["PerformanceRating"], model.predict(rows[PERFORMANCE_FEATURES])
        return {"accuracy": float(accuracy_score(actual, predicted)), "balanced_accuracy": float(balanced_accuracy_score(actual, predicted)), "f1_macro": float(f1_score(actual, predicted, average="macro"))}

    metadata = {
        "model_version": MODEL_VERSION, "algorithm": ALGORITHM,
        "trained_at_utc": datetime.now(timezone.utc).isoformat(),
        "dataset_sha256": hashlib.sha256(DATA_PATH.read_bytes()).hexdigest(),
        "dataset": "IBM HR synthetic employee_data.csv", "target": "PerformanceRating",
        "features": PERFORMANCE_FEATURES, "excluded_from_features": ["PerformanceRating", "Attrition", "EmployeeCount", "EmployeeNumber", "Over18", "StandardHours"],
        "hyperparameters": HYPERPARAMETERS, "sklearn_version": sklearn.__version__, "split_seeds": [42, 43],
        "counts": {"total": len(data), "train": len(train), "validation": len(validation), "test": len(test)},
        "split_row_indices": {"train": train.index.tolist(), "validation": validation.index.tolist(), "test": test.index.tolist()},
        "metrics": {"validation": metrics(validation), "test": metrics(test)}, "limitations": LIMITATIONS,
    }
    output_path = Path(output_path)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    joblib.dump({"model": model, "metadata": metadata}, output_path)
    output_path.with_suffix(".json").write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
    return model


def load_performance_model():
    if not MODEL_PATH.exists():
        raise PerformanceModelUnavailable("Performance model unavailable; run python -m src.train_performance_model explicitly.")
    try:
        artifact = joblib.load(MODEL_PATH)
        if artifact["metadata"]["model_version"] != MODEL_VERSION:
            raise ValueError("Incompatible model version")
        return artifact
    except Exception as exc:
        raise PerformanceModelUnavailable("Performance model artifact is invalid or incompatible.") from exc


def predict_performance(employee_data: dict) -> dict:
    artifact = load_performance_model()
    frame = pd.DataFrame([{name: employee_data[name] for name in PERFORMANCE_FEATURES}])
    model = artifact["model"]
    probabilities = model.predict_proba(frame)[0]
    classes = model.named_steps["classifier"].classes_
    index = int(np.argmax(probabilities))
    return {"performance_prediction": int(classes[index]), "performance_probability": float(probabilities[index]), "class_probabilities": {str(int(c)): float(p) for c, p in zip(classes, probabilities)}, "model_version": artifact["metadata"]["model_version"], "algorithm": artifact["metadata"]["algorithm"], "limitations": artifact["metadata"]["limitations"]}
