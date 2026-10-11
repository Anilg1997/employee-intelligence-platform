import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { vi } from 'vitest';
import { routes } from '../app.routes';
import { AuthService, AppRole } from '../services/auth.service';
import { authGuard } from './auth.guard';
import { AUTH_ENABLED_TOKEN } from '../config/auth.config';

@Component({ template: 'Protected page' })
class ProtectedPage {}

describe('auth navigation', () => {
  let auth: AuthService;
  let router: Router;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [{ provide: AUTH_ENABLED_TOKEN, useValue: true }, provideRouter(routes.map(route => route.canActivate ? { ...route, component: ProtectedPage } : route))]
    });
    auth = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    vi.restoreAllMocks();
    sessionStorage.clear();
  });

  function signIn(roles: AppRole[], exp = Math.floor(Date.now() / 1000) + 3600) {
    auth.setAccessToken(`e.${btoa(JSON.stringify({ roles, exp }))}.s`);
  }

  it('redirects anonymous users to an unguarded access-required route', () => {
    const result = TestBed.runInInjectionContext(() => authGuard(
      { data: {} } as ActivatedRouteSnapshot, {} as RouterStateSnapshot
    )) as UrlTree;
    expect(router.serializeUrl(result)).toBe('/access-required');
    expect(routes.find(route => route.path === 'access-required')?.canActivate).toBeUndefined();
  });

  it.each(['/', '/dashboard', '/audit'])('finishes anonymous navigation to %s without a redirect loop', async url => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl(url);
    expect(router.url).toBe('/access-required');
    expect(harness.routeNativeElement?.textContent).toContain('Access required');
    expect(harness.routeNativeElement?.textContent).toContain('Sign-in is not yet integrated');
  });

  it('allows the access-required page to be opened directly', async () => {
    const harness = await RouterTestingHarness.create('/access-required');
    expect(router.url).toBe('/access-required');
    expect(harness.routeNativeElement?.textContent).toContain('Access required');
  });

  it('redirects a session that expires after sign-in and removes stale roles', async () => {
    const now = Date.now();
    signIn(['HR_ADMIN'], Math.floor(now / 1000) + 60);
    expect(auth.hasAnyRole(['HR_ADMIN'])).toBe(true);
    vi.spyOn(Date, 'now').mockReturnValue(now + 120000);
    expect(auth.hasAnyRole(['HR_ADMIN'])).toBe(false);
    expect(auth.state().roles).toEqual([]);
    const harness = await RouterTestingHarness.create('/audit');
    expect(router.url).toBe('/access-required');
    expect(harness.routeNativeElement?.textContent).toContain('Access required');
  });

  it.each([
    ['HR_ADMIN', '/audit', '/audit'],
    ['EMPLOYEE', '/audit', '/dashboard'],
    ['HR_ANALYST', '/salary-prediction', '/salary-prediction'],
    ['EMPLOYEE', '/salary-prediction', '/dashboard'],
    ['EMPLOYEE', '/dashboard', '/dashboard']
  ] as const)('handles %s access to %s', async (role, url, expected) => {
    signIn([role]);
    await RouterTestingHarness.create(url);
    expect(router.url).toBe(expected);
  });

  it('retains unrestricted local demo navigation', async () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [{ provide: AUTH_ENABLED_TOKEN, useValue: false }, provideRouter(routes.map(route => route.canActivate ? { ...route, component: ProtectedPage } : route))]
    });
    const harness = await RouterTestingHarness.create('/');
    expect(TestBed.inject(Router).url).toBe('/dashboard');
    await harness.navigateByUrl('/audit');
    expect(TestBed.inject(Router).url).toBe('/audit');
    expect(TestBed.inject(AuthService).state().demo).toBe(true);
  });
});
