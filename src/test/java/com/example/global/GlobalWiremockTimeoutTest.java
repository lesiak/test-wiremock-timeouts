package com.example.global;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static java.time.temporal.ChronoUnit.SECONDS;

@ExtendWith(GlobalWireMockInitializer.class)
public class GlobalWiremockTimeoutTest {

    @BeforeEach
     void setupStubs() throws Exception {
        GlobalWireMock.getServer().stubFor(
                post(urlMatching("/test"))
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("{\"error\": \"Expected error returned from Wiremock server.\"}")
                                        .withHeader("Connection", "close")
                        )
        );
        //Thread.sleep(5);
    }

    @Test
    void testRandomWiremockTimeouts() throws URISyntaxException, IOException, InterruptedException {

        var uri = "http://localhost:" + GlobalWireMock.getServer().port() +"/test";
        var request = HttpRequest.newBuilder()
                .uri(new URI(uri))
                .timeout(Duration.of(10, SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString("Sample request body"))
                .build();
        var client = HttpClient.newHttpClient();
        var response = client.send(request,  HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(400, response.statusCode());
    }
}
