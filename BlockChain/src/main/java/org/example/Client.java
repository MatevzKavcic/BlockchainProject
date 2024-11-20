package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Client extends Thread{

    String hostName;
    int portNumber;
    private final BlockingQueue<String> messageQueue;
    private ConcurrentHashMap<String, Socket> connectedPeers;
    private PublicKey publicKey;

    private PrivateKey privateKey;



    public Client(String hostName, int portNumber, BlockingQueue<String> messageQueue, ConcurrentHashMap<String, Socket> connectedPeers, PublicKey publicKey, PrivateKey privateKey) {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.messageQueue = messageQueue;
        this.connectedPeers = connectedPeers;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(hostName, 6000); // Don't use try-with-resources here

            System.out.println("Connected to server: " + hostName + ":" + portNumber);

            // Add this socket to the connectedPeers map
            String clientKey = socket.getInetAddress() + ":" + socket.getPort();

            System.out.println("This is my public key: " + publicKey);
            System.out.println("This is my private key: " + privateKey);

            connectedPeers.put(clientKey, socket);

            // Spawn a thread to handle incoming messages from the server
            new Thread(() -> handleServerMessages(socket)).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Method to handle messages from the server
    private void handleServerMessages(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Server says: " + message);
                messageQueue.put(message); // Add server message to queue
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
