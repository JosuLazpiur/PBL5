package com.pbl.recycleai.repository;

import com.pbl.recycleai.model.Bin;
import com.pbl.recycleai.model.Domain;
import com.pbl.recycleai.model.Image;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class ImageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ImageRepository imageRepository;

    @Test
    void testImageRepositoryIsNotNull() {
        Assertions.assertNotNull(imageRepository);
    }

    @Test
    void testSaveImage() {
        // Create and persist a Domain
        Domain domain = new Domain();
        domain.setName("Test Domain for Image");
        entityManager.persist(domain);
        entityManager.flush();

        // Create and persist a Bin
        Bin bin = new Bin();
        bin.setUbication("Test Ubication for Image");
        bin.setDomain(domain);
        entityManager.persist(bin);
        entityManager.flush();

        Image image = new Image();
        image.setPath("test/path/image.jpg");
        image.setBin(bin); // Set the bin for the image

        Image savedImage = imageRepository.save(image);
        Assertions.assertNotNull(savedImage.getImageId());
        Assertions.assertNotNull(savedImage.getBin());

        Image foundImage = entityManager.find(Image.class, savedImage.getImageId());
        Assertions.assertEquals("test/path/image.jpg", foundImage.getPath());
        Assertions.assertEquals(bin.getUbication(), foundImage.getBin().getUbication());
    }
}
