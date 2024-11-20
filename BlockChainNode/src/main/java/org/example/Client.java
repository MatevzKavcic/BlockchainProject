package org.example;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
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
import java.util.logging.Handler;

public class Client extends Thread{

    String hostName;
    int portNumber;
    private final BlockingQueue<String> messageQueue;
    private ConcurrentHashMap<PublicKey, Socket> connectedPeers;
    private PublicKey publicKey;

    private PrivateKey privateKey;



    public Client(String hostName, int portNumber, BlockingQueue<String> messageQueue, ConcurrentHashMap<PublicKey, Socket> connectedPeers, PublicKey publicKey, PrivateKey privateKey) {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.messageQueue = messageQueue;
        this.connectedPeers = connectedPeers;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(hostName, 6000);

            handleHandshakeFromServer(socket);


            // Spawn a thread to handle incoming messages from the server
            // It only listens to the server messages and puts them in a queue
            new Thread(() -> handleServerMessages(socket)).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

        //you succesfuly got the server key. save in the connected peers sockets
        connectedPeers.put(serverPublicKey, socket);

        //send a new message to the server to let him know your public key;

        Message responseMessage = new Message(MessageType.HANDSHAKEKEYRETURN, "", publicKeyToString(publicKey));
        String jsonResponse = gson.toJson(responseMessage);
        System.out.println("Sending response handshake to server: " + jsonResponse);

        out.println(jsonResponse);



        System.out.println("This is my public key: " + publicKey);


    }

    // Method to handle messages from the server
    private void handleServerMessages(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Server says: " + message);
                messageQueue.put(message); // Add server message to queue
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
