package com.agent.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.agent.model.SupportTicket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Parses support tickets from CSV files.
 */
@Slf4j
@Service
public class InputParser {

    /**
     * Parse support tickets from a CSV file.
     * 
     * Expected CSV format:
     * issue,subject,company
     * "Issue text","Subject","HackerRank|Claude|Visa|None"
     * 
     * @param filePath Path to the CSV file
     * @return List of parsed SupportTicket objects
     */
    public List<SupportTicket> parseTickets(String filePath) {
        List<SupportTicket> tickets = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> allRecords = reader.readAll();
            
            if (allRecords.isEmpty()) {
                log.warn("CSV file is empty: {}", filePath);
                return tickets;
            }
            
            // Skip header row
            for (int i = 1; i < allRecords.size(); i++) {
                String[] record = allRecords.get(i);
                
                if (record.length < 3) {
                    log.warn("Skipping malformed row {}: {}", i, Arrays.toString(record));
                    continue;
                }
                
                String issue = record[0].trim();
                String subject = record[1].trim();
                String company = record[2].trim();
                
                // Validate company
                if (!company.isEmpty() && !isValidCompany(company)) {
                    log.warn("Skipping row {} with invalid company: {}", i, company);
                    continue;
                }
                
                SupportTicket ticket = SupportTicket.builder()
                    .id(i)
                    .issue(issue)
                    .subject(subject)
                    .company(company.isEmpty() ? "None" : company)
                    .build();
                
                tickets.add(ticket);
            }
            
            log.info("Parsed {} tickets from {}", tickets.size(), filePath);
            
        } catch (IOException | CsvException e) {
            log.error("Error parsing CSV file {}: {}", filePath, e.getMessage());
            throw new RuntimeException("Failed to parse input CSV", e);
        }
        
        return tickets;
    }
    
    private boolean isValidCompany(String company) {
        return company.equalsIgnoreCase("HackerRank") ||
               company.equalsIgnoreCase("Claude") ||
               company.equalsIgnoreCase("Visa") ||
               company.equalsIgnoreCase("None");
    }
}
