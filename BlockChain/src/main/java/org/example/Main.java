package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Enumeration;

public class Main {
    public static String myIp;

    public static int portNumber = 6001;
    public static String hostName = "127.0.0.0";

    public static boolean firstNode = true;


    public static void main(String[] args) {
                if (!firstNode) {
                    try (
                            Socket echoSocket = new Socket(hostName, portNumber);        // 1st statement
                            PrintWriter out =                                            // 2nd statement
                                    new PrintWriter(echoSocket.getOutputStream(), true);
                            BufferedReader in =                                          // 3rd statement
                                    new BufferedReader(
                                            new InputStreamReader(echoSocket.getInputStream()));
                            BufferedReader stdIn =                                       // 4th statement
                                    new BufferedReader(
                                            new InputStreamReader(System.in))
                    ) {
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                else {
                    try (
                            ServerSocket serverSocket = new ServerSocket(portNumber);
                            Socket clientSocket = serverSocket.accept();
                            PrintWriter out =
                                    new PrintWriter(clientSocket.getOutputStream(), true);
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(clientSocket.getInputStream()));
                    ) {
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }



    }


    public static void findMyIp() {
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();

            while(true) {
                NetworkInterface inter;
                do {
                    do {
                        if (!e.hasMoreElements()) {
                            return;
                        }

                        inter = (NetworkInterface)e.nextElement();
                    } while(!inter.isUp());
                } while(inter.isLoopback());

                Enumeration addr = inter.getInetAddresses();

                while(addr.hasMoreElements()) {
                    InetAddress address = (InetAddress)addr.nextElement();
                    myIp = address.getHostAddress().replace("/", "");
                    if (myIp.length() <= 15) {
                        System.out.println("\u001b[34m  LocalIp: " + myIp);
                    }
                }
            }
        } catch (SocketException var4) {
            var4.printStackTrace();
        }
    }

}
