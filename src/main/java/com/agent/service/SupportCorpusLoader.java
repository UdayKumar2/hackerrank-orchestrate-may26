package com.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.agent.model.SupportDocument;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Loads support documentation corpus from the data/ folder.
 * Supports HackerRank, Claude, and Visa support docs.
 */
@Slf4j
@Service
public class SupportCorpusLoader {

    private List<SupportDocument> corpus = new ArrayList<>();
    private static final String CORPUS_BASE_PATH = "data";
    private static final Map<String, String> DOMAIN_FOLDERS = Map.of(
        "hackerrank", "hackerrank",
        "claude", "claude",
        "visa", "visa"
    );

    /**
     * Load all support documents from the data folder.
     */
    public List<SupportDocument> loadCorpus() {
        corpus.clear();
        
        for (Map.Entry<String, String> domain : DOMAIN_FOLDERS.entrySet()) {
            String domainName = domain.getKey();
            String folderName = domain.getValue();
            loadDomainDocs(domainName, folderName);
        }
        
        log.info("Loaded corpus with {} documents", corpus.size());
        return corpus;
    }

    /**
     * Load documents for a specific domain.
     */
    private void loadDomainDocs(String domain, String folderName) {
        String folderPath = CORPUS_BASE_PATH + File.separator + folderName;
        Path path = Paths.get(folderPath);
        
        if (!Files.exists(path)) {
            log.warn("Domain folder not found: {}", folderPath);
            return;
        }
        
        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".md"))
                .filter(p -> !p.getFileName().toString().equals("index.md"))  // Exclude index files
                .forEach(p -> loadDocument(domain, p));
        } catch (IOException e) {
            log.error("Error loading domain {} docs: {}", domain, e.getMessage());
        }
    }

    /**
     * Load a single markdown document.
     */
    private void loadDocument(String domain, Path filePath) {
        try {
            String content = Files.readString(filePath);
            String title = extractTitle(filePath.getFileName().toString());
            String productArea = extractProductArea(filePath);
            
            SupportDocument doc = SupportDocument.builder()
                .id(filePath.toString())
                .domain(domain)
                .title(title)
                .content(content)
                .productArea(productArea)
                .relevanceScore(0.0)
                .build();
            
            corpus.add(doc);
            
        } catch (IOException e) {
            log.warn("Failed to load document {}: {}", filePath, e.getMessage());
        }
    }

    /**
     * Extract title from filename (remove extension, convert underscores to spaces).
     */
    private String extractTitle(String filename) {
        return filename.replace(".md", "")
                      .replace("-", " ")
                      .replace("_", " ");
    }

    /**
     * Extract product area from file path.
     */
    private String extractProductArea(Path filePath) {
        String pathStr = filePath.toString();
        // Use directory name as product area
        int depth = filePath.getNameCount();
        if (depth > 1) {
            return filePath.getName(depth - 2).toString();
        }
        return "General";
    }

    /**
     * Get all documents in corpus.
     */
    public List<SupportDocument> getCorpus() {
        return new ArrayList<>(corpus);
    }

    /**
     * Search corpus by keywords.
     */
    public List<SupportDocument> search(String keywords) {
        if (keywords == null || keywords.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String[] terms = keywords.toLowerCase().split("\\s+");
        List<SupportDocument> results = new ArrayList<>();
        
        for (SupportDocument doc : corpus) {
            double score = calculateRelevance(doc, terms);
            if (score > 0) {
                doc.setRelevanceScore(score);
                results.add(doc);
            }
        }
        
        // Sort by relevance score descending
        results.sort((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()));
        
        return results;
    }

    /**
     * Calculate relevance score based on keyword matching.
     */
    private double calculateRelevance(SupportDocument doc, String[] terms) {
        double score = 0.0;
        String docText = (doc.getTitle() + " " + doc.getContent()).toLowerCase();
        
        for (String term : terms) {
            int count = countOccurrences(docText, term);
            if (count > 0) {
                // Title matches score higher
                int titleCount = countOccurrences(doc.getTitle().toLowerCase(), term);
                score += titleCount * 2 + count;
            }
        }
        
        return score;
    }

    /**
     * Count occurrences of a substring in text.
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
}
