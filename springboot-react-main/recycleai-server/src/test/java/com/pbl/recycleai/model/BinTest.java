package com.pbl.recycleai.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BinTest {

    @Test
    void testNoArgsConstructor() {
        Bin bin = new Bin();
        Assertions.assertNull(bin.getBinId());
        Assertions.assertNull(bin.getUbication());
        Assertions.assertNull(bin.getDomain());
    }

    @Test
    void testAllArgsConstructor() {
        Domain domain = new Domain();
        Bin bin = new Bin(1, "Test Ubication", domain);
        Assertions.assertEquals(1, bin.getBinId());
        Assertions.assertEquals("Test Ubication", bin.getUbication());
        Assertions.assertEquals(domain, bin.getDomain());
    }

    @Test
    void testSettersAndGetters() {
        Bin bin = new Bin();
        Domain domain = new Domain();

        bin.setBinId(1);
        Assertions.assertEquals(1, bin.getBinId());

        bin.setUbication("Test Ubication");
        Assertions.assertEquals("Test Ubication", bin.getUbication());

        bin.setDomain(domain);
        Assertions.assertEquals(domain, bin.getDomain());
    }
}
