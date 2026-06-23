package com.pbl.recycleai.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ImageTest {

    @Test
    void testNoArgsConstructor() {
        Image image = new Image();
        Assertions.assertNull(image.getImageId());
        Assertions.assertNull(image.getPath());
        Assertions.assertNull(image.getBin());
    }

    @Test
    void testAllArgsConstructor() {
        Bin bin = new Bin();
        Image image = new Image(1, "Test Path", bin);
        Assertions.assertEquals(1, image.getImageId());
        Assertions.assertEquals("Test Path", image.getPath());
        Assertions.assertEquals(bin, image.getBin());
    }

    @Test
    void testSettersAndGetters() {
        Image image = new Image();
        Bin bin = new Bin();

        image.setImageId(1);
        Assertions.assertEquals(1, image.getImageId());

        image.setPath("Test Path");
        Assertions.assertEquals("Test Path", image.getPath());

        image.setBin(bin);
        Assertions.assertEquals(bin, image.getBin());
    }
}
