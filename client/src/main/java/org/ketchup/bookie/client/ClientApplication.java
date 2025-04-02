package org.ketchup.bookie.client;

import org.ketchup.bookie.client.config.ClientConfig;
import org.ketchup.bookie.client.service.ClientService;
import org.ketchup.bookie.client.ui.ConsoleUI;

public class ClientApplication {
    
    public static void main(String[] args) {
        // Parse command line arguments
        if (args.length < 2) {
            System.out.println("Usage: java ClientApplication <serverIP> <serverPort> [atMostOnceEnabled]");
            System.exit(1);
        }
        
        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);
        boolean atMostOnceEnabled = args.length > 2 && Boolean.parseBoolean(args[2]);
        
        // Initialize client configuration
        ClientConfig config = new ClientConfig(serverIP, serverPort, atMostOnceEnabled);
        
        // Initialize client service
        ClientService clientService = new ClientService(config);
        
        // Start the console UI
        ConsoleUI console = new ConsoleUI(clientService);
        console.start();
    }
}
