package org.example;

import com.google.gson.Gson;
import util.LogLevel;
import util.Logger;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RandomTransactionMakerThread extends Thread{

    private PublicKey publicKey; // The public key of this miner
    private UTXOPool utxoPool; // Local UTXO pool for this miner
    private ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers; // Peers in the network
    private Random random; // Random generator for testing
    public Gson gson = new Gson();

    private TransactionPool transactionPool;

    public RandomTransactionMakerThread(PublicKey publicKey, UTXOPool utxoPool, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers) {
        this.publicKey = publicKey;
        this.utxoPool = UTXOPool.getInstance();  // Ensures we're using the singleton UTXOPool instance
        this.connectedPeers = connectedPeers;
        this.transactionPool = TransactionPool.getInstance();  // Use the singleton instance
        this.random = new Random();
        this.setName("RandomTransactionMakerThread");
    }


    @Override
    public void run() {
        while (true) {
            try {
                // Sleep for 20 seconds
                Thread.sleep(5000);

                // Create a random transaction
                Transaction transaction = createRandomTransaction();
                if (transaction==null){

                }
                else {
                    synchronized (UTXOPool.getInstance()){
                        boolean tmp = transaction.validateTransaction(utxoPool);
                        if (tmp) {
                            Logger.log("Transaction is valid.", LogLevel.Info);

                            // Broadcast the transaction
                            broadcastTransaction(transaction);

                            // Update UTXOPool only after broadcasting
                            utxoPool.updateUTXOPool(transaction);
                            Logger.log("UTXOPool updated with transaction.", LogLevel.Success);


                            // Add transaction to the pool
                            transactionPool.addTransaction(transaction);
                            logAllPeerBalances();
                        } else {
                            Logger.log("Transaction validation failed.", LogLevel.Error);
                        }
                    }
                }


            } catch (InterruptedException e) {
                Logger.log("MinerThread interrupted: " + e.getMessage());
                break;
            }
        }
    }

    private Transaction createRandomTransaction() {
        // Get a random recipient from connected peers
        if (connectedPeers.isEmpty()) return null;

        List<PublicKey> peerKeys = new ArrayList<>(connectedPeers.keySet());
        PublicKey recipientKey = peerKeys.get(random.nextInt(peerKeys.size()));
        String recipientPublicKey = publicKeyToString(recipientKey);

        // Random amount to send
        int amount = random.nextInt(10) + 1;

        // Gather UTXOs to cover the amount
        List<TransactionInput> inputs = new ArrayList<>();
        List<TransactionOutput> outputs = new ArrayList<>();
        int total = 0;

        String senderPublicKey = publicKeyToString(publicKey);

        if (utxoPool.getMyTotalFunds(senderPublicKey) < amount) {
            Logger.log("Insufficient funds for transaction. Needed: " + amount);
            return null;
        }

        for (TransactionOutput output : utxoPool.getUTXOPool().values()) {
            if (output.isMine(senderPublicKey)) {
                inputs.add(new TransactionInput(output.getId()));
                total += output.getAmount();
                if (total >= amount) break;
            }
        }

        if (total < amount) return null;

        // Create outputs
        String transactionId = UUID.randomUUID().toString();
        outputs.add(new TransactionOutput(recipientPublicKey, amount, transactionId));
        if (total > amount) {
            outputs.add(new TransactionOutput(senderPublicKey, total - amount, transactionId));
        }

        // Return the transaction without modifying UTXOPool
        return new Transaction(senderPublicKey, recipientPublicKey, amount, inputs, outputs, transactionId);
    }

    private void broadcastTransaction(Transaction transaction) {

        String transactionString = gson.toJson(transaction);
        Message message = new Message(MessageType.TRANSACTION,transactionString,publicKeyToString(publicKey));
        String messageString = gson.toJson(message);

        //send the transaction to everyone.
        for (PeerInfo peer : connectedPeers.values()) {
            WriteMeThread thread =(WriteMeThread) peer.getThread();
            thread.sendMessage(messageString);
        }

    }


    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    public PublicKey stringToPublicKey(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    public void logAllPeerBalances() {
        UTXOPool utxoPool = UTXOPool.getInstance();
        StringBuilder balanceReport = new StringBuilder();

        balanceReport.append(String.format("\n%-30s | %-10s\n", "Peer", "Balance"));
        balanceReport.append("-".repeat(42)).append("\n");

        // Add your own balance
        String myPublicKeyString = publicKeyToString(publicKey); // Assume `publicKey` is your node's public key
        String myName = generateNameFromPublicKey(myPublicKeyString);
        int myBalance = utxoPool.getMyTotalFunds(myPublicKeyString);

        balanceReport.append(String.format("%-30s | %-10d\n", myName + " (You)", myBalance));

        // Add connected peers' balances
        for (PublicKey peer : connectedPeers.keySet()) {
            String peerName = generateNameFromPublicKey(publicKeyToString(peer));
            String peerPublicKey = publicKeyToString(peer);
            int peerBalance = utxoPool.getMyTotalFunds(peerPublicKey);

            balanceReport.append(String.format("%-30s | %-10d\n", peerName, peerBalance));
        }

        Logger.log(balanceReport.toString(), LogLevel.Info);
    }

    public static String generateNameFromPublicKey(String publicKey) {
        // Generate a UUID based on the public key hash
        UUID uuid = UUID.nameUUIDFromBytes(publicKey.getBytes());
        return uuid.toString().split("-")[0]; // Use the first part for brevity
    }




}
