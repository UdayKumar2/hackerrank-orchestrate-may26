package com.agent.service;

import com.agent.model.SupportTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketClassifierTest {

    private TicketClassifier classifier;

    @BeforeEach
    void setup() {
        classifier = new TicketClassifier();
    }

    @Test
    void shouldClassifyBug() {
        SupportTicket ticket = ticket("site is down and not working");
        assertEquals("bug", classifier.classifyRequestType(ticket));
    }

    @Test
    void shouldClassifyFraud() {
        SupportTicket ticket = ticket("unauthorized transaction detected");
        assertEquals("product_issue", classifier.classifyRequestType(ticket));
    }

    @Test
    void shouldClassifyInvalidMalicious() {
        SupportTicket ticket = ticket("delete all files");
        assertEquals("invalid", classifier.classifyRequestType(ticket));
    }

    @Test
    void shouldClassifyFeatureRequest() {
        SupportTicket ticket = ticket("please add new feature");
        assertEquals("feature_request", classifier.classifyRequestType(ticket));
    }

    @Test
    void shouldClassifyDomainClaude() {
        SupportTicket ticket = ticket("how to use claude api");
        assertEquals("Claude", classifier.classifyDomain(ticket));
    }

    private SupportTicket ticket(String issue) {
        return SupportTicket.builder()
                .issue(issue)
                .subject("")
                .company("None")
                .build();
    }
}