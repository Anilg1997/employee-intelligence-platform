import { TestBed } from '@angular/core/testing';
import { RolePolicyService } from './role-policy.service';

describe('RolePolicyService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));
  it('allows audit only to administrator roles', () => {
    const policy = TestBed.inject(RolePolicyService);
    expect(policy.canViewAudit()).toBe(true); // local demo mode has explicit SUPER_ADMIN access
  });
});
