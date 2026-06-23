package com.pbl.recycleai.repository;

import com.pbl.recycleai.model.Domain;
import com.pbl.recycleai.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testUserRepositoryIsNotNull() {
        Assertions.assertNotNull(userRepository);
    }

    @Test
    void testSaveUser() {
        // Create and persist a Domain
        Domain domain = new Domain();
        domain.setName("Test Domain");
        entityManager.persist(domain); // Persist the domain first
        entityManager.flush();

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");
        user.setName("Test User");
        user.setDomain(domain); // Set the domain for the user

        User savedUser = userRepository.save(user);
        Assertions.assertNotNull(savedUser.getUserId());
        Assertions.assertNotNull(savedUser.getDomain()); // Assert domain is also saved

        User foundUser = entityManager.find(User.class, savedUser.getUserId());
        Assertions.assertEquals("testuser", foundUser.getUsername());
        Assertions.assertEquals(domain.getName(), foundUser.getDomain().getName()); // Verify domain association
    }
}
