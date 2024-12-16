package org.example;

public class MiningCoordinator {
    private volatile boolean miningInterrupted = false;

    // Method to check if mining is interrupted
    public boolean isMiningInterrupted() {
        return miningInterrupted;
    }

    // Method to interrupt mining
    public void interruptMining() {
        miningInterrupted = true;
    }

    // Method to reset the mining flag
    public void resetMiningFlag() {
        miningInterrupted = false;
    }
}
