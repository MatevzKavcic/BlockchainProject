package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;


public class Server extends Thread{
    int portNumber;
    private final BlockingQueue<String> messageQueue;
    public Server(int portNumber, BlockingQueue<String> messageQueue) {
        this.portNumber = portNumber;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Start a thread to handle this client
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
                System.out.println("Server received: " + message);
                messageQueue.put(message); // Add message to queue
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
