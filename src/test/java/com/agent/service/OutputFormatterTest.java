package com.agent.service;

import com.agent.model.AgentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutputFormatterTest {

    private OutputFormatter formatter;

    @BeforeEach
    void setup() {
        formatter = new OutputFormatter();
    }

    @Test
    void shouldValidateResponse() {
        AgentResponse res = AgentResponse.builder()
                .status("replied")
                .productArea("Technical Support")
                .response("Test response")
                .justification("Valid")
                .requestType("bug")
                .build();

        assertTrue(formatter.validateResponse(res));
    }
}