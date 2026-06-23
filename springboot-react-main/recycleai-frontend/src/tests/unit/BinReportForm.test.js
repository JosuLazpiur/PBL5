import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import BinReportForm from '../../pages/BinReportForm';

// Mocks
jest.mock('../../components/HeaderComponent', () => () => <div data-testid="header">Header</div>);
jest.mock('../../components/FooterComponent', () => ({ FooterComponent: () => <div data-testid="footer">Footer</div> }));

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
  useParams: () => ({ binId: '123' }),
}));

// Mock global fetch
global.fetch = jest.fn();

describe('BinReportForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('validates form inputs', async () => {
    render(<BinReportForm />);

    const submitButton = screen.getByRole('button', { name: /Submit/i });
    fireEvent.click(submitButton);

    expect(screen.getByText('Please fill in both title and description.')).toBeInTheDocument();
    expect(global.fetch).not.toHaveBeenCalled();
  });

  test('submits report successfully', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      json: async () => ({ success: true })
    });

    render(<BinReportForm />);

    fireEvent.change(screen.getByLabelText(/Issue Title/i), { target: { value: 'Broken Lid' } });
    fireEvent.change(screen.getByLabelText(/Description/i), { target: { value: 'Lid is completely detached' } });

    const submitButton = screen.getByRole('button', { name: /Submit/i });
    fireEvent.click(submitButton);

    // Wait for async actions and UI update
    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:1880/new-alert',
        expect.objectContaining({
          method: 'POST',
          body: expect.stringContaining('Broken Lid')
        })
      );
      expect(screen.getByText('Report submitted successfully!')).toBeInTheDocument();
    });
    
    // Check navigation after timeout (using jest timers might be cleaner, but waitFor works too if timeout is short)
    // The component has a 1500ms timeout. Using jest.useFakeTimers would be better.
  });

  test('handles submission error', async () => {
    global.fetch.mockRejectedValue(new Error('Network error'));

    render(<BinReportForm />);

    fireEvent.change(screen.getByLabelText(/Issue Title/i), { target: { value: 'Broken Lid' } });
    fireEvent.change(screen.getByLabelText(/Description/i), { target: { value: 'Lid is completely detached' } });

    fireEvent.click(screen.getByRole('button', { name: /Submit/i }));

    await waitFor(() => {
      expect(screen.getByText('Failed to submit report. Please check connection.')).toBeInTheDocument();
    });
  });
});
