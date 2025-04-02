package org.ketchup.bookie.client.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientConfig {
    private String serverIP;
    private int serverPort;
    private boolean atMostOnceEnabled;
}
