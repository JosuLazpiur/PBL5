package com.pbl.recycleai.repository;

import com.pbl.recycleai.model.Bin;
import com.pbl.recycleai.model.Domain;
import com.pbl.recycleai.model.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class LogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LogRepository logRepository;

    @Test
    void testLogRepositoryIsNotNull() {
        Assertions.assertNotNull(logRepository);
    }

    @Test
    void testSaveLog() {
        // Create and persist a Domain
        Domain domain = new Domain();
        domain.setName("Test Domain for Log");
        entityManager.persist(domain);
        entityManager.flush();

        // Create and persist a Bin
        Bin bin = new Bin();
        bin.setUbication("Test Ubication for Log");
        bin.setDomain(domain);
        entityManager.persist(bin);
        entityManager.flush();

        Log log = new Log();
        log.setDescription("Test Log");
        log.setBin(bin); // Set the bin for the log

        Log savedLog = logRepository.save(log);
        Assertions.assertNotNull(savedLog.getLogId());
        Assertions.assertNotNull(savedLog.getBin());

        Log foundLog = entityManager.find(Log.class, savedLog.getLogId());
        Assertions.assertEquals("Test Log", foundLog.getDescription());
        Assertions.assertEquals(bin.getUbication(), foundLog.getBin().getUbication());
    }
}
