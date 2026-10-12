export interface MlPredictionResponse {
  attrition_prediction: string;
  attrition_probability: number;
  model_version?: string;
  decision_threshold?: number;
  dataset?: string;
  training_provenance?: string;
  limitations?: string[];
}
