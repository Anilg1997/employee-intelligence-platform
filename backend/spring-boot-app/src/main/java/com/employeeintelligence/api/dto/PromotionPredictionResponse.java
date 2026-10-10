package com.employeeintelligence.api.dto;

import java.util.List;
import java.util.Map;

public class PromotionPredictionResponse {
    private String promotion_prediction;
    private double promotion_probability;
    private double rule_score;
    private Map<String, Boolean> rule_checks;
    private String model_version;
    private String algorithm;
    private String target_type;
    private List<String> limitations;
    public String getPromotion_prediction() { return promotion_prediction; }
    public void setPromotion_prediction(String value) { promotion_prediction = value; }
    public double getPromotion_probability() { return promotion_probability; }
    public void setPromotion_probability(double value) { promotion_probability = value; }
    public double getRule_score() { return rule_score; }
    public void setRule_score(double value) { rule_score = value; }
    public Map<String, Boolean> getRule_checks() { return rule_checks; }
    public void setRule_checks(Map<String, Boolean> value) { rule_checks = value; }
    public String getModel_version() { return model_version; }
    public void setModel_version(String value) { model_version = value; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String value) { algorithm = value; }
    public String getTarget_type() { return target_type; }
    public void setTarget_type(String value) { target_type = value; }
    public List<String> getLimitations() { return limitations; }
    public void setLimitations(List<String> value) { limitations = value; }
}
