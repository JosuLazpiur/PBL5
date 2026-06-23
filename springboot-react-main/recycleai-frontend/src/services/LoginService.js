import axios from "axios";

const API_URL = "http://localhost:8080";

const login = (username, password) => {
    return axios.post(`${API_URL}/login`, { username, password });
};

const LoginService = { login };

export default LoginService;