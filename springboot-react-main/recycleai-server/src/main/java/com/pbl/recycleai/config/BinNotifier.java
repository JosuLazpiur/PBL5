package com.pbl.recycleai.config;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.pbl.recycleai.model.Bin;

@Service
public class BinNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public BinNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyBinUpdated(Bin bin) {
        messagingTemplate.convertAndSend("/topic/bins/" + bin.getBinId(), bin);
    }
}
