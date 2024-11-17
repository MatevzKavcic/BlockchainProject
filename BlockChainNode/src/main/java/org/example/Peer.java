package org.example;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static org.example.Main.*;



public class Peer implements Runnable {

    @Override
    public void run() {
        try {
            // Get and print the local IP address
            InetAddress localHost = InetAddress.getLocalHost();
            String myIp = localHost.getHostAddress();
            System.out.println("My IP: " + myIp);

            if (firstNode) {
                // Act as a server
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
                }
            } else {
                // Act as a client
                System.out.println("Starting as a client...");
                try (Socket echoSocket = new Socket(hostName, 6000);
                     PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(
                             new InputStreamReader(echoSocket.getInputStream()));
                     BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
                    System.out.println("Connected to the server at " + hostName + ":" + portNumber);

                    // Example: Send user input to the server
                    String userInput;
                    while ((userInput = stdIn.readLine()) != null) {
                        out.println(userInput);
                        System.out.println("Server response: " + in.readLine());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}