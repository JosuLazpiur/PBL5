import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import BinEvents from '../../components/BinEvents';

// Mock useNavigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}));

describe('BinEvents', () => {
  const logs = [{ logId: 1, description: 'Log 1', datetime: '2023-01-01T10:00:00' }];
  const alerts = [{ alertId: 1, description: 'Alert 1', datetime: '2023-01-01T11:00:00' }];

  test('renders combined sorted events', () => {
    render(<BinEvents logs={logs} alerts={alerts} binId={1} />);
    
    // Alert is newer, should be first (if logic is descending)
    // The code: sort((a, b) => new Date(b.datetime) - new Date(a.datetime)) -> DESCENDING
    
    const items = screen.getAllByText(/Log 1|Alert 1/);
    expect(items[0]).toHaveTextContent('Alert 1');
    expect(items[1]).toHaveTextContent('Log 1');
  });

  test('renders no events message', () => {
    render(<BinEvents logs={[]} alerts={[]} binId={1} />);
    expect(screen.getByText('No recent events')).toBeInTheDocument();
  });

  test('navigates to report', () => {
    render(<BinEvents logs={[]} alerts={[]} binId={99} />);
    
    fireEvent.click(screen.getByText('Report Issue'));
    expect(mockNavigate).toHaveBeenCalledWith('/bin/99/report');
  });
});
