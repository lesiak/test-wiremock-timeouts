package com.example.jetty12;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Main {


    public static void main(String[] args) throws Exception {
        // Run the cycle in a loop to demonstrate server start/stop
        //var usedPorts = new java.util.HashSet<Integer>();

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        for (int i = 0;; i++) {
            System.out.println("=== Cycle " + (i + 1) + " ===");
            SingleEndpointServer server = null;
            try {
                // 1. Construct and launch the server
                server = new SingleEndpointServer();
                server.start();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(server.getBaseUrl() + "/test"))
                        .POST(HttpRequest.BodyPublishers.ofString("{}"))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(5))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // 3. Print response details
               // System.out.println("---------------------------------");
                System.out.println("Response Status Code: " + response.statusCode());
                //System.out.println("Response Body: " + response.body());
                //System.out.println("---------------------------------");

            } finally {
                // 4. Cleanup server
                if (server != null) {
                    server.stop();
                    server.destroy();
                }
            }

            //Thread.sleep(200);

        }
    }
}


