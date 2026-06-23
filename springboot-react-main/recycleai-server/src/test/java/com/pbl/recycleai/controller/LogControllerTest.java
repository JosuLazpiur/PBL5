package com.pbl.recycleai.controller;

import com.pbl.recycleai.model.Bin;
import com.pbl.recycleai.model.Log;
import com.pbl.recycleai.repository.BinRepository;
import com.pbl.recycleai.repository.LogRepository;
import com.pbl.recycleai.config.LogNotifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class LogControllerTest {

    @Test
    void testGetLast10Logs() {
        LogRepository logRepository = Mockito.mock(LogRepository.class);
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        LogNotifier logNotifier = Mockito.mock(LogNotifier.class);
        LogController logController = new LogController(logRepository, binRepository, logNotifier);

        Log log1 = new Log();
        log1.setLogId(1);
        log1.setDescription("Log 1");

        Log log2 = new Log();
        log2.setLogId(2);
        log2.setDescription("Log 2");

        when(logRepository.findTop10ByBin_BinIdOrderByDatetimeDesc(1)).thenReturn(Arrays.asList(log1, log2));

        List<Log> logs = logController.getLast10Logs(1);

        Assertions.assertEquals(2, logs.size());
        Assertions.assertEquals("Log 1", logs.get(0).getDescription());
    }

    @Test
    void testCreateLog() {
        LogRepository logRepository = Mockito.mock(LogRepository.class);
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        LogNotifier logNotifier = Mockito.mock(LogNotifier.class);
        LogController logController = new LogController(logRepository, binRepository, logNotifier);

        Bin bin = new Bin();
        bin.setBinId(1);

        Log log = new Log();
        log.setDescription("New Log");
        log.setBin(bin);
        log.setDatetime(LocalDateTime.now().toString());

        when(binRepository.findById(1)).thenReturn(Optional.of(bin));
        when(logRepository.save(any(Log.class))).thenReturn(log);

        Log createdLog = logController.createLog(log);

        Assertions.assertEquals("New Log", createdLog.getDescription());
    }

    @Test
    void testCreateLog_MissingBin() {
        LogRepository logRepository = Mockito.mock(LogRepository.class);
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        LogNotifier logNotifier = Mockito.mock(LogNotifier.class);
        LogController logController = new LogController(logRepository, binRepository, logNotifier);

        Log log = new Log();
        log.setDescription("Log without bin");

        Assertions.assertThrows(com.pbl.recycleai.exception.BadRequestException.class, () -> {
            logController.createLog(log);
        });
    }

    @Test
    void testCreateLog_BinNotFound() {
        LogRepository logRepository = Mockito.mock(LogRepository.class);
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        LogNotifier logNotifier = Mockito.mock(LogNotifier.class);
        LogController logController = new LogController(logRepository, binRepository, logNotifier);

        Bin bin = new Bin();
        bin.setBinId(99);

        Log log = new Log();
        log.setDescription("Log for missing bin");
        log.setBin(bin);

        when(binRepository.findById(99)).thenReturn(Optional.empty());

        Assertions.assertThrows(com.pbl.recycleai.exception.ResourceNotFoundException.class, () -> {
            logController.createLog(log);
        });
    }

    @Test
    void testNotifyNewLog() {
        LogRepository logRepository = Mockito.mock(LogRepository.class);
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        LogNotifier logNotifier = Mockito.mock(LogNotifier.class);
        LogController logController = new LogController(logRepository, binRepository, logNotifier);

        Log latestLog = new Log();
        latestLog.setLogId(10);
        latestLog.setDescription("Latest Log");

        when(logRepository.findTopByBin_BinIdOrderByLogIdDesc(1)).thenReturn(latestLog);

        logController.notifyNewLog(1);

        Mockito.verify(logNotifier, Mockito.times(1)).notifyLogCreated(latestLog);
    }

    @Test
    void testNotifyNewLog_NotFound() {
        LogRepository logRepository = Mockito.mock(LogRepository.class);
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        LogNotifier logNotifier = Mockito.mock(LogNotifier.class);
        LogController logController = new LogController(logRepository, binRepository, logNotifier);

        when(logRepository.findTopByBin_BinIdOrderByLogIdDesc(1)).thenReturn(null);

        logController.notifyNewLog(1);

        Mockito.verify(logNotifier, Mockito.never()).notifyLogCreated(any());
    }
}