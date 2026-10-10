package com.employeeintelligence.api.dto;
public class SalaryPredictionResponse {
    private double estimated_monthly_income; private String model_version; private String algorithm;
    public double getEstimated_monthly_income(){return estimated_monthly_income;} public void setEstimated_monthly_income(double v){estimated_monthly_income=v;}
    public String getModel_version(){return model_version;} public void setModel_version(String v){model_version=v;} public String getAlgorithm(){return algorithm;} public void setAlgorithm(String v){algorithm=v;}
}
