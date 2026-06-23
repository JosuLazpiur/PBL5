package com.pbl.recycleai.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void testNoArgsConstructor() {
        User user = new User();
        Assertions.assertNull(user.getUserId());
        Assertions.assertNull(user.getName());
        Assertions.assertNull(user.getUsername());
        Assertions.assertNull(user.getPassword());
        Assertions.assertNull(user.getDomain());
    }

    @Test
    void testAllArgsConstructor() {
        Domain domain = new Domain();
        User user = new User(1, "Test Name", "testuser", "password", domain);
        Assertions.assertEquals(1, user.getUserId());
        Assertions.assertEquals("Test Name", user.getName());
        Assertions.assertEquals("testuser", user.getUsername());
        Assertions.assertEquals("password", user.getPassword());
        Assertions.assertEquals(domain, user.getDomain());
    }

    @Test
    void testSettersAndGetters() {
        User user = new User();
        Domain domain = new Domain();

        user.setUserId(1);
        Assertions.assertEquals(1, user.getUserId());

        user.setName("Test Name");
        Assertions.assertEquals("Test Name", user.getName());

        user.setUsername("testuser");
        Assertions.assertEquals("testuser", user.getUsername());

        user.setPassword("password");
        Assertions.assertEquals("password", user.getPassword());

        user.setDomain(domain);
        Assertions.assertEquals(domain, user.getDomain());
    }
}
