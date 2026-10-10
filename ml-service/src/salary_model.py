"""Deterministic, leakage-safe salary regression model.

MonthlyIncome is the target and is deliberately absent from SALARY_FEATURES.
"""
from pathlib import Path
from datetime import datetime, timezone
import hashlib
import json
import numpy as np
import sklearn
import joblib
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestRegressor
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score

BASE_DIR = Path(__file__).resolve().parent.parent
DATA_PATH = BASE_DIR / "data" / "raw" / "employee_data.csv"
MODEL_PATH = BASE_DIR / "models" / "employee_salary_model.pkl"
MODEL_VERSION = "salary-regression-v2"
ALGORITHM = "random-forest-regressor"
SALARY_FEATURES = ["Age", "Department", "JobLevel", "JobRole", "TotalWorkingYears",
                   "YearsAtCompany", "Education", "PercentSalaryHike",
                   "PerformanceRating", "OverTime",
                   "StockOptionLevel"]
CATEGORICAL_FEATURES = ["Department", "JobRole", "OverTime"]
NUMERIC_FEATURES = [name for name in SALARY_FEATURES if name not in CATEGORICAL_FEATURES]


HYPERPARAMETERS = dict(n_estimators=200, random_state=42, min_samples_leaf=3, n_jobs=1)
LIMITATIONS = [
    "Synthetic IBM HR data; not representative of a real employer or market.",
    "Age remains a feature and can encode protected-characteristic bias; Gender and MaritalStatus are ignored.",
    "Not suitable for compensation decisions; no fairness or external validity assessment.",
    "Held-out metrics describe this dataset only; no accuracy guarantee or uncertainty interval.",
    "Income is in dataset units; currency is not verified.",
]


class SalaryModelUnavailable(RuntimeError):
    pass


def split_salary_data(data):
    """Split raw rows 60/20/20 before any preprocessing or fitting."""
    train, held_out = train_test_split(data, test_size=0.4, random_state=42)
    validation, test = train_test_split(held_out, test_size=0.5, random_state=43)
    return train, validation, test


def train_salary_model(output_path: Path = MODEL_PATH):
    data = pd.read_csv(DATA_PATH)
    train, validation, test = split_salary_data(data)
    preprocessor = ColumnTransformer([
        ("categorical", OneHotEncoder(handle_unknown="ignore"), CATEGORICAL_FEATURES),
        ("numeric", "passthrough", NUMERIC_FEATURES),
    ])
    model = Pipeline([
        ("preprocessor", preprocessor),
        ("regressor", RandomForestRegressor(**HYPERPARAMETERS)),
    ])
    model.fit(train[SALARY_FEATURES], train["MonthlyIncome"])
    # Fixed configuration: neither held-out partition participates in fitting/tuning.
    def metrics(rows):
        actual = rows["MonthlyIncome"]
        predicted = model.predict(rows[SALARY_FEATURES])
        return {"mae": float(mean_absolute_error(actual, predicted)),
                "rmse": float(np.sqrt(mean_squared_error(actual, predicted))),
                "r2": float(r2_score(actual, predicted))}

    metadata = {
        "model_version": MODEL_VERSION, "algorithm": ALGORITHM,
        "trained_at_utc": datetime.now(timezone.utc).isoformat(),
        "dataset_sha256": hashlib.sha256(DATA_PATH.read_bytes()).hexdigest(),
        "dataset": "IBM HR synthetic employee_data.csv", "target": "MonthlyIncome",
        "features": SALARY_FEATURES, "ignored_compatibility_fields": ["Gender", "MaritalStatus"],
        "hyperparameters": HYPERPARAMETERS, "sklearn_version": sklearn.__version__,
        "split_seeds": [42, 43],
        "counts": {"total": len(data), "train": len(train), "validation": len(validation), "test": len(test)},
        "split_row_indices": {"train": train.index.tolist(), "validation": validation.index.tolist(), "test": test.index.tolist()},
        "metrics": {"validation": metrics(validation), "test": metrics(test)},
        "limitations": LIMITATIONS,
    }
    output_path = Path(output_path)
    joblib.dump({"model": model, "metadata": metadata}, output_path)
    output_path.with_suffix(".json").write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
    return model


def load_salary_model():
    if not MODEL_PATH.exists():
        raise SalaryModelUnavailable("Salary model unavailable; run python -m src.train_salary_model explicitly.")
    try:
        artifact = joblib.load(MODEL_PATH)
        if artifact["metadata"]["model_version"] != MODEL_VERSION:
            raise ValueError("Incompatible model version")
        return artifact
    except Exception as exc:
        raise SalaryModelUnavailable("Salary model artifact is invalid or incompatible.") from exc


def predict_salary(employee_data: dict) -> dict:
    artifact = load_salary_model()
    model = artifact["model"]
    estimate = float(model.predict(pd.DataFrame([{name: employee_data[name] for name in SALARY_FEATURES}]))[0])
    return {"estimated_monthly_income": round(max(0, estimate), 2),
            "model_version": artifact["metadata"]["model_version"], "algorithm": artifact["metadata"]["algorithm"]}
