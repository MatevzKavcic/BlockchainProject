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
    private int amount; // Total amount being transferred

    public Transaction(String sender, String recipient, int amount, List<TransactionInput> inputs, List<TransactionOutput> outputs, String transactionId) {
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
        int inputSum = 0;

        // Verify inputs are valid
        for (TransactionInput input : inputs) {
            TransactionOutput UTXO = utxoPool.getUTXOPool().get(input.getTransactionOutputId());
            if (UTXO == null || !UTXO.isMine(sender)) {
                Logger.log("Input is invalid or not owned by the sender", LogLevel.Error);
                return false; // Input is invalid or not owned by the sender
            }
            input.setUTXO(UTXO); // Attach the UTXO to the input
            inputSum += UTXO.getAmount();
        }

        // Ensure the sender has sufficient funds
        if (inputSum < amount) {
            return false; // Insufficient balance
        }

        // Outputs must not exceed the inputs
        int outputSum = outputs.stream().mapToInt(TransactionOutput::getAmount).sum();
        return inputSum == outputSum;
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
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
