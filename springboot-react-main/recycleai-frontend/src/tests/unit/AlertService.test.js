import axios from 'axios';
import AlertService from '../../services/AlertService';

describe('AlertService', () => {
  beforeEach(() => {
    axios.post.mockClear();
    axios.get.mockClear();
  });

  test('createAlert sends correct payload', async () => {
    const mockResponse = { data: { id: 1, title: 'Test Alert' } };
    axios.post.mockResolvedValue(mockResponse);

    const binId = 123;
    const title = 'Full Bin';
    const description = 'Bin is 100% full';

    const result = await AlertService.createAlert(binId, title, description);

    expect(axios.post).toHaveBeenCalledWith('http://localhost:8080/api/alerts', {
      title,
      description,
      bin: { binId: binId }
    });
    expect(result).toEqual(mockResponse.data);
  });

  test('getAlertsByBin fetches alerts correctly', async () => {
    const mockAlerts = [{ id: 1, title: 'Alert 1' }];
    axios.get.mockResolvedValue({ data: mockAlerts });

    const binId = 123;
    const result = await AlertService.getAlertsByBin(binId);

    expect(axios.get).toHaveBeenCalledWith(`http://localhost:8080/api/alerts/${binId}`);
    expect(result).toEqual(mockAlerts);
  });
});
