import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { useParams } from 'react-router-dom';
import BinDetailPage from '../../pages/BinDetailPage';
import BinService from '../../services/BinService';
import { getLogsByBin } from '../../services/LogService';
import { useAuth } from '../../context/AuthContext';

// Mocks
jest.mock('../../services/BinService');
jest.mock('../../services/LogService');
jest.mock('../../context/AuthContext');
jest.mock('../../components/HeaderComponent', () => () => <div data-testid="header">Header</div>);
jest.mock('../../components/FooterComponent', () => ({ FooterComponent: () => <div data-testid="footer">Footer</div> }));
jest.mock('../../components/BinMonitor', () => ({ bin, logs }) => (
  <div data-testid="bin-monitor">
    Monitor for {bin?.name} with {logs?.length} logs
  </div>
));

describe('BinDetailPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useAuth.mockReturnValue({
      user: { domain: { domainId: 'domain1' } }
    });
    // Mock useParams to return a binId
    require('react-router-dom').useParams.mockReturnValue({ binId: '123' });
  });

  test('fetches bin and logs data and renders monitor', async () => {
    const mockBin = { binId: 123, name: 'Test Bin', imageUrl: 'img.jpg' };
    const mockLogs = [{ id: 1, message: 'Log 1' }];

    BinService.getBinById.mockResolvedValue({ data: mockBin });
    getLogsByBin.mockResolvedValue(mockLogs);

    render(<BinDetailPage />);

    // Check loading initially
    // expect(screen.getByRole('progressbar')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByTestId('bin-monitor')).toBeInTheDocument();
    });

    expect(BinService.getBinById).toHaveBeenCalledWith('123');
    expect(getLogsByBin).toHaveBeenCalledWith('123');
    expect(screen.getByText('Monitor for Test Bin with 1 logs')).toBeInTheDocument();
  });

  test('handles fetch error', async () => {
    BinService.getBinById.mockRejectedValue(new Error('Fetch failed'));
    getLogsByBin.mockResolvedValue([]);

    render(<BinDetailPage />);

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Could not load bin details. Please try again.');
    });
  });
});
