import axios from 'axios';
import { getLogsByBin } from '../../services/LogService'; // LogService uses named export

describe('LogService', () => {
  beforeEach(() => {
    axios.get.mockClear();
  });

  test('getLogsByBin fetches logs correctly', async () => {
    const mockLogs = [{ id: 1, message: 'Log 1' }];
    axios.get.mockResolvedValue({ data: mockLogs });

    const binId = 123;
    const result = await getLogsByBin(binId);

    expect(axios.get).toHaveBeenCalledWith(`http://localhost:8080/api/logs/${binId}`);
    expect(result).toEqual(mockLogs);
  });
});
