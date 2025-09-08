package com.example.global;



import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class GlobalWireMockInitializer implements BeforeAllCallback, BeforeEachCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        // Start the global server once before any test class runs
        GlobalWireMock.start();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        // Reset the server before each @Test method
        GlobalWireMock.getServer().resetAll();
    }
}
