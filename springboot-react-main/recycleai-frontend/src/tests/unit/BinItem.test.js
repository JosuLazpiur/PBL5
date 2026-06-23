import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import BinItem from '../../components/BinItem';
import BinService from "../../services/BinService";

// Mock useNavigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}));

// Mock BinService
jest.mock('../../services/BinService');

jest.mock('sockjs-client', () => jest.fn());

// Explicit Connect Mock for Stomp
jest.mock('@stomp/stompjs', () => {
  let onConnectCb;
  let subscriptions = {}; // Map topic -> callback

  const mockActivate = jest.fn(); 
  const mockDeactivate = jest.fn();
  const mockSubscribe = jest.fn((topic, cb) => {
    console.log(`[MockStomp] Subscribing to ${topic}`);
    subscriptions[topic] = cb;
    return { unsubscribe: jest.fn() };
  });

  return {
    Client: class {
      constructor(config) {
        console.log('[MockStomp] Constructor called with config keys:', Object.keys(config));
        if (config) onConnectCb = config.onConnect;
        this.activate = mockActivate;
        this.deactivate = mockDeactivate;
      }
      subscribe(topic, cb) {
          return mockSubscribe(topic, cb);
      }
    },
    __triggerConnect: () => {
        console.log('[MockStomp] Triggering connect. Callback exists?', !!onConnectCb);
        if (onConnectCb) {
            onConnectCb();
        } else {
            console.warn('[MockStomp] No onConnect callback found!');
        }
    },
    __triggerMessage: (topic, body) => {
        if (subscriptions[topic]) {
            subscriptions[topic]({ body: JSON.stringify(body) });
        } else {
             console.warn(`[MockStomp] No subscription found for ${topic}. Available: ${Object.keys(subscriptions)}`);
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


describe('BinItem', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockNavigate.mockClear();
    __reset();
  });

  const mockBin = {
    binId: 1,
    ubication: 'Main Hall',
    operative: true
  };

  test('renders bin details correctly', () => {
    render(<BinItem bin={mockBin} />);
    
    expect(screen.getByText('Bin 1')).toBeInTheDocument();
    expect(screen.getByText('Main Hall')).toBeInTheDocument();
    expect(screen.getByText('OPERATIVE')).toBeInTheDocument();
  });

  test('renders inactive status', () => {
    const inactiveBin = { ...mockBin, operative: false };
    render(<BinItem bin={inactiveBin} />);
    
    expect(screen.getByText('MAINTENANCE')).toBeInTheDocument();
  });

  test('navigates to details on card click', () => {
    render(<BinItem bin={mockBin} />);
    
    // El Card entero es clickeable. Buscamos texto y subimos o clickeamos algo dentro que propague.
    fireEvent.click(screen.getByText('Bin 1'));
    
    expect(mockNavigate).toHaveBeenCalledWith('/bin/1');
  });

  test('navigates to edit on edit button click', () => {
    render(<BinItem bin={mockBin} />);
    
    const editButton = screen.getByRole('button', { name: /Edit/i });
    fireEvent.click(editButton);
    
    expect(mockNavigate).toHaveBeenCalledWith('/bin/edit/1');
        expect(mockNavigate).toHaveBeenCalledTimes(1);
      });
    
        test('handles reporting bin as broken', async () => {          BinService.updateBin.mockResolvedValue({});
          render(<BinItem bin={mockBin} />);
    
          const brokenButton = screen.getByRole('button', { name: /Broken/i });
          fireEvent.click(brokenButton);
    
          // Verify optimistic update (loading state or changed text)
          expect(screen.getByText('Reporting...')).toBeInTheDocument();
    
          await waitFor(() => {
              expect(BinService.updateBin).toHaveBeenCalledWith(mockBin.binId, { ...mockBin, operative: false });
          });
          
          // Verify final state
          await waitFor(() => {
             expect(screen.getByText('MAINTENANCE')).toBeInTheDocument();
          });
      });
    });
