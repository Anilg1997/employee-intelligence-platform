# Risk engine

The read-only `GET /api/ml/models` registry records the active model name, version,
algorithm, status, metrics where available, and limitations. `GET
/api/ai/employees/{id}/risk-summary` composes the existing attrition probability,
performance prediction, and promotion-readiness rule score.

The summary's `performanceRisk` is explicitly derived as `1 - class 4 score` (or
the existing prediction fallback), not a calibrated probability. `overallRisk` is
the deterministic equal-weight mean of attrition risk, performance risk, and
`1 - promotionReadiness`. It is a prioritization aid only; scores are not
certainty and must not be used as a standalone employment decision.
