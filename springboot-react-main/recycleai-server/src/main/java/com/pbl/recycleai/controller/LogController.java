package com.pbl.recycleai.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.pbl.recycleai.config.LogNotifier;
import com.pbl.recycleai.exception.BadRequestException;
import com.pbl.recycleai.exception.ResourceNotFoundException;
import com.pbl.recycleai.model.Bin;
import com.pbl.recycleai.model.Log;
import com.pbl.recycleai.repository.BinRepository;
import com.pbl.recycleai.repository.LogRepository;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogRepository logRepository;
    private final BinRepository binRepository;
    private final LogNotifier logNotifier;

    public LogController(LogRepository logRepository, BinRepository binRepository, LogNotifier logNotifier) {
        this.logRepository = logRepository;
        this.binRepository = binRepository;
        this.logNotifier = logNotifier;
    }

    @GetMapping(value = "/{binId}", produces = { "application/json", "application/xml" })
    public List<Log> getLast10Logs(@PathVariable("binId") Integer binId) {
        return logRepository.findTop10ByBin_BinIdOrderByDatetimeDesc(binId);
    }

    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }, 
                 produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public Log createLog(@RequestBody Log log) {
        if (log.getBin() == null || log.getBin().getBinId() == null) {
            throw new BadRequestException("Bin ID is required for the log");
        }
        Bin bin = binRepository.findById(log.getBin().getBinId())
                .orElseThrow(() -> new ResourceNotFoundException("Bin not found"));
        log.setBin(bin);

        if (log.getDatetime() == null) {
            log.setDatetime(LocalDateTime.now().toString());
        }
        Log savedLog = logRepository.save(log);
        logNotifier.notifyLogCreated(savedLog);
        return savedLog;
    }

    @PostMapping("/notify/{binId}")
    public void notifyNewLog(@PathVariable("binId") Integer binId) {
        Log latestLog = logRepository.findTopByBin_BinIdOrderByLogIdDesc(binId);
        if (latestLog != null) {
            logNotifier.notifyLogCreated(latestLog);
        }
    }
}