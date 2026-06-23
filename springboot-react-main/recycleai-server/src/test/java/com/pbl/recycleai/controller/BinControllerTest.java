package com.pbl.recycleai.controller;

import com.pbl.recycleai.config.BinNotifier;
import com.pbl.recycleai.model.Bin;
import com.pbl.recycleai.model.Domain;
import com.pbl.recycleai.repository.BinRepository;
import com.pbl.recycleai.repository.DomainRepository;

import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BinControllerTest {

    @Test
    void testGetBinById() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinController binController = new BinController(binRepository, domainRepository, null);

        Bin bin = new Bin();
        bin.setBinId(1);
        bin.setUbication("Test Ubication");

        when(binRepository.findById(1)).thenReturn(Optional.of(bin));

        Bin result = binController.getBinById(1);

        Assertions.assertEquals("Test Ubication", result.getUbication());
    }

    @Test
    void testGetBinByIdNotFound() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinController binController = new BinController(binRepository, domainRepository, null);

        when(binRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> binController.getBinById(1));
    }

    @Test
    void testUpdateBin() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinNotifier binNotifier = Mockito.mock(BinNotifier.class); // Mock Notifier
        BinController binController = new BinController(binRepository, domainRepository, binNotifier);

        Bin existingBin = new Bin();
        existingBin.setBinId(1);
        existingBin.setUbication("Old Ubication");
        existingBin.setOperative(true);

        Bin updatedBinRequest = new Bin();
        updatedBinRequest.setUbication("New Ubication");
        updatedBinRequest.setOperative(false);

        when(binRepository.findById(1)).thenReturn(Optional.of(existingBin));
        when(binRepository.save(any(Bin.class))).thenAnswer(i -> i.getArguments()[0]);

        ResponseEntity<Bin> response = binController.updateBin(1, updatedBinRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("New Ubication", response.getBody().getUbication());
        Assertions.assertEquals(false, response.getBody().isOperative());
        
        verify(binNotifier).notifyBinUpdated(any(Bin.class));
    }

    @Test
    void testUpdateBinWithDomain() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinController binController = new BinController(binRepository, domainRepository, null);

        Bin existingBin = new Bin();
        existingBin.setBinId(1);
        
        Domain oldDomain = new Domain();
        oldDomain.setDomainId(1);
        existingBin.setDomain(oldDomain);

        Domain newDomain = new Domain();
        newDomain.setDomainId(2);

        Bin request = new Bin();
        request.setDomain(newDomain);

        when(binRepository.findById(1)).thenReturn(Optional.of(existingBin));
        when(domainRepository.findById(2)).thenReturn(Optional.of(newDomain));
        when(binRepository.save(any(Bin.class))).thenAnswer(i -> i.getArguments()[0]);

        ResponseEntity<Bin> response = binController.updateBin(1, request);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals(2, response.getBody().getDomain().getDomainId());
    }

    @Test
    void testUpdateBinNotFound() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinController binController = new BinController(binRepository, domainRepository, null);

        when(binRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> binController.updateBin(1, new Bin()));
    }

    @Test
    void testUpdateBinDomainNotFound() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinController binController = new BinController(binRepository, domainRepository, null);

        Bin existingBin = new Bin();
        existingBin.setBinId(1);

        Domain newDomain = new Domain();
        newDomain.setDomainId(99);
        Bin request = new Bin();
        request.setDomain(newDomain);

        when(binRepository.findById(1)).thenReturn(Optional.of(existingBin));
        when(domainRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> binController.updateBin(1, request));
    }

    @Test
    void testSaveBin() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinController binController = new BinController(binRepository, domainRepository, null);

        Domain domain = new Domain();
        domain.setDomainId(1);

        Bin bin = new Bin();
        bin.setUbication("New Bin");
        bin.setDomain(domain);

        when(domainRepository.findById(1)).thenReturn(Optional.of(domain));
        when(binRepository.save(any(Bin.class))).thenReturn(bin);

        Bin savedBin = binController.saveUser(bin);

        Assertions.assertEquals("New Bin", savedBin.getUbication());
    }

    @Test
    void testSaveBinDomainNotFound() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinController binController = new BinController(binRepository, domainRepository, null);

        Domain domain = new Domain();
        domain.setDomainId(99);
        Bin bin = new Bin();
        bin.setDomain(domain);

        when(domainRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> binController.saveUser(bin));
    }

    @Test
    void testGetBinsByDomainId() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinController binController = new BinController(binRepository, domainRepository, null);

        Domain domain = new Domain();
        domain.setDomainId(1);

        Bin bin1 = new Bin();
        bin1.setBinId(1);
        bin1.setUbication("Bin 1");
        bin1.setDomain(domain);

        Bin bin2 = new Bin();
        bin2.setBinId(2);
        bin2.setUbication("Bin 2");
        bin2.setDomain(domain);

        when(domainRepository.findById(1)).thenReturn(Optional.of(domain));
        when(binRepository.findByDomain(domain)).thenReturn(Arrays.asList(bin1, bin2));

        ResponseEntity<List<Bin>> response = binController.getBinsByDomainId(1);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals(2, response.getBody().size());
        Assertions.assertEquals("Bin 1", response.getBody().get(0).getUbication());
    }

    @Test
    void testGetBinsByDomainIdNotFound() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinController binController = new BinController(binRepository, domainRepository, null);

        when(domainRepository.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<List<Bin>> response = binController.getBinsByDomainId(1);
        Assertions.assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetBinsByDomainIdNoContent() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinController binController = new BinController(binRepository, domainRepository, null);

        Domain domain = new Domain();
        domain.setDomainId(1);

        when(domainRepository.findById(1)).thenReturn(Optional.of(domain));
        when(binRepository.findByDomain(domain)).thenReturn(Arrays.asList());

        ResponseEntity<List<Bin>> response = binController.getBinsByDomainId(1);
        Assertions.assertEquals(204, response.getStatusCode().value());
    }

    @Test
    void testDeleteBin() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinController binController = new BinController(binRepository, domainRepository, null);

        when(binRepository.existsById(1)).thenReturn(true);

        ResponseEntity<Void> response = binController.deleteBin(1);

        Assertions.assertEquals(204, response.getStatusCode().value());
        Mockito.verify(binRepository).deleteById(1);
    }

    @Test
    void testDeleteBinNotFound() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinController binController = new BinController(binRepository, domainRepository, null);

        when(binRepository.existsById(1)).thenReturn(false);

        ResponseEntity<Void> response = binController.deleteBin(1);

        Assertions.assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetAllBins() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinController binController = new BinController(binRepository, domainRepository, null);

        when(binRepository.findAll()).thenReturn(Arrays.asList(new Bin()));

        ResponseEntity<List<Bin>> response = binController.getAllBins();

        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testGetAllBinsEmpty() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinController binController = new BinController(binRepository, domainRepository, null);

        when(binRepository.findAll()).thenReturn(Arrays.asList());

        ResponseEntity<List<Bin>> response = binController.getAllBins();

        Assertions.assertEquals(204, response.getStatusCode().value());
    }

    @Test
    void testForceNotification() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinNotifier binNotifier = Mockito.mock(BinNotifier.class);
        BinController binController = new BinController(binRepository, domainRepository, binNotifier);

        Bin bin = new Bin();
        bin.setBinId(1);

        when(binRepository.findById(1)).thenReturn(Optional.of(bin));

        ResponseEntity<Void> response = binController.forceNotification(1);

        Assertions.assertEquals(200, response.getStatusCode().value());
        verify(binNotifier).notifyBinUpdated(bin);
    }
    
    @Test
    void testForceNotificationBinNotFound() {
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        DomainRepository domainRepository = Mockito.mock(DomainRepository.class);
        BinController binController = new BinController(binRepository, domainRepository, null);

        when(binRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> binController.forceNotification(1));
    }
}