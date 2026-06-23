import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import HeaderComponent from '../../components/HeaderComponent';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom'; // Mockeado globalmente

// Mock de AuthContext
jest.mock('../../context/AuthContext');

// Mocks de MUI Icons (opcional, pero buena práctica si fallan por renderizado)
// En este caso, MUI Icons son SVGs, suelen renderizar bien sin mock, pero si hay problemas lo haré.

describe('HeaderComponent', () => {
  const mockLogout = jest.fn();
  const mockNavigate = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    useAuth.mockReturnValue({
      user: { name: 'TestUser', role: 'User', domain: { domainId: '123' } },
      logout: mockLogout
    });
    
    // Ahora useNavigate es un jest.fn(), así que podemos mockear su implementación
    const { useNavigate } = require('react-router-dom');
    useNavigate.mockReturnValue(mockNavigate);
  });

  test('renders logo correctly', () => {
    render(<HeaderComponent />);
    expect(screen.getByText(/RECYCL/)).toBeInTheDocument();
    expect(screen.getByText(/AI/)).toBeInTheDocument();
  });

  test('renders user info when logged in', () => {
    render(<HeaderComponent />);
    expect(screen.getByText('TestUser')).toBeInTheDocument();
    expect(screen.getByText('User')).toBeInTheDocument();
    // Avatar initial
    expect(screen.getByText('T')).toBeInTheDocument();
  });

  test('calls logout and navigates to login on logout click', () => {
    render(<HeaderComponent />);
    
    // Buscar el botón de logout (es un IconButton con Tooltip "Logout")
    // Tooltip a veces complica el queryByRole, intentemos por el aria-label si existe o el icono.
    // El código no pone aria-label explícito, pero el Tooltip pone title.
    // Probemos buscar el botón.
    const buttons = screen.getAllByRole('button');
    // El último botón suele ser el logout o user profile.
    const logoutBtn = buttons[buttons.length - 1]; 
    
    fireEvent.click(logoutBtn);

    expect(mockLogout).toHaveBeenCalled();
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  test('navigates home on logo click', () => {
    render(<HeaderComponent />);
    
    // El texto está dividido en dos partes: "RECYCLE" y "AI" (dentro de un span)
    // Hacemos click en "RECYCLE" y el evento debería subir al Box padre
    const logoText = screen.getByText('RECYCL');
    fireEvent.click(logoText);
    
    expect(mockNavigate).toHaveBeenCalledWith('/user/123');
  });

  test('does not render user info if not logged in', () => {
    useAuth.mockReturnValue({ user: null, logout: mockLogout });
    render(<HeaderComponent />);
    
    expect(screen.queryByText('TestUser')).not.toBeInTheDocument();
    // No debería haber avatar
    expect(screen.queryByText('T')).not.toBeInTheDocument();
  });
});
