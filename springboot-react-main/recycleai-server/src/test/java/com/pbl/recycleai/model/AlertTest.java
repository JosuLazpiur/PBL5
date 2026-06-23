package com.pbl.recycleai.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AlertTest {

    @Test
    void testNoArgsConstructor() {
        Alert alert = new Alert();
        Assertions.assertNull(alert.getAlertId());
        Assertions.assertNull(alert.getTitle());
        Assertions.assertNull(alert.getDescription());
        Assertions.assertNull(alert.getDatetime());
        Assertions.assertNull(alert.getBin());
    }

    @Test
    void testAllArgsConstructor() {
        Bin bin = new Bin();
        Alert alert = new Alert(1, "Test Title", "Test Description", "2025-12-12T12:00:00", bin);
        Assertions.assertEquals(1, alert.getAlertId());
        Assertions.assertEquals("Test Title", alert.getTitle());
        Assertions.assertEquals("Test Description", alert.getDescription());
        Assertions.assertEquals("2025-12-12T12:00:00", alert.getDatetime());
        Assertions.assertEquals(bin, alert.getBin());
    }

    @Test
    void testSettersAndGetters() {
        Alert alert = new Alert();
        Bin bin = new Bin();

        alert.setAlertId(1);
        Assertions.assertEquals(1, alert.getAlertId());

        alert.setTitle("Test Title");
        Assertions.assertEquals("Test Title", alert.getTitle());

        alert.setDescription("Test Description");
        Assertions.assertEquals("Test Description", alert.getDescription());

        alert.setDatetime("2025-12-12T12:00:00");
        Assertions.assertEquals("2025-12-12T12:00:00", alert.getDatetime());

        alert.setBin(bin);
        Assertions.assertEquals(bin, alert.getBin());
    }
}
