import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))

from salary_model import MODEL_PATH, SALARY_FEATURES, predict_salary
import hashlib
import json
import joblib
import pandas as pd
import pytest
import salary_model as salary


def test_salary_model_artifact_and_contract():
    result = predict_salary({
        "Age": 35, "Department": "Research & Development", "JobLevel": 2,
        "JobRole": "Research Scientist", "TotalWorkingYears": 10,
        "YearsAtCompany": 5, "Education": 3, "PercentSalaryHike": 15,
        "PerformanceRating": 3, "Gender": "Female", "MaritalStatus": "Single",
        "OverTime": "No", "StockOptionLevel": 1,
    })
    assert MODEL_PATH.exists()
    assert result["estimated_monthly_income"] > 0
    assert result["model_version"] == "salary-regression-v2"
    assert result["algorithm"] == "random-forest-regressor"


def test_target_is_not_a_salary_feature():
    assert "MonthlyIncome" not in SALARY_FEATURES
    assert not {"Gender", "MaritalStatus"} & set(SALARY_FEATURES)


def test_raw_splits_are_disjoint_complete_and_reproducible():
    data = pd.read_csv(salary.DATA_PATH)
    splits = salary.split_salary_data(data)
    repeated = salary.split_salary_data(data)
    assert [len(s) for s in splits] == [882, 294, 294]
    indices = [set(s.index) for s in splits]
    assert not indices[0] & indices[1]
    assert not indices[0] & indices[2]
    assert not indices[1] & indices[2]
    assert set.union(*indices) == set(data.index)
    for left, right in zip(splits, repeated):
        pd.testing.assert_frame_equal(left, right)


def test_missing_model_never_trains_or_writes(tmp_path, monkeypatch):
    path = tmp_path / "missing.pkl"
    monkeypatch.setattr(salary, "MODEL_PATH", path)
    monkeypatch.setattr(salary, "train_salary_model", lambda: pytest.fail("Inference must not train"))
    with pytest.raises(salary.SalaryModelUnavailable):
        salary.load_salary_model()
    assert not path.exists()


def test_training_fits_only_training_partition_and_persists_evidence(tmp_path, monkeypatch):
    data = pd.read_csv(salary.DATA_PATH)
    train, validation, test = salary.split_salary_data(data)
    original_fit = salary.Pipeline.fit
    fitted_indices = []

    def guarded_fit(self, X, y, **kwargs):
        fitted_indices.append(set(X.index))
        assert list(X.columns) == SALARY_FEATURES
        assert set(X.index) == set(train.index)
        return original_fit(self, X, y, **kwargs)

    monkeypatch.setattr(salary.Pipeline, "fit", guarded_fit)
    path = tmp_path / "salary.pkl"
    model = salary.train_salary_model(path)
    metadata = json.loads(path.with_suffix(".json").read_text())
    assert fitted_indices == [set(train.index)]
    assert metadata == joblib.load(path)["metadata"]
    assert metadata["dataset_sha256"] == hashlib.sha256(salary.DATA_PATH.read_bytes()).hexdigest()
    assert metadata["counts"] == {"total": 1470, "train": 882, "validation": 294, "test": 294}
    assert metadata["features"] == SALARY_FEATURES
    assert metadata["hyperparameters"] == salary.HYPERPARAMETERS
    assert metadata["trained_at_utc"] and metadata["limitations"]
    for name, rows in [("validation", validation), ("test", test)]:
        predictions = model.predict(rows[SALARY_FEATURES])
        assert metadata["metrics"][name]["mae"] == pytest.approx(salary.mean_absolute_error(rows.MonthlyIncome, predictions))
        assert metadata["metrics"][name]["rmse"] == pytest.approx(salary.np.sqrt(salary.mean_squared_error(rows.MonthlyIncome, predictions)))
        assert metadata["metrics"][name]["r2"] == pytest.approx(salary.r2_score(rows.MonthlyIncome, predictions))
    # Encoder categories were learned from training rows only.
    encoder = model.named_steps["preprocessor"].named_transformers_["categorical"]
    for column, categories in zip(salary.CATEGORICAL_FEATURES, encoder.categories_):
        assert set(categories) == set(train[column])


def test_missing_artifact_returns_service_unavailable(tmp_path, monkeypatch):
    sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
    from src import salary_model
    from src.api import app
    from fastapi.testclient import TestClient
    monkeypatch.setattr(salary_model, "MODEL_PATH", tmp_path / "missing.pkl")
    response = TestClient(app).post("/predict/salary", json={
        "Age": 35, "Department": "Sales", "JobLevel": 2, "JobRole": "Sales Executive",
        "TotalWorkingYears": 10, "YearsAtCompany": 5, "Education": 3,
        "PercentSalaryHike": 15, "PerformanceRating": 3, "Gender": "Female",
        "MaritalStatus": "Single", "OverTime": "No", "StockOptionLevel": 1,
    })
    assert response.status_code == 503
