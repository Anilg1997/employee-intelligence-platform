package com.employeeintelligence.api.dto;
import java.util.List;
import java.util.Map;
public class SalaryPredictionResponse {
    private Double estimated_monthly_income; private String model_version; private String algorithm;
    private String dataset; private String training_provenance;
    private Map<String, Map<String, Double>> metrics; private List<String> limitations;
    public Double getEstimated_monthly_income(){return estimated_monthly_income;} public void setEstimated_monthly_income(Double v){estimated_monthly_income=v;}
    public String getModel_version(){return model_version;} public void setModel_version(String v){model_version=v;} public String getAlgorithm(){return algorithm;} public void setAlgorithm(String v){algorithm=v;}
    public String getDataset(){return dataset;} public void setDataset(String v){dataset=v;}
    public String getTraining_provenance(){return training_provenance;} public void setTraining_provenance(String v){training_provenance=v;}
    public Map<String, Map<String, Double>> getMetrics(){return metrics;} public void setMetrics(Map<String, Map<String, Double>> v){metrics=v;}
    public List<String> getLimitations(){return limitations;} public void setLimitations(List<String> v){limitations=v;}
}
