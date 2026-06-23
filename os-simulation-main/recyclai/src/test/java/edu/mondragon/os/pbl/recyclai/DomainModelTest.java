package edu.mondragon.os.pbl.recyclai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Bin (Monitor Pattern) and TrashBinSystem (Message Passing)
 */
class DomainModelTest {

    private TrashBinSystem system;
    private Bin bin;

    private void pause(long millis) {
        try {
            new CountDownLatch(1).await(millis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @BeforeEach
    void setUp() {
        system = new TrashBinSystem(1, 4);
        bin = system.getBin(0);
    }

    // ==================== TrashBinSystem Tests ====================

    @Test
    void testSystemInitialization() {
        assertEquals(1, system.getNumBins());
        assertNotNull(system.getRandomBin());
        assertNotNull(system.time());
        assertFalse(system.isShutdown());
        assertNotNull(bin.getId());
        assertTrue(bin.getId().startsWith("BIN_"));
    }

    @Test
    void testProducerConsumerQueue() throws InterruptedException {
        system.requestCollection(bin);
        Bin retrieved = system.waitForNextJob();
        assertEquals(bin, retrieved);
    }

    @Test
    void testSystemShutdown() throws InterruptedException {
        Thread consumer = new Thread(() -> {
            try {
                Bin result = system.waitForNextJob();
                assertNull(result);
            } catch (InterruptedException e) {
                fail("Should not throw exception");
            }
        });

        consumer.start();

        long start = System.currentTimeMillis();
        while (consumer.getState() != Thread.State.WAITING && 
               (System.currentTimeMillis() - start) < 1000) {
            Thread.onSpinWait();
        }

        system.stopSystem();
        assertTrue(system.isShutdown());
        consumer.join(1000);
        assertFalse(consumer.isAlive());
    }

    @Test
    void testRequestCollectionInterrupted() throws InterruptedException {
        Thread t = new Thread(() -> {
            Thread.currentThread().interrupt();
            system.requestCollection(bin);
        });
        
        t.start();
        t.join(1000);
        assertTrue(t.isInterrupted());
    }

    @Test
    void testStopSystemInterrupted() throws InterruptedException {
        Thread t = new Thread(() -> {
            Thread.currentThread().interrupt();
            system.stopSystem();
        });
        
        t.start();
        t.join(1000);
        assertTrue(system.isShutdown());
    }

    // ==================== Bin Monitor Tests ====================

    @Test
    void testBinFullTriggersCollection() throws InterruptedException {
        for (int i = 0; i < 4; i++) {
            bin.deposit(TrashType.PLASTIC, "User" + i);
        }

        Bin queued = system.waitForNextJob();
        assertNotNull(queued);
        assertEquals(bin.getId(), queued.getId());
    }

    @Test
    void testBinMaintenanceBlocksUsers() throws InterruptedException {
        bin.setUnderMaintenance(true);
        assertTrue(bin.isUnderMaintenance());

        CountDownLatch finished = new CountDownLatch(1);
        Thread user = new Thread(() -> {
            try {
                bin.deposit(TrashType.PLASTIC, "Person");
                finished.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        user.start();
        assertFalse(finished.await(50, TimeUnit.MILLISECONDS));
        
        bin.setUnderMaintenance(false);
        assertTrue(finished.await(1000, TimeUnit.MILLISECONDS));
        user.join(1000);
    }

    @Test
    void testBinBrokenBlocksUsers() throws InterruptedException {
        bin.setBroken(true);
        assertTrue(bin.isBroken());

        CountDownLatch done = new CountDownLatch(1);
        Thread user = new Thread(() -> {
            try {
                bin.deposit(TrashType.GLASS, "Person");
                done.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        user.start();
        assertFalse(done.await(50, TimeUnit.MILLISECONDS));
        
        bin.setBroken(false);
        assertFalse(bin.isBroken());
        assertTrue(done.await(1000, TimeUnit.MILLISECONDS));
        user.join(1000);
    }

    @Test
    void testEmptyAllCompartments() throws InterruptedException {
        bin.deposit(TrashType.GLASS, "Person");
        bin.emptyAllCompartments();
        
        assertDoesNotThrow(() -> bin.deposit(TrashType.GLASS, "Person 2"));
    }

    @Test
    void testEmptyAllCompartmentsWakesWaitingThreads() throws InterruptedException {
        TrashBinSystem sys = new TrashBinSystem(1, 2);
        Bin b = sys.getBin(0);

        b.deposit(TrashType.PLASTIC, "Person 1");
        b.deposit(TrashType.PLASTIC, "Person 2");

        CountDownLatch waiting = new CountDownLatch(1);
        CountDownLatch deposited = new CountDownLatch(1);
        
        Thread t = new Thread(() -> {
            try {
                waiting.countDown();
                b.deposit(TrashType.PLASTIC, "Person 3");
                deposited.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        t.start();

        assertTrue(waiting.await(1000, TimeUnit.MILLISECONDS));
        pause(100);
        assertFalse(deposited.await(100, TimeUnit.MILLISECONDS));

        b.emptyAllCompartments();

        assertTrue(deposited.await(1000, TimeUnit.MILLISECONDS));
        t.join(1000);
    }

    @Test
    void testSpecificTypeConditionWaiting() throws InterruptedException {
        for (int i = 0; i < 4; i++) {
            bin.deposit(TrashType.PLASTIC, "Person " + i);
        }

        CountDownLatch plasticBlocked = new CountDownLatch(1);
        CountDownLatch glassDone = new CountDownLatch(1);

        Thread plasticUser = new Thread(() -> {
            try {
                bin.deposit(TrashType.PLASTIC, "Blocked");
                plasticBlocked.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread glassUser = new Thread(() -> {
            try {
                bin.deposit(TrashType.GLASS, "Glass");
                glassDone.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        plasticUser.start();
        glassUser.start();

        assertTrue(glassDone.await(1000, TimeUnit.MILLISECONDS));
        assertFalse(plasticBlocked.await(50, TimeUnit.MILLISECONDS));

        bin.emptyAllCompartments();
        assertTrue(plasticBlocked.await(1000, TimeUnit.MILLISECONDS));
        
        plasticUser.join(1000);
        glassUser.join(1000);
    }

    @Test
    void testMaintenanceAndBrokenConflict() throws InterruptedException {
        CountDownLatch maintenanceStarted = new CountDownLatch(1);

        Thread collector = new Thread(() -> {
            try {
                bin.setUnderMaintenance(true);
                maintenanceStarted.countDown();
                pause(100);
                bin.setUnderMaintenance(false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        collector.start();
        assertTrue(maintenanceStarted.await(1000, TimeUnit.MILLISECONDS));

        CountDownLatch brokenSet = new CountDownLatch(1);
        Thread tech = new Thread(() -> {
            try {
                bin.setBroken(true);
                brokenSet.countDown();
                bin.setBroken(false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        tech.start();

        assertFalse(brokenSet.await(50, TimeUnit.MILLISECONDS));

        collector.join(2000);
        assertTrue(brokenSet.await(1000, TimeUnit.MILLISECONDS));
        tech.join(2000);
    }

    @Test
    void testServiceRequestedPriority() throws InterruptedException {
        CountDownLatch userBlocked = new CountDownLatch(1);

        Thread service = new Thread(() -> {
            try {
                bin.setBroken(true);
                userBlocked.countDown();
                pause(100);
                bin.setBroken(false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        service.start();
        assertTrue(userBlocked.await(1000, TimeUnit.MILLISECONDS));

        CountDownLatch depositDone = new CountDownLatch(1);
        Thread user = new Thread(() -> {
            try {
                bin.deposit(TrashType.PLASTIC, "Person");
                depositDone.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        user.start();
        assertFalse(depositDone.await(50, TimeUnit.MILLISECONDS));

        service.join(2000);
        assertTrue(depositDone.await(1000, TimeUnit.MILLISECONDS));
        user.join(2000);
    }

    @Test
    void testReportFullCompartmentDoesNotDuplicateRequest() throws InterruptedException {
        TrashBinSystem sys = new TrashBinSystem(1, 2);
        Bin b = sys.getBin(0);
        
        b.deposit(TrashType.PLASTIC, "Person 1");
        b.deposit(TrashType.PLASTIC, "Person 2");
        
        Bin queued1 = sys.waitForNextJob();
        assertNotNull(queued1);
        
        b.emptyAllCompartments();
        b.setUnderMaintenance(true);
        b.setUnderMaintenance(false);
        
        b.deposit(TrashType.GLASS, "Person 3");
        b.deposit(TrashType.GLASS, "Person 4");
        
        Bin queued2 = sys.waitForNextJob();
        assertNotNull(queued2);
    }
}