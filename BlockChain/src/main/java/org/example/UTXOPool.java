package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UTXOPool {

    private Map<String, TransactionOutput> UTXOPool ;

    public UTXOPool() {
        this.UTXOPool = new ConcurrentHashMap<>();
    }


    public Map<String, TransactionOutput> getUTXOPool() {
        return UTXOPool;
    }

    public void updateUTXOPool(Transaction transaction) {
        // Remove spent UTXOs
        for (TransactionInput input : transaction.getInputs()) {
            UTXOPool.remove(input.getTransactionOutputId());
        }
        // Add new UTXOs
        for (TransactionOutput output : transaction.getOutputs()) {
            UTXOPool.put(output.getId(), output);
        }
    }


    public void addUTXO(TransactionOutput output) {
        UTXOPool.put(output.getId(), output);
    }

    // Remove a UTXO from the pool
    public void removeUTXO(String outputId) {
        UTXOPool.remove(outputId);
    }

    // Get all UTXOs
    public Map<String, TransactionOutput> getUTXOs() {
        return UTXOPool;
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
