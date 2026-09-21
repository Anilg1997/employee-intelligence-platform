package com.employeeintelligence.api.dto;

public class AttritionRiskResponse {

    private Long employeeId;
    private String prediction;
    private double probability;
    private String riskLevel;

    public AttritionRiskResponse(
            Long employeeId,
            String prediction,
            double probability,
            String riskLevel) {

        this.employeeId = employeeId;
        this.prediction = prediction;
        this.probability = probability;
        this.riskLevel = riskLevel;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getPrediction() {
        return prediction;
    }

    public double getProbability() {
        return probability;
    }

    public String getRiskLevel() {
        return riskLevel;
    }
}