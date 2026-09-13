package com.employeeintelligence.api.dto;

public class MlPredictionResponse {

    private String attrition_prediction;
    private double attrition_probability;

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
}