package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Server extends Thread{
    int portNumber;
    public Server(int portNumber) {
        this.portNumber = portNumber;
    }

    @Override
    public void run() {
        System.out.println("Starting as a server...");
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server is listening on port: " + portNumber);
            try (Socket clientSocket = serverSocket.accept();
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(clientSocket.getInputStream()))) {
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Example: Echo back received messages
                String received;
                while ((received = in.readLine()) != null) {
                    System.out.println("Received: " + received);
                    out.println("Echo: " + received);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
