package org.example;

import com.google.gson.Gson;
import util.LogLevel;
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

    private int connectToPort;

    boolean isSpecial = false;
    public TransactionManager transactionManager;



    public Client(String hostName, int portNumber, BlockingQueue<String> messageQueue, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers, PublicKey publicKey, PrivateKey privateKey, int connectToPort,TransactionManager transactionManager) {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.messageQueue = messageQueue;
        this.connectedPeers = connectedPeers;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.connectToPort = connectToPort;
        this.transactionManager = transactionManager;
        this.setName("Primary Client Thread");
    }
    public Client(String hostName, int portNumber, BlockingQueue<String> messageQueue, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers, PublicKey publicKey, PrivateKey privateKey, int connectToPort,boolean isSpecial) {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.messageQueue = messageQueue;
        this.connectedPeers = connectedPeers;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.connectToPort = connectToPort;
        this.isSpecial= isSpecial;
        this.setName("Secondary client Thread" + connectedPeers.size());
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(hostName, connectToPort);

            // this method takes in the socket that was currently created.
            // what it does is a bit more complicated. It initiates the handshake protocol  and exchanges the public keys with the client and then creates two threads.
            //one thread will only listen to the socket and put messages that it recieves to a message queue
            // one thrad will be created for messaging. it will have a method send that will send a something to that socket output. and i will have an array of those sockets that will handle the sockets? i guess ?

            if (isSpecial){
                handleHandshakeFromServerIfSpecial(socket);
            }

            handleHandshakeFromServer(socket);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleHandshakeFromServer(Socket socket) throws Exception {


        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        //System.out.println("Connected to server: " + hostName + ":" + connectToPort);

        // read the first message from the server. it sohuld be the handshakeMessage

        String jsonMessage = in.readLine(); // Read the JSON message
        
        Gson gson = new Gson();
        Message handshakeMessage = gson.fromJson(jsonMessage, Message.class);
        PublicKey serverPublicKey = stringToPublicKey(handshakeMessage.getPublicKey());

        if (handshakeMessage.getHeader()==MessageType.TRANSACTION){
            messageQueue.put(jsonMessage);
            return;
        }

        Logger.log("Received handshake message from server  " + handshakeMessage.getHeader() + " || body ->" + handshakeMessage.getBody() + "\n || public key -> " + handshakeMessage.getPublicKey());


        //send a new message to the server to let him know your public key and your in the body of the message(port number);

        Message responseMessage = new Message(MessageType.HANDSHAKEKEYRETURN, ""+portNumber, publicKeyToString(publicKey));
        String jsonResponse = gson.toJson(responseMessage);
        Logger.log("Sending response handshake to server: " , LogLevel.Status);

        out.println(jsonResponse);


        ListenToMeThred listenThread = new ListenToMeThred(socket, in, messageQueue);
        new Thread(listenThread).start(); // Run the listening thread

        WriteMeThread writeMeThread = new WriteMeThread(out);
        new Thread(writeMeThread).start(); // Run the listening thread

        PeerInfo peerInfo = new PeerInfo(socket,writeMeThread,connectToPort);

        // Store the client's information in connectedPeers
        //it stores the publicKey and the peers socket and the writemeThread;
        synchronized (SharedResources.LOCK) {
            connectedPeers.put(serverPublicKey, peerInfo);
            notifyUpdates();
        }
        //Logger.log("new connection :  ");
        //Logger.log("----> (kao sem se povezes) Local IP :  " +socket.getLocalAddress());
        //Logger.log("----> (my port where i'm open) Local PORT :  " + socket.getLocalPort());
        //Logger.log("----> IP :  " + socket.getInetAddress());
        //Logger.log("----> (odprt port ku poslusa) PORT :  " + socket.getPort());
        //Logger.log("----------------------------");

        Logger.log("i have " + connectedPeers.size() + " peers connected to me. those peers are on ports" , LogLevel.Status);

        for (PublicKey publicKey1 : connectedPeers.keySet()) {
            PeerInfo pInfo = connectedPeers.get(publicKey1);
            Logger.log("Server Port: " + pInfo.getServerPort() + "and their public key is " + publicKey1 , LogLevel.Success);
        }

        transactionManager.requestBlockchain(serverPublicKey);

    }

    // Method to handle messages from the server

    private void handleHandshakeFromServerIfSpecial(Socket socket) throws Exception {


        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        //System.out.println("Connected to server: " + hostName + ":" + connectToPort);

        // read the first message from the server. it sohuld be the handshakeMessage

        String jsonMessage = in.readLine(); // Read the JSON message... blocking method in caka na response.
        Gson gson = new Gson();
        Message handshakeMessage = gson.fromJson(jsonMessage, Message.class);
        PublicKey serverPublicKey = stringToPublicKey(handshakeMessage.getPublicKey());
        Logger.log("Received handshake message from server  " + handshakeMessage.getHeader() + " || body ->" + handshakeMessage.getBody() + "\n || public key -> " + handshakeMessage.getPublicKey());


        //send a new message to the server to let him know your public key and your in the body of the message(port number);

        Message responseMessage = new Message(MessageType.PEERLISTRETURN, ""+portNumber, publicKeyToString(publicKey));
        String jsonResponse = gson.toJson(responseMessage);
        Logger.log("Sending PEERLISTRETURN to server of a new Client so he knows about me and my pulic key.: " , LogLevel.Status);

        out.println(jsonResponse);


        ListenToMeThred listenThread = new ListenToMeThred(socket, in, messageQueue);
        new Thread(listenThread).start(); // Run the listening thread

        WriteMeThread writeMeThread = new WriteMeThread(out);
        new Thread(writeMeThread).start(); // Run the listening thread

        PeerInfo peerInfo = new PeerInfo(socket,writeMeThread,connectToPort);

        // Store the client's information in connectedPeers
        //it stores the publicKey and the peers socket and the writemeThread;
        connectedPeers.put(serverPublicKey, peerInfo);


        Logger.log("i have " + connectedPeers.size() + "peers connected to me. those peers are on ports" , LogLevel.Status);

        for (PublicKey publicKey1 : connectedPeers.keySet()) {
            PeerInfo pInfo = connectedPeers.get(publicKey1);
            Logger.log("Server Port: " + pInfo.getServerPort() + "and their public key is " + publicKey1 , LogLevel.Success);
        }

        //na koncu nesmes requestat za blockchain ker ga ze mas od prevega node ko se povezes.

    }



    //methods that help decode messages and keys and stuff
    public PublicKey stringToPublicKey(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    public synchronized void notifyUpdates() {
        synchronized (SharedResources.LOCK) {
            SharedResources.LOCK.notifyAll(); // Notify all waiting threads
            Logger.log("Updating threads");
        }
    }
}
