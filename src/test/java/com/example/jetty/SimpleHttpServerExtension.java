package com.example.jetty;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * JUnit 5 Extension to manage the lifecycle of a SimpleHttpServer instance.
 * It starts the server before all tests in a class and stops it after all tests.
 * It can also inject the server instance into test methods.
 */
public class SimpleHttpServerExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

    private SimpleHttpServer server;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // Start the server once for all tests in the class.
        // The constructor and start() method may now throw a generic Exception.
        server = new SimpleHttpServer();
        server.start();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        // Stop the server after all tests have run
        if (server != null) {
            server.stop();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        // This extension supports injecting parameters of type SimpleHttpServer
        return parameterContext.getParameter().getType() == SimpleHttpServer.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        // Provide the server instance to the test method
        return server;
    }
}

