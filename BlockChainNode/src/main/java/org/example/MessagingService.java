package org.example;

import com.google.gson.Gson;
import util.LogLevel;
import util.Logger;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class MessagingService extends Thread {
    private final BlockingQueue<String> messageQueue;
    private ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers;

    private int portNumber;

    private PublicKey publicKey;

    private PrivateKey privateKey;

    private String hostName;

    public MessagingService(BlockingQueue<String> messageQueue, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers, int portNumber, PublicKey publicKey, PrivateKey privateKey, String hostName) {
        this.messageQueue = messageQueue;
        this.connectedPeers = connectedPeers;
        this.portNumber = portNumber;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.hostName = hostName;
    }

    @Override
    public void run() {
        Gson gson = new Gson();

        try {

            // bussy waiting for someone to put something in the queue. when that happens it proceses it.
            while (true) {
                // Wait for a message to process
                String message = messageQueue.take();
                Logger.log("Processing message: " + message, LogLevel.Status);

                Message messageObject = gson.fromJson(message,Message.class);
                int serverPort = messageObject.getServerPort();

                switch (messageObject.getHeader()) {
                    case HANDSHAKE -> {
                    }
                    case HANDSHAKEKEYRETURN -> {
                    }

                    case NEWPEER -> {
                        //When I get this message i want to connect to this peer on this message port.
                        //so i create a socket that will connect to it.
                        Client client= new Client(hostName,serverPort,messageQueue,connectedPeers,publicKey, privateKey,true); // posljes se true variable da ves da bos pol poslau difrent message
                        client.start();
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + messageObject.getHeader());
                }

                System.out.println(message);




                // Process the message (e.g., log, broadcast, or route)
                // For now, simply print it
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }
}