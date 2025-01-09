package org.example;

import com.google.gson.internal.bind.util.ISO8601Utils;
import util.LogLevel;
import util.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Enumeration;
import java.util.Random;

public class Main {

    Random random = new Random(6000);

    public static int portNumber =  6000 + (int)(Math.random() * 2001);

    public static String portNumberDockerString = System.getenv("NODE_PORT");

    public static int portNumberDocker = Integer.parseInt(portNumberDockerString);

    public static int portNumberOfFirstConnect= 6000;
    public static String hostName = "mainnode";
    public static boolean firstNode = false;


    // this is the primary node or the root node so it will act as a server!
    public static void main(String[] args) {
        Peer peer = new Peer(portNumberDocker,hostName,firstNode,portNumberOfFirstConnect);
        //Peer peer = new Peer(portNumber,hostName,firstNode,portNumberOfFirstConnect);
        peer.start();
        Logger.log(":"+portNumberDocker+":la", LogLevel.Success);

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
