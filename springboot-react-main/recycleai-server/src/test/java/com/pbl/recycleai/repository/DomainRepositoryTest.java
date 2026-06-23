package com.pbl.recycleai.repository;

import com.pbl.recycleai.model.Domain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class DomainRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DomainRepository domainRepository;

    @Test
    void testDomainRepositoryIsNotNull() {
        Assertions.assertNotNull(domainRepository);
    }

    @Test
    void testSaveDomain() {
        Domain domain = new Domain();
        domain.setName("Test Domain");

        Domain savedDomain = domainRepository.save(domain);
        Assertions.assertNotNull(savedDomain.getDomainId());

        Domain foundDomain = entityManager.find(Domain.class, savedDomain.getDomainId());
        Assertions.assertEquals("Test Domain", foundDomain.getName());
    }
}