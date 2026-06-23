const { test, expect } = require('@playwright/test');

test.describe('Login Flow', () => {
  test.beforeEach(async ({ page }) => {
    // Mock the login API call
    await page.route('http://localhost:8080/login', async route => {
      const request = route.request();
      const postData = request.postDataJSON();

      if (postData.username === 'testuser' && postData.password === 'password') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            domain: { domainId: 'domain123' },
            token: 'fake-jwt-token',
            username: 'testuser'
          })
        });
      } else {
        await route.fulfill({
          status: 401,
          contentType: 'application/json',
          body: JSON.stringify({ message: 'Unauthorized' })
        });
      }
    });

    await page.goto('/login');
  });

  test('should display login form', async ({ page }) => {
    await expect(page.getByRole('heading', { name: 'Welcome' })).toBeVisible();
    await expect(page.getByLabel('Username')).toBeVisible();
    await expect(page.getByLabel('Password')).toBeVisible();
    await expect(page.getByRole('button', { name: 'LOGIN' })).toBeVisible();
  });

  test('should login successfully with valid credentials', async ({ page }) => {
    await page.getByLabel('Username').fill('testuser');
    await page.getByLabel('Password').fill('password');
    await page.getByRole('button', { name: 'LOGIN' }).click();

    // Expect redirection to user page
    await expect(page).toHaveURL(/\/user\/domain123/);
  });

  test('should show error with invalid credentials', async ({ page }) => {
    await page.getByLabel('Username').fill('wronguser');
    await page.getByLabel('Password').fill('wrongpass');
    await page.getByRole('button', { name: 'LOGIN' }).click();

    await expect(page.getByRole('alert')).toContainText('Invalid username or password.');
  });
});
