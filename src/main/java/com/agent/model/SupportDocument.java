package com.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a document from the support corpus.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportDocument
{
    private String id;
    private String domain;
    private String title;
    private String content;
    private String productArea;
    private double relevanceScore;
}
