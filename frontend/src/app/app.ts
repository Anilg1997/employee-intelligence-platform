import { Component } from '@angular/core';

import { EmployeeList } from './components/employee-list/employee-list';
import { EmployeeForm } from './components/employee-form/employee-form';
import { AttritionPrediction } from './components/attrition-prediction/attrition-prediction';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    EmployeeList,
    EmployeeForm,
    AttritionPrediction
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {

  refreshEmployees(): void {
    // Employee list refresh is handled by the existing event flow.
  }
}