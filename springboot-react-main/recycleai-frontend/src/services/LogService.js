import axios from "axios";

const API_URL = "http://localhost:8080/api/logs";

export const getLogsByBin = async (binId) => {
  const response = await axios.get(`${API_URL}/${binId}`);
  return response.data;
};