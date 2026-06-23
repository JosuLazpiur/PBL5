package com.pbl.recycleai.config;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.pbl.recycleai.model.Alert;

@Service
public class AlertNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public AlertNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyAlertCreated(Alert alert) {
        messagingTemplate.convertAndSend("/topic/alerts/" + alert.getBin().getBinId(), alert);
    }
}
