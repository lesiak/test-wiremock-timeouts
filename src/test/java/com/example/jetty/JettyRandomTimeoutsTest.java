package com.example.jetty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An example test class demonstrating how to use the SimpleHttpServer.
 */
public class JettyRandomTimeoutsTest {

    // The extension will manage starting and stopping the server.
    @RegisterExtension
    static SimpleHttpServerExtension serverExtension = new SimpleHttpServerExtension();

    // The server instance is injected by the extension.
    private final SimpleHttpServer server;

    public JettyRandomTimeoutsTest(SimpleHttpServer server) {
        this.server = server;
    }

    @BeforeEach
    void setupStubs() {
        // Reset stubs before each test to ensure isolation
        server.reset();

        // This is the equivalent of your WireMock stub configuration.
        server.stubFor(new SimpleHttpServer.Stub(
                "POST",
                "/test",
                400,
                Map.of(
                        "Content-Type", "application/json",
                        "Connection", "close"
                ),
                "{\"error\": \"Expected error returned from custom server.\"}"
        ));
    }

    @Test
    void shouldReturnStubbedErrorResponse() throws IOException, InterruptedException {
        // Arrange
        HttpClient client = HttpClient.newHttpClient();
//        HttpClient client = HttpClient.newBuilder()
//                .version(HttpClient.Version.HTTP_2)
//                .followRedirects(HttpClient.Redirect.NORMAL)
//                .connectTimeout(Duration.ofSeconds(20))
//                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/test"))
                .POST(HttpRequest.BodyPublishers.ofString("Some request body"))
                .header("Accept", "application/json")
                .build();

        // Act
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert
        assertEquals(400, response.statusCode());
        assertEquals("{\"error\": \"Expected error returned from custom server.\"}", response.body());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        assertTrue(response.headers().firstValue("Connection").orElse("").contains("close"));
    }

    @Test
    void shouldReturn404ForUnmatchedRequest() throws IOException, InterruptedException {
        // Arrange
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/unmatched-path"))
                .GET()
                .build();

        // Act
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("No matching stub found"));
    }
}
