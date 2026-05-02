package com.agent.service;

import com.agent.model.SupportDocument;
import info.debatty.java.stringsimilarity.JaroWinkler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Retrieves the most relevant support documents for a given query.
 * Uses keyword matching and semantic similarity.
 */
@Slf4j
@Service
public class SupportRetriever {

    private final SupportCorpusLoader corpusLoader;
    private final JaroWinkler similarity = new JaroWinkler();

    public SupportRetriever(SupportCorpusLoader corpusLoader) {
        this.corpusLoader = corpusLoader;
    }

    /**
     * Retrieve the most relevant documents for a query.
     * 
     * @param query Search query
     * @param domain Optional domain filter (e.g., "HackerRank", "Claude", "Visa")
     * @param topK Number of top results to return
     * @return List of relevant documents sorted by relevance
     */
    public List<SupportDocument> retrieve(String query, String domain, int topK) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<SupportDocument> corpus = corpusLoader.getCorpus();
        
        // Filter by domain first (most important for disambiguation)
        List<SupportDocument> candidates = corpus.stream()
            .filter(doc -> {
                String normalizedDomain = domain == null ? "" : domain.toLowerCase();
                String docDomain = doc.getDomain().toLowerCase();
                
                // Exact match, or if domain is "unknown" accept all
                return normalizedDomain.isEmpty() || 
                       normalizedDomain.equals("unknown") ||
                       docDomain.contains(normalizedDomain) ||
                       normalizedDomain.contains(docDomain);
            })
            .collect(Collectors.toList());
        
        // If no domain matches, fall back to cross-domain search with higher threshold
        if (candidates.isEmpty()) {
            candidates = corpus;
        }
        
        // Calculate relevance scores
        List<ScoredDocument> scored = new ArrayList<>();
        for (SupportDocument doc : candidates) {
            double score = calculateRelevance(doc, query);
            if (score > 0) {
                scored.add(new ScoredDocument(doc, score));
            }
        }
        
        // Sort by score descending
        scored.sort((a, b) -> Double.compare(b.score, a.score));
        
        // Return top K with minimum confidence threshold
        List<SupportDocument> results = new ArrayList<>();
        int count = 0;
        for (ScoredDocument sd : scored) {
            if (count >= topK) break;
            
            // Log each result with score for debugging
            log.debug("Retrieved doc [score={}]: {} ({})", 
                String.format("%.2f", sd.score), sd.doc.getTitle(), sd.doc.getDomain());
            
            sd.doc.setRelevanceScore(sd.score);
            results.add(sd.doc);
            count++;
        }
        
        log.debug("Retrieved {} documents for query: '{}' (domain={})", 
            results.size(), query, domain == null ? "any" : domain);
        
        return results;
    }

    /**
     * Calculate relevance score for a document.
     * Uses: keyword matching (title boost), semantic similarity, term frequency weighting.
     */
    private double calculateRelevance(SupportDocument doc, String query) {
        String[] queryTerms = query.toLowerCase().split("\\s+");
        double score = 0.0;
        
        String docTitle = doc.getTitle().toLowerCase();
        String docContent = doc.getContent().toLowerCase();
        
        // Filter out single-letter and stopwords
        java.util.List<String> significantTerms = new java.util.ArrayList<>();
        for (String term : queryTerms) {
            if (term.length() > 1 && !isStopword(term)) {
                significantTerms.add(term);
            }
        }
        
        if (significantTerms.isEmpty()) {
            return 0.0;
        }
        
        for (String term : significantTerms) {
            // Title matches score high (multiplier: 4x)
            int titleOccurrences = countOccurrences(docTitle, term);
            if (titleOccurrences > 0) {
                score += titleOccurrences * 4.0;
            }
            
            // Content matches (base weight: 1.5x, capped at 5 occurrences)
            int contentOccurrences = countOccurrences(docContent, term);
            if (contentOccurrences > 0) {
                score += Math.min(contentOccurrences, 5) * 1.5;
            }
            
            // Semantic similarity for close matches (weight: 0.3x)
            double titleSimilarity = calculateTitleSimilarity(docTitle, term);
            score += titleSimilarity * 0.3;
        }
        
        // Normalize by query length to prefer docs matching more terms
        score = score / significantTerms.size();
        
        return score;
    }
    
    /**
     * Check if term is a common stopword to avoid noise.
     */
    private boolean isStopword(String term) {
        java.util.Set<String> stopwords = new java.util.HashSet<>(
            java.util.Arrays.asList(
                "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
                "of", "is", "are", "was", "were", "be", "been", "being", "have", "has",
                "do", "does", "did", "will", "would", "could", "should", "can", "may",
                "this", "that", "these", "those", "i", "you", "he", "she", "it", "we",
                "they", "what", "which", "who", "where", "when", "why", "how"
            )
        );
        return stopwords.contains(term);
    }

    /**
     * Calculate semantic similarity for title.
     */
    private double calculateTitleSimilarity(String title, String term) {
        String[] titleWords = title.toLowerCase().split("\\s+");
        double maxSimilarity = 0.0;
        
        for (String word : titleWords) {
            double sim = similarity.similarity(word, term);
            maxSimilarity = Math.max(maxSimilarity, sim);
        }
        
        return maxSimilarity > 0.7 ? maxSimilarity * 1.5 : 0;  // Boost high similarities
    }

    /**
     * Count occurrences of substring.
     */
    private int countOccurrences(String text, String substring) {
        if (substring.length() == 0) return 0;
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

    /**
     * Helper class for scored documents.
     */
    private static class ScoredDocument {
        SupportDocument doc;
        double score;

        ScoredDocument(SupportDocument doc, double score) {
            this.doc = doc;
            this.score = score;
        }
    }
}
