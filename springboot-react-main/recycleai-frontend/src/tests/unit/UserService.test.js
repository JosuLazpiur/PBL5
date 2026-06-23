import axios from 'axios';
import UserService from '../../services/UserService';

// axios ya está mockeado globalmente a través de src/__mocks__/axios.js

describe('UserService', () => {
  beforeEach(() => {
    // Limpiar los mocks de axios antes de cada test
    axios.get.mockClear();
  });

  test('getAllUsers should make a GET request to the correct API endpoint', async () => {
    const mockUsers = [{ id: 1, name: 'User 1' }, { id: 2, name: 'User 2' }];
    axios.get.mockResolvedValue({ data: mockUsers }); // Simular respuesta exitosa

    const result = await UserService.getAllUsers();

    // Verificar que axios.get fue llamado con la URL correcta
    expect(axios.get).toHaveBeenCalledTimes(1);
    expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/api/users');

    // Verificar que la función devuelve el resultado esperado
    expect(result.data).toEqual(mockUsers);
  });

  test('getAllUsers should handle API errors', async () => {
    const mockError = new Error('Network Error');
    axios.get.mockRejectedValue(mockError); // Simular un error de la API

    await expect(UserService.getAllUsers()).rejects.toThrow('Network Error');
    expect(axios.get).toHaveBeenCalledTimes(1);
    expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/api/users');
  });
});
