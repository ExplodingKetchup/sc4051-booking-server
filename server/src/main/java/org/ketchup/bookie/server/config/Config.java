package org.ketchup.bookie.server.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Config {

    @Value("${server.port:55555}")
    private int port;

    @Value("${server.at_most_once.enabled:false}")
    private boolean atMostOnceEnabled;

    @Value("${server.simulated.message.drop.rate:0.0")
    private double simulatedMessageDropRate;

}
