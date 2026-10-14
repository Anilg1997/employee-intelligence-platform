export type ReportType = 'EMPLOYEE' | 'DEPARTMENT' | 'WORKFORCE';

export interface ReportRequest {
  reportType: ReportType;
  employeeId?: number;
  department?: string;
}

export interface Report {
  id: number | string;
  reportType: ReportType | string;
  status: string;
  employeeId?: number | null;
  employeeName?: string | null;
  department?: string | null;
  content?: string | null;
  createdAt: string;
  aiAssisted: boolean;
  limitations?: string | null;
  requester?: string | null;
  humanReviewState?: string | null;
}

export interface ReportListResponse {
  content?: Report[];
  reports?: Report[];
  totalElements?: number;
}
