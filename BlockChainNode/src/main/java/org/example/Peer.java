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
                // Create a Server Thread !
                Server server = new Server(portNumber);
                server.start();
            }
            //this part of the code will never be true, because this node is the "Server" node
            else {
                // Create the Client thread
                Client client = new Client(hostName,portNumber);
                client.start();
                // create the server node so you become a true Peer
                Server server = new Server(portNumber);
                server.start();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
