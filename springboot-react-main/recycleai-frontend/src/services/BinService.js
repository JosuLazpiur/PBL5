import axios from "axios";

const API_URL = "http://localhost:8080/api";

const getBinById = (binId) => {
  return axios.get(`${API_URL}/bin/${binId}`);
};

const getBinsByDomain = (domainId) => {
  return axios.get(`${API_URL}/bin/domain/${domainId}`);
};

const updateBin = (binId, binData) => {
  return axios.put(`${API_URL}/bin/${binId}`, binData);
};

const BinService = {
  getBinById,
  getBinsByDomain,
  updateBin,
};

export default BinService;
