package org.example;

import util.LogLevel;
import util.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;




public class Peer extends Thread {


     int portNumber; // portnumber je numbeer serverja kje se bo odprl
     String hostName;
    int portNumberOfFirstConnect;
    boolean firstNode ;

    KeyGenerator keyGenerator ;

    private ConcurrentHashMap<PublicKey,PeerInfo> connectedPeers = new ConcurrentHashMap<>();


    public Peer(int portNumber, String hostName, boolean firstNode, int portNumberOfFirstConnect) {
        this.portNumber = portNumber;
        this.hostName = hostName;
        this.firstNode = firstNode;
        this.portNumberOfFirstConnect= portNumberOfFirstConnect;
        keyGenerator=new KeyGenerator();
    }

    @Override
    public void run() {
        try {
            // Get and print the local IP address
            InetAddress localHost = InetAddress.getLocalHost();
            String myIp = localHost.getHostAddress();
            Logger.log("My IP: " + myIp, LogLevel.Info);

            BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

            UTXOPool UTXOPool = new UTXOPool();

            Blockchain blockchain = Blockchain.getInstance();

            blockchain.addGenesisBlock(keyGenerator.getPublicKey());
            TransactionPool transactionPool = TransactionPool.getInstance();
            MiningCoordinator miningCoordinator = new MiningCoordinator();

            TransactionManager transactionManager = new TransactionManager(connectedPeers, keyGenerator.getPublicKey(),blockchain);
            transactionManager.start();

            MinerThread minerThread = new MinerThread(keyGenerator.getPublicKey(), keyGenerator.getPrivateKey(), connectedPeers,miningCoordinator);
            minerThread.start();

            MessagingService messagingServiceThread = new MessagingService(messageQueue,connectedPeers,hostName,portNumber, keyGenerator.getPublicKey(), keyGenerator.getPrivateKey(),blockchain,UTXOPool,transactionManager,miningCoordinator);
            messagingServiceThread.start();

            if (firstNode) {
                // Create a Server Thread !
                Server server = new Server(portNumber,messageQueue,connectedPeers, keyGenerator.getPublicKey(), keyGenerator.getPrivateKey(),blockchain,UTXOPool);
                server.start();
            }

            //this part of the code will never be true, because this node is the "Server" node
            else {
                // Create a server Thread that will listen
                Server server= new Server(portNumber,messageQueue,connectedPeers,
                        keyGenerator.getPublicKey(), keyGenerator.getPrivateKey(),
                        blockchain, UTXOPool);

                server.start();

                // Make a client Thread that will connect to a server
                Client client = new Client(hostName,portNumber,messageQueue,
                        connectedPeers, keyGenerator.getPublicKey(), keyGenerator.getPrivateKey(),
                        portNumberOfFirstConnect,transactionManager);

                client.start();




            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

