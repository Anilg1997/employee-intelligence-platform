import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Reports } from './reports';
import { ReportService } from '../../services/report.service';
import { Report } from '../../models/report';

const report: Report = {
  id: 7, reportType: 'WORKFORCE', status: 'GENERATED', createdAt: '2026-01-01T00:00:00Z',
  aiAssisted: false, humanReviewState: 'PENDING', content: 'Stable workforce trends.'
};

describe('Reports', () => {
  let fixture: ComponentFixture<Reports>;
  let component: Reports;
  let service: { list: ReturnType<typeof vi.fn>; getById: ReturnType<typeof vi.fn>; generate: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    service = { list: vi.fn().mockReturnValue(of([report])), getById: vi.fn().mockReturnValue(of(report)), generate: vi.fn().mockReturnValue(of(report)) };
    await TestBed.configureTestingModule({ imports: [Reports], providers: [{ provide: ReportService, useValue: service }] }).compileComponents();
    fixture = TestBed.createComponent(Reports);
    component = fixture.componentInstance;
  });

  it('renders reports and loads detail through an accessible report control', () => {
    fixture.detectChanges();
    const button = fixture.nativeElement.querySelector('.report-item') as HTMLButtonElement;
    expect(button.getAttribute('aria-label')).toContain('Workforce');
    button.click();
    fixture.detectChanges();
    expect(service.getById).toHaveBeenCalledWith(7);
    expect(fixture.nativeElement.textContent).toContain('Human review is pending');
  });

  it('sends only populated optional scope fields when generating', () => {
    fixture.detectChanges();
    component.department = 'Engineering';
    component.generate();
    expect(service.generate).toHaveBeenCalledWith({ reportType: 'WORKFORCE', department: 'Engineering' });
  });

  it('shows the empty state after a successful empty response', () => {
    service.list.mockReturnValue(of([]));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No reports yet');

  });
});
