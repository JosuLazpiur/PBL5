import { renderHook, waitFor, act } from '@testing-library/react';
import useBinRealTime from '../../hooks/useBinRealTime';

// Mocks
jest.mock('sockjs-client', () => jest.fn());

// Mock Stomp con soporte para múltiples topics y trigger manual
jest.mock('@stomp/stompjs', () => {
  let onConnectCb;
  let subscriptions = {}; // Map topic -> callback

  const mockActivate = jest.fn();
  const mockDeactivate = jest.fn();
  const subscribeImpl = (topic, cb) => {
    console.log(`[MockStomp] Subscribing to: ${topic}`);
    subscriptions[topic] = cb;
    return { unsubscribe: jest.fn() };
  };
  const mockSubscribe = jest.fn(subscribeImpl);

  return {
    Client: class {
      constructor(config) {
        console.log('[MockStomp] Client constructor called');
        if (config) {
            onConnectCb = config.onConnect;
        }
      }
      activate() { return mockActivate(); }
      deactivate() { return mockDeactivate(); }
      subscribe(topic, cb) { return mockSubscribe(topic, cb); }
    },
    // Helpers para test
    __triggerConnect: () => {
        console.log('[MockStomp] Triggering connect');
        if (onConnectCb) {
            try {
                onConnectCb();
            } catch (e) {
                console.error('[MockStomp] Error in onConnectCb:', e);
            }
        } else {
            console.warn('[MockStomp] onConnectCb is null');
        }
    },
    __triggerMessage: (topicFragment, body) => {
        const keys = Object.keys(subscriptions);
        
        // Find subscription by partial match
        const matchedTopic = keys.find(t => t.includes(topicFragment));
        
        if (matchedTopic && subscriptions[matchedTopic]) {
            subscriptions[matchedTopic]({ body: JSON.stringify(body) });
        } else {
            console.warn(`[MockStomp] No subscription found matching: ${topicFragment}. Available: ${keys.join(', ')}`);
        }
    },
    __reset: () => {
        console.log('[MockStomp] Resetting');
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

global.fetch = jest.fn();

describe('useBinRealTime Hook', () => {
  const binId = 123;

  beforeEach(() => {
    jest.clearAllMocks();
    __reset();
    
    // Default fetch mocks
    global.fetch.mockImplementation((url) => {
        if (url.includes('/alerts')) {
            return Promise.resolve({ ok: true, json: async () => [] });
        }
        if (url.includes('/images')) {
            return Promise.resolve({ ok: true, json: async () => ({ path: '/img/init.jpg' }) });
        }
        return Promise.resolve({ ok: false });
    });
  });

  test('fetches initial data and connects to websocket', async () => {
    const { result } = renderHook(() => useBinRealTime(binId));

    // Verificar estado inicial
    expect(result.current.logs).toEqual([]);
    expect(result.current.alerts).toEqual([]);
    
    // Esperar a que se llame a fetch image
    await waitFor(() => {
        expect(result.current.currentImage).toBe('http://localhost:8080/img/init.jpg');
    });

    // Verificar conexión socket
    expect(__mockActivate).toHaveBeenCalled();
    
    // Simular conexión
    act(() => {
        __triggerConnect();
    });

    // Verificar suscripciones
    await waitFor(() => {
        expect(__mockSubscribe).toHaveBeenCalledWith(expect.stringContaining(`/topic/logs/${binId}`), expect.any(Function));
        expect(__mockSubscribe).toHaveBeenCalledWith(expect.stringContaining(`/topic/alerts/${binId}`), expect.any(Function));
        expect(__mockSubscribe).toHaveBeenCalledWith(expect.stringContaining(`/topic/images/${binId}`), expect.any(Function));
    });
  });

  test('handles fetch errors gracefully', async () => {
      global.fetch.mockImplementation(() => Promise.reject('Network Error'));
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
      const consoleWarnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});

      const { result } = renderHook(() => useBinRealTime(binId));
      
      await waitFor(() => {
          expect(consoleSpy).toHaveBeenCalled();
          expect(consoleWarnSpy).toHaveBeenCalled();
      });

      expect(result.current.alerts).toEqual([]);
      expect(result.current.currentImage).toBe("");
      
      consoleSpy.mockRestore();
      consoleWarnSpy.mockRestore();
  });

  test('uses provided initial values', async () => {
      const initialLogs = [{ logId: 99, message: 'Init' }];
      const initialImage = 'http://custom/image.jpg';

      const { result } = renderHook(() => useBinRealTime(binId, initialLogs, initialImage));

      // Should use provided values immediately
      expect(result.current.logs).toEqual(initialLogs);
      expect(result.current.currentImage).toBe(initialImage);

      // Should NOT fetch image if provided
      await waitFor(() => {
          expect(global.fetch).not.toHaveBeenCalledWith(expect.stringContaining('/images/latest'));
      });
  });
});