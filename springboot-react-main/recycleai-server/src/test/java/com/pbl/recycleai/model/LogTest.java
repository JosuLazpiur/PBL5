package com.pbl.recycleai.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LogTest {

    @Test
    void testNoArgsConstructor() {
        Log log = new Log();
        Assertions.assertNull(log.getLogId());
        Assertions.assertNull(log.getDatetime());
        Assertions.assertNull(log.getDescription());
        Assertions.assertNull(log.getBin());
    }

    @Test
    void testAllArgsConstructor() {
        Bin bin = new Bin();
        Log log = new Log(1, "2025-12-12T12:00:00", "Test Description", bin);
        Assertions.assertEquals(1, log.getLogId());
        Assertions.assertEquals("2025-12-12T12:00:00", log.getDatetime());
        Assertions.assertEquals("Test Description", log.getDescription());
        Assertions.assertEquals(bin, log.getBin());
    }

    @Test
    void testSettersAndGetters() {
        Log log = new Log();
        Bin bin = new Bin();

        log.setLogId(1);
        Assertions.assertEquals(1, log.getLogId());

        log.setDatetime("2025-12-12T12:00:00");
        Assertions.assertEquals("2025-12-12T12:00:00", log.getDatetime());

        log.setDescription("Test Description");
        Assertions.assertEquals("Test Description", log.getDescription());

        log.setBin(bin);
        Assertions.assertEquals(bin, log.getBin());
    }
}
