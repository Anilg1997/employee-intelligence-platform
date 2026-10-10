# Performance model

The performance vertical slice predicts `PerformanceRating` (classes 3 and 4)
from the IBM HR synthetic CSV. Raw rows are split deterministically into 60% train,
20% validation, and 20% test (seeds 42 and 43, stratified by the target) before
the one-hot encoder or classifier is fitted. `PerformanceRating` is never sent as
a feature; identifiers, constants, and `Attrition` are also excluded.

Run explicit training from `ml-service` with `python -m src.train_performance_model`.
This writes `models/employee_performance_model.pkl` and companion JSON with
dataset hash, split indices, fixed model metadata, held-out metrics, and limitations.
Inference never trains. A missing or incompatible artifact returns HTTP 503.
FastAPI exposes `POST /predict/performance`; Spring proxies it at
`POST /api/ml/predict/performance`.

This is a synthetic-data demonstration. Probabilities are model scores rather than
calibrated confidence, and the result must not be used as a standalone employment
or performance decision.
