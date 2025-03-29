package org.tomato.bookie.distributedSystem.faultolerance;

import java.util.Random;

public class MessageLossSimulator {
    private final Random random = new Random();
    private final double lossProbability;

    public MessageLossSimulator(double lossProbability) {
        this.lossProbability = lossProbability;
    }

    public boolean shouldDropMessage() {
        return random.nextDouble() < lossProbability;
    }
}