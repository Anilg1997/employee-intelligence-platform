"""Validated public salary output; also validates artifact serving metadata."""
from typing import Annotated

from pydantic import BaseModel, ConfigDict, Field, StringConstraints

NonBlank = Annotated[str, StringConstraints(pattern=r"\S")]


class StrictResponse(BaseModel):
    model_config = ConfigDict(strict=True, extra="forbid", allow_inf_nan=False)


class RegressionMetrics(StrictResponse):
    mae: float = Field(..., ge=0)
    rmse: float = Field(..., ge=0)
    # R² may legitimately be negative; it is not a probability.
    r2: float = Field(..., le=1)


class SalaryMetrics(StrictResponse):
    validation: RegressionMetrics
    test: RegressionMetrics


class SalaryPredictionResponse(StrictResponse):
    estimated_monthly_income: float = Field(..., ge=0)
    model_version: NonBlank
    algorithm: NonBlank
    dataset: NonBlank
    training_provenance: NonBlank
    metrics: SalaryMetrics
    limitations: list[NonBlank] = Field(..., min_length=1)
