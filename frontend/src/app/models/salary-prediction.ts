export interface SalaryPredictionRequest {
  Age: number; Department: string; JobLevel: number; JobRole: string;
  TotalWorkingYears: number; YearsAtCompany: number; Education: number;
  PercentSalaryHike: number; PerformanceRating: number; Gender: string;
  MaritalStatus: string; OverTime: string; StockOptionLevel: number;
}
export interface SalaryPredictionResponse {
  estimated_monthly_income: number; model_version: string; algorithm: string;
  dataset?: string; training_provenance?: string; limitations?: string[];
  metrics?: Record<string, number | string | Record<string, number>>;
}
