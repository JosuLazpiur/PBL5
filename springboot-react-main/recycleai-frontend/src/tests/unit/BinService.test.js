import axios from 'axios';
import BinService from '../../services/BinService';

describe('BinService', () => {
  beforeEach(() => {
    axios.get.mockClear();
    axios.put.mockClear();
  });

  test('getBinById fetches bin correctly', async () => {
    const mockBin = { binId: 1, name: 'Bin 1' };
    axios.get.mockResolvedValue({ data: mockBin });

    const binId = 1;
    await BinService.getBinById(binId);

    expect(axios.get).toHaveBeenCalledWith(`http://localhost:8080/api/bin/${binId}`);
  });

  test('getBinsByDomain fetches bins list correctly', async () => {
    const mockBins = [{ binId: 1 }, { binId: 2 }];
    axios.get.mockResolvedValue({ data: mockBins });

    const domainId = 'domain123';
    await BinService.getBinsByDomain(domainId);

    expect(axios.get).toHaveBeenCalledWith(`http://localhost:8080/api/bin/domain/${domainId}`);
  });

  test('updateBin sends correct update data', async () => {
    const mockResponse = { data: { success: true } };
    axios.put.mockResolvedValue(mockResponse);

    const binId = 1;
    const binData = { name: 'Updated Name' };
    await BinService.updateBin(binId, binData);

    expect(axios.put).toHaveBeenCalledWith(`http://localhost:8080/api/bin/${binId}`, binData);
  });
});
