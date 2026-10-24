
import { Routes } from '@angular/router';

import { authGuard } from './guards/auth.guard';
import { ROLE_POLICY } from './services/role-policy.service';

export const routes: Routes = [
  {
    path: 'access-required',
    loadComponent: () =>
      import('./components/access-required/access-required').then(module => module.AccessRequired)
  },
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./components/dashboard/dashboard').then(module => module.Dashboard),
    canActivate: [authGuard]
  },
  {
    path: 'employees/:id',
    loadComponent: () =>
      import('./components/employee-profile/employee-profile').then(module => module.EmployeeProfile),
    canActivate: [authGuard]
  },
  {
    path: 'employees',
    loadComponent: () =>
      import('./components/employees-page/employees-page').then(module => module.EmployeesPage),
    canActivate: [authGuard]
  },
  {
    path: 'attrition-prediction',
    loadComponent: () =>
      import('./components/attrition-prediction/attrition-prediction').then(module => module.AttritionPrediction),
    canActivate: [authGuard]
  },
  {
    path: 'salary-prediction',
    loadComponent: () =>
      import('./components/salary-prediction/salary-prediction').then(module => module.SalaryPrediction),
    canActivate: [authGuard],
    data: { roles: ROLE_POLICY.sensitiveInsights }
  },
  {
    path: 'performance-prediction',
    loadComponent: () =>
      import('./components/performance-prediction/performance-prediction').then(module => module.PerformancePrediction),
    canActivate: [authGuard],
    data: { roles: ROLE_POLICY.sensitiveInsights }
  },
  {
    path: 'promotion-prediction',
    loadComponent: () =>
      import('./components/promotion-prediction/promotion-prediction').then(module => module.PromotionPrediction),
    canActivate: [authGuard],
    data: { roles: ROLE_POLICY.sensitiveInsights }
  },
  {
    path: 'risk-intelligence',
    loadComponent: () =>
      import('./components/risk-intelligence/risk-intelligence').then(module => module.RiskIntelligence),
    canActivate: [authGuard],
    data: { roles: ROLE_POLICY.sensitiveInsights }
  },
  {
    path: 'audit',
    loadComponent: () =>
      import('./components/audit/audit').then(module => module.Audit),
    canActivate: [authGuard],
    data: { roles: ROLE_POLICY.audit }
  },
  {
    path: 'hr-assistant',
    loadComponent: () =>
      import('./components/hr-assistant/hr-assistant').then(module => module.HrAssistant),
    canActivate: [authGuard]
  }
];
