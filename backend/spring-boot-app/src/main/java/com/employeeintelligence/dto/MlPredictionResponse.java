package com.employeeintelligence.api.dto;

import java.util.List;

public class MlPredictionResponse {

    private String attrition_prediction;
    private double attrition_probability;
    private String model_version;
    private Double decision_threshold;
    private String dataset;
    private String training_provenance;
    private List<String> limitations;

    public String getAttrition_prediction() {
        return attrition_prediction;
    }

    public void setAttrition_prediction(String attrition_prediction) {
        this.attrition_prediction = attrition_prediction;
    }

    public double getAttrition_probability() {
        return attrition_probability;
    }

    public void setAttrition_probability(double attrition_probability) {
        this.attrition_probability = attrition_probability;
    }

    public String getModel_version() { return model_version; }
    public void setModel_version(String value) { this.model_version = value; }
    public Double getDecision_threshold() { return decision_threshold; }
    public void setDecision_threshold(Double value) { this.decision_threshold = value; }
    public String getDataset() { return dataset; }
    public void setDataset(String value) { this.dataset = value; }
    public String getTraining_provenance() { return training_provenance; }
    public void setTraining_provenance(String value) { this.training_provenance = value; }
    public List<String> getLimitations() { return limitations; }
    public void setLimitations(List<String> value) { this.limitations = value; }
}
