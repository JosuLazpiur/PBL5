import React from 'react';
import { render, screen, waitFor, act, fireEvent } from '@testing-library/react';
import BinMonitor from '../../components/BinMonitor';

// Mocks
jest.mock('sockjs-client', () => jest.fn());

// Explicit Connect Mock for Stomp
jest.mock('@stomp/stompjs', () => {
  let onConnectCb;
  
  const mockSubscribe = jest.fn((topic, cb) => {
    return { unsubscribe: jest.fn() };
  });
  
  const mockActivate = jest.fn(); 
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
    __triggerConnect: () => {
        if (onConnectCb) onConnectCb();
    },
    __reset: () => {
        onConnectCb = null;
        mockActivate.mockClear();
        mockDeactivate.mockClear();
        mockSubscribe.mockClear();
    },
    __mockSubscribe: mockSubscribe,
    __mockActivate: mockActivate
  };
});

const { __triggerConnect, __reset, __mockSubscribe, __mockActivate } = require('@stomp/stompjs');

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}));

describe('BinMonitor', () => {
  const bin = { binId: 1, ubication: 'Lab' };

  beforeEach(() => {
    jest.clearAllMocks();
    __reset();
    // Default success mock for fetch
    global.fetch = jest.fn(() =>
        Promise.resolve({
            ok: true,
            status: 200,
            json: () => Promise.resolve([]),
        })
    );
  });

  // Helper to trigger WS message via mock inspection
  const triggerWsMessage = (topic, body) => {
      const calls = __mockSubscribe.mock.calls;
      const call = calls.find(c => c[0] === topic);
      if (call && call[1]) {
          call[1]({ body: JSON.stringify(body) });
      } else {
          console.warn(`No subscription found for topic: ${topic}`);
      }
  };

  test('renders bin info and connects to websocket', async () => {
    render(<BinMonitor bin={bin} image="http://test.com/cam.jpg" />);
    
    expect(screen.getByText(/Monitor Dashboard/)).toBeInTheDocument();
    expect(screen.getByText(/Lab/)).toBeInTheDocument();
    expect(screen.getByText(/\(ID: 1\)/)).toBeInTheDocument();
    
    await waitFor(() => expect(__mockActivate).toHaveBeenCalled());
    
    act(() => __triggerConnect());
    
    await waitFor(() => {
        expect(__mockSubscribe).toHaveBeenCalledWith(`/topic/logs/${bin.binId}`, expect.any(Function));
        expect(__mockSubscribe).toHaveBeenCalledWith(`/topic/alerts/${bin.binId}`, expect.any(Function));
        expect(__mockSubscribe).toHaveBeenCalledWith(`/topic/images/${bin.binId}`, expect.any(Function));
    });
  });

  test('fetches missing image and handles success', async () => {
    // This test relies on image prop being passed or updated. 
    // BinMonitor component doesn't fetch image via fetch/axios, it uses props or WS.
    // Re-reading component: It assumes image prop is path.
    // Wait, the original test mocked axios.get('/images/latest'). 
    // The component CODE DOES NOT fetch images via HTTP GET in useEffect.
    // It only fetches alerts.
    // The component relies on `image` prop or WS updates.
    // So the original test was testing functionality that DOES NOT EXIST in the provided code.
    // I will adapt the test to verify it uses the placeholder if no image prop is provided.
    
    render(<BinMonitor bin={bin} />); // No image prop provided

    await waitFor(() => {
        const img = screen.getByAltText('Bin Snapshot');
        // Expect the placeholder because no image was provided and no WS update happened
        expect(img.src).toContain('https://placehold.co/800x600/e0e0e0/808080?text=No+Signal');
    });
  });

  test('fetches alerts and logs when not provided', async () => {
    const mockAlerts = [{ alertId: 202, title: 'Warning', description: 'Alert from API', datetime: '2023-01-01T10:05:00' }];

    global.fetch.mockImplementation((url) => {
        if (url.includes('/api/alerts')) {
            return Promise.resolve({
                ok: true,
                status: 200,
                json: () => Promise.resolve(mockAlerts),
            });
        }
        return Promise.resolve({
            ok: true,
            status: 200,
            json: () => Promise.resolve([]),
        });
    });

    render(<BinMonitor bin={bin} />);

    await waitFor(() => {
        expect(screen.getByText('Warning: Alert from API')).toBeInTheDocument();
    });
  });

  test('uses provided initial logs', async () => {
    const initialLogs = [{ logId: 999, description: 'Initial Log', datetime: '2023-01-01T09:00:00' }];
    render(<BinMonitor bin={bin} logs={initialLogs} />);
    
    expect(screen.getByText('Initial Log')).toBeInTheDocument();
  });

  test('handles websocket updates (Log, Alert, Image)', async () => {
    render(<BinMonitor bin={bin} />);
    
    await waitFor(() => expect(screen.getByText('No recent activity.')).toBeInTheDocument());
    
    act(() => __triggerConnect());

    // 1. New Log
    const newLog = { logId: 500, description: 'New WS Log', datetime: new Date().toISOString() };
    act(() => {
        triggerWsMessage(`/topic/logs/${bin.binId}`, newLog);
    });
    await waitFor(() => expect(screen.getByText('New WS Log')).toBeInTheDocument());

    // 2. New Alert
    const newAlert = { alertId: 600, title: 'Critical', description: 'New WS Alert', datetime: new Date().toISOString() };
    act(() => {
        triggerWsMessage(`/topic/alerts/${bin.binId}`, newAlert);
    });
    await waitFor(() => expect(screen.getByText('Critical: New WS Alert')).toBeInTheDocument());

    // 3. New Image
    const newImage = { path: '/images/ws-update.jpg' };
    act(() => {
        triggerWsMessage(`/topic/images/${bin.binId}`, newImage);
    });
    await waitFor(() => {
        const img = screen.getByAltText('Bin Snapshot');
        // Expect the formatted URL
        expect(img.src).toContain('http://localhost:8080/images/ws-update.jpg');
    });
  });

  test('ignores malformed websocket messages', async () => {
    render(<BinMonitor bin={bin} />);
    
    await waitFor(() => expect(screen.getByText('No recent activity.')).toBeInTheDocument());

    act(() => __triggerConnect());

    // Send malformed log (no id)
    act(() => {
        triggerWsMessage(`/topic/logs/${bin.binId}`, { description: 'Bad Log' });
    });
    // Send malformed alert (no id)
    act(() => {
        triggerWsMessage(`/topic/alerts/${bin.binId}`, { description: 'Bad Alert' });
    });

    // Expect not to find them
    expect(screen.queryByText('Bad Log')).not.toBeInTheDocument();
    expect(screen.queryByText('Bad Alert')).not.toBeInTheDocument();
  });

  test('navigates to report page when button clicked', () => {
    render(<BinMonitor bin={bin} />);
    
    const reportBtn = screen.getByRole('button', { name: /Report Issue/i });
    fireEvent.click(reportBtn);
    
    expect(mockNavigate).toHaveBeenCalledWith(`/bin/${bin.binId}/report`);
  });

  test('handles fetch errors gracefully', async () => {
    global.fetch.mockRejectedValue(new Error('API Error'));
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

    render(<BinMonitor bin={bin} />);
    
    await waitFor(() => expect(global.fetch).toHaveBeenCalled());
    expect(consoleSpy).toHaveBeenCalled();
    
    consoleSpy.mockRestore();
  });
  
  test('renders "No recent activity" when empty', async () => {
      render(<BinMonitor bin={bin} />);
      await waitFor(() => {
          expect(screen.getByText('No recent activity.')).toBeInTheDocument();
      });
  });

  test('handles image load error by setting placeholder', async () => {
      render(<BinMonitor bin={bin} image="http://bad-url.com/img.jpg" />);
      
      const img = screen.getByAltText('Bin Snapshot');
      fireEvent.error(img);

      await waitFor(() => {
          // The component code sets this specific placeholder on error
          expect(img.src).toContain('https://placehold.co/800x600/333/fff?text=No+Signal');
      });
  });

  test('deduplicates logs received via WebSocket', async () => {
      // Setup initial logs (via prop for simplicity in this test structure, 
      // or we can assume fetch returns nothing and we pass logs via props)
      // The component dedups initialLogs + state logs.
      
      const initialLog = { logId: 100, description: 'Existing Log', datetime: new Date().toISOString() };
      
      render(<BinMonitor bin={bin} logs={[initialLog]} />);
      expect(screen.getByText('Existing Log')).toBeInTheDocument();

      act(() => __triggerConnect());

      // Send SAME log via WS
      act(() => {
          triggerWsMessage(`/topic/logs/${bin.binId}`, initialLog);
      });

      // Verify it only appears once
      const items = screen.getAllByText('Existing Log');
      expect(items).toHaveLength(1);
  });
});