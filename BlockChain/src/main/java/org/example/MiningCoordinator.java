package org.example;

import util.LogLevel;
import util.Logger;

public class MiningCoordinator {
    private volatile boolean miningInterrupted = false;

    // Method to check if mining is interrupted
    public boolean isMiningInterrupted() {
        return miningInterrupted;
    }

    // Method to interrupt mining
    public void interruptMining() {
        //Logger.log("Mining Coordinator Interupting mining", LogLevel.Error);
        miningInterrupted = true;
    }

    // Method to reset the mining flag
    public void resetMiningFlag() {
        //Logger.log("Mining Coordinator reseting mining", LogLevel.Error);
        miningInterrupted = false;
    }
}
