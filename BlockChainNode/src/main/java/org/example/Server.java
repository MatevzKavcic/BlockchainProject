package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


public class Server extends Thread{
    int portNumber;
    private final BlockingQueue<String> messageQueue;
    private ConcurrentHashMap<String, Socket> connectedPeers;

    private PublicKey publicKey;

    private PrivateKey privateKey;

    public Server(int portNumber, BlockingQueue<String> messageQueue, ConcurrentHashMap<String, Socket> connectedPeers, PublicKey publicKey, PrivateKey privateKey) {
        this.portNumber = portNumber;
        this.messageQueue = messageQueue;
        this.connectedPeers = connectedPeers;
        this.publicKey =publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientKey = clientSocket.getInetAddress() + ":" + clientSocket.getPort(); // Unique key
                connectedPeers.put(clientKey, clientSocket);
                System.out.println("Client connected: " + clientKey);

                System.out.println("This is my public key" + publicKey);
                System.out.println("this is my private key" + privateKey);
                // Start a thread to handle this client and handle messages from this client.
                // if they send a message you put it in the messageQueue. it only listens.
                new Thread(() -> handleClient(clientSocket)).start();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Server recived message : " + message + " from socket "+ clientSocket.getPort());
                messageQueue.put(message); // Add message to queue
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
