package com.pbl.recycleai.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbl.recycleai.model.Domain;
import com.pbl.recycleai.model.User;
import com.pbl.recycleai.repository.DomainRepository;
import com.pbl.recycleai.repository.UserRepository;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class UserController {
    
    private final UserRepository userRepository;

    private final DomainRepository domainRepository;

    public UserController(UserRepository userRepository, DomainRepository domainRepository) {
        this.userRepository = userRepository;
        this.domainRepository = domainRepository;
    }

    @GetMapping("/users")   
    public List<User> userList(){
        return userRepository.findAll();
    }

    @PostMapping(value = "/user", 
        consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    public User saveUser(@RequestBody User user){
        
        Domain domain = domainRepository.findById(user.getDomain().getDomainId())
                            .orElseThrow(() -> new RuntimeException("Domain not found"));
        user.setDomain(domain);
        return userRepository.save(user);
    }


    @GetMapping("/user/{id}")   
    public ResponseEntity<User> userListById(@PathVariable Integer id){
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }
 
    @PutMapping("user/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User userRequest){
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setName(userRequest.getName());
        user.setPassword(userRequest.getPassword());
        user.setUsername(userRequest.getUsername());

        Domain domain = domainRepository.findById(userRequest.getDomain().getDomainId())
                            .orElseThrow(() -> new RuntimeException("Domain not found"));
        user.setDomain(domain);

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteUser(@PathVariable Integer id){
        User user = userRepository.findById(id)
        .orElseThrow();
        userRepository.delete(user);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }
}
