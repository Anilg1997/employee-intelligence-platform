import { Routes } from '@angular/router';

import { Dashboard } from './components/dashboard/dashboard';
import { EmployeesPage } from './components/employees-page/employees-page';
import { AttritionPrediction } from './components/attrition-prediction/attrition-prediction';
import { HrAssistant } from './components/hr-assistant/hr-assistant';
import { EmployeeProfile } from './components/employee-profile/employee-profile';
import { SalaryPrediction } from './components/salary-prediction/salary-prediction';
import { PerformancePrediction } from './components/performance-prediction/performance-prediction';
import { PromotionPrediction } from './components/promotion-prediction/promotion-prediction';
import { RiskIntelligence } from './components/risk-intelligence/risk-intelligence';

export const routes: Routes = [

  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },

  {
    path: 'dashboard',
    component: Dashboard
  },

  {
    path: 'employees',
    component: EmployeesPage
  },

  {
    path: 'employees/:id',
    component: EmployeeProfile
  },

  {
    path: 'attrition-prediction',
    component: AttritionPrediction
  },
  { path: 'salary-prediction', component: SalaryPrediction },
  { path: 'performance-prediction', component: PerformancePrediction },
  { path: 'promotion-prediction', component: PromotionPrediction },
  { path: 'risk-intelligence', component: RiskIntelligence },

  {
    path: 'hr-assistant',
    component: HrAssistant
  }

];
