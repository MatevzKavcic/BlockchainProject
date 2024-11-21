package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;




public class Peer extends Thread {


     int portNumber;
     String hostName;
    boolean firstNode ;

    KeyGenerator keyGenerator ;

    private ConcurrentHashMap<PublicKey,PeerInfo> connectedPeers = new ConcurrentHashMap<>();


    public Peer(int portNumber, String hostName, boolean firstNode) {
        this.portNumber = portNumber;
        this.hostName = hostName;
        this.firstNode = firstNode;
        keyGenerator=new KeyGenerator();
    }

    @Override
    public void run() {
        try {
            // Get and print the local IP address
            InetAddress localHost = InetAddress.getLocalHost();
            String myIp = localHost.getHostAddress();
            System.out.println("My IP: " + myIp);

            BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

            MessagingService messagingServiceThread = new MessagingService(messageQueue,connectedPeers);
            messagingServiceThread.start();

            if (firstNode) {
                // Create a Server Thread !
                Server server = new Server(portNumber,messageQueue,connectedPeers, keyGenerator.getPublicKey(), keyGenerator.getPrivateKey());
                server.start();

            }
            //this part of the code will never be true, because this node is the "Server" node
            else {
                // Act as a client
                Client client = new Client(hostName,portNumber,messageQueue,connectedPeers, keyGenerator.getPublicKey(), keyGenerator.getPrivateKey());
                client.start();

                System.out.println("testing print");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

