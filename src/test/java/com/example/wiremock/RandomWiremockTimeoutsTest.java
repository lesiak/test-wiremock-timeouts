package com.example.wiremock;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.time.temporal.ChronoUnit.SECONDS;

public class RandomWiremockTimeoutsTest {
    @RegisterExtension
    WireMockExtension wireMockServer = WireMockExtension
            .newInstance()
            .options(wireMockConfig().dynamicPort().http2PlainDisabled(true))
            .build();

    @BeforeEach
     void setupStubs() throws Exception {
        wireMockServer.stubFor(
                post(urlMatching("/test"))
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("{\"error\": \"Expected error returned from Wiremock server.\"}")
                                        .withHeader("Connection", "close")
                        )
        );
        Thread.sleep(5);
    }

    @Test
    void testRandomWiremockTimeouts() throws URISyntaxException, IOException, InterruptedException {

        var uri = "http://localhost:" + wireMockServer.getPort() +"/test";
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
