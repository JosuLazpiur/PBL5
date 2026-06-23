package com.pbl.recycleai.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.pbl.recycleai.model.User;
import com.pbl.recycleai.service.LoginService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping(value = "/login", 
        consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    public User login(@RequestBody User userRequest) {
        return loginService.login(userRequest.getUsername(), userRequest.getPassword());
    }
}
