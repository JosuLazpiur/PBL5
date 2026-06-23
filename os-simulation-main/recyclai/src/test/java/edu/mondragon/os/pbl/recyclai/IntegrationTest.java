package edu.mondragon.os.pbl.recyclai;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * System Integration Tests
 */
class IntegrationTest {

    private void pause(long millis) {
        try {
            new CountDownLatch(1).await(millis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testConcurrentBinAccess() throws Exception {
        TrashBinSystem system = new TrashBinSystem(1, 10);
        Bin bin = system.getBin(0);
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(5);
        
        for (int i = 0; i < 5; i++) {
            final int num = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    bin.deposit(TrashType.values()[num % 5], "User" + num);
                    doneLatch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
        
        startLatch.countDown();
        assertTrue(doneLatch.await(3000, TimeUnit.MILLISECONDS));
    }

    @Test
    void testFullSystemIntegration() throws Exception {
        TrashBinSystem system = new TrashBinSystem(2, 4);
        
        Technician tech = new Technician(system);
        GarbageCollector collector = new GarbageCollector(system);
        Person p1 = new Person("U1", List.of(TrashType.PLASTIC, TrashType.GLASS), system);
        Person p2 = new Person("U2", List.of(TrashType.PAPER), system);
        
        tech.start();
        collector.start();
        p1.start();
        p2.start();
        
        pause(500);
        
        Bin bin = system.getBin(0);
        bin.setBroken(true);
        pause(50);
        bin.setBroken(false);
        
        BackendService.updateBinStatus(bin.getId(), true);
        BackendService.sendLog(bin.getId(), "Test");
        BackendService.sendAlert(bin.getId(), "T", "A");
        
        p1.join(2000);
        p2.join(2000);
        
        system.stopSystem();
        tech.interrupt();
        collector.interrupt();
        
        tech.join(1000);
        collector.join(1000);
        
        assertFalse(tech.isAlive());
        assertFalse(collector.isAlive());
        assertTrue(system.isShutdown());
        
        pause(500);
    }

    @Test
    void testSystemWithBackendIntegration() throws Exception {
        TrashBinSystem system = new TrashBinSystem(1, 2);
        Bin bin = system.getBin(0);
        
        Person person = new Person("Tester", 
            List.of(TrashType.PLASTIC, TrashType.PLASTIC, TrashType.PLASTIC), 
            system);
        
        GarbageCollector collector = new GarbageCollector(system);
        
        collector.start();
        person.start();
        
        pause(500);
        
        BackendService.sendAlert(bin.getId(), "Test", "Integration test");
        boolean status = BackendService.getBinOperativeStatus(bin.getId());
        assertTrue(status);
        
        person.join(3000);
        system.stopSystem();
        collector.join(2000);
        
        assertFalse(person.isAlive());
        assertFalse(collector.isAlive());
    }
}