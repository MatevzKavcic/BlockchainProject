package org.example;
import util.LogLevel;
import util.Logger;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

public class Transaction {
    private String transactionId; // Unique identifier for the transaction
    private List<TransactionInput> inputs; // UTXOs being spent
    private List<TransactionOutput> outputs; // New UTXOs being created
    private String sender; // Public key of the sender
    private String recipient; // Public key of the recipient
    private double amount; // Total amount being transferred

    public Transaction(String sender, String recipient, double amount, List<TransactionInput> inputs, List<TransactionOutput> outputs, String transactionId) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.inputs = inputs;
        this.outputs = outputs;
        this.transactionId = transactionId;
    }

    private String calculateHash() {
        // Generate a unique ID for the transaction
        return HashingUtils.applySHA256(sender + recipient + amount + inputs.toString() + outputs.toString());
    }

    public boolean validateTransaction(UTXOPool utxoPool) {
        double inputSum = 0;
        double outputSum = 0;

        Logger.log(utxoPool.toString());

        // Validate inputs
        for (TransactionInput input : inputs) {
            // Find the corresponding UTXO
            TransactionOutput UTXO = utxoPool.getUTXOPool().get(input.getTransactionOutputId());
            if (UTXO == null) {
                Logger.log("UTXO not found for input: " + input.getTransactionOutputId(), LogLevel.Error);
                return false; // UTXO does not exist
            }
            if (!UTXO.isMine(sender)) {
                Logger.log("UTXO is not owned by sender: " + sender, LogLevel.Error);
                return false; // UTXO is not owned by the sender
            }

            // Attach the UTXO to the input for later reference
            input.setUTXO(UTXO);


            // Sum up the input values
            inputSum += UTXO.getAmount();
        }

        // Validate sufficient balance
        if (inputSum < amount) {
            Logger.log("Insufficient balance. Input sum: " + inputSum + ", required: " + amount, LogLevel.Error);
            return false;
        }

        // Validate outputs
        outputSum = outputs.stream().mapToDouble(TransactionOutput::getAmount).sum();
        if (inputSum != outputSum) {
            Logger.log("Input sum does not match output sum. Input sum: " + inputSum + ", output sum: " + outputSum, LogLevel.Error);
            return false; // The transaction does not balance
        }

        return true; // Transaction is valid
    }


    // Getters and setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<TransactionInput> inputs) {
        this.inputs = inputs;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<TransactionOutput> outputs) {
        this.outputs = outputs;
    }

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


    public PublicKey stringToPublicKey(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
