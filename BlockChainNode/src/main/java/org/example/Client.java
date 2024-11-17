package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

public class Client extends Thread{

    String hostName;
    int portNumber;
    private final BlockingQueue<String> messageQueue;


    public Client(String hostName, int portNumber, BlockingQueue<String> messageQueue) {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(hostName, 6000);  // hardcoding portnumber
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to server: " + hostName + ":" + portNumber);

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput); // Send to the connected peer
                messageQueue.put("From Client: " + userInput); // Add to message queue
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
