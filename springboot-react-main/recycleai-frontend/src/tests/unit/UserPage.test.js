import React from 'react';
import { render, screen, waitFor, act } from '@testing-library/react';
import UserPage from '../../pages/UserPage';
import BinService from '../../services/BinService';
import { useAuth } from '../../context/AuthContext';

// Mocks
jest.mock('../../services/BinService');
jest.mock('../../context/AuthContext');
jest.mock('../../components/HeaderComponent', () => () => <div data-testid="header">Header</div>);
jest.mock('../../components/FooterComponent', () => ({ FooterComponent: () => <div data-testid="footer">Footer</div> }));
jest.mock('../../components/BinListComponent', () => ({ bins }) => (
  <div data-testid="bin-list">
    {bins ? bins.map(b => <div key={b.binId}>{b.name} - {b.ubication}</div>) : null}
  </div>
));

jest.mock('sockjs-client', () => jest.fn());

// Robust Mock for Stomp
jest.mock('@stomp/stompjs', () => {
  let onConnectCb;
  let subscriptions = {};
  
  const mockSubscribe = jest.fn((topic, cb) => {
    subscriptions[topic] = cb;
    return { unsubscribe: jest.fn() };
  });
  
  const mockActivate = jest.fn(); // No auto-connect
  const mockDeactivate = jest.fn();

  return {
    Client: class {
      constructor(config) {
        if (config) onConnectCb = config.onConnect;
        this.activate = mockActivate;
        this.deactivate = mockDeactivate;
        this.subscribe = mockSubscribe;
      }
    },
    // Helpers
    __triggerConnect: () => {
        if (onConnectCb) onConnectCb();
    },
    __triggerMessage: (body) => {
        // UserPage only subscribes to /topic/bins
        const topic = "/topic/bins";
        if (subscriptions[topic]) {
            subscriptions[topic]({ body: JSON.stringify(body) });
        }
    },
    __reset: () => {
      onConnectCb = null;
      subscriptions = {};
      mockActivate.mockClear();
      mockDeactivate.mockClear();
      mockSubscribe.mockClear();
    },
    __mockSubscribe: mockSubscribe,
    __mockActivate: mockActivate
  };
});

const { __triggerConnect, __triggerMessage, __reset, __mockSubscribe, __mockActivate } = require('@stomp/stompjs');

describe('UserPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    __reset();
    useAuth.mockReturnValue({
      user: { domain: { domainId: 1 }, name: 'User' }
    });
  });

  test('fetches bins and renders them', async () => {
    const mockBins = [{ binId: 1, name: 'Bin 1', ubication: 'Loc 1', domain: { domainId: 1 } }];
    BinService.getBinsByDomain.mockResolvedValue({ data: mockBins });

    render(<UserPage />);

    await waitFor(() => {
      expect(screen.getByText('Bin 1 - Loc 1')).toBeInTheDocument();
    });
  });

  test('handles fetch error', async () => {
    BinService.getBinsByDomain.mockRejectedValue(new Error('Fetch error'));
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

    render(<UserPage />);

    await waitFor(() => {
        expect(BinService.getBinsByDomain).toHaveBeenCalled();
    });
    consoleSpy.mockRestore();
  });

  test('ignores new bin via WebSocket if domain does not match', async () => {
      BinService.getBinsByDomain.mockResolvedValue({ data: [] });
      render(<UserPage />);
      await waitFor(() => expect(BinService.getBinsByDomain).toHaveBeenCalled());
      
      act(() => __triggerConnect());
  
      const otherDomainBin = { binId: 88, name: 'Other Bin', ubication: 'Loc 88', domain: { domainId: 2 } };
      await act(async () => {
          __triggerMessage(otherDomainBin);
      });
  
      // Wait a bit to ensure it doesn't appear
      await new Promise(r => setTimeout(r, 200));
      expect(screen.queryByText(/Other Bin/)).not.toBeInTheDocument();
  });

});