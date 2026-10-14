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
import { authGuard } from './guards/auth.guard';
import { Audit } from './components/audit/audit';
import { ROLE_POLICY } from './services/role-policy.service';
import { AccessRequired } from './components/access-required/access-required';
import { Notifications } from './components/notifications/notifications';
import { AuthCallback } from './components/auth-callback/auth-callback';
import { Reports } from './components/reports/reports';
import { AiOperations } from './components/ai-operations/ai-operations';
import { SystemHealth } from './components/system-health/system-health';
import { Administration } from './components/administration/administration';
import { Knowledge } from './components/knowledge/knowledge';

export const routes: Routes = [
  { path: 'access-required', component: AccessRequired },
  { path: 'auth/callback', component: AuthCallback },

  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },

  {
    path: 'dashboard',
    component: Dashboard,
    canActivate: [authGuard]
  },

  {
    path: 'employees',
    component: EmployeesPage,
    canActivate: [authGuard]
  },

  {
    path: 'employees/:id',
    component: EmployeeProfile,
    canActivate: [authGuard]
  },

  {
    path: 'attrition-prediction',
    component: AttritionPrediction,
    canActivate: [authGuard]
  },
  { path: 'salary-prediction', component: SalaryPrediction, canActivate: [authGuard], data: { roles: ROLE_POLICY.sensitiveInsights } },
  { path: 'performance-prediction', component: PerformancePrediction, canActivate: [authGuard], data: { roles: ROLE_POLICY.sensitiveInsights } },
  { path: 'promotion-prediction', component: PromotionPrediction, canActivate: [authGuard], data: { roles: ROLE_POLICY.sensitiveInsights } },
  { path: 'risk-intelligence', component: RiskIntelligence, canActivate: [authGuard], data: { roles: ROLE_POLICY.sensitiveInsights } },
  { path: 'audit', component: Audit, canActivate: [authGuard], data: { roles: ROLE_POLICY.audit } },
  { path: 'notifications', component: Notifications, canActivate: [authGuard] },
  { path: 'reports', component: Reports, canActivate: [authGuard] },
  { path: 'ai-operations', component: AiOperations, canActivate: [authGuard], data: { roles: ROLE_POLICY.sensitiveInsights } },
  { path: 'system-health', component: SystemHealth, canActivate: [authGuard], data: { roles: ROLE_POLICY.audit } },
  { path: 'administration', component: Administration, canActivate: [authGuard], data: { roles: ROLE_POLICY.audit } },
  { path: 'knowledge', component: Knowledge, canActivate: [authGuard], data: { roles: ROLE_POLICY.audit } },

  {
    path: 'hr-assistant',
    component: HrAssistant,
    canActivate: [authGuard]
  }

];
