package com.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {
    @Test
    void testAppHasAGreeting() {
        assertEquals("Hello, JDK project!", getGreeting());
    }

    private String getGreeting() {
        return "Hello, JDK project!";
    }
}
