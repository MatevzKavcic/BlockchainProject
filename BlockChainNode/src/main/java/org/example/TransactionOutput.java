package org.example;

public class TransactionOutput {
    private String id; // Unique identifier for this output
    private String recipient; // The public key (or address) of the recipient
    private double amount; // The amount this output is worth
    private String parentTransactionId; // ID of the transaction this output was created in

    public TransactionOutput(String recipient, double amount, String parentTransactionId) {
        this.recipient = recipient;
        this.amount = amount;
        this.parentTransactionId = parentTransactionId;
        this.id = HashingUtils.applySHA256(recipient + amount + parentTransactionId); // Unique ID
    }

    // Check if this output belongs to a certain public key
    public boolean isMine(String publicKey) {
        return publicKey.equals(recipient);
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getParentTransactionId() {
        return parentTransactionId;
    }

    public void setParentTransactionId(String parentTransactionId) {
        this.parentTransactionId = parentTransactionId;
    }

    @Override
    public String toString() {
        return "TransactionOutput{" +
                "id='" + id + '\'' +
                ", recipient='" + recipient + '\'' +
                ", amount=" + amount +
                ", parentTransactionId='" + parentTransactionId + '\'' +
                '}';
    }
}
