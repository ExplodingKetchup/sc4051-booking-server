package org.ketchup.bookie.server.beanfactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tomato.bookie.distributedSystem.faultolerance.Deduplicator;

@Configuration
public class MiddlewareServiceBeans {

    @Bean
    public Deduplicator deduplicator() {
        return new Deduplicator();
    }
}
