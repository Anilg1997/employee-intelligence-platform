import { Employee } from '../models/employee';

type BusinessTravel = 'Travel_Rarely' | 'Travel_Frequently' | 'Non-Travel';
type Department = 'Sales' | 'Research & Development' | 'Human Resources';
type EducationField =
  | 'Life Sciences'
  | 'Medical'
  | 'Marketing'
  | 'Technical Degree'
  | 'Human Resources'
  | 'Other';
type Gender = 'Male' | 'Female';
type JobRole =
  | 'Sales Executive'
  | 'Research Scientist'
  | 'Laboratory Technician'
  | 'Manufacturing Director'
  | 'Healthcare Representative'
  | 'Manager'
  | 'Sales Representative'
  | 'Research Director'
  | 'Human Resources';
type MaritalStatus = 'Single' | 'Married' | 'Divorced';
type OverTime = 'Yes' | 'No';

export interface EmployeePredictionFeatures extends Record<string, string | number> {
  Age: number;
  BusinessTravel: BusinessTravel;
  DailyRate: number;
  Department: Department;
  DistanceFromHome: number;
  Education: number;
  EducationField: EducationField;
  EnvironmentSatisfaction: number;
  Gender: Gender;
  HourlyRate: number;
  JobInvolvement: number;
  JobLevel: number;
  JobRole: JobRole;
  JobSatisfaction: number;
  MaritalStatus: MaritalStatus;
  MonthlyIncome: number;
  MonthlyRate: number;
  NumCompaniesWorked: number;
  OverTime: OverTime;
  PercentSalaryHike: number;
  RelationshipSatisfaction: number;
  StockOptionLevel: number;
  TotalWorkingYears: number;
  TrainingTimesLastYear: number;
  WorkLifeBalance: number;
  YearsAtCompany: number;
  YearsInCurrentRole: number;
  YearsSinceLastPromotion: number;
  YearsWithCurrManager: number;
}

export interface AttritionPredictionFeatures extends EmployeePredictionFeatures {
  PerformanceRating: number;
}

export interface SalaryPredictionFeatures {
  Age: number;
  Department: Department;
  JobLevel: number;
  JobRole: JobRole;
  TotalWorkingYears: number;
  YearsAtCompany: number;
  Education: number;
  PercentSalaryHike: number;
  PerformanceRating: number;
  Gender: Gender;
  MaritalStatus: MaritalStatus;
  OverTime: OverTime;
  StockOptionLevel: number;
}

function numberFeature(
  name: string,
  value: unknown,
  minimum: number,
  maximum: number
): number {
  if (
    typeof value !== 'number' ||
    !Number.isInteger(value) ||
    value < minimum ||
    value > maximum
  ) {
    throw new Error(
      `${name} must be an integer between ${minimum} and ${maximum}. Check the employee source data.`
    );
  }

  return value;
}

function enumFeature<T extends string>(
  name: string,
  value: unknown,
  options: readonly T[]
): T {
  if (typeof value !== 'string' || value.trim() === '') {
    throw new Error(`${name} is missing. Check the employee source data.`);
  }

  const normalized = value.trim();
  const match = options.find(option => option === normalized);

  if (match === undefined) {
    throw new Error(
      `${name} is not supported by the prediction model. Check the employee source data.`
    );
  }

  return match;
}

const departments = [
  'Sales',
  'Research & Development',
  'Human Resources'
] as const;
const jobRoles = [
  'Sales Executive',
  'Research Scientist',
  'Laboratory Technician',
  'Manufacturing Director',
  'Healthcare Representative',
  'Manager',
  'Sales Representative',
  'Research Director',
  'Human Resources'
] as const;

function commonFeatures(employee: Employee): EmployeePredictionFeatures {
  return {
    Age: numberFeature('Age', employee.age, 18, 60),
    BusinessTravel: enumFeature('BusinessTravel', employee.businessTravel, [
      'Travel_Rarely',
      'Travel_Frequently',
      'Non-Travel'
    ]),
    DailyRate: numberFeature('DailyRate', employee.dailyRate, 102, 1499),
    Department: enumFeature('Department', employee.department, departments),
    DistanceFromHome: numberFeature('DistanceFromHome', employee.distanceFromHome, 1, 29),
    Education: numberFeature('Education', employee.education, 1, 5),
    EducationField: enumFeature('EducationField', employee.educationField, [
      'Life Sciences',
      'Medical',
      'Marketing',
      'Technical Degree',
      'Human Resources',
      'Other'
    ]),
    EnvironmentSatisfaction: numberFeature(
      'EnvironmentSatisfaction',
      employee.environmentSatisfaction,
      1,
      4
    ),
    Gender: enumFeature('Gender', employee.gender, ['Male', 'Female']),
    HourlyRate: numberFeature('HourlyRate', employee.hourlyRate, 30, 100),
    JobInvolvement: numberFeature('JobInvolvement', employee.jobInvolvement, 1, 4),
    JobLevel: numberFeature('JobLevel', employee.jobLevel, 1, 5),
    JobRole: enumFeature('JobRole', employee.jobRole, jobRoles),
    JobSatisfaction: numberFeature('JobSatisfaction', employee.jobSatisfaction, 1, 4),
    MaritalStatus: enumFeature('MaritalStatus', employee.maritalStatus, [
      'Single',
      'Married',
      'Divorced'
    ]),
    MonthlyIncome: numberFeature('MonthlyIncome', employee.monthlyIncome, 1009, 19999),
    MonthlyRate: numberFeature('MonthlyRate', employee.monthlyRate, 2094, 26999),
    NumCompaniesWorked: numberFeature('NumCompaniesWorked', employee.numCompaniesWorked, 0, 9),
    OverTime: enumFeature('OverTime', employee.overTime, ['Yes', 'No']),
    PercentSalaryHike: numberFeature('PercentSalaryHike', employee.percentSalaryHike, 11, 25),
    RelationshipSatisfaction: numberFeature(
      'RelationshipSatisfaction',
      employee.relationshipSatisfaction,
      1,
      4
    ),
    StockOptionLevel: numberFeature('StockOptionLevel', employee.stockOptionLevel, 0, 3),
    TotalWorkingYears: numberFeature('TotalWorkingYears', employee.totalWorkingYears, 0, 40),
    TrainingTimesLastYear: numberFeature('TrainingTimesLastYear', employee.trainingTimesLastYear, 0, 6),
    WorkLifeBalance: numberFeature('WorkLifeBalance', employee.workLifeBalance, 1, 4),
    YearsAtCompany: numberFeature('YearsAtCompany', employee.yearsAtCompany, 0, 40),
    YearsInCurrentRole: numberFeature('YearsInCurrentRole', employee.yearsInCurrentRole, 0, 18),
    YearsSinceLastPromotion: numberFeature(
      'YearsSinceLastPromotion',
      employee.yearsSinceLastPromotion,
      0,
      15
    ),
    YearsWithCurrManager: numberFeature('YearsWithCurrManager', employee.yearsWithCurrManager, 0, 17)
  };
}

export function toAttritionPredictionFeatures(
  employee: Employee
): AttritionPredictionFeatures {
  return {
    ...commonFeatures(employee),
    PerformanceRating: numberFeature('PerformanceRating', employee.performanceRating, 1, 4)
  };
}

export function toPerformancePredictionFeatures(
  employee: Employee
): EmployeePredictionFeatures {
  return commonFeatures(employee);
}

export function toPromotionPredictionFeatures(
  employee: Employee
): AttritionPredictionFeatures {
  return toAttritionPredictionFeatures(employee);
}

export function toSalaryPredictionFeatures(
  employee: Employee
): SalaryPredictionFeatures {
  return {
    Age: numberFeature('Age', employee.age, 18, 60),
    Department: enumFeature('Department', employee.department, departments),
    JobLevel: numberFeature('JobLevel', employee.jobLevel, 1, 5),
    JobRole: enumFeature('JobRole', employee.jobRole, jobRoles),
    TotalWorkingYears: numberFeature('TotalWorkingYears', employee.totalWorkingYears, 0, 40),
    YearsAtCompany: numberFeature('YearsAtCompany', employee.yearsAtCompany, 0, 40),
    Education: numberFeature('Education', employee.education, 1, 5),
    PercentSalaryHike: numberFeature('PercentSalaryHike', employee.percentSalaryHike, 11, 25),
    PerformanceRating: numberFeature('PerformanceRating', employee.performanceRating, 1, 4),
    Gender: enumFeature('Gender', employee.gender, ['Male', 'Female']),
    MaritalStatus: enumFeature('MaritalStatus', employee.maritalStatus, [
      'Single',
      'Married',
      'Divorced'
    ]),
    OverTime: enumFeature('OverTime', employee.overTime, ['Yes', 'No']),
    StockOptionLevel: numberFeature('StockOptionLevel', employee.stockOptionLevel, 0, 3)
  };
}
