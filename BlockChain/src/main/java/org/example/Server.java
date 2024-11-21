package org.example;

import com.google.gson.Gson;

import java.io.BufferedReader;
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
    private ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers;


    private PublicKey publicKey;

    private PrivateKey privateKey;

    public Server(int portNumber, BlockingQueue<String> messageQueue, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers, PublicKey publicKey, PrivateKey privateKey) {
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

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // this method takes in the socket that was currently created.
    // what it does is a bit more complicated. It initiates the handshake protocol  and exchanges the public keys with the client and then creates two threads.
    //one thread will only listen to the socket and put messages that it recieves to a message queue
    // one thrad will be created for messaging. it will have a method send that will send a something to that socket output. and i will have an array of those sockets that will handle the sockets? i guess ?
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


        ListenToMeThred listenThread = new ListenToMeThred(clientSocket, in, messageQueue);
        new Thread(listenThread).start(); // Run the listening thread

        WriteMeThread writeMeThread = new WriteMeThread(out);
        new Thread(writeMeThread).start(); // Run the listening thread

        PeerInfo peerInfo = new PeerInfo(clientSocket,writeMeThread);

        // Store the client's information in connectedPeers
        //it stores the publicKey and the peers socket and the writemeThread;
        connectedPeers.put(clientPublicKey, peerInfo);

    }

//peer -> server,client -> writeMeThread,ListenToMeThread (Messaging service bo rabu met sepravi )
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
