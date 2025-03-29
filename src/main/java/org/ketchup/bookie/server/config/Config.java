package org.ketchup.bookie.server.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Config {

    @Value("${server.port}")
    private int port;

}
