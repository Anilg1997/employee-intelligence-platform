package com.employeeintelligence.api.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Shared create/update payload. Nulls remain valid for partial demo records. */
public record EmployeeRequest(
        Long id,
        @Size(max = 255) String name,
        @Size(max = 255) String department,
        @Size(max = 255) String jobRole,
        @PositiveOrZero Integer age,
        @Size(max = 255) String attrition,
        @Size(max = 255) String businessTravel,
        @PositiveOrZero Integer dailyRate,
        @PositiveOrZero Integer distanceFromHome,
        @PositiveOrZero Integer education,
        @Size(max = 255) String educationField,
        @PositiveOrZero Integer employeeCount,
        @PositiveOrZero Integer employeeNumber,
        @PositiveOrZero Integer environmentSatisfaction,
        @Size(max = 255) String gender,
        @PositiveOrZero Integer hourlyRate,
        @PositiveOrZero Integer jobInvolvement,
        @PositiveOrZero Integer jobLevel,
        @PositiveOrZero Integer jobSatisfaction,
        @Size(max = 255) String maritalStatus,
        @PositiveOrZero Integer monthlyIncome,
        @PositiveOrZero Integer monthlyRate,
        @PositiveOrZero Integer numCompaniesWorked,
        @Size(max = 255) String over18,
        @Size(max = 255) String overTime,
        @PositiveOrZero Integer percentSalaryHike,
        @PositiveOrZero Integer performanceRating,
        @PositiveOrZero Integer relationshipSatisfaction,
        @PositiveOrZero Integer standardHours,
        @PositiveOrZero Integer stockOptionLevel,
        @PositiveOrZero Integer totalWorkingYears,
        @PositiveOrZero Integer trainingTimesLastYear,
        @PositiveOrZero Integer workLifeBalance,
        @PositiveOrZero Integer yearsAtCompany,
        @PositiveOrZero Integer yearsInCurrentRole,
        @PositiveOrZero Integer yearsSinceLastPromotion,
        @PositiveOrZero Integer yearsWithCurrManager) {
}
