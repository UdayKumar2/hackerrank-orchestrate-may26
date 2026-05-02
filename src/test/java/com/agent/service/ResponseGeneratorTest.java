package com.agent.service;

import com.agent.model.SupportDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResponseGeneratorTest {

    private ResponseGenerator generator;

    @BeforeEach
    void setup() {
        generator = new ResponseGenerator();
    }

    @Test
    void shouldGenerateResponse() {
        String response = generator.generateResponse(
                List.of(mockDoc()),
                "login issue"
        );

        assertNotNull(response);
        assertTrue(response.length() > 10);
    }

    @Test
    void shouldHandleNoDocs() {
        String response = generator.generateResponse(List.of(), "unknown");

        assertTrue(response.contains("support team"));
    }

    private SupportDocument mockDoc() {
        return SupportDocument.builder()
                .content("Login issues can be resolved by resetting password.")
                .relevanceScore(2.0)
                .build();
    }
}