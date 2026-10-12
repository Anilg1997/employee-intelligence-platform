import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { NotificationService } from './services/notification.service';
import { signal } from '@angular/core';
import { of, Subject } from 'rxjs';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), {
        provide: NotificationService,
        useValue: { streamState: signal('idle'), notifications$: new Subject(), list: () => of([]), connect: () => undefined, disconnect: () => undefined, markAsRead: () => of(null) }
      }]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render title', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.brand-name')?.textContent).toContain('Employee Intelligence');
  });

  it('should render accessible primary navigation links', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    const navigation = compiled.querySelector('nav[aria-label="Primary navigation"]');
    const links = compiled.querySelectorAll('.main-nav a');

    expect(navigation).toBeTruthy();
    expect(links.length).toBe(10);
    expect(compiled.querySelector('.main-nav a[routerlink="/audit"]')).toBeTruthy();
    expect(compiled.querySelector('.main-nav a[routerlink="/salary-prediction"]')).toBeTruthy();
    expect(compiled.querySelector('.main-nav a[routerlink="/employees"]')).toBeTruthy();
    expect(compiled.querySelector('.main-nav a[routerlink="/notifications"]')).toBeTruthy();
    expect(compiled.querySelector('.menu-toggle')?.getAttribute('aria-controls')).toBe('primary-navigation');
  });
});
