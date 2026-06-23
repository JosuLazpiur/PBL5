package edu.mondragon.os.pbl.recyclai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * INTEGRATION MODULE
 * Connects the Simulation (OS Project) with the Web Backend (Web Project).
 * Uses asynchronous HTTP requests to avoid blocking the simulation threads.
 */
public class BackendService {

    // Target API Address
    private static final String API_URL = "http://localhost:1880/api";

    // CHANGE 1: Force HTTP_1_1 and add connection timeout to prevent hanging
    private static HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private BackendService() {
        throw new IllegalStateException("Utility class");
    }

    // Auxiliar method to extract the ID ("BIN_1" -> 1)
    private static int extractBinId(String binId) {
        try {
            return Integer.parseInt(binId.replace("BIN_", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Synchronous GET (Polling).
     * Blocking call required for decision making in Technician thread.
     */
    public static boolean getBinOperativeStatus(String binId) {
        int id = extractBinId(binId);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/bin/" + id))
                    .GET()
                    .build();

            // Send synchronously
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String body = response.body();
            // CHANGE 2: Null check for robustness
            return body != null && !body.contains("\"operative\":false");

        } catch (InterruptedException e) {
            // SONAR FIX: Restore interrupted status
            Thread.currentThread().interrupt();
            System.err.println("[SYNC ERROR] Interrupted during status check");
            return true; // Fallback to operative to avoid blocking
        } catch (Exception e) {
            System.err.println("[SYNC ERROR] No se pudo comprobar estado: " + e.getMessage());
            return true;
        }
    }

    /**
     * Uses HTTP PUT to modify the operative variable.
     */
    public static void updateBinStatus(String binId, boolean isOperative) {
        int id = extractBinId(binId);
        String json = String.format("{\"operative\":%b}", isOperative);
        sendAsyncRequest(API_URL + "/bin/" + id, "PUT", json);
    }

    /*
     * Endpoint: POST /api/alerts)
     */
    public static void sendAlert(String binId, String title, String description) {
        int id = extractBinId(binId);
        String json = String.format(
                "{\"title\":\"%s\", \"description\":\"%s\", \"datetime\":\"%s\", \"bin\":{\"binId\":%d}}",
                title, description, LocalDateTime.now().toString(), id);

        sendAsyncRequest(API_URL + "/alerts", "POST", json);
    }

    /*
     * Endpoint: POST /api/logs)
     */
    public static void sendLog(String binId, String description) {
        int id = extractBinId(binId);
        String json = String.format(
                "{\"description\":\"%s\", \"datetime\":\"%s\", \"bin\":{\"binId\":%d}}",
                description, LocalDateTime.now().toString(), id);

        sendAsyncRequest(API_URL + "/logs", "POST", json);
    }

    /**
     * Executes the HTTP request asynchronously using CompletableFuture.
     */
    private static void sendAsyncRequest(String endpoint, String method, String jsonBody) {
        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(5));

                if ("POST".equalsIgnoreCase(method)) {
                    builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
                } else if ("PUT".equalsIgnoreCase(method)) {
                    builder.PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
                }

                client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // CHANGE 4: Improved error logging
                System.err.println("[INTEGRATION ERROR] Could not connect to Backend: " + e.getMessage());
            }
        });
    }
}