package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import util.LogLevel;
import util.Logger;

import java.lang.reflect.Type;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class MessagingService extends Thread {
    private final BlockingQueue<String> messageQueue;
    private ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers;

    public Blockchain blockchain;

    private UTXOPool utxoPool;

    private TransactionManager transactionManager;

    public MessagingService(BlockingQueue<String> messageQueue, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers, String hostName, int portNumber, PublicKey publicKey, PrivateKey privateKey, Blockchain blockchain, UTXOPool utxoPool, TransactionManager transactionManager) {
        this.messageQueue = messageQueue;
        this.connectedPeers = connectedPeers;
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.blockchain  = blockchain;
        this.utxoPool = utxoPool;
        this.transactionManager = transactionManager;
        this.setName("Messaging service Thread");
    }

    String hostName;
    int portNumber;

    private PublicKey publicKey;

    private PrivateKey privateKey;


    Gson gson = new Gson();



    @Override
    public void run() {
        try {

            // bussy waiting for someone to put something in the queue. when that happens it proceses it.
            while (true) {
                // Wait for a message to process
                String message = messageQueue.take();
                Type type = new TypeToken<ArrayList<Integer>>() {}.getType();

                Message messageObject = gson.fromJson(message,Message.class);

                PublicKey sender  = stringToPublicKey(messageObject.getPublicKey()) ;
                switch (messageObject.getHeader()) {
                    case HANDSHAKE -> {
                    }
                    case HANDSHAKEKEYRETURN -> {

                    }
                    case PEERLIST -> {
                        ArrayList<Integer> arrayListOfPorts = gson.fromJson(messageObject.getBody(), type);

                        // in this case i need to connnect to all peers in the list
                        Logger.log("i recived this array of ports i neeed to connect to : " + arrayListOfPorts, LogLevel.Info);

                        for (Integer connectToPort : arrayListOfPorts) {
                            //Is special variable has to be true so the servir will get the correct header.
                            Client novaPovezava = new Client(hostName,portNumber,messageQueue,connectedPeers,publicKey,privateKey,connectToPort,true);
                            novaPovezava.start();

                        }
                    }
                    case PEERLISTRETURN -> {
                    }
                    case BLOCKCHAINREQUEST -> {
                        PeerInfo peerInfo = connectedPeers.get(sender);

                        WriteMeThread thread = (WriteMeThread) peerInfo.getThread();
                        String pkString = publicKeyToString(publicKey);
                        Message m = new Message(MessageType.BLOCKCHAINRESPONSE,gson.toJson(blockchain), pkString);
                        String mString = gson.toJson(m);
                        thread.sendMessage( mString);
                    }
                    case BLOCKCHAINRESPONSE -> {
                        blockchain = gson.fromJson(messageObject.getBody(), Blockchain.class);//string zs blockchainBody
                        notifyUpdates();
                    }

                    // if you get this block it means that you just connected to a network and the node you connected to sent you this message.
                    case BLOCKCHAINSEND -> {
                    }
                    case BLOCKCHAINITIALIZE -> {
                        blockchain = gson.fromJson(messageObject.getBody(), Blockchain.class);//string zs blockchainBody

                        utxoPool= blockchain.getUTXOPool();
                        Logger.log(blockchain.getUTXOPool().toString() ,LogLevel.Warn);

                    }


                    case UTXOPOOLINITIALIZATION -> {
                        utxoPool= gson.fromJson(messageObject.getBody(), UTXOPool.class);

                        Logger.log(blockchain.getUTXOPool().toString() ,LogLevel.Warn);

                    }


                    case TRANSACTION -> {
                    Logger.log("RECIEVED A NEW TRANSACTION FROM : ",LogLevel.Success);

                    }
                    case REQUESTTRANSPOOL -> {
                        Logger.log("recived REQTRANSPOOL message from : ", LogLevel.Status);
                        transactionManager.sendTransactionPool(sender);
                    }
                    case RESPONSETRANSPOOL ->{
                        Logger.log("recived RESPONSE TRANSPOOL message from : ", LogLevel.Status);
                        transactionManager.updateTransactionPool(messageObject.getBody());
                    }
                    case REQUESTUTXOPOOL -> {
                        Logger.log("recived REQUESTUTXOPOOL message from : "+ sender, LogLevel.Status);
                        transactionManager.sendUTXOPool(sender);
                    }
                    case RESPONSEUTXOPOOL->{
                        Logger.log("recived RESPONSE UTXOPOOL message from : "+ sender, LogLevel.Status);
                        transactionManager.updateUTXOPool(messageObject.getBody());
                    }
                }


                // Process the message (e.g., log, broadcast, or route)
                // For now, simply print it
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendTransactionPool() {
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
    public synchronized void notifyUpdates() {
        notifyAll(); // Notify all waiting threads
    }
}