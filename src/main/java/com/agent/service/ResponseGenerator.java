package com.agent.service;

import com.agent.model.SupportDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates clean, user-friendly responses grounded in the support corpus.
 * Converts raw excerpts into readable, concise answers.
 */
@Slf4j
@Service
public class ResponseGenerator {

    /**
     * Generate a response based on retrieved documents.
     */
    public String generateResponse(List<SupportDocument> retrievedDocs, String query) {

        if (retrievedDocs == null || retrievedDocs.isEmpty()) {
            return "I don’t have enough information in our knowledge base to answer this request. Please contact our support team for assistance.";
        }

        SupportDocument topDoc = retrievedDocs.get(0);

        // Step 1: Extract relevant excerpt
        String excerpt = extractRelevantExcerpt(topDoc.getContent(), query);

        // Step 2: Clean noisy text (markdown, links, etc.)
        String cleaned = cleanText(excerpt);

        // Step 3: Summarize into readable answer
        String summary = summarize(cleaned);

        return summary + " If you need more assistance, please contact our support team.";
    }

    /**
     * Generate escalation message based on product area.
     */
    public String generateEscalationMessage(String productArea) {

        switch (productArea) {

            case "Account Security":
                return "This is a sensitive account-related issue. Our support team will assist you with secure verification.";

            case "Billing & Payments":
                return "This request involves billing or payment details and requires manual review by our billing team.";

            case "Fraud & Compliance":
                return "This appears to be a potential fraud-related issue. Our security team will investigate and respond promptly.";

            case "Data & Privacy":
                return "This request involves sensitive data. Our compliance team will review and provide further details.";

            default:
                return "This issue requires manual review. Our support team will get back to you shortly.";
        }
    }

    /**
     * Extract relevant excerpt from document using query matching.
     */
    private String extractRelevantExcerpt(String content, String query) {

        if (content == null || content.isEmpty()) {
            return "Please refer to our documentation for more details.";
        }

        String[] terms = query.toLowerCase().split("\\s+");
        String[] sentences = content.split("(?<=[.!?])\\s+");

        List<String> relevant = java.util.Arrays.stream(sentences)
                .filter(sentence -> {
                    String lower = sentence.toLowerCase();
                    return java.util.Arrays.stream(terms)
                            .anyMatch(lower::contains);
                })
                .limit(3)
                .collect(Collectors.toList());

        if (relevant.isEmpty()) {
            return content.substring(0, Math.min(300, content.length()));
        }

        return String.join(" ", relevant);
    }

    /**
     * Clean unwanted formatting from extracted text.
     */
    private String cleanText(String text) {

        if (text == null) return "";

        return text
                .replaceAll("#+", "")                 // remove markdown headers
                .replaceAll("\\[.*?\\]", "")          // remove [links]
                .replaceAll("\\(.*?\\)", "")          // remove (urls)
                .replaceAll("`", "")                  // remove code markers
                .replaceAll("\\s+", " ")              // normalize spaces
                .trim();
    }

    /**
     * Convert cleaned excerpt into a short, readable summary.
     */
    private String summarize(String text) {

        if (text.isEmpty()) {
            return "Please refer to the documentation for more details.";
        }

        String[] sentences = text.split("(?<=[.!?])\\s+");

        StringBuilder result = new StringBuilder();
        int count = 0;

        for (String sentence : sentences) {

            // Skip noisy or irrelevant content
            if (sentence.length() < 30) continue;
            if (sentence.contains("http")) continue;
            if (sentence.contains("title:")) continue;

            result.append(sentence.trim()).append(" ");
            count++;

            if (count == 2) break; // limit to 2 sentences
        }

        if (result.length() == 0) {
            return text.substring(0, Math.min(200, text.length()));
        }

        return result.toString().trim();
    }

    /**
     * Optional: Validate grounding (unchanged from your logic)
     */
    public boolean isGrounded(String response, List<SupportDocument> sources) {

        if (sources == null || sources.isEmpty()) {
            return false;
        }

        for (SupportDocument source : sources) {

            String sourceText = source.getContent().toLowerCase();
            String responseText = response.toLowerCase();

            String[] terms = sourceText.split("\\s+");
            int matches = 0;

            for (String term : terms) {
                if (term.length() > 4 && responseText.contains(term)) {
                    matches++;
                }
            }

            if (matches > 2) return true;
        }

        return false;
    }
}