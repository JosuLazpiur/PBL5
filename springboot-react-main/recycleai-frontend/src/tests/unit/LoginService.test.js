import axios from 'axios';
import LoginService from '../../services/LoginService';

// El mock de axios ya está configurado globalmente en src/__mocks__/axios.js
// y el moduleNameMapper en package.json lo dirige correctamente.

describe('LoginService', () => {
  beforeEach(() => {
    // Limpiar el mock de axios.post antes de cada test
    axios.post.mockClear();
  });

  test('should call the login API with correct credentials', async () => {
    const mockResponse = { data: { token: 'fake-token' } };
    axios.post.mockResolvedValue(mockResponse); // Simula una respuesta exitosa

    const username = 'testuser';
    const password = 'testpassword';
    
    // Llama a la función login del servicio
    const result = await LoginService.login(username, password);

    // Verifica que axios.post fue llamado con la URL y los datos correctos
    expect(axios.post).toHaveBeenCalledTimes(1);
    expect(axios.post).toHaveBeenCalledWith('http://localhost:8080/login', { username, password });

    // Verifica que la función devuelve el resultado esperado
    expect(result).toEqual(mockResponse);
  });

  test('should handle login API errors', async () => {
    const mockError = new Error('Network Error');
    axios.post.mockRejectedValue(mockError); // Simula un error de la API

    const username = 'testuser';
    const password = 'testpassword';

    // Se espera que la promesa sea rechazada
    await expect(LoginService.login(username, password)).rejects.toThrow('Network Error');

    // Verifica que axios.post fue llamado
    expect(axios.post).toHaveBeenCalledTimes(1);
    expect(axios.post).toHaveBeenCalledWith('http://localhost:8080/login', { username, password });
  });
});
