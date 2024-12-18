package org.example;

import util.LogLevel;
import util.Logger;

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

    public boolean mineBlock(int difficulty, MiningCoordinator miningCoordinator) {
        String target = new String(new char[difficulty]).replace('\0', '0'); // Difficulty target
        Logger.log("mining block on index -> "+index);
        while (!hash.substring(0, difficulty).equals(target)) {
            if (miningCoordinator.isMiningInterrupted()){
                break;
            }
            nonce++;
            hash = calculateHash();
            if (miningCoordinator.isMiningInterrupted()){
                Logger.log("Mining was interupetd by a new block ", LogLevel.Warn);
                return false;
            }
        }

        int tmpCheck = Blockchain.getInstance().getChain().size();

        if (tmpCheck>=index){
            return false;
        }

        Logger.log("MINED A BLOCK! index -> "+ index  + " CHain is on lenght -> " +Blockchain.getInstance().getChain().size(),LogLevel.Warn);
        return true;
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