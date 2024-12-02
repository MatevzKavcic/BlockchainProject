package org.example;

import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private String sender;
    private String recipient;
    private double amount;

    public Transaction(String sender, String recipient, double amount) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
    }

    /*public Transaction createTransaction(String senderPublicKey, String recipientPublicKey, double amount, UTXOPool utxoPool) {
        List<TransactionInput> inputs = new ArrayList<>();
        List<TransactionOutput> outputs = new ArrayList<>();
        double total = 0;

        // Gather sufficient outputs
       for (TransactionOutput output : utxoPool.getUTXOPool().values()) {
           if (output.isOwnedBy(senderPublicKey)) {
               inputs.add(new TransactionInput(output.getId()));
               total += output.getAmount();
               utxoPool.removeOutput(output.getId()); // Remove spent output
               if (total >= amount) break;
           }
       }

        if (total < amount) {
            throw new IllegalArgumentException("Insufficient balance.");
        }

        // Create outputs
        outputs.add(new TransactionOutput(recipientPublicKey, amount, "NEW_TRANSACTION_ID", 0));
        if (total > amount) {
            outputs.add(new TransactionOutput(senderPublicKey, total - amount, "NEW_TRANSACTION_ID", 1)); // Change
        }

        // Add new outputs to the pool
        for (TransactionOutput output : outputs) {
             utxoPool.addOutput(output);
        }

        return new Transaction(senderPublicKey, recipientPublicKey, amount, inputs, outputs);
    }

     */


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return sender + " -> " + recipient + ": " + amount;
    }

    // Getters and setters
}
