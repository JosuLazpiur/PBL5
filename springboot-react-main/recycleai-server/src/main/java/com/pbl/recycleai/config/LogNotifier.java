package com.pbl.recycleai.config;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.pbl.recycleai.model.Log;

@Service
public class LogNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public LogNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyLogCreated(Log log) {
        messagingTemplate.convertAndSend("/topic/logs/" + log.getBin().getBinId(), log);
    }
}
