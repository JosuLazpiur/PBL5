import { render, screen } from '@testing-library/react';
import App from './App';

// Mock child pages to avoid testing their internal logic or routing dependencies here
jest.mock('./pages/UserPage', () => () => <div>UserPage</div>);
jest.mock('./pages/BinDetailPage', () => () => <div>BinDetailPage</div>);
jest.mock('./pages/BinEditForm', () => () => <div>BinEditForm</div>);
jest.mock('./pages/BinReportForm', () => () => <div>BinReportForm</div>);
// We don't mock LoginForm because we want to verify it renders on the default route

test('renders login form by default', () => {
  render(<App />);
  // Use getAllByText and check that at least one exists, or use getByRole for better specificity
  const welcomeElements = screen.getAllByText(/Welcome/i);
  expect(welcomeElements.length).toBeGreaterThan(0);
});