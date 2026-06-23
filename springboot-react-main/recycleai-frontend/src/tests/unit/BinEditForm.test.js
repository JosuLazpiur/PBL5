import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import BinEditForm from '../../pages/BinEditForm';
import BinService from '../../services/BinService';
import { useAuth } from '../../context/AuthContext';

// Mocks
jest.mock('../../services/BinService');
jest.mock('../../context/AuthContext');
jest.mock('../../components/HeaderComponent', () => () => <div data-testid="header">Header</div>);
jest.mock('../../components/FooterComponent', () => ({ FooterComponent: () => <div data-testid="footer">Footer</div> }));

// Mock navigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
  useParams: () => ({ binId: '123' }),
}));

describe('BinEditForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    // Default user with domain
    useAuth.mockReturnValue({
      user: { domain: { domainId: 'domain1', name: 'DomainName' } }
    });
  });

  test('loads bin data and renders form', async () => {
    const mockBin = { binId: 123, name: 'Bin 1', ubication: 'Floor 1', domain: { name: 'DomainName' } };
    BinService.getBinById.mockResolvedValue({ data: mockBin });

    render(<BinEditForm />);

    await waitFor(() => {
      expect(screen.getByDisplayValue('Floor 1')).toBeInTheDocument();
    });
  });

  test('handles load error and redirects to user page if user has domain', async () => {
    BinService.getBinById.mockRejectedValue(new Error('Fetch error'));
    
    render(<BinEditForm />);

    await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/user/domain1');
    });
  });

  test('handles load error and redirects to login if user has no domain', async () => {
    useAuth.mockReturnValue({ user: {} }); // No domain
    BinService.getBinById.mockRejectedValue(new Error('Fetch error'));
    
    render(<BinEditForm />);

    await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/login');
    });
  });

  test('updates bin on submit', async () => {
    const mockBin = { binId: 123, ubication: 'Floor 1' };
    BinService.getBinById.mockResolvedValue({ data: mockBin });
    BinService.updateBin.mockResolvedValue({});

    render(<BinEditForm />);
    await waitFor(() => screen.getByDisplayValue('Floor 1'));

    fireEvent.change(screen.getByLabelText(/Location/i), { target: { value: 'Floor 2' } });
    fireEvent.click(screen.getByRole('button', { name: /Save/i }));

    await waitFor(() => {
      expect(BinService.updateBin).toHaveBeenCalledWith('123', expect.objectContaining({ ubication: 'Floor 2' }));
    });
    expect(mockNavigate).toHaveBeenCalledWith('/user/domain1');
  });

  test('handles update error', async () => {
    const mockBin = { binId: 123, ubication: 'Floor 1' };
    BinService.getBinById.mockResolvedValue({ data: mockBin });
    BinService.updateBin.mockRejectedValue(new Error('Update failed'));
    
    // Spy on console.error to suppress output
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

    render(<BinEditForm />);
    await waitFor(() => screen.getByDisplayValue('Floor 1'));

    fireEvent.click(screen.getByRole('button', { name: /Save/i }));

    await waitFor(() => {
      expect(BinService.updateBin).toHaveBeenCalled();
    });
    // Navigation should NOT happen on error
    expect(mockNavigate).not.toHaveBeenCalled();
    
    consoleSpy.mockRestore();
  });

  test('navigates back on cancel (user with domain)', async () => {
    const mockBin = { binId: 123, ubication: 'Floor 1' };
    BinService.getBinById.mockResolvedValue({ data: mockBin });

    render(<BinEditForm />);
    await waitFor(() => screen.getByDisplayValue('Floor 1'));

    fireEvent.click(screen.getByRole('button', { name: /Cancel/i }));
    expect(mockNavigate).toHaveBeenCalledWith('/user/domain1');
  });

  test('navigates back on cancel (user without domain)', async () => {
    useAuth.mockReturnValue({ user: {} });
    const mockBin = { binId: 123, ubication: 'Floor 1' };
    BinService.getBinById.mockResolvedValue({ data: mockBin });

    render(<BinEditForm />);
    await waitFor(() => screen.getByDisplayValue('Floor 1'));

    fireEvent.click(screen.getByRole('button', { name: /Cancel/i }));
    expect(mockNavigate).toHaveBeenCalledWith('/');
  });
  
  test('redirects to login after update if user lost session/domain', async () => {
      useAuth.mockReturnValue({ user: null });
      const mockBin = { binId: 123, ubication: 'Floor 1' };
      BinService.getBinById.mockResolvedValue({ data: mockBin });
      BinService.updateBin.mockResolvedValue({});
      
      render(<BinEditForm />);
      await waitFor(() => screen.getByDisplayValue('Floor 1'));
      
      fireEvent.click(screen.getByRole('button', { name: /Save/i }));
      
      await waitFor(() => {
          expect(mockNavigate).toHaveBeenCalledWith('/login');
      });
  });
});