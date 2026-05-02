package com.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.agent.service.AgentOrchestrator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class HackathonAgentApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(HackathonAgentApplication.class, args);
        
        // Run the agent orchestrator
        AgentOrchestrator orchestrator = context.getBean(AgentOrchestrator.class);
        try
        {
            orchestrator.process();
            System.exit(0);
        }
        catch (Exception e)
        {
            log.debug("The agent execution failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
