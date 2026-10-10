package com.employeeintelligence.api.dto;
import java.util.List;
import java.util.Map;
public class PerformancePredictionResponse {
    private int performance_prediction; private double performance_probability; private Map<String, Double> class_probabilities; private String model_version; private String algorithm; private List<String> limitations;
    public int getPerformance_prediction() { return performance_prediction; } public void setPerformance_prediction(int v) { performance_prediction = v; }
    public double getPerformance_probability() { return performance_probability; } public void setPerformance_probability(double v) { performance_probability = v; }
    public Map<String, Double> getClass_probabilities() { return class_probabilities; } public void setClass_probabilities(Map<String, Double> v) { class_probabilities = v; }
    public String getModel_version() { return model_version; } public void setModel_version(String v) { model_version = v; }
    public String getAlgorithm() { return algorithm; } public void setAlgorithm(String v) { algorithm = v; }
    public List<String> getLimitations() { return limitations; } public void setLimitations(List<String> v) { limitations = v; }
}
