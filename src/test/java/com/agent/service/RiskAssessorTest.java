package com.agent.service;

import com.agent.model.AgentResponse;
import com.agent.model.SupportDocument;
import com.agent.model.SupportTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RiskAssessorTest {

    private RiskAssessor riskAssessor;

    @BeforeEach
    void setup() {
        riskAssessor = new RiskAssessor();
    }

    @Test
    void shouldEscalateBilling() {
        boolean result = riskAssessor.shouldEscalate(
                ticket("billing issue"),
                "Billing & Payments",
                "product_issue",
                List.of(mockDoc())
        );

        assertTrue(result);
    }

    @Test
    void shouldEscalateNoDocs() {
        boolean result = riskAssessor.shouldEscalate(
                ticket("some issue"),
                "Technical Support",
                "bug",
                List.of()
        );

        assertTrue(result);
    }

    @Test
    void shouldAllowReply() {
        boolean result = riskAssessor.shouldEscalate(
                ticket("how to use api"),
                "Features & Integration",
                "product_issue",
                List.of(mockDocHigh())
        );

        assertFalse(result);
    }

    private SupportTicket ticket(String issue) {
        return SupportTicket.builder().issue(issue).subject("").company("None").build();
    }

    private SupportDocument mockDoc() {
        return SupportDocument.builder().content("doc").relevanceScore(1.0).build();
    }

    private SupportDocument mockDocHigh() {
        return SupportDocument.builder().content("doc").relevanceScore(3.0).build();
    }
}