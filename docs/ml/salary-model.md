# Salary regression: experimental model v2

## Training and evaluation

From `ml-service/`, run `python -m src.train_salary_model` explicitly to generate
`models/employee_salary_model.pkl` and `models/employee_salary_model.json`.
The JSON and embedded artifact metadata record the dataset SHA256, UTC training
timestamp, scikit-learn version, feature allowlist, hyperparameters, row counts,
partition row indices, metrics, and limitations. Reproduction requires the same
dataset and dependency versions; timestamps intentionally change on retraining.

The target is `MonthlyIncome` from the repository's synthetic IBM HR dataset.
Raw rows are split before fitting any preprocessing:

- Train: 882 rows (60%).
- Validation: 294 rows (20%).
- Final test: 294 rows (20%).

`train_test_split` first holds out 40% with seed 42, then divides that holdout
equally with seed 43. One-hot encoding and the forest fit only the training
partition. Hyperparameters are fixed in source: 200 trees, minimum leaf size 3,
random seed 42, one worker, other scikit-learn defaults. There is no hyperparameter
search, test tuning, or refit on the full dataset. Validation and final test are
evaluated separately against the same training-only model.

Actual regenerated artifact results (rounded for display):

| Partition | MAE | RMSE | R² |
| --- | ---: | ---: | ---: |
| Validation | 854.18 | 1167.63 | 0.941156 |
| Final test | 840.65 | 1123.93 | 0.933409 |

MAE and RMSE use MonthlyIncome dataset units. Currency is not independently
verified. R² is not percentage accuracy. These metrics describe only this
synthetic dataset, not real-world compensation validity.

## Features and limitations

Features: Age, Department, JobLevel, JobRole, TotalWorkingYears, YearsAtCompany,
Education, PercentSalaryHike, PerformanceRating, OverTime, StockOptionLevel.
MonthlyIncome is never an input; Attrition and employee identifiers are excluded.

The initial v1 used protected-characteristic fields Gender, MaritalStatus, and
Age. In v2, Gender and MaritalStatus remain accepted by the existing request
schema for compatibility but are ignored by feature selection. Age remains used
and may encode protected-characteristic bias. Removing two fields does not remove
proxy bias. No fairness or external validation has been performed.

This synthetic IBM sample does not establish market salary levels, causal pay
drivers, or equitable compensation. **This model is not suitable for compensation
decisions.** It has no validated prediction interval or production accuracy claim.

## Serving

Inference never trains or writes artifacts. Missing, corrupt, or incompatible
salary artifacts raise a controlled unavailable error; `/predict/salary` returns
HTTP 503. The operator must explicitly train before serving salary predictions.
The response retains its existing fields, with version `salary-regression-v2`.

Checks: `python -m pytest -q` from `ml-service/`. Tests protect disjoint,
reproducible splits, training-only fitting, encoder categories, persisted metadata
and recomputed metrics, and missing-artifact behavior including HTTP 503.
