import { Routes } from '@angular/router';

import { Dashboard } from './components/dashboard/dashboard';
import { EmployeeList } from './components/employee-list/employee-list';
import { AttritionPrediction } from './components/attrition-prediction/attrition-prediction';
import { HrAssistant } from './components/hr-assistant/hr-assistant';

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
    component: EmployeeList
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