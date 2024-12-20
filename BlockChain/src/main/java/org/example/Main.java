package org.example;

import com.google.gson.internal.bind.util.ISO8601Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Enumeration;

public class Main {

    public static int portNumber = 6000;
    public static String hostName = "127.0.0.1";
    public static boolean firstNode = false;

    public static int portNumberOfFirstConnect= 6000;


    // this is the primary node or the root node so it will act as a server!
    public static void main(String[] args) {
        Peer peer = new Peer(portNumber,hostName,firstNode,portNumberOfFirstConnect);
        peer.start();

    }



//    public static void findMyIp() {
//        try {
//            Enumeration e = NetworkInterface.getNetworkInterfaces();
//
//            while(true) {
//                NetworkInterface inter;
//                do {
//                    do {
//                        if (!e.hasMoreElements()) {
//                            return;
//                        }
//
//                        inter = (NetworkInterface)e.nextElement();
//                    } while(!inter.isUp());
//                } while(inter.isLoopback());
//
//                Enumeration addr = inter.getInetAddresses();
//
//                while(addr.hasMoreElements()) {
//                    InetAddress address = (InetAddress)addr.nextElement();
//                    myIp = address.getHostAddress().replace("/", "");
//                    if (myIp.length() <= 15) {
//                        System.out.println("\u001b[34m  LocalIp: " + myIp);
//                    }
//                }
//            }
//        } catch (SocketException var4) {
//            var4.printStackTrace();
//        }
//    }

}
