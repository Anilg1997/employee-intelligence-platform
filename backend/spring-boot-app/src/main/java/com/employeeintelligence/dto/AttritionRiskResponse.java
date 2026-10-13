package com.employeeintelligence.api.dto;

public class AttritionRiskResponse {

    private Long employeeId;
    private String prediction;
    private double probability;
    private String riskLevel;
    private String modelVersion;
    private String dataset;
    private String trainingProvenance;
    private java.util.List<String> limitations;

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

    public AttritionRiskResponse(
            Long employeeId,
            String prediction,
            double probability,
            String riskLevel,
            String modelVersion,
            String dataset,
            String trainingProvenance,
            java.util.List<String> limitations) {
        this(employeeId, prediction, probability, riskLevel);
        this.modelVersion = modelVersion;
        this.dataset = dataset;
        this.trainingProvenance = trainingProvenance;
        this.limitations = limitations == null ? java.util.List.of() : java.util.List.copyOf(limitations);
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

    public String getModelVersion() { return modelVersion; }
    public String getDataset() { return dataset; }
    public String getTrainingProvenance() { return trainingProvenance; }
    public java.util.List<String> getLimitations() { return limitations; }
}
