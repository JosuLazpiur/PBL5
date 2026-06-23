package com.pbl.recycleai.controller;

import com.pbl.recycleai.model.Domain;
import com.pbl.recycleai.repository.DomainRepository;


import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DomainControllerTest {

    @Test
    void testDomainList() {
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        DomainController domainController = new DomainController(domainRepository);

        Domain domain1 = new Domain();
        domain1.setDomainId(1);
        domain1.setName("Domain 1");

        Domain domain2 = new Domain();
        domain2.setDomainId(2);
        domain2.setName("Domain 2");

        when(domainRepository.findAll()).thenReturn(Arrays.asList(domain1, domain2));

        List<Domain> domains = domainController.domainList();

        Assertions.assertEquals(2, domains.size());
        Assertions.assertEquals("Domain 1", domains.get(0).getName());
    }

    @Test
    void testSaveDomain() {
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        DomainController domainController = new DomainController(domainRepository);

        Domain domain = new Domain();
        domain.setName("New Domain");

        when(domainRepository.save(any(Domain.class))).thenReturn(domain);

        Domain savedDomain = domainController.saveUser(domain);

        Assertions.assertEquals("New Domain", savedDomain.getName());
    }
}
