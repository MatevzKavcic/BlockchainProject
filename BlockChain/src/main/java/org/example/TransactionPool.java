package org.example;

import util.LogLevel;
import util.Logger;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TransactionPool {
    private static final TransactionPool INSTANCE = new TransactionPool();
    private ConcurrentHashMap<String, Transaction> transactionPool;

    // Private constructor to prevent instantiation
    private TransactionPool() {
        transactionPool = new ConcurrentHashMap<>();
    }

    // Get the singleton instance
    public static TransactionPool getInstance() {
        return INSTANCE;
    }

    public synchronized void addTransaction(Transaction transaction) {
        transactionPool.put(transaction.getTransactionId(), transaction);
        Logger.log("Transaction added to the pool", LogLevel.Success);
    }

    public synchronized List<Transaction> getTransactionsForBlock(int maxCount) {
        List<Transaction> selectedTransactions = transactionPool.values()
                .stream()
                .limit(maxCount)
                .collect(Collectors.toList());

        return selectedTransactions;
    }

    public synchronized void removeTransactions(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            transactionPool.remove(transaction.getTransactionId());
        }
        //usefull logger
        //Logger.log(transactions.size() + " transactions removed from the pool", LogLevel.Warn);
    }

    public synchronized String getTransactionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("%-20s | %-20s | %-10s\n", "Sender", "Recipient", "Amount"));
        summary.append("-".repeat(55)).append("\n");

        for (Transaction transaction : transactionPool.values()) {
            String sender = generateNameFromPublicKey(transaction.getSender());
            String recipient = generateNameFromPublicKey(transaction.getRecipient());
            int amount = transaction.getAmount();

            summary.append(String.format("%-20s | %-20s | %-10d\n", sender, recipient, amount));
        }

        return summary.toString();
    }

    // Helper method to generate a name from a public key (simplifies the output)
    public static String generateNameFromPublicKey(String publicKey) {
        // Generate a UUID based on the public key hash
        UUID uuid = UUID.nameUUIDFromBytes(publicKey.getBytes());
        return uuid.toString().split("-")[0]; // Use the first part for brevity
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


    public int size() {
        return transactionPool.size();
    }
}
