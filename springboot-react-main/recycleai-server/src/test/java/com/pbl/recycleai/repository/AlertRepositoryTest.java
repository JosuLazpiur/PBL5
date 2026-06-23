package com.pbl.recycleai.repository;

import com.pbl.recycleai.model.Alert;
import com.pbl.recycleai.model.Bin;
import com.pbl.recycleai.model.Domain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class AlertRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AlertRepository alertRepository;

    @Test
    void testAlertRepositoryIsNotNull() {
        Assertions.assertNotNull(alertRepository);
    }

    @Test
    void testSaveAlert() {
        // Create and persist a Domain
        Domain domain = new Domain();
        domain.setName("Test Domain for Alert");
        entityManager.persist(domain);
        entityManager.flush();

        // Create and persist a Bin
        Bin bin = new Bin();
        bin.setUbication("Test Ubication for Alert");
        bin.setDomain(domain);
        entityManager.persist(bin);
        entityManager.flush();

        Alert alert = new Alert();
        alert.setTitle("Test Alert");
        alert.setDescription("Test Description");
        alert.setBin(bin); // Set the bin for the alert

        Alert savedAlert = alertRepository.save(alert);
        Assertions.assertNotNull(savedAlert.getAlertId());
        Assertions.assertNotNull(savedAlert.getBin());

        Alert foundAlert = entityManager.find(Alert.class, savedAlert.getAlertId());
        Assertions.assertEquals("Test Alert", foundAlert.getTitle());
        Assertions.assertEquals(bin.getUbication(), foundAlert.getBin().getUbication());
    }
}
