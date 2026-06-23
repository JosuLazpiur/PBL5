package com.pbl.recycleai.service;

import org.springframework.stereotype.Service;

import com.pbl.recycleai.exception.ResourceNotFoundException;
import com.pbl.recycleai.exception.UnauthorizedException;
import com.pbl.recycleai.model.User;
import com.pbl.recycleai.repository.UserRepository;

@Service
public class LoginService {

    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String username, String password) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        if (!user.getPassword().equals(password)) {
            throw new UnauthorizedException("Incorrect password");
        }

        return user;
    }
}
