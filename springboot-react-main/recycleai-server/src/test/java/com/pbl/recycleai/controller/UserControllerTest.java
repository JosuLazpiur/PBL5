package com.pbl.recycleai.controller;

import com.pbl.recycleai.model.Domain;
import com.pbl.recycleai.model.User;
import com.pbl.recycleai.repository.DomainRepository;
import com.pbl.recycleai.repository.UserRepository;

import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Test
    void testUserList() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        UserController userController = new UserController(userRepository, domainRepository);

        User user1 = new User();
        user1.setUserId(1);
        user1.setName("User 1");

        User user2 = new User();
        user2.setUserId(2);
        user2.setName("User 2");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = userController.userList();

        Assertions.assertEquals(2, users.size());
        Assertions.assertEquals("User 1", users.get(0).getName());
    }

    @Test
    void testSaveUser() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        UserController userController = new UserController(userRepository, domainRepository);

        Domain domain = new Domain();
        domain.setDomainId(1);
        domain.setName("Test Domain");

        User user = new User();
        user.setName("New User");
        user.setDomain(domain);

        when(domainRepository.findById(1)).thenReturn(Optional.of(domain));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userController.saveUser(user);

        Assertions.assertEquals("New User", savedUser.getName());
    }

    @Test
    void testSaveUserDomainNotFound() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        UserController userController = new UserController(userRepository, domainRepository);

        Domain domain = new Domain();
        domain.setDomainId(1);

        User user = new User();
        user.setDomain(domain);

        when(domainRepository.findById(1)).thenReturn(Optional.empty());

        Assertions.assertThrows(RuntimeException.class, () -> userController.saveUser(user));
    }

    @Test
    void testUserListById() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        UserController userController = new UserController(userRepository, domainRepository);

        User user = new User();
        user.setUserId(1);
        user.setName("Test User");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userController.userListById(1);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("Test User", response.getBody().getName());
    }

    @Test
    void testUpdateUser() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        UserController userController = new UserController(userRepository, domainRepository);

        Domain domain = new Domain();
        domain.setDomainId(1);
        domain.setName("Test Domain");

        User existingUser = new User();
        existingUser.setUserId(1);
        existingUser.setName("Old Name");
        existingUser.setDomain(domain);

        User updatedUser = new User();
        updatedUser.setName("New Name");
        updatedUser.setUsername("newuser");
        updatedUser.setPassword("newpass");
        updatedUser.setDomain(domain);

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(domainRepository.findById(1)).thenReturn(Optional.of(domain));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        ResponseEntity<User> response = userController.updateUser(1, updatedUser);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("New Name", response.getBody().getName());
    }

    @Test
    void testDeleteUser() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        UserController userController = new UserController(userRepository, domainRepository);

        User user = new User();
        user.setUserId(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        ResponseEntity<Map<String, Boolean>> response = userController.deleteUser(1);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertTrue(response.getBody().get("deleted"));
        Mockito.verify(userRepository).delete(user);
    }
}