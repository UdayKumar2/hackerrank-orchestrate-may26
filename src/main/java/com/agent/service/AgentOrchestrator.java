package com.agent.service;

import com.agent.model.AgentResponse;
import com.agent.model.SupportDocument;
import com.agent.model.SupportTicket;
import com.agent.util.AgentLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AgentOrchestrator {

    private final InputParser inputParser;
    private final SupportCorpusLoader corpusLoader;
    private final TicketClassifier classifier;
    private final SupportRetriever retriever;
    private final RiskAssessor riskAssessor;
    private final ResponseGenerator responseGenerator;
    private final OutputFormatter outputFormatter;
    private final AgentLogger logger;

    @Value("${agent.input-file:support_tickets/support_tickets.csv}")
    private String inputFile;

    @Value("${agent.output-file:support_tickets/output.csv}")
    private String outputFile;

    @Value("${agent.retrieval-top-k:3}")
    private int topK;

    public AgentOrchestrator(
            InputParser inputParser,
            SupportCorpusLoader corpusLoader,
            TicketClassifier classifier,
            SupportRetriever retriever,
            RiskAssessor riskAssessor,
            ResponseGenerator responseGenerator,
            OutputFormatter outputFormatter,
            AgentLogger logger) {

        this.inputParser = inputParser;
        this.corpusLoader = corpusLoader;
        this.classifier = classifier;
        this.retriever = retriever;
        this.riskAssessor = riskAssessor;
        this.responseGenerator = responseGenerator;
        this.outputFormatter = outputFormatter;
        this.logger = logger;
    }

    public void process() {

        log.info("========== SUPPORT TRIAGE AGENT START ==========");

        try {
            // Step 1: Load corpus
            log.info("Loading support corpus...");
            List<SupportDocument> corpus = corpusLoader.loadCorpus();
            log.info("Loaded {} documents", corpus.size());

            // Step 2: Parse tickets
            log.info("Parsing input tickets from: {}", inputFile);
            List<SupportTicket> tickets = inputParser.parseTickets(inputFile);
            log.info("Parsed {} tickets", tickets.size());

            // Step 3: Process tickets
            List<AgentResponse> responses = new ArrayList<>();

            for (SupportTicket ticket : tickets) {
                try {
                    AgentResponse response = processTicket(ticket);
                    responses.add(response);

                } catch (Exception e) {
                    log.error("Error processing ticket {}: {}", ticket.getId(), e.getMessage());

                    responses.add(
                            AgentResponse.builder()
                                    .status("escalated")
                                    .productArea("General Support")
                                    .response("An error occurred while processing your request.")
                                    .justification("Internal error - requires manual review.")
                                    .requestType("invalid")
                                    .build()
                    );
                }
            }

            // Step 4: Write output
            log.info("Writing output to: {}", outputFile);
            outputFormatter.writeOutput(responses, outputFile);
            outputFormatter.printSummary(responses);

            // 🔥 IMPORTANT: write log.txt
            logger.write();

            log.info("========== SUPPORT TRIAGE AGENT COMPLETE ==========");

        } catch (Exception e) {
            log.error("Fatal error in agent pipeline", e);
            throw new RuntimeException("Agent failed", e);
        }
    }

    /**
     * Process a single ticket
     */
    private AgentResponse processTicket(SupportTicket ticket) {

        // Step 1: Classification
        String domain = classifier.classifyDomain(ticket);
        String productArea = classifier.classifyProductArea(ticket);
        String requestType = classifier.classifyRequestType(ticket);
        String urgency = classifier.assessUrgency(ticket);

        log.debug("Ticket {} classified: domain={}, area={}, type={}, urgency={}",
                ticket.getId(), domain, productArea, requestType, urgency);

        // Step 2: Build query
        String searchQuery = buildSearchQuery(ticket);

        // Step 3: Retrieve documents
        List<SupportDocument> retrievedDocs =
                retriever.retrieve(searchQuery, domain, topK);

        boolean hasDocs = retrievedDocs != null && !retrievedDocs.isEmpty();

        log.debug("Retrieved {} docs for ticket {}",
                hasDocs ? retrievedDocs.size() : 0, ticket.getId());

        // Step 4: Risk decision
        boolean shouldEscalate = riskAssessor.shouldEscalate(
                ticket, productArea, requestType, retrievedDocs);

        // Step 5: Generate response
        String status;
        String response;

        if (shouldEscalate) {
            status = "escalated";
            response = responseGenerator.generateEscalationMessage(productArea);
        } else {
            status = "replied";
            response = responseGenerator.generateResponse(retrievedDocs, searchQuery);
        }

        // 🔥 Step 6: Strong justification
        String text = (ticket.getIssue() + " " + ticket.getSubject()).toLowerCase();
        String justification = riskAssessor.buildJustification(
                shouldEscalate,
                productArea,
                requestType,
                hasDocs,
                text
        );

        // 🔥 Step 7: Log reasoning
        logger.log(
                ticket.getId(),
                ticket.getIssue(),
                domain,
                productArea,
                requestType,
                status.toUpperCase(),
                justification,
                hasDocs ? retrievedDocs.size() : 0
        );

        // Step 8: Build response
        return AgentResponse.builder()
                .status(status)
                .productArea(productArea)
                .response(response)
                .justification(justification)
                .requestType(requestType)
                .build();
    }

    /**
     * Build search query from ticket
     */
    private String buildSearchQuery(SupportTicket ticket) {

        StringBuilder query = new StringBuilder();

        if (ticket.getSubject() != null && !ticket.getSubject().isEmpty()) {
            query.append(ticket.getSubject()).append(" ");
        }

        query.append(ticket.getIssue());

        return query.toString().trim();
    }
}