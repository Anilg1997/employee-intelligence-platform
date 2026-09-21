import { Component } from '@angular/core';

import { EmployeeForm } from '../employee-form/employee-form';
import { EmployeeList } from '../employee-list/employee-list';

@Component({
  selector: 'app-employees-page',
  standalone: true,
  imports: [EmployeeForm, EmployeeList],
  templateUrl: './employees-page.html',
  styleUrl: './employees-page.scss'
})
export class EmployeesPage {

  refreshTrigger = 0;

  refreshEmployees(): void {
    this.refreshTrigger++;
  }
}