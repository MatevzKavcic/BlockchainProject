package org.example;
import java.util.List;

public class Transaction {
    private String transactionId; // Unique identifier for the transaction
    private List<TransactionInput> inputs; // UTXOs being spent
    private List<TransactionOutput> outputs; // New UTXOs being created
    private String sender; // Public key of the sender
    private String recipient; // Public key of the recipient
    private double amount; // Total amount being transferred

    public Transaction(String sender, String recipient, double amount, List<TransactionInput> inputs, List<TransactionOutput> outputs) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.inputs = inputs;
        this.outputs = outputs;
        this.transactionId = calculateHash();
    }

    private String calculateHash() {
        // Generate a unique ID for the transaction
        return HashingUtils.applySHA256(sender + recipient + amount + inputs.toString() + outputs.toString());
    }

    public boolean validateTransaction(UTXOPool utxoPool) {
        double inputSum = 0;

        // Verify inputs are valid
        for (TransactionInput input : inputs) {
            TransactionOutput UTXO = utxoPool.getUTXOPool().get(input.getTransactionOutputId());
            if (UTXO == null || !UTXO.isMine(sender)) {
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
        double outputSum = outputs.stream().mapToDouble(TransactionOutput::getAmount).sum();
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
