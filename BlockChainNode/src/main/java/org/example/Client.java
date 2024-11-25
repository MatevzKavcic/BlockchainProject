package org.example;

import com.google.gson.Gson;
import util.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Client extends Thread{

    String hostName;
    int portNumber;
    private final BlockingQueue<String> messageQueue;
    private ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers;
    private PublicKey publicKey;

    private PrivateKey privateKey;

    private boolean isNew= false;



    public Client(String hostName, int portNumber, BlockingQueue<String> messageQueue, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers, PublicKey publicKey, PrivateKey privateKey) {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.messageQueue = messageQueue;
        this.connectedPeers = connectedPeers;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public Client(String hostName, int portNumber, BlockingQueue<String> messageQueue, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers, PublicKey publicKey, PrivateKey privateKey, boolean isNew) {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.messageQueue = messageQueue;
        this.connectedPeers = connectedPeers;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.isNew = isNew;
    }

    @Override
    public void run() {
        try {
            Socket socket;

            if (isNew){
                 socket = new Socket(hostName, portNumber);
            }
            else {
                 socket = new Socket(hostName, 6000);
            }

            //thiss method handles everything... it connects the two peers, makes the handshake, gets the information from the other peer
            // it creates two threads, one that only listens to the socket output and rads it and one that will handle writting to the other sockets. description is above the method


            handleHandshakeFromServer(socket);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // this method takes in the socket that was currently created.
    // what it does is a bit more complicated. It initiates the handshake protocol  and exchanges the public keys with the client and then creates two threads.
    //one thread will only listen to the socket and put messages that it recieves to a message queue
    // one thrad will be created for messaging. it will have a method send that will send a something to that socket output. and i will have an array of those sockets that will handle the sockets? i guess ?

    private void handleHandshakeFromServer(Socket socket) throws Exception {


        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        System.out.println("Connected to server: " + hostName + ":" + portNumber);

        // read the first message from the server. it sohuld be the handshakeMessage

        String jsonMessage = in.readLine(); // Read the JSON message
        System.out.println("Received handshake message: " + jsonMessage);


        Gson gson = new Gson();
        Message handshakeMessage = gson.fromJson(jsonMessage, Message.class);
        PublicKey serverPublicKey = stringToPublicKey(handshakeMessage.getPublicKey());
        System.out.println("Server's public key: " + serverPublicKey);


        //send a new message to the server to let him know your public key and your port number;
        if (isNew){
            Message responseMessage = new Message(MessageType.HANDSHAKEKEYRETURNKNOWN, ""+portNumber, publicKeyToString(publicKey));
            String jsonResponse = gson.toJson(responseMessage);
            System.out.println("Sending response handshake to server: " + jsonResponse);
            out.println(jsonResponse);


        }else {
            Message responseMessage = new Message(MessageType.HANDSHAKEKEYRETURN, ""+portNumber, publicKeyToString(publicKey));
            String jsonResponse = gson.toJson(responseMessage);
            System.out.println("Sending response handshake to server: " + jsonResponse);
            out.println(jsonResponse);

        }



        ListenToMeThread listenThread = new ListenToMeThread(socket, in, messageQueue);
        new Thread(listenThread).start(); // Run the listening thread

        WriteMeThread writeMeThread = new WriteMeThread(out);
        new Thread(writeMeThread).start(); // Run the listening thread

        // ta port number je lahko zavajujoč in narobe... tle moras dat od serverja number na katerega si se povezal.

        PeerInfo peerInfo;
        int i = 0;
       if (i == 0) {
            peerInfo = new PeerInfo(socket,writeMeThread,6000);
       }
       else {
            peerInfo = new PeerInfo(socket,writeMeThread,portNumber);

       }





        // Store the client's information in connectedPeers
        //it stores the publicKey and the peers socket and the writemeThread;
        connectedPeers.put(serverPublicKey, peerInfo);

        System.out.println("This is my public key: " + publicKey);


        Logger.log("new connection :  ");
        Logger.log("----> (kao sem se povezes) Local IP :  " +socket.getLocalAddress());
        Logger.log("----> (my port where i'm open) Local PORT :  " + socket.getLocalPort());
        Logger.log("----> IP :  " + socket.getInetAddress());
        Logger.log("----> (odprt port ku poslusa) PORT :  " + socket.getPort());
        Logger.log("----------------------------");


    }

    // Method to handle messages from the server

    public PublicKey stringToPublicKey(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
