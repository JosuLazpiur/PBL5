import axios from "axios"
const CLIENT_BASE_REST_API = "http://localhost:8080/api/users"

class UserService{
    getAllUsers(){
        return axios.get(CLIENT_BASE_REST_API)
    }
}

export default new UserService();