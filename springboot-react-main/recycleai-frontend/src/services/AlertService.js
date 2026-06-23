import axios from "axios";

const API_URL = "http://localhost:8080/api/alerts"; 

const createAlert = async (binId, title, description) => {

  const payload = {
    title,
    description,
    bin: {           
        binId: binId
    }
  };

  const response = await axios.post(API_URL, payload);
  return response.data;
};

const getAlertsByBin = async (binId) => {
  const response = await axios.get(`${API_URL}/${binId}`);
  return response.data;
};

export default {
  createAlert,
  getAlertsByBin,
};