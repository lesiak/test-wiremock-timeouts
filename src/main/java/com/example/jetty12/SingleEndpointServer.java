package com.example.jetty12;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SingleEndpointServer implements AutoCloseable {

    private final Server server;

    public SingleEndpointServer() {
        this.server = new Server();

        ServerConnector connector = new ServerConnector(this.server);
        connector.setPort(0); // Use port 0 for an ephemeral port
        connector.setReuseAddress(true); // Set SO_REUSEADDR

        this.server.addConnector(connector);
        this.server.setHandler(new FixedEndpointHandler());
    }

    public void start() throws Exception {
        this.server.start();
        //System.out.println("Server started on port: " + getPort());
    }

    public void stop() throws Exception {
        //System.out.println("Stopping server on port: " + getPort());
        this.server.stop();
    }

    public void destroy() throws Exception {
        this.server.destroy();
    }

    public int getPort() {
        return ((ServerConnector) this.server.getConnectors()[0]).getLocalPort();
    }

    public String getBaseUrl() {
        return "http://localhost:" + getPort();
    }

    @Override
    public void close() throws Exception {
        stop();
    }

    private static class FixedEndpointHandler extends Handler.Abstract {
        @Override
        public boolean handle(Request request, Response response, Callback callback) {
            // Check if the request matches our hardcoded endpoint
            if ("POST".equals(request.getMethod()) && "/test".equals(request.getHttpURI().getPath())) {
                applyStubResponse(response, callback);
            } else {
                sendNotFoundResponse(response, callback, request);
            }
            return true; // Mark the request as handled
        }

        private void applyStubResponse(Response response, Callback callback) {
            response.setStatus(400);
            response.getHeaders().put(HttpHeader.CONTENT_TYPE, "application/json");

            String body = "{\"error\": \"Expected error returned from server.\"}";
            byte[] responseBody = body.getBytes(StandardCharsets.UTF_8);
            ByteBuffer bodyBuffer = ByteBuffer.wrap(responseBody);

            response.write(true, bodyBuffer, callback);
        }

        private void sendNotFoundResponse(Response response, Callback callback, Request request) {
            String errorMessage = "Endpoint not found: " + request.getMethod() + " " + request.getHttpURI().getPath();
            byte[] responseBody = errorMessage.getBytes(StandardCharsets.UTF_8);
            ByteBuffer bodyBuffer = ByteBuffer.wrap(responseBody);

            response.setStatus(404);
            response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/plain; charset=utf-8");

            response.write(true, bodyBuffer, callback);
        }
    }
}
