package org.example;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


public class Server extends Thread{
    int portNumber;
    private final BlockingQueue<String> messageQueue;
    private ConcurrentHashMap<PublicKey, Socket> connectedPeers;

    private PublicKey publicKey;

    private PrivateKey privateKey;

    public Server(int portNumber, BlockingQueue<String> messageQueue, ConcurrentHashMap<PublicKey, Socket> connectedPeers, PublicKey publicKey, PrivateKey privateKey) {
        this.portNumber = portNumber;
        this.messageQueue = messageQueue;
        this.connectedPeers = connectedPeers;
        this.publicKey =publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (true) {
                Socket clientSocket = serverSocket.accept();

                // handshakeProtocol Initiated by server.
                handShakeProtocol(clientSocket);

                // Send handshake message


                // Start a thread to handle further communication with this client
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handShakeProtocol(Socket clientSocket) throws Exception {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        Message handshakeMessage = new Message(MessageType.HANDSHAKE, "", publicKeyToString(publicKey));
        Gson gson = new Gson();
        String jsonMessage = gson.toJson(handshakeMessage);

        System.out.println("Sending handshake message to client: " + jsonMessage);
        out.println(jsonMessage); // Send handshake to client

        // Read the response handshake message
        String jsonResponseMessage = in.readLine(); // Wait for client's response
        System.out.println("Received response handshake message: " + jsonResponseMessage);

        // Deserialize the response and process the client's public key
        Message responseMessage = gson.fromJson(jsonResponseMessage, Message.class);
        PublicKey clientPublicKey = stringToPublicKey(responseMessage.getPublicKey());
        System.out.println("Client's public key: " + clientPublicKey);

        // Store the client's information in connectedPeers
        connectedPeers.put(clientPublicKey, clientSocket);
    }


    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Server recived message : " + message + " from socket "+ clientSocket.getPort());
                messageQueue.put(message); // Add message to queue
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    public PublicKey stringToPublicKey(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }


}
