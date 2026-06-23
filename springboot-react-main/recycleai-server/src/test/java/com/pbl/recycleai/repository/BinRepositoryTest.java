package com.pbl.recycleai.repository;

import com.pbl.recycleai.model.Bin;
import com.pbl.recycleai.model.Domain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class BinRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BinRepository binRepository;

    @Test
    void testBinRepositoryIsNotNull() {
        Assertions.assertNotNull(binRepository);
    }

    @Test
    void testSaveBin() {
        // Create and persist a Domain
        Domain domain = new Domain();
        domain.setName("Test Domain for Bin");
        entityManager.persist(domain);
        entityManager.flush();

        Bin bin = new Bin();
        bin.setUbication("Test Ubication");
        bin.setDomain(domain); // Set the domain for the bin

        Bin savedBin = binRepository.save(bin);
        Assertions.assertNotNull(savedBin.getBinId());
        Assertions.assertNotNull(savedBin.getDomain());

        Bin foundBin = entityManager.find(Bin.class, savedBin.getBinId());
        Assertions.assertEquals("Test Ubication", foundBin.getUbication());
        Assertions.assertEquals(domain.getName(), foundBin.getDomain().getName());
    }
}
