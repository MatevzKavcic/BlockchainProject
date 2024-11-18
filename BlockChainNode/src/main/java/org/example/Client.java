package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Client extends Thread{

    String hostName;
    int portNumber;
    private final BlockingQueue<String> messageQueue;
    private ConcurrentHashMap<String, Socket> connectedPeers;


    public Client(String hostName, int portNumber, BlockingQueue<String> messageQueue,ConcurrentHashMap<String, Socket> connectedPeers) {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.messageQueue = messageQueue;
        this.connectedPeers = connectedPeers;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(hostName, 6000);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to server: " + hostName + ":" + portNumber);

            System.out.println();

            // Thread to listen for messages from the server and put them in a messageQueue.
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("Server says: " + serverMessage);
                        messageQueue.put(serverMessage); // Add server message to queue
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // Read user input and send to the server
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput); // Send to the server
                messageQueue.put("From Client: " + userInput); // Add to local queue
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
