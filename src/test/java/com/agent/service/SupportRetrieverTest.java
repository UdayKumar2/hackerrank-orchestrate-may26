package com.agent.service;

import com.agent.model.SupportDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupportRetrieverTest {

    private SupportRetriever retriever;

    @BeforeEach
    void setup() {
        SupportCorpusLoader loader = new SupportCorpusLoader();
        loader.loadCorpus();
        retriever = new SupportRetriever(loader);
    }

    @Test
    void shouldRetrieveDocuments() {
        List<SupportDocument> docs = retriever.retrieve("login issue", "Claude", 3);

        assertNotNull(docs);
        assertTrue(docs.size() <= 3);
    }
}