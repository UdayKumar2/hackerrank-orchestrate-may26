package com.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the output of the agent's triage decision for a support ticket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentResponse {
    private String status;
    private String productArea;
    private String response;
    private String justification;
    private String requestType;
}
