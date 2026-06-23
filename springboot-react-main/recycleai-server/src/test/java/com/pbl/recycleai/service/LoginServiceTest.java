package com.pbl.recycleai.service;

import com.pbl.recycleai.exception.ResourceNotFoundException;
import com.pbl.recycleai.exception.UnauthorizedException;
import com.pbl.recycleai.model.User;
import com.pbl.recycleai.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoginService loginService;

    @Test
    void whenLoginWithCorrectCredentials_thenReturnUser() {
        // given
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        when(userRepository.findByUsername("testuser")).thenReturn(user);

        // when
        User result = loginService.login("testuser", "password");

        // then
        Assertions.assertEquals(user, result);
    }

    @Test
    void whenLoginWithNonExistentUser_thenThrowException() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(null);

        // when & then
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            loginService.login("testuser", "password");
        });
    }

    @Test
    void whenLoginWithIncorrectPassword_thenThrowException() {
        // given
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        when(userRepository.findByUsername("testuser")).thenReturn(user);

        // when & then
        Assertions.assertThrows(UnauthorizedException.class, () -> {
            loginService.login("testuser", "wrongpassword");
        });
    }
}
