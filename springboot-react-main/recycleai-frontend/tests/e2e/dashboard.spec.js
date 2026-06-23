const { test, expect } = require('@playwright/test');

test.describe('Dashboard & User Page Flow', () => {
  const domainId = 'domain123';
  const mockBins = [
    { binId: '1', binName: 'Main Hall Bin', capacity: 100, load: 50, batteryLevel: 80, ubication: 'Hallway', type: 'General' },
    { binId: '2', binName: 'Cafeteria Bin', capacity: 200, load: 90, batteryLevel: 20, ubication: 'Cafeteria', type: 'Plastic' }
  ];

  test.beforeEach(async ({ page }) => {
    // 1. Mock Login API
    await page.route('http://localhost:8080/login', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          domain: { domainId: domainId },
          token: 'fake-jwt-token',
          username: 'testuser'
        })
      });
    });

    // 2. Mock Bins List API
    await page.route(`http://localhost:8080/api/bin/domain/${domainId}`, async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockBins)
      });
    });

    // 3. Perform Login to reach Dashboard
    await page.goto('/login');
    await page.getByLabel('Username').fill('testuser');
    await page.getByLabel('Password').fill('password');
    await page.getByRole('button', { name: 'LOGIN' }).click();
    await expect(page).toHaveURL(new RegExp(`/user/${domainId}`));
  });

  test('should display the list of bins fetched from API', async ({ page }) => {
    // Verify header or title if existing
    // Check if bin cards/items are visible
    // Assuming BinListComponent renders text with bin names
    await expect(page.getByText('Bin 1')).toBeVisible();
    await expect(page.getByText('Hallway')).toBeVisible(); // ubication for Bin #1
    await expect(page.getByText('Bin 2')).toBeVisible();
    await expect(page.getByText('Cafeteria')).toBeVisible(); // ubication for Bin #2
  });
});
