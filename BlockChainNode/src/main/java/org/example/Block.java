package org.example;

import java.io.Serial;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;



public class Block implements Serializable {
    private int index;
    private long timestamp;
    private List<Transaction> transactions; // List of transactions in the block
    private String previousHash;
    private String hash;
    private int nonce;

    private byte[] signature;

    public Block(int index, long timestamp, List<Transaction> transactions, String previousHash, int nonce, byte[] signature) {
        this.index = index;
        this.timestamp = timestamp;
        this.transactions = transactions;
        this.previousHash = previousHash;
        this.nonce = nonce;
        this.signature = signature;
        this.hash = calculateHash(); // Generate hash during initialization
    }

    public String calculateHash() {
        String dataToHash = index + timestamp + transactions.toString() + previousHash + nonce;
        return HashingUtils.applySHA256(dataToHash); // Use a utility method for SHA-256 hashing
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0'); // Difficulty target
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block mined: " + hash);
    }

    public int getIndex() {
        return index;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public List<Transaction> getTransactions() {
        return transactions;
    }
    public String getPreviousHash() {
        return previousHash;
    }
    public String getHash() {
        return hash;
    }
    public int getNonce() {
        return nonce;
    }

    public boolean validateHash() {
        return calculateHash().equals(hash);
    }

    public void signBlock(PrivateKey privateKey) throws Exception {
        String dataToSign = calculateHash();
        this.signature = HashingUtils.sign(dataToSign.getBytes(), privateKey);
    }

    // Add a method to verify the block's signature
    public boolean verifySignature(PublicKey publicKey) throws Exception {
        return HashingUtils.verify(calculateHash().getBytes(), signature, publicKey);
    }


    // Getters for block properties
}