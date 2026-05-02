package com.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a support ticket from the input CSV.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket {
    private int id;
    private String issue;
    private String subject;
    private String company; 
}
