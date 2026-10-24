import {
  toAttritionPredictionFeatures,
  toPerformancePredictionFeatures,
  toPromotionPredictionFeatures,
  toSalaryPredictionFeatures
} from './prediction-features';
import { Employee } from '../models/employee';

describe('employee prediction feature mapping', () => {
  const employee: Employee = {
    name: 'Test employee',
    age: 35,
    businessTravel: 'Travel_Rarely',
    dailyRate: 500,
    department: 'Research & Development',
    distanceFromHome: 10,
    education: 3,
    educationField: 'Life Sciences',
    environmentSatisfaction: 3,
    gender: 'Female',
    hourlyRate: 60,
    jobInvolvement: 3,
    jobLevel: 1,
    jobRole: 'Research Scientist',
    jobSatisfaction: 3,
    maritalStatus: 'Married',
    monthlyIncome: 3000,
    monthlyRate: 14000,
    numCompaniesWorked: 1,
    overTime: 'No',
    percentSalaryHike: 15,
    performanceRating: 3,
    relationshipSatisfaction: 3,
    stockOptionLevel: 0,
    totalWorkingYears: 5,
    trainingTimesLastYear: 3,
    workLifeBalance: 3,
    yearsAtCompany: 1,
    yearsInCurrentRole: 1,
    yearsSinceLastPromotion: 0,
    yearsWithCurrManager: 1
  };

  it('maps only the fields required by each model contract', () => {
    expect(toAttritionPredictionFeatures(employee).PerformanceRating).toBe(3);
    expect(toPerformancePredictionFeatures(employee)).not.toHaveProperty('PerformanceRating');
    expect(toPromotionPredictionFeatures(employee).PerformanceRating).toBe(3);
    expect(toSalaryPredictionFeatures(employee)).toEqual({
      Age: 35,
      Department: 'Research & Development',
      JobLevel: 1,
      JobRole: 'Research Scientist',
      TotalWorkingYears: 5,
      YearsAtCompany: 1,
      Education: 3,
      PercentSalaryHike: 15,
      PerformanceRating: 3,
      Gender: 'Female',
      MaritalStatus: 'Married',
      OverTime: 'No',
      StockOptionLevel: 0
    });
  });

  it('rejects missing model features instead of inventing defaults', () => {
    expect(() => toPerformancePredictionFeatures({
      ...employee,
      dailyRate: undefined
    })).toThrow('DailyRate must be an integer');
  });

  it('rejects unsupported enum values and out-of-range values', () => {
    expect(() => toAttritionPredictionFeatures({
      ...employee,
      department: 'Engineering'
    })).toThrow('Department is not supported');
    expect(() => toSalaryPredictionFeatures({
      ...employee,
      stockOptionLevel: 4
    })).toThrow('StockOptionLevel must be an integer between 0 and 3');
  });
});
