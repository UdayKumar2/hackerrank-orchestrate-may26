package com.agent.service;

import com.agent.model.SupportDocument;
import com.agent.model.SupportTicket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RiskAssessor {

    public boolean shouldEscalate(SupportTicket ticket,
                                  String productArea,
                                  String requestType,
                                  List<SupportDocument> retrievedDocs) {

        String text = (ticket.getIssue() + " " + ticket.getSubject()).toLowerCase();

        if (productArea.contains("Account Security")) return true;

        if (productArea.contains("Billing") || productArea.contains("Payment")) return true;

        if (productArea.contains("Fraud")) return true;

        if (productArea.contains("Data") || productArea.contains("Privacy")) return true;

        if (text.contains("security vulnerability") || text.contains("vulnerability") ||
                text.contains("zero-day")) {
            return true;
        }

        if ("invalid".equals(requestType)) {
            return true;
        }

        if (retrievedDocs == null || retrievedDocs.isEmpty()) return true;

        boolean lowConfidence = retrievedDocs.stream()
                .noneMatch(doc -> doc.getRelevanceScore() > 1.5);

        if (lowConfidence) return true;

        if (hasMaliciousPatterns(text)) return true;

        if (isUnclear(text)) return true;

        return false;
    }

    private boolean hasMaliciousPatterns(String text) {

        return text.contains("drop table") ||
                text.contains("union select") ||
                text.contains("<script>") ||
                text.contains("javascript:") ||
                text.contains("exploit") ||
                text.contains("bypass") ||
                text.contains("hack");
    }

    private boolean isUnclear(String text) {

        if (text.length() < 5) return true;

        if (text.contains("???") || text.contains("...")) return true;

        return false;
    }

    public String buildJustification(boolean escalated,
                                     String productArea,
                                     String requestType,
                                     boolean hasDocs,
                                     String text) {

        if (escalated) {

            if (productArea.contains("Account Security"))
                return "Sensitive account issue requiring identity verification.";

            if (productArea.contains("Billing"))
                return "Billing or payment issue requires manual processing.";

            if (productArea.contains("Fraud"))
                return "Potential fraud-related issue requiring investigation.";

            if (productArea.contains("Privacy") || productArea.contains("Data"))
                return "Data or privacy concern requiring compliance review.";

            if (escalated && justificationContainsSecurity(text)) {
                return "Security vulnerability requires investigation by the security team.";
            }

                if (!hasDocs)
                return "No relevant documentation found, escalation required.";

            return "Issue requires manual review due to risk or low confidence.";
        }

        return "Relevant documentation found and issue is low risk, so a direct response was provided.";
    }

    private boolean justificationContainsSecurity(String text) {
        return text.contains("security vulnerability") ||
                text.contains("vulnerability") ||
                text.contains("zero-day");
    }
}