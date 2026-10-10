export interface PromotionPredictionRequest { [key: string]: string | number; }
export interface PromotionPredictionResponse {
  promotion_prediction: string;
  promotion_probability: number;
  rule_score: number;
  rule_checks: Record<string, boolean>;
  model_version: string;
  algorithm: string;
  target_type: string;
  limitations: string[];
}
