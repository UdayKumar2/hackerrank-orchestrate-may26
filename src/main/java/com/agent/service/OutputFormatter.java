package com.agent.service;

import com.opencsv.CSVWriter;
import com.agent.model.AgentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Formats and writes agent responses to output CSV file.
 */
@Slf4j
@Service
public class OutputFormatter {

    /**
     * Write responses to output CSV file.
     */
    public void writeOutput(List<AgentResponse> responses, String outputPath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputPath))) {
            
            // Write header
            writer.writeNext(new String[]{
                "status",
                "product_area",
                "response",
                "justification",
                "request_type"
            });
            
            // Write data rows
            for (AgentResponse response : responses) {
                writer.writeNext(new String[]{
                    response.getStatus(),
                    response.getProductArea(),
                    sanitizeForCsv(response.getResponse()),
                    sanitizeForCsv(response.getJustification()),
                    response.getRequestType()
                });
            }
            
            log.info("Wrote {} responses to {}", responses.size(), outputPath);
            
        } catch (IOException e) {
            log.error("Error writing output CSV: {}", e.getMessage());
            throw new RuntimeException("Failed to write output CSV", e);
        }
    }

    /**
     * Sanitize text for CSV output (handle newlines, quotes, etc).
     */
    private String sanitizeForCsv(String text) {
        if (text == null) {
            return "";
        }
        
        // Replace newlines with spaces
        text = text.replace("\n", " ").replace("\r", " ");
        
        // Trim excessive whitespace
        text = text.replaceAll("\\s+", " ").trim();
        
        // Limit length to reasonable size
        if (text.length() > 1000) {
            text = text.substring(0, 997) + "...";
        }
        
        return text;
    }

    /**
     * Validate output format.
     */
    public boolean validateResponse(AgentResponse response) {
        if (response.getStatus() == null || response.getStatus().isEmpty()) {
            log.warn("Response missing status");
            return false;
        }
        
        if (!response.getStatus().equals("replied") && !response.getStatus().equals("escalated")) {
            log.warn("Invalid status: {}", response.getStatus());
            return false;
        }
        
        if (response.getProductArea() == null || response.getProductArea().isEmpty()) {
            log.warn("Response missing product_area");
            return false;
        }
        
        if (response.getResponse() == null || response.getResponse().isEmpty()) {
            log.warn("Response missing response text");
            return false;
        }
        
        if (response.getJustification() == null || response.getJustification().isEmpty()) {
            log.warn("Response missing justification");
            return false;
        }
        
        if (response.getRequestType() == null || response.getRequestType().isEmpty()) {
            log.warn("Response missing request_type");
            return false;
        }
        
        String validTypes = "product_issue,feature_request,bug,invalid";
        if (!validTypes.contains(response.getRequestType())) {
            log.warn("Invalid request_type: {}", response.getRequestType());
            return false;
        }
        
        return true;
    }

    /**
     * Generate summary statistics.
     */
    public void printSummary(List<AgentResponse> responses) {
        long repliedCount = responses.stream()
            .filter(r -> "replied".equals(r.getStatus()))
            .count();
        long escalatedCount = responses.stream()
            .filter(r -> "escalated".equals(r.getStatus()))
            .count();
        
        log.info("========== SUMMARY ==========");
        log.info("Total responses: {}", responses.size());
        log.info("Replied: {}", repliedCount);
        log.info("Escalated: {}", escalatedCount);
        
        // Print by request type
        responses.stream()
            .map(AgentResponse::getRequestType)
            .distinct()
            .forEach(type -> {
                long count = responses.stream()
                    .filter(r -> type.equals(r.getRequestType()))
                    .count();
                log.info("  {}: {}", type, count);
            });
        
        log.info("=============================");
    }
}
