package org.example;


import com.google.gson.Gson;
import util.LogLevel;
import util.Logger;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionManager extends Thread{


    private UTXOPool utxoPool ;

    private ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers;

    public Gson gson = new Gson();

    private PublicKey publicKey;

    private Random random; // Random generator for testing
    private Blockchain blockchain;

    private TransactionPool transactionPool;


    public TransactionManager(ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers, PublicKey publicKey, Blockchain blockchain, TransactionPool transactionPool) {
        this.utxoPool = UTXOPool.getInstance();
        this.connectedPeers = connectedPeers;
        this.publicKey = publicKey;
        this.blockchain = blockchain;
        this.transactionPool = transactionPool;
        this.random = new Random();
        this.setName("transaction Mannager Thread");

    }

    public void run(){


        //  vedno vprasas prvega na katerega se povezes. za zdej je uredi za debuging
        synchronized (SharedResources.LOCK) {

            // Wait until blockchain` is not null
            try {
                // Wait until blockchain is not null AND connectedPeers is not empty
                while (connectedPeers.isEmpty()) {
                    Logger.log("WAITING FOR A Updated connected peers");
                    SharedResources.LOCK.wait();
                }

                if (transactionPool==null&&blockchain==null){
                    Logger.log("NOT WAITING ANYMORE REQUESTING");

                    // When notified and conditions are met, send requests
                    List<PublicKey> peerKeys = new ArrayList<>(connectedPeers.keySet());
                    Random random = new Random();
                    requestTransactionPool(peerKeys.get(random.nextInt(peerKeys.size())));
                    requestUTXOPool(peerKeys.get(random.nextInt(peerKeys.size())));
                    requestBlockchain(peerKeys.get(random.nextInt(peerKeys.size())));
                }


            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

            // If the blockchain, transactionPool, and UTXOPool are already available
            Logger.log(transactionPool + "" + blockchain, LogLevel.Debug);
        }

        //make new thread that is only for making new transactions
        RandomTransactionMakerThread randomTransactionMakerThread = new RandomTransactionMakerThread(publicKey,utxoPool,connectedPeers,transactionPool);
        randomTransactionMakerThread.start();
    }

    // to bo class ki booo na zacetku requestou blockchain
    public void validateNewTransaction(Transaction transaction) {
        if (utxoPool == null) {
            Logger.log("UTXOpool is null, cannot validate transaction", LogLevel.Error);
            return;
        }

        Logger.log("Validating transaction: " + transaction, LogLevel.Debug);
        boolean isValid = transaction.validateTransaction(utxoPool);

        synchronized (UTXOPool.getInstance()){
            if (isValid) {
                Logger.log("Transaction is valid, adding to the transaction pool.", LogLevel.Success);
                utxoPool.updateUTXOPool(transaction); // updajti se transaction pool
                transactionPool.addTransaction(transaction); // Assuming `TransactionPool` has an `addTransaction` method
                logTransactionDetails(transaction);
                logAllPeerBalances();
            } else {
                Logger.log("Transaction validation failed.", LogLevel.Error);
            }
        }
    }

    //oz bo requestau za thread pool in pol ko bo dobil kaksno transakcijo jo bo moral obbdelat in ja...

    private void requestUTXOPool(PublicKey sendToPublicKey) {

        Message m = new Message(MessageType.REQUESTUTXOPOOL,"",publicKeyToString(publicKey));
        String mString = gson.toJson(m);

        WriteMeThread thread = (WriteMeThread) connectedPeers.get(sendToPublicKey).getThread();

        thread.sendMessage( mString);
    }

    private void requestTransactionPool(PublicKey sendToPublicKey) {

        Message m = new Message(MessageType.REQUESTTRANSPOOL,"",publicKeyToString(publicKey));
        String mString = gson.toJson(m);

        WriteMeThread thread = (WriteMeThread) connectedPeers.get(sendToPublicKey).getThread();

        thread.sendMessage( mString);


    }


    public void sendTransactionPool(PublicKey sendToPublicKey) {
        String transPoolString = gson.toJson(transactionPool);

        Message m = new Message(MessageType.RESPONSETRANSPOOL,transPoolString,publicKeyToString(publicKey));
        String mString = gson.toJson(m);

        WriteMeThread thread = (WriteMeThread) connectedPeers.get(sendToPublicKey).getThread();

        thread.sendMessage( mString);
    }

    public void updateTransactionPool(String transactionPoolString) {
        transactionPool=gson.fromJson(transactionPoolString,TransactionPool.class);
    }

    public void requestBlockchain(PublicKey sendToPublicKey) {

        //enega rendom peera dobi in poslji blockchainrequest.

        Message m = new Message(MessageType.BLOCKCHAINREQUEST,"",publicKeyToString(publicKey));
        String mString = gson.toJson(m);

        WriteMeThread thread = (WriteMeThread) connectedPeers.get(sendToPublicKey).getThread();

        thread.sendMessage( mString);

    }

    //method ki bo poslau v network prosnjo da se updejta UTXO pool tako da bojo dodali se njega.

    public void sendUTXOPool(PublicKey sendToPublicKey) {
        String UTXOpoolString = gson.toJson(utxoPool);

        Message m = new Message(MessageType.RESPONSEUTXOPOOL,UTXOpoolString,publicKeyToString(publicKey));
        String mString = gson.toJson(m);

        WriteMeThread thread = (WriteMeThread) connectedPeers.get(sendToPublicKey).getThread();

        thread.sendMessage( mString);
    }

    public void updateUTXOPool(String UTXOPoolString) {
        synchronized (UTXOPool.getInstance()) {
            Logger.log("Before update: " + UTXOPool.getInstance().toString(), LogLevel.Debug);

            // Deserialize into a temporary UTXOPool instance
            UTXOPool newUTXOPool = gson.fromJson(UTXOPoolString, UTXOPool.class);

            // Clear the current singleton and repopulate it
            UTXOPool.getInstance().getUTXOPool().clear();
            UTXOPool.getInstance().getUTXOPool().putAll(newUTXOPool.getUTXOPool());

            Logger.log("Updating UTXO pool", LogLevel.Success);
            Logger.log("After update: " + UTXOPool.getInstance().toString(), LogLevel.Debug);
        }
    }


    private void logTransactionDetails(Transaction transaction) {
        // Get sender and recipient names
        String senderName = generateNameFromPublicKey(transaction.getSender());
        String recipientName = generateNameFromPublicKey(transaction.getRecipient());


        int senderBalance = UTXOPool.getInstance().getMyTotalFunds(transaction.getSender());
        int recipientBalance = UTXOPool.getInstance().getMyTotalFunds(transaction.getRecipient());

        // Log the transaction details
        String transactionLog = String.format("%s (%d credits) --> %s (%d credits) %d credits transferred",
                senderName, senderBalance, recipientName, recipientBalance, transaction.getAmount());

        Logger.log(transactionLog, LogLevel.Info);
    }
    public void logAllPeerBalances() {
        UTXOPool utxoPool = UTXOPool.getInstance();
        StringBuilder balanceReport = new StringBuilder();

        balanceReport.append(String.format("%-30s | %-10s\n", "Peer", "Balance"));
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


