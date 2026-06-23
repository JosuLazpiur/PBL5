package com.pbl.recycleai.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.pbl.recycleai.config.AlertNotifier;
import com.pbl.recycleai.model.Alert;
import com.pbl.recycleai.model.Bin;
import com.pbl.recycleai.repository.AlertRepository;
import com.pbl.recycleai.repository.BinRepository;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final BinRepository binRepository;
    private final AlertRepository alertRepository;
    private final AlertNotifier alertNotifier;

    public AlertController(BinRepository binRepository, AlertRepository alertRepository, AlertNotifier alertNotifier) {
        this.binRepository = binRepository;
        this.alertRepository = alertRepository;
        this.alertNotifier = alertNotifier;
    }

    @GetMapping(value = "/{binId}", produces = { "application/json", "application/xml" })
    public List<Alert> getAlertsByBin(@PathVariable("binId") Integer binId) {
        return alertRepository.findByBin_BinIdOrderByDatetimeDesc(binId);
    }

    @PostMapping(
        consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    public Alert createAlert(@RequestBody Alert alert) {
        Bin bin = binRepository.findById(alert.getBin().getBinId())
                    .orElseThrow(() -> new RuntimeException("Bin not found"));
        alert.setBin(bin);

        if (alert.getDatetime() == null || alert.getDatetime().isEmpty()) {
            alert.setDatetime(LocalDateTime.now().toString());
        }
        Alert savedAlert = alertRepository.save(alert);
        alertNotifier.notifyAlertCreated(savedAlert);
        return savedAlert;
    }

    @PostMapping("/notify/{binId}")
    public void notifyNewAlert(@PathVariable("binId") Integer binId) {
        Alert latestAlert = alertRepository.findTopByBin_BinIdOrderByAlertIdDesc(binId);
        if (latestAlert != null) {
            alertNotifier.notifyAlertCreated(latestAlert);
        }
    }
}