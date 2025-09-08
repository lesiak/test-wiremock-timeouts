package com.example.jetty;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A simple, lightweight HTTP server for testing purposes, using Jetty 12.
 * It provides functionality similar to WireMock but with a minimal footprint.
 *
 * Maven/Gradle Dependencies required for this class:
 * - org.eclipse.jetty.ee10:jetty-ee10-server (or equivalent for your setup)
 */
public class SimpleHttpServer implements AutoCloseable {

    private final Server server;
    private final List<Stub> stubs = new CopyOnWriteArrayList<>();

    /**
     * Creates a server that binds to a dynamic, ephemeral port with SO_REUSEADDR enabled.
     */
    public SimpleHttpServer() {
        this.server = new Server();


        // 1. Create a standard HTTP configuration.
//        HttpConfiguration httpConfig = new HttpConfiguration();
//
//        // 2. Create a custom HttpConnectionFactory to set socket options.
//        HttpConnectionFactory connectionFactory = new HttpConnectionFactory(httpConfig) {
//
//            @Override
//            public void onOpened(org.eclipse.jetty.io.Connection connection) {
//                super.onOpened(connection);
//                try {
//                    // Get the underlying socket channel and set the SO_LINGER option.
//                    // A linger time of 0 forces an abortive close (RST packet), bypassing TIME_WAIT.
//                    SocketChannel channel = (SocketChannel) connection.getEndPoint().getTransport();
//                    channel.setOption(StandardSocketOptions.SO_LINGER, 0);
//                } catch (IOException e) {
//                    throw new RuntimeException("Failed to set socket options", e);
//                }
//            }
//        };








        // The ServerConnector is where network settings are configured.
        ServerConnector connector = new ServerConnector(this.server);

        // Use port 0 to let the OS assign an available ephemeral port.
        connector.setPort(0);

        // *** This sets the SO_REUSEADDR socket option ***
        connector.setReuseAddress(true);



        this.server.addConnector(connector);
        this.server.setHandler(new StubMatchingHandler());
    }

    /**
     * Starts the server in a background thread.
     * @throws Exception if the server fails to start
     */
    public void start() throws Exception {
        this.server.start();
        System.out.println("SimpleHttpServer (Jetty) started on port: " + getPort());
    }

    /**
     * Stops the server.
     * @throws Exception if the server fails to stop
     */
    public void stop() throws Exception {
        System.out.println("Stopping SimpleHttpServer (Jetty) on port: " + getPort());
        this.server.stop();
    }

    /**
     * Gets the port the server is running on.
     *
     * @return The port number.
     */
    public int getPort() {
        // The port is available from the connector after the server has started.
        return ((ServerConnector) this.server.getConnectors()[0]).getLocalPort();
    }

    /**
     * Gets the base URL of the server.
     *
     * @return The base URL as a string (e.g., "http://localhost:12345").
     */
    public String getBaseUrl() {
        return "http://localhost:" + getPort();
    }

    /**
     * Adds a stub configuration to the server.
     *
     * @param stub The stub to add.
     */
    public void stubFor(Stub stub) {
        this.stubs.add(stub);
    }

    /**
     * Clears all configured stubs.
     */
    public void reset() {
        this.stubs.clear();
    }

    @Override
    public void close() throws Exception {
        stop();
    }

    /**
     * A record representing a stubbed response.
     */
    public record Stub(String method, String path, int statusCode, Map<String, String> headers, String body) {}

    /**
     * The main handler that finds a matching stub for incoming requests.
     * This now uses the modern, asynchronous Handler API from Jetty 12.
     */
    private class StubMatchingHandler extends Handler.Abstract {
        @Override
        public boolean handle(Request request, Response response, Callback callback) throws IOException {
            // Find the last matching stub to allow for overrides.
            Stub matchingStub = null;
            for (int i = stubs.size() - 1; i >= 0; i--) {
                Stub stub = stubs.get(i);
                // In Jetty 12, path is retrieved from HttpURI
                if (Objects.equals(stub.method(), request.getMethod()) &&
                    Objects.equals(stub.path(), request.getHttpURI().getPath())) {
                    matchingStub = stub;
                    break;
                }
            }

            if (matchingStub != null) {
                applyStubResponse(response, callback, matchingStub);
            } else {
                sendDefaultResponse(response, callback, request);
            }

            // Inform Jetty that we have handled this request.
            return true;
        }

        private void applyStubResponse(Response response, Callback callback, Stub stub) {
            response.setStatus(stub.statusCode());

            for (Map.Entry<String, String> header : stub.headers().entrySet()) {
                response.getHeaders().put(header.getKey(), header.getValue());
            }

            byte[] responseBody = stub.body().getBytes(StandardCharsets.UTF_8);
            ByteBuffer bodyBuffer = ByteBuffer.wrap(responseBody);

            // Asynchronously write the response body and complete the request
            // This is now an instance method call on the response object.
            response.write(true, bodyBuffer, callback);
        }

        private void sendDefaultResponse(Response response, Callback callback, Request request) {
            String errorMessage = "No matching stub found for " + request.getMethod() + " " + request.getHttpURI().getPath();
            byte[] responseBody = errorMessage.getBytes(StandardCharsets.UTF_8);
            ByteBuffer bodyBuffer = ByteBuffer.wrap(responseBody);

            response.setStatus(404);
            response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/plain; charset=utf-8");

            // Asynchronously write the response body and complete the request
            // This is now an instance method call on the response object.
            response.write(true, bodyBuffer, callback);
        }
    }
}

