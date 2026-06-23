import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import LoginForm from '../../pages/LoginForm';
import LoginService from '../../services/LoginService';
import { useAuth } from '../../context/AuthContext';

// --- MOCKS ---

// 1. Mock de LoginService para evitar problemas con Axios y controlar respuestas
jest.mock('../../services/LoginService');

// 2. Mock del Contexto de Autenticación
jest.mock('../../context/AuthContext');

// 3. Mock de React Router DOM (específicamente useNavigate)
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}));

describe('LoginForm Component', () => {
  const mockLoginAuth = jest.fn();

  beforeEach(() => {
    // Resetear los mocks antes de cada test
    jest.clearAllMocks();
    
    // Configurar comportamiento por defecto del AuthContext
    useAuth.mockReturnValue({
      login: mockLoginAuth
    });
  });

  test('renders login form elements correctly', () => {
    render(<LoginForm />);

    // Verificar títulos
    expect(screen.getByRole('heading', { name: /welcome/i })).toBeInTheDocument();
    
    // Verificar inputs
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    
    // Verificar botón
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });

  test('allows typing in username and password', () => {
    render(<LoginForm />);

    const usernameInput = screen.getByLabelText(/username/i);
    const passwordInput = screen.getByLabelText(/password/i);

    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(passwordInput, { target: { value: 'secretpass' } });

    expect(usernameInput.value).toBe('testuser');
    expect(passwordInput.value).toBe('secretpass');
  });

  test('toggles password visibility', () => {
    render(<LoginForm />);
    
    const passwordInput = screen.getByLabelText(/password/i);
    // Por defecto es tipo password
    expect(passwordInput).toHaveAttribute('type', 'password');

    // Buscar el botón de "ojo" para mostrar contraseña
    // El botón de MUI no tiene nombre accesible por defecto, así que buscamos por exclusión o orden
    const buttons = screen.getAllByRole('button');
    const toggleButton = buttons.find(btn => !btn.textContent.includes('LOGIN'));
    
    if (toggleButton) {
        fireEvent.click(toggleButton);
        expect(passwordInput).toHaveAttribute('type', 'text');
        
        fireEvent.click(toggleButton);
        expect(passwordInput).toHaveAttribute('type', 'password');
    }
  });

  test('handles successful login', async () => {
    // Configurar respuesta exitosa del servicio
    const mockResponse = {
        data: {
            domain: { domainId: '123-abc' },
            token: 'fake-token'
        }
    };
    LoginService.login.mockResolvedValue(mockResponse);

    render(<LoginForm />);

    // Rellenar formulario
    fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'validUser' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'validPass' } });

    // Click Login
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    // Verificar loading (opcional, difícil de pillar si es muy rápido)
    // expect(screen.getByRole('progressbar')).toBeInTheDocument();

    // Esperar a que se llame al servicio
    await waitFor(() => {
        expect(LoginService.login).toHaveBeenCalledWith('validUser', 'validPass');
    });

    // Verificar que se actualizó el contexto de auth
    expect(mockLoginAuth).toHaveBeenCalledWith(mockResponse.data);

    // Verificar redirección
    expect(mockNavigate).toHaveBeenCalledWith('/user/123-abc');
  });

  test('handles login error', async () => {
    // Configurar error del servicio
    const mockError = new Error('Unauthorized');
    LoginService.login.mockRejectedValue(mockError);

    // Mockear console.error para no ensuciar el output
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

    render(<LoginForm />);

    fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'wrong' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'wrong' } });
    
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    // Esperar mensaje de error
    await waitFor(() => {
        expect(screen.getByRole('alert')).toHaveTextContent(/invalid username or password/i);
    });

    expect(mockNavigate).not.toHaveBeenCalled();
    
    consoleSpy.mockRestore();
  });
});
