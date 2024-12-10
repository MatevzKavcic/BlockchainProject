package org.example;

import util.LogLevel;
import util.Logger;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class TransactionOutput {
    private String id; // Unique identifier for this output
    private String recipient; // The public key (or address) of the recipient
    private int amount; // The amount this output is worth
    private String parentTransactionId; // ID of the transaction this output was created in

    public TransactionOutput(String recipient, int amount, String parentTransactionId) {
        this.recipient = recipient;
        this.amount = amount;
        this.parentTransactionId = parentTransactionId;
        this.id = HashingUtils.applySHA256(recipient + amount + parentTransactionId); // Unique ID
    }

    // Check if this output belongs to a certain public key
    public boolean isMine(String publicKey) {
        try {
            PublicKey pk = stringToPublicKey(publicKey);
            String Spk = publicKeyToString(pk);
            if (recipient.equals(pk.toString())){
                return true;
            }
            PublicKey rpk = stringToPublicKey(recipient);
            String Srpk = publicKeyToString(rpk);
            return Spk.equals(Srpk);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
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

    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    public PublicKey stringToPublicKey(String key) throws Exception {
        key = key.trim();
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }
}
