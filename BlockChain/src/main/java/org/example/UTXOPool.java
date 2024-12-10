package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UTXOPool {

    private Map<String, TransactionOutput> UTXOPool ;

    private static UTXOPool instance; // Singleton instance
    public UTXOPool() {
        this.UTXOPool = new ConcurrentHashMap<>();
    }

    public static UTXOPool getInstance() {
        if (instance == null) {
            synchronized (UTXOPool.class) {
                if (instance == null) {
                    instance = new UTXOPool();
                }
            }
        }
        return instance;
    }


    public synchronized Map<String, TransactionOutput> getUTXOPool() {
        return UTXOPool;
    }

    public synchronized void updateUTXOPool(Transaction transaction) {
        // Remove spent UTXOs
        for (TransactionInput input : transaction.getInputs()) {
            UTXOPool.remove(input.getTransactionOutputId());
        }
        // Add new UTXOs
        for (TransactionOutput output : transaction.getOutputs()) {
            UTXOPool.put(output.getId(), output);
        }
    }

    public synchronized int getMyTotalFunds(String myPublicKey) {
        return UTXOPool.values().stream()
                .filter(output -> output.isMine(myPublicKey))  // Only select UTXOs belonging to the specified public key
                .mapToInt(TransactionOutput::getAmount)  // Sum the amounts of those UTXOs
                .sum();
    }


    public synchronized void addUTXO(TransactionOutput output) {
        UTXOPool.put(output.getId(), output);
    }

    // Remove a UTXO from the pool
    public synchronized void removeUTXO(String outputId) {
        UTXOPool.remove(outputId);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UTXOPool:\n");
        for (Map.Entry<String, TransactionOutput> entry : UTXOPool.entrySet()) {
            sb.append("Key: ").append(entry.getKey())
                    .append(", Value: ").append(entry.getValue().toString())
                    .append("\n");
        }
        return sb.toString();
    }



}
