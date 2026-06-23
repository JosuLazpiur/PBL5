package edu.mondragon.os.pbl.recyclai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for basic components: TrashType, TrashGenerator, Compartment,
 * BackendService
 */
class CoreTest {

    private HttpServer mockServer;

    @AfterEach
    void tearDown() {
        if (mockServer != null) {
            mockServer.stop(0);
            mockServer = null;
        }
    }

    // ==================== TrashType Tests ====================

    @Test
    void testTrashTypeEnum() {
        assertEquals(5, TrashType.values().length);
        assertEquals(TrashType.PLASTIC, TrashType.valueOf("PLASTIC"));
    }

    // ==================== TrashGenerator Tests ====================

    @Test
    void testTrashGenerator() {
        int size = 10;
        List<TrashType> list = TrashGenerator.randomList(size);

        assertNotNull(list);
        assertEquals(size, list.size());

        for (TrashType type : list) {
            assertNotNull(type);
        }

        assertTrue(TrashGenerator.randomList(0).isEmpty());
    }

    @Test
    void testTrashGeneratorConstructor() throws Exception {
        Constructor<TrashGenerator> constructor = TrashGenerator.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            fail("Should have thrown InvocationTargetException");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }

    // ==================== Compartment Tests ====================

    @Test
    void testCompartmentLogic() {
        TrashType type = TrashType.GLASS;
        int capacity = 2;
        Compartment c = new Compartment(type, capacity);

        assertEquals(type, c.getType());
        assertEquals(capacity, c.getCapacity());
        assertEquals(0, c.getLevel());
        assertFalse(c.isFull());

        c.add();
        assertEquals(1, c.getLevel());
        assertFalse(c.isFull());

        c.add();
        assertEquals(2, c.getLevel());
        assertTrue(c.isFull());

        c.add();
        assertEquals(2, c.getLevel());
        assertTrue(c.isFull());

        c.empty();
        assertEquals(0, c.getLevel());
        assertFalse(c.isFull());
    }

    // ==================== TrashBinSystem Tests ====================

    @Test
    void testSystemTimeFormatting() {
        TrashBinSystem system = new TrashBinSystem(1, 1);
        String time = system.time();

        assertNotNull(time);
        assertTrue(time.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testGetBinWithModulo() {
        TrashBinSystem system = new TrashBinSystem(3, 1);

        Bin bin1 = system.getBin(0);
        Bin bin2 = system.getBin(3);
        Bin bin3 = system.getBin(5);

        assertNotNull(bin1);
        assertNotNull(bin2);
        assertNotNull(bin3);
        assertEquals(bin1.getId(), bin2.getId());
    }

    // ==================== BackendService Tests ====================

    @Test
    void testBackendServiceConstructor() throws Exception {
        Constructor<BackendService> constructor = BackendService.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            fail("Should throw IllegalStateException");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            assertEquals("Utility class", e.getCause().getMessage());
        }
    }

    @Test
    void testBackendServiceGetStatusFallback() {
        // When service is unavailable, should return true (fallback)
        assertTrue(BackendService.getBinOperativeStatus("BIN_1"));
        assertTrue(BackendService.getBinOperativeStatus("BIN_0"));
        assertTrue(BackendService.getBinOperativeStatus("INVALID"));
        assertTrue(BackendService.getBinOperativeStatus(""));
    }

    @Test
    void testBackendServiceAsyncMethods() throws InterruptedException {
        // Test all async methods execute without exceptions
        assertDoesNotThrow(() -> {
            BackendService.sendAlert("BIN_1", "Test Alert", "Test Description");
            BackendService.sendLog("BIN_1", "Test log entry");
            BackendService.updateBinStatus("BIN_1", true);
            BackendService.updateBinStatus("BIN_2", false);
        });

        Thread.sleep(200);
    }

    @Test
    void testBackendServiceConcurrency() throws Exception {
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            final int num = i;
            new Thread(() -> {
                BackendService.sendLog("BIN_" + num, "Concurrent " + num);
                BackendService.updateBinStatus("BIN_" + num, num % 2 == 0);
                BackendService.getBinOperativeStatus("BIN_" + num);
                latch.countDown();
            }).start();
        }

        assertTrue(latch.await(3000, TimeUnit.MILLISECONDS));
        Thread.sleep(500);
    }

    @Test
    void testBackendServiceInterruptionHandling() throws Exception {
        // Test sync interruption - covers InterruptedException catch and restore
        CountDownLatch syncDone = new CountDownLatch(1);
        Thread syncThread = new Thread(() -> {
            Thread.currentThread().interrupt();
            boolean status = BackendService.getBinOperativeStatus("BIN_1");
            assertTrue(status);
            syncDone.countDown();
        });
        syncThread.start();
        assertTrue(syncDone.await(2000, TimeUnit.MILLISECONDS));

        // Test async interruption - covers InterruptedException in async
        CountDownLatch asyncDone = new CountDownLatch(1);
        Thread asyncThread = new Thread(() -> {
            Thread.currentThread().interrupt();
            BackendService.sendAlert("BIN_1", "A", "D");
            BackendService.sendLog("BIN_1", "L");
            asyncDone.countDown();
        });
        asyncThread.start();
        assertTrue(asyncDone.await(2000, TimeUnit.MILLISECONDS));

        Thread.sleep(200);
    }

    @Test
    void testBackendServiceEdgeCases() throws InterruptedException {
        // Test that all edge cases execute without throwing exceptions
        assertDoesNotThrow(() -> {
            // Test with special characters
            BackendService.sendAlert("BIN_1", "Alert: \"Special\"", "Desc with 'quotes'");
            BackendService.sendLog("BIN_1", "Log with @#$%");

            // Test with empty strings
            BackendService.sendAlert("BIN_2", "", "");
            BackendService.sendLog("BIN_2", "");

            // Test various bin ID formats
            BackendService.getBinOperativeStatus("BIN_999");
            BackendService.updateBinStatus("BIN_123", true);
        });

        Thread.sleep(200);
    }

    // ==================== BackendService with Mock Server (100% Coverage)
    // ====================

    @Test
    void testBackendServiceWithRealHttpCalls() throws Exception {
        mockServer = HttpServer.create(new InetSocketAddress(1880), 0);

        // Mock GET /api/bin/1 - operative:false
        mockServer.createContext("/api/bin/1", exchange -> {
            String response = "{\"id\":1,\"operative\":false}";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });

        // Mock GET /api/bin/2 - operative:true
        mockServer.createContext("/api/bin/2", exchange -> {
            String response = "{\"id\":2,\"operative\":true}";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });

        // Mock PUT /api/bin/*
        mockServer.createContext("/api/bin/", exchange -> {
            if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
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

        // Mock POST /api/alerts
        mockServer.createContext("/api/alerts", exchange -> {
            String response = "{\"success\":true}";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });

        mockServer.start();
        Thread.sleep(300);

        // COVERS: HttpResponse<String> response = client.send(...)
        // COVERS: String body = response.body()
        // COVERS: return body != null && !body.contains("\"operative\":false")

        boolean status1 = BackendService.getBinOperativeStatus("BIN_1");
        assertFalse(status1); // operative:false

        boolean status2 = BackendService.getBinOperativeStatus("BIN_2");
        assertTrue(status2); // operative:true

        BackendService.updateBinStatus("BIN_1", false);

        // COVERS: POST branch and client.send(...)
        BackendService.sendLog("BIN_1", "Test");
        BackendService.sendAlert("BIN_1", "Alert", "Desc");

        Thread.sleep(1000); // Wait for async operations

        mockServer.stop(0);
        mockServer = null;
    }
}