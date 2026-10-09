import { Employee } from './employee';

export interface EmployeePageResponse {
  content: Employee[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
