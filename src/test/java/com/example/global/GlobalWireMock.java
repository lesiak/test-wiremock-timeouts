package com.example.global;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class GlobalWireMock {

    private static WireMockServer wireMockServer;

    public static void start() {
        if (wireMockServer == null) {
            // Start the server only if it's not already running
            wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
            wireMockServer.start();

            // Ensure the server is stopped when the JVM exits
            Runtime.getRuntime().addShutdownHook(new Thread(wireMockServer::stop));
        }
    }

    public static WireMockServer getServer() {
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            throw new IllegalStateException("WireMock server is not started!");
        }
        return wireMockServer;
    }



    // You don't need a stop() method here because the shutdown hook handles it.
}