package com.pbl.recycleai.controller;

import com.pbl.recycleai.model.User;
import com.pbl.recycleai.service.LoginService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

class LoginControllerTest {

    @Test
    void testLogin() {
        LoginService loginService = Mockito.mock(LoginService.class);
        LoginController loginController = new LoginController(loginService);

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");

        User returnedUser = new User();
        returnedUser.setUserId(1);
        returnedUser.setUsername("testuser");


        when(loginService.login("testuser", "password")).thenReturn(returnedUser);

        User result = loginController.login(user);

        Assertions.assertEquals("testuser", result.getUsername());
    }
}
