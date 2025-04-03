package org.ketchup.bookie.server.beanfactory;

import org.ketchup.bookie.server.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tomato.bookie.distributedSystem.faultolerance.Deduplicator;
import org.tomato.bookie.distributedSystem.faultolerance.MessageLossSimulator;

@Configuration
public class MiddlewareServiceBeans {

    private final Config config;

    public MiddlewareServiceBeans(Config config) {
        this.config = config;
    }

    @Bean
    public Deduplicator deduplicator() {
        return new Deduplicator();
    }

    @Bean
    public MessageLossSimulator messageLossSimulator() {
        return new MessageLossSimulator(config.getSimulatedMessageDropRate());
    }
}
