# Promotion-readiness demo model

## Target and provenance

`employee_data.csv` is the IBM HR synthetic dataset and contains no promotion
outcome column. This slice therefore does **not** claim to predict historical
promotions. It exposes a clearly labelled `portfolio_demo_rule_target` called
promotion readiness, derived only from current employee attributes and the
business rules below.

## Deterministic policy

Each employee gets one point for each condition: at least two years at the
company, at least two years since the last promotion, performance rating at
least 3, and job level below 5. `Ready` requires at least 3 of 4 points. The
reported `promotion_probability` is the fraction of satisfied rules (a rule
score), **not** a calibrated probability.

No fitting, train/test split, persisted learned artifact, or promotion ground
truth is used. This avoids leakage and makes the result reproducible, while
keeping the policy auditable.

## Limitations

This is a portfolio/demo visualization aid only. It omits manager review,
open roles, skills, compensation bands, succession planning, and organizational
context. IBM HR data is synthetic and the policy is an assumption. Never use
this output as a standalone employment or promotion decision.
