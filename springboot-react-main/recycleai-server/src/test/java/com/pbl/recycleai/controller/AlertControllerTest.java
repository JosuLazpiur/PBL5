package com.pbl.recycleai.controller;

import com.pbl.recycleai.config.AlertNotifier;
import com.pbl.recycleai.model.Alert;
import com.pbl.recycleai.model.Bin;
import com.pbl.recycleai.repository.AlertRepository;
import com.pbl.recycleai.repository.BinRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AlertControllerTest {

    @Test
    void testGetAlertsByBin() {
        AlertRepository alertRepository = Mockito.mock(AlertRepository.class);
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        AlertNotifier alertNotifier = Mockito.mock(AlertNotifier.class);
        AlertController alertController = new AlertController(binRepository, alertRepository, alertNotifier);

        Alert alert1 = new Alert();
        alert1.setAlertId(1);
        alert1.setTitle("Alert 1");

        Alert alert2 = new Alert();
        alert2.setAlertId(2);
        alert2.setTitle("Alert 2");

        when(alertRepository.findByBin_BinIdOrderByDatetimeDesc(1)).thenReturn(Arrays.asList(alert1, alert2));

        List<Alert> alerts = alertController.getAlertsByBin(1);

        Assertions.assertEquals(2, alerts.size());
        Assertions.assertEquals("Alert 1", alerts.get(0).getTitle());
    }

    @Test
    void testCreateAlert() {
        AlertRepository alertRepository = Mockito.mock(AlertRepository.class);
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        AlertNotifier alertNotifier = Mockito.mock(AlertNotifier.class);
        AlertController alertController = new AlertController(binRepository, alertRepository, alertNotifier);

        Bin bin = new Bin();
        bin.setBinId(1);

        Alert alert = new Alert();
        alert.setTitle("New Alert");
        alert.setBin(bin);
        alert.setDatetime(LocalDateTime.now().toString());


        when(binRepository.findById(1)).thenReturn(Optional.of(bin));
        when(alertRepository.save(any(Alert.class))).thenReturn(alert);

        Alert createdAlert = alertController.createAlert(alert);

        Assertions.assertEquals("New Alert", createdAlert.getTitle());
    }

    @Test
    void testNotifyNewAlert() {
        AlertRepository alertRepository = Mockito.mock(AlertRepository.class);
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        AlertNotifier alertNotifier = Mockito.mock(AlertNotifier.class);
        AlertController alertController = new AlertController(binRepository, alertRepository, alertNotifier);

        Alert latestAlert = new Alert();
        latestAlert.setAlertId(10);
        latestAlert.setTitle("Latest Alert");

        when(alertRepository.findTopByBin_BinIdOrderByAlertIdDesc(1)).thenReturn(latestAlert);

        alertController.notifyNewAlert(1);

        Mockito.verify(alertNotifier, Mockito.times(1)).notifyAlertCreated(latestAlert);
    }

    @Test
    void testNotifyNewAlert_NotFound() {
        AlertRepository alertRepository = Mockito.mock(AlertRepository.class);
        BinRepository binRepository = Mockito.mock(BinRepository.class);
        AlertNotifier alertNotifier = Mockito.mock(AlertNotifier.class);
        AlertController alertController = new AlertController(binRepository, alertRepository, alertNotifier);

        when(alertRepository.findTopByBin_BinIdOrderByAlertIdDesc(1)).thenReturn(null);

        alertController.notifyNewAlert(1);

        Mockito.verify(alertNotifier, Mockito.never()).notifyAlertCreated(any());
    }
}