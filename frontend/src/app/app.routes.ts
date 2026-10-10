import { Routes } from '@angular/router';

import { Dashboard } from './components/dashboard/dashboard';
import { EmployeesPage } from './components/employees-page/employees-page';
import { AttritionPrediction } from './components/attrition-prediction/attrition-prediction';
import { HrAssistant } from './components/hr-assistant/hr-assistant';
import { EmployeeProfile } from './components/employee-profile/employee-profile';

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

  {
    path: 'hr-assistant',
    component: HrAssistant
  }

];
