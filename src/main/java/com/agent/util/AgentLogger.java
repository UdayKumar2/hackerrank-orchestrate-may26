package com.agent.util;

import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AgentLogger {

    private final List<String> logs = new ArrayList<>();
    private static final String FILE = "log.txt";

    public void log(int id,
                    String issue,
                    String domain,
                    String productArea,
                    String requestType,
                    String decision,
                    String justification,
                    int docCount) {

        String entry = "\n==============================\n" +
                "TICKET #" + id + "\n" +
                "Issue: " + issue + "\n" +
                "Domain: " + domain + "\n" +
                "Product Area: " + productArea + "\n" +
                "Request Type: " + requestType + "\n" +
                "Docs Retrieved: " + docCount + "\n" +
                "Decision: " + decision + "\n" +
                "Justification: " + justification + "\n" +
                "==============================\n";

        logs.add(entry);
    }

    public void write() throws IOException {
        FileWriter writer = new FileWriter(FILE);
        for (String log : logs) {
            writer.write(log);
        }
        writer.close();
    }
}