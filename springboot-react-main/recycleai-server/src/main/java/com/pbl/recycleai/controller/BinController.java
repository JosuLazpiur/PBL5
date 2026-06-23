package com.pbl.recycleai.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; 

import com.pbl.recycleai.config.BinNotifier;
import com.pbl.recycleai.model.Bin;
import com.pbl.recycleai.model.Domain;
import com.pbl.recycleai.repository.BinRepository;
import com.pbl.recycleai.repository.DomainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class BinController {

    private static final String BIN_NOT_FOUND = "Bin not found";
    private static final Logger logger = LoggerFactory.getLogger(BinController.class);

    private final BinRepository binRepository;
    private final DomainRepository domainRepository;
    private final BinNotifier binNotifier;

    public BinController(BinRepository binRepository, DomainRepository domainRepository, @Autowired(required = false) BinNotifier binNotifier) {
        this.binRepository = binRepository;
        this.domainRepository = domainRepository;
        this.binNotifier = binNotifier;
    }

    @GetMapping(value = "/bin/{binId}", produces = { "application/json", "application/xml" })
    public Bin getBinById(@PathVariable("binId") Integer binId) {
        return binRepository.findById(binId)
                .orElseThrow(() -> new RuntimeException(BIN_NOT_FOUND));
    }

    @GetMapping(value = "/bins", produces = { "application/json", "application/xml" })
    public ResponseEntity<List<Bin>> getAllBins() {
        List<Bin> bins = binRepository.findAll();
        if (bins.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(bins);
    }

    @PutMapping("/bin/{binId}")
    public ResponseEntity<Bin> updateBin(@PathVariable("binId") Integer binId, @RequestBody Bin binRequest) {
        Bin bin = binRepository.findById(binId)
                .orElseThrow(() -> new RuntimeException(BIN_NOT_FOUND));

        if (binRequest.getUbication() != null) {
            bin.setUbication(binRequest.getUbication());
        }

        if (binRequest.isOperative() != null) { 
            bin.setOperative(binRequest.isOperative());
        }

        if (binRequest.getDomain() != null && binRequest.getDomain().getDomainId() != null) {
            Integer domainId = binRequest.getDomain().getDomainId();
            Domain domain = domainRepository.findById(domainId)
                    .orElseThrow(() -> new RuntimeException("Domain not found"));
            bin.setDomain(domain);
        }

        Bin updatedBin = binRepository.save(bin);

        if (binNotifier != null) {
            binNotifier.notifyBinUpdated(updatedBin);
        }

        return ResponseEntity.ok(updatedBin);
    }

    @PostMapping("/bin")
    public Bin saveUser(@RequestBody Bin bin) {
        Domain domain = domainRepository.findById(bin.getDomain().getDomainId())
                .orElseThrow(() -> new RuntimeException("Domain not found"));
        bin.setDomain(domain);
        return binRepository.save(bin);
    }

    @GetMapping("/bin/domain/{domainId}")
    public ResponseEntity<List<Bin>> getBinsByDomainId(@PathVariable("domainId") Integer domainId) {
        Optional<Domain> domainOpt = domainRepository.findById(domainId);
        if (domainOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Bin> bins = binRepository.findByDomain(domainOpt.get());
        if (bins.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(bins);
    }

    @DeleteMapping("/bin/{binId}")
    public ResponseEntity<Void> deleteBin(@PathVariable("binId") Integer binId) {
        if (!binRepository.existsById(binId)) {
            return ResponseEntity.notFound().build();
        }
        binRepository.deleteById(binId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/notify/{binId}")
    public ResponseEntity<Void> forceNotification(@PathVariable("binId") Integer binId) {
        logger.info(">> Notificación recibida para Bin {}", binId);
        Bin bin = binRepository.findById(binId)
                .orElseThrow(() -> new RuntimeException(BIN_NOT_FOUND));

        if (binNotifier != null) {
            binNotifier.notifyBinUpdated(bin);
        }
        return ResponseEntity.ok().build();
    }
}