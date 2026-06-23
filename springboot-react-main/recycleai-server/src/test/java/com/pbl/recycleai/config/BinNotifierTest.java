package com.pbl.recycleai.config;

import com.pbl.recycleai.model.Bin;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.verify;

class BinNotifierTest {

    @Test
    void testNotifyBinUpdated() {
        SimpMessagingTemplate messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);
        BinNotifier binNotifier = new BinNotifier(messagingTemplate);

        Bin bin = new Bin();
        bin.setBinId(1);
        bin.setUbication("Location 1");

        binNotifier.notifyBinUpdated(bin);

        verify(messagingTemplate).convertAndSend("/topic/bins/1", bin);
    }
}
