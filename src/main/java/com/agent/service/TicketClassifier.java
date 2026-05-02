package com.agent.service;

import com.agent.model.SupportTicket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Classifies support tickets into domain, product area, request type, and urgency.
 */
@Slf4j
@Service
public class TicketClassifier {

    /**
     * Classify the domain (company).
     */
    public String classifyDomain(SupportTicket ticket) {

        if (ticket.getCompany() != null && !ticket.getCompany().equalsIgnoreCase("None")) {
            return ticket.getCompany();
        }

        String text = (ticket.getIssue() + " " + ticket.getSubject()).toLowerCase();

        if (containsAny(text, "hackerrank", "assessment", "coding", "interview", "test")) {
            return "HackerRank";
        }

        if (containsAny(text, "claude", "anthropic", "api", "ai")) {
            return "Claude";
        }

        if (containsAny(text, "visa", "card", "payment", "transaction", "credit")) {
            return "Visa";
        }

        // Safe fallback
        return "HackerRank";
    }

    /**
     * Classify product area - IMPROVED with domain-specific logic.
     */
    public String classifyProductArea(SupportTicket ticket) {

        String domain = classifyDomain(ticket);
        String text = (ticket.getIssue() + " " + ticket.getSubject()).toLowerCase();

        log.debug("Classifying product area for domain: {} with text: {}", domain, text.substring(0, Math.min(50, text.length())));

        // 🔐 Account Security - HIGHEST PRIORITY (most risky)
        if (containsAny(text, "login", "password", "password reset", "account access", 
                        "forgot password", "cannot login", "account locked", "verify", 
                        "authenticate", "two-factor", "2fa", "mfa", "security")) {
            log.debug("Classified as Account Security");
            return "Account Security";
        }

        // 💳 Billing & Payments - HIGH PRIORITY (financial)
        if (containsAny(text, "billing", "payment", "charge", "invoice", "subscription", 
                        "upgrade", "pricing", "credit", "refund", "cancel subscription", "plan")) {
            log.debug("Classified as Billing & Payments");
            return "Billing & Payments";
        }

        // 🕵️ Fraud & Compliance - HIGH PRIORITY (security)
        if (containsAny(text, "fraud", "unauthorized", "stolen", "suspicious",
                "chargeback", "identity theft", "identity stolen", "scam", "phishing",
                "compromised", "breached", "hacked")) {
            log.debug("Classified as Fraud & Compliance");
            return "Fraud & Compliance";
        }

        // 🔒 Privacy - HIGH PRIORITY (compliance)
        if (containsAny(text, "privacy", "gdpr", "delete", "personal information", "pii",
                "export data", "download data", "account deletion", "data retention")) {
            log.debug("Classified as Data & Privacy");
            return "Data & Privacy";
        }

        // DOMAIN-SPECIFIC CLASSIFICATIONS
        if (domain.equalsIgnoreCase("HackerRank")) {
            // HackerRank specific areas
            if (containsAny(text, "test", "assessment", "assessment platform", "hackerrank for work",
                           "screen", "coding assessment", "test active", "invite", "candidate",
                           "reinvite", "time accommodation", "extra time")) {
                log.debug("Classified as Assessments & Testing (HackerRank)");
                return "Assessments & Testing";
            }
            if (containsAny(text, "report", "analytics", "scorecard", "result", "leaderboard")) {
                log.debug("Classified as Reporting & Analytics (HackerRank)");
                return "Reporting & Analytics";
            }
            if (containsAny(text, "interview", "video", "live code", "codepair")) {
                log.debug("Classified as Interviews (HackerRank)");
                return "Interviews";
            }
            if (containsAny(text, "community", "forum", "discuss", "question")) {
                log.debug("Classified as Community (HackerRank)");
                return "Community";
            }
        }
        
        if (domain.equalsIgnoreCase("Claude")) {
            // Claude specific areas
            if (containsAny(text, "api", "integration", "code", "claude api", "api key",
                           "anthropic", "model")) {
                log.debug("Classified as API & Integration (Claude)");
                return "API & Integration";
            }
            if (containsAny(text, "conversation", "chat", "message", "history", "delete conversation",
                           "rename conversation")) {
                log.debug("Classified as Conversations (Claude)");
                return "Conversations";
            }
            if (containsAny(text, "model", "output", "response", "performance", "accuracy",
                           "opus", "sonnet", "haiku")) {
                log.debug("Classified as Features & Capabilities (Claude)");
                return "Features & Capabilities";
            }
        }
        
        if (domain.equalsIgnoreCase("Visa")) {
            // Visa specific areas
            if (containsAny(text, "card", "debit card", "credit card", "virtual card", "issue card")) {
                log.debug("Classified as Card Services (Visa)");
                return "Card Services";
            }
            if (containsAny(text, "travel", "traveler", "cheque", "foreign", "exchange")) {
                log.debug("Classified as Travel Services (Visa)");
                return "Travel Services";
            }
        }

        // ⚙️ Technical Issues (for all domains)
        if (containsAny(text,
                "bug", "error", "crash", "fail", "failed", "failing",
                "down", "offline", "broken", "not working", "stopped working",
                "site down", "page not accessible")) {
            log.debug("Classified as Technical Support");
            return "Technical Support";
        }
        
        // 📚 Features & Documentation
        if (containsAny(text, "feature", "api", "integration", "documentation", "how to", "guide", 
                        "tutorial", "example", "sample", "request feature")) {
            log.debug("Classified as Features & Integration");
            return "Features & Integration";
        }

        // Default fallback
        log.debug("Classified as General Support (default)");
        return "General Support";
    }


    /**
     * Classify request type.
     */
    public String classifyRequestType(SupportTicket ticket) {

        String text = (ticket.getIssue() + " " + ticket.getSubject()).toLowerCase();

        if (containsAny(text, "fraud", "stolen", "unauthorized", "suspicious",
                "chargeback", "identity theft", "scam", "phishing")) {
            return "product_issue";
        }

        if (containsAny(text,
                "bug", "error", "crash", "broken",
                "not working", "not able", "unable",
                "not accessible", "unable to access",
                "fail", "failed", "failing",
                "down", "offline", "stopped working",
                "not functioning", "not processing", "not submitting")) {
            return "bug";
        }

        if (containsAny(text,
                "infosec", "process", "forms", "onboarding", "setup", "provision", "reschedule", "schedule", "assessment")) {
            return "product_issue";
        }
        if (containsAny(text,
                "remove user", "add user", "delete user",
                "employee", "team member", "interviewer")) {
            return "product_issue";
        }

        if (containsAny(text,
                "feature", "enhancement", "improve", "suggestion")) {
            return "feature_request";
        }

        if (containsAny(text,
                "delete all files", "delete everything",
                "wipe system", "wipe all data",
                "destroy data", "format disk",
                "ddos", "denial of service", "overload system")) {
            return "invalid";
        }

        if (isInvalid(text)) {
            return "invalid";
        }

        return "product_issue";
    }

    /**
     * Assess urgency.
     */
    public String assessUrgency(SupportTicket ticket) {

        String text = (ticket.getIssue() + " " + ticket.getSubject()).toLowerCase();

        if (containsAny(text, "urgent", "critical", "emergency", "blocked",
                "down", "offline", "immediately", "asap")) {
            return "CRITICAL";
        }

        if (containsAny(text, "login", "account", "billing", "fraud",
                "security", "payment", "data", "privacy",
                "bug", "error", "crash", "fail", "stolen", "unauthorized")) {
            return "HIGH";
        }

        if (containsAny(text, "question", "help", "how to", "guide", "documentation")) {
            return "MEDIUM";
        }

        return "LOW";
    }

    /**
     * Detect invalid / out-of-scope queries.
     */
    private boolean isInvalid(String text) {

        if (text == null || text.trim().length() < 3) {
            return true;
        }

        if (containsAny(text, "hack", "exploit", "sql injection", "xss",
                "<script>", "javascript:", "bypass", "union select", "drop table")) {
            return true;
        }

        if (containsAny(text,
                "weather", "recipe", "movie", "sports", "actor", "film", "song",
                "news", "celebrity", "joke", "riddle", "poem", "story")) {
            return true;
        }

        return false;
    }

    /**
     * Utility
     */
    private boolean containsAny(String text, String... keywords) {

        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}