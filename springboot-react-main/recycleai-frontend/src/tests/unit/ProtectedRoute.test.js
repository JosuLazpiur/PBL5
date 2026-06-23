import React from 'react';
import { render, screen } from '@testing-library/react';
import ProtectedRoute from '../../routes/ProtectedRoute';
import { useAuth } from '../../context/AuthContext';

// Mock de AuthContext
jest.mock('../../context/AuthContext');

describe('ProtectedRoute', () => {
  test('renders children when user is authenticated', () => {
    useAuth.mockReturnValue({ user: { username: 'test' } });

    render(
      <ProtectedRoute>
        <div data-testid="protected-content">Secret Content</div>
      </ProtectedRoute>
    );

    expect(screen.getByTestId('protected-content')).toBeInTheDocument();
    expect(screen.queryByText(/Redirected to/)).not.toBeInTheDocument();
  });

  test('redirects to login when user is not authenticated', () => {
    useAuth.mockReturnValue({ user: null });

    render(
      <ProtectedRoute>
        <div data-testid="protected-content">Secret Content</div>
      </ProtectedRoute>
    );

    expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
    expect(screen.getByText('Redirected to /login')).toBeInTheDocument();
  });
});
