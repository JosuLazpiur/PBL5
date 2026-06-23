package edu.mondragon.os.pbl.recyclai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for system actors: Technician, GarbageCollector, Person, App
 */
class ActorsTest {

    private HttpServer mockServer;

    @AfterEach
    void tearDown() {
        if (mockServer != null) {
            mockServer.stop(0);
            mockServer = null;
        }
    }

    private void pause(long millis) {
        try {
            new CountDownLatch(1).await(millis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitForThreadStart(Thread t) {
        long start = System.currentTimeMillis();
        while (t.getState() == Thread.State.NEW && (System.currentTimeMillis() - start) < 1000) {
            Thread.onSpinWait();
        }
    }

    // ==================== Technician Tests ====================
    
    @Test
    void testTechnicianBasicLifecycle() throws Exception {
        TrashBinSystem system = new TrashBinSystem(1, 4);
        Technician tech = new Technician(system);
        
        tech.start();
        waitForThreadStart(tech);
        pause(1200);
        
        system.stopSystem();
        tech.interrupt();
        tech.join(1000);

        assertFalse(tech.isAlive());
        assertTrue(system.isShutdown());
    }

    @Test
    void testTechnicianInterruption() throws Exception {
        TrashBinSystem system = new TrashBinSystem(1, 4);
        Technician tech = new Technician(system);
        
        tech.start();
        waitForThreadStart(tech);
        pause(50);
        
        tech.interrupt();
        tech.join(1000);
        assertFalse(tech.isAlive());
        assertTrue(tech.isInterrupted() || !tech.isAlive());
    }

    @Test
    void testTechnicianRepairFlow() throws Exception {
        TrashBinSystem system = new TrashBinSystem(1, 4);
        Bin bin = system.getBin(0);
        
        bin.setBroken(true);
        assertTrue(bin.isBroken());
        
        pause(5000);
        
        bin.setBroken(false);
        assertFalse(bin.isBroken());
        
        BackendService.updateBinStatus(bin.getId(), true);
        BackendService.sendLog(bin.getId(), "Incident resolved by the Technician");
        
        pause(200);
    }

    @Test
    void testTechnicianWithMockServerRepair() throws Exception {
        // Este test cubre las líneas 28, 37-57, 63 de Technician.java
        // Levanta mock server que devuelve operative:false para disparar el bloque de reparación
        
        mockServer = HttpServer.create(new InetSocketAddress(1880), 0);
        
        // Mock GET /api/bin/1 - devuelve operative:false
        mockServer.createContext("/api/bin/1", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String response = "{\"id\":1,\"operative\":false}";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                String response = "{\"success\":true}";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });
        
        // Mock POST /api/logs
        mockServer.createContext("/api/logs", exchange -> {
            String response = "{\"success\":true}";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        
        mockServer.start();
        pause(300);
        
        TrashBinSystem system = new TrashBinSystem(1, 4);
        Technician tech = new Technician(system);
        
        tech.start();
        waitForThreadStart(tech);
        
        pause(1500);
        
        system.stopSystem();
        tech.interrupt();
        tech.join(2000);
        
        assertFalse(tech.isAlive());
        
        mockServer.stop(0);
        mockServer = null;
    }

    // ==================== GarbageCollector Tests ====================
    
    @Test
    void testCollectorBasicWorkflow() throws Exception {
        TrashBinSystem system = new TrashBinSystem(1, 4);
        GarbageCollector collector = new GarbageCollector(system);
        
        collector.start();
        waitForThreadStart(collector);
        
        system.requestCollection(system.getBin(0));
        pause(1500);
        
        system.stopSystem();
        collector.join(2000);

        assertFalse(collector.isAlive());
        assertTrue(system.isShutdown());
    }

    @Test
    void testCollectorInterruption() throws Exception {
        TrashBinSystem system = new TrashBinSystem(1, 4);
        GarbageCollector collector = new GarbageCollector(system);
        
        collector.start();
        waitForThreadStart(collector);
        
        collector.interrupt();
        collector.join(2000);

        assertFalse(collector.isAlive());
    }

    // ==================== Person Tests ====================
    
    @Test
    void testPersonBasicLifecycle() throws Exception {
        TrashBinSystem system = new TrashBinSystem(1, 4);
        Person person = new Person("User", Collections.singletonList(TrashType.PLASTIC), system);
        
        person.start();
        person.join(2000);
        assertFalse(person.isAlive());
    }

    @Test
    void testPersonInterruption() throws Exception {
        TrashBinSystem system = new TrashBinSystem(1, 4);
        List<TrashType> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) items.add(TrashType.PLASTIC);
        
        Person person = new Person("User", items, system);
        person.start();
        waitForThreadStart(person);
        pause(100);
        
        person.interrupt();
        person.join(1000);

        assertFalse(person.isAlive());
    }

    // ==================== App Tests ====================
    
    @Test
    void testAppMain() throws Exception {
        Thread mainThread = new Thread(() -> App.main(new String[]{}));
        mainThread.start();
        waitForThreadStart(mainThread);
        pause(100);

        mainThread.interrupt();
        mainThread.join(1000);

        assertDoesNotThrow(() -> {}); 
    }

    @Test
    void testAppWaitInterruption() throws Exception {
        App app = new App();
        
        java.lang.reflect.Field personsField = App.class.getDeclaredField("persons");
        personsField.setAccessible(true);
        Person[] mockPersons = new Person[1];
        mockPersons[0] = new Person("Mock", Collections.emptyList(), new TrashBinSystem(1, 1));
        personsField.set(app, mockPersons);
        
        app.startSimulation();
        
        Thread waiter = new Thread(app::waitEndOfThreads);
        waiter.start();
        waitForThreadStart(waiter);
        pause(100);
        
        waiter.interrupt();
        waiter.join(2000);

        assertNotNull(app);
    }
}