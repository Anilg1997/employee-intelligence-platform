export interface PerformancePredictionRequest { [key: string]: string | number; }
export interface PerformancePredictionResponse {
  performance_prediction: number;
  performance_probability: number;
  class_probabilities: Record<string, number>;
  model_version: string;
  algorithm: string;
  dataset?: string;
  training_provenance?: string;
  metrics?: Record<string, number | string | Record<string, number>>;
  limitations: string[];
}
