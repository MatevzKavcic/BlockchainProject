package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import util.LogLevel;
import util.Logger;

import java.lang.reflect.Type;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class MessagingService extends Thread {
    private final BlockingQueue<String> messageQueue;
    private ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers;

    public MessagingService(BlockingQueue<String> messageQueue, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers, String hostName, int portNumber, PublicKey publicKey, PrivateKey privateKey) {
        this.messageQueue = messageQueue;
        this.connectedPeers = connectedPeers;
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
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

                ArrayList<Integer> arrayListOfPorts = gson.fromJson(messageObject.getBody(), type);

                switch (messageObject.getHeader()) {
                    case HANDSHAKE -> {
                    }
                    case HANDSHAKEKEYRETURN -> {

                    }
                    case PEERLIST -> {
                        // in this case i need to connnect to all peers in the list
                        Logger.log("i recived this array of ports i neeed to connect to : " + arrayListOfPorts, LogLevel.Info);

                        for (Integer connectToPort : arrayListOfPorts) {
                            Client novaPovezava = new Client(hostName,portNumber,messageQueue,connectedPeers,publicKey,privateKey,connectToPort,true);
                            novaPovezava.start();

                        }



                    }
                }


                // Process the message (e.g., log, broadcast, or route)
                // For now, simply print it
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }
}