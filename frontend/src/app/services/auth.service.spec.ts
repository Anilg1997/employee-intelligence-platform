import { parseJwtPayload, isTokenExpired } from './auth.service';

const token = (payload: object) => `e.${btoa(JSON.stringify(payload)).replace(/=/g, '')}.s`;

describe('AuthService', () => {
  it('parses JWT payloads without trusting the signature', () => {
    expect(parseJwtPayload(token({ roles: ['HR_ANALYST'] }))?.['roles']).toEqual(['HR_ANALYST']);
    expect(parseJwtPayload('invalid')).toBeNull();
  });

  it('detects expired tokens and role claims', () => {
    const valid = token({ exp: 2000, roles: ['HR_ANALYST'] });
    expect(isTokenExpired(valid, 1999 * 1000)).toBe(false);
    expect(isTokenExpired(valid, 2001 * 1000)).toBe(true);
    expect(parseJwtPayload(valid)?.['roles']).toEqual(['HR_ANALYST']);
  });

  it('treats tokens without an expiry claim as expired', () => {
    expect(isTokenExpired(token({ roles: ['HR_ANALYST'] }), 1999 * 1000)).toBe(true);
  });
});
