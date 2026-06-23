package com.pbl.recycleai.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DomainTest {

    @Test
    void testNoArgsConstructor() {
        Domain domain = new Domain();
        Assertions.assertNull(domain.getDomainId());
        Assertions.assertNull(domain.getName());
    }

    @Test
    void testAllArgsConstructor() {
        Domain domain = new Domain(1, "Test Name");
        Assertions.assertEquals(1, domain.getDomainId());
        Assertions.assertEquals("Test Name", domain.getName());
    }

    @Test
    void testSetters() {
        Domain domain = new Domain();
        domain.setDomainId(1);
        domain.setName("Test Name");
        Assertions.assertEquals(1, domain.getDomainId());
        Assertions.assertEquals("Test Name", domain.getName());
    }
}
