import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { EmployeeDirectory } from './employee-directory';

describe('EmployeeDirectory', () => {
  let component: EmployeeDirectory;
  let fixture: ComponentFixture<EmployeeDirectory>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeDirectory],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeeDirectory);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
