import { test, expect } from '@playwright/test';

// These browser checks isolate navigation/rendering from API availability.
// Unknown API calls fail closed rather than reaching a developer's backend.
test.beforeEach(async ({ page }) => {
  await page.route('**/api/**', async route => {
    const path = new URL(route.request().url()).pathname;
    if (path === '/api/dashboard/summary') {
      await route.fulfill({ json: { totalEmployees: 0, departmentStatistics: {}, attritionRate: null } });
    } else if (path === '/api/employees/search') {
      await route.fulfill({ json: { content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 } });
    } else {
      await route.fulfill({ status: 503, json: { message: 'Unconfigured smoke API stub' } });
    }
  });
});

test('dashboard renders an empty snapshot', async ({ page }) => {
  await page.goto('/dashboard');
  await expect(page.getByRole('heading', { name: 'Employee Intelligence Dashboard' })).toBeVisible();
  await expect(page.getByText('There are no department records yet')).toBeVisible();
});

test('dashboard API failure offers retry and recovers', async ({ page }) => {
  let failed = false;
  await page.route('**/api/dashboard/summary', async route => {
    if (!failed) { failed = true; await route.fulfill({ status: 503, json: {} }); }
    else { await route.fulfill({ json: { totalEmployees: 0, departmentStatistics: {} } }); }
  });
  await page.goto('/dashboard');
  await expect(page.getByRole('alert')).toBeVisible();
  await page.getByRole('button', { name: 'Retry', exact: true }).click();
  await expect(page.getByText('There are no department records yet')).toBeVisible();
});

test('employee directory deep link renders', async ({ page }) => {
  await page.goto('/employees');
  await expect(page.getByRole('heading', { name: 'Workforce Directory', exact: true })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Employees', exact: true })).toBeVisible();
});
