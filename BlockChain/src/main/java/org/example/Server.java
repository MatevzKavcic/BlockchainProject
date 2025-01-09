package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import util.LogLevel;
import util.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


public class Server extends Thread{
    int portNumber;
    private final BlockingQueue<String> messageQueue;
    private ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers;

    private UTXOPool UTXOPool;
    private PublicKey publicKey;

    private PrivateKey privateKey;

    private Blockchain blockchain;
    public Server(int portNumber, BlockingQueue<String> messageQueue, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers, PublicKey publicKey, PrivateKey privateKey, Blockchain blockchain, UTXOPool UTXOPool) {
        this.portNumber = portNumber;
        this.messageQueue = messageQueue;
        this.connectedPeers = connectedPeers;
        this.publicKey =publicKey;
        this.privateKey = privateKey;
        this.blockchain = blockchain;
        this.UTXOPool = UTXOPool;
        this.setName("Server Thread");
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (true) {
                Socket clientSocket = serverSocket.accept();

                // handshakeProtocol Initiated by server.
                handShakeProtocol(clientSocket);

                // now that you have shook hands with the other peer
                //send information that a new peer joined the network and send then the port of that peer



            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // this method takes in the socket that was currently created.
    // what it does is a bit more complicated. It initiates the handshake protocol  and exchanges the public keys with the client and then creates two threads.
    //one thread will only listen to the socket and put messages that it recieves to a message queue
    // one thrad will be created for messaging. it will have a method send that will send a something to that socket output. and i will have an array of those sockets that will handle the sockets? i guess ?
    private synchronized void handShakeProtocol(Socket clientSocket) throws Exception {
        try{
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            Message handshakeMessage = new Message(MessageType.HANDSHAKE, "", publicKeyToString(publicKey));
            Gson gson = new Gson();
            String jsonMessage = gson.toJson(handshakeMessage);

            Logger.log("Someon connected to me. Sending HANSHAKE message to client: ",LogLevel.Info);
            out.println(jsonMessage); // Send handshake to client

            // Read the response handshake message... public key and in the body there is a port number of the server port.
            String jsonResponseMessage = in.readLine(); // Wait for client's response
            Logger.log("Received response handshake message: " + jsonResponseMessage,LogLevel.Success);

            // Deserialize the response and process the client's public key
            // and the body of the message is the portnumber of the server
            Message responseMessage = gson.fromJson(jsonResponseMessage, Message.class);
            PublicKey clientPublicKey = stringToPublicKey(responseMessage.getPublicKey());

            int peersPortNum = Integer.parseInt(responseMessage.getBody()); // port serverja od peera ki se je povezal

            ListenToMeThread listenThread = new ListenToMeThread(clientSocket, in, messageQueue,stringToPublicKey(responseMessage.getPublicKey()),connectedPeers);
            new Thread(listenThread).start(); // Run the listening thread

            WriteMeThread writeMeThread = new WriteMeThread(out);
            new Thread(writeMeThread).start(); // Run the listening thread

            PeerInfo peerInfo = new PeerInfo(clientSocket,writeMeThread,peersPortNum);

            // ko pride na server moras vedt al se je prvo povezu nate ali ze na drugega.
            if (responseMessage.getHeader()==MessageType.PEERLISTRETURN){
                connectedPeers.put(clientPublicKey, peerInfo); // save it in your storage
            }
            else {
                sendListToPeer(gson,writeMeThread);
                connectedPeers.put(clientPublicKey, peerInfo);
                notifyUpdates();
            }

            Logger.log("i have " + connectedPeers.size() + "peers connected to me. " , LogLevel.Status);


        } catch (
                JsonSyntaxException e) {
            Logger.log("Failed to parse JSON message: " + e.getMessage(), LogLevel.Error);
            Logger.log("Malformed JSON: " , LogLevel.Error);
            // Optionally, send a request to the sender to resend the message
        }
        /*for (PublicKey publicKey1 : connectedPeers.keySet()) {
            PeerInfo pInfo = connectedPeers.get(publicKey1);
            Logger.log("Server Port: " + pInfo.getServerPort() + "and their public key is " + publicKey1 , LogLevel.Success);
        }
         */

    }


    // metoda ki poslje array portov na katere se mora peer povezat. to naredi kinda se mi zdi
    private void sendListToPeer(Gson gson,WriteMeThread writeMeThread) {

        if (!connectedPeers.isEmpty()) {
            List<Integer> serverPorts = new ArrayList<>();
            for (PeerInfo peerInformation : connectedPeers.values()) {
                serverPorts.add(peerInformation.getServerPort());
            }
            String serverPortsString = gson.toJson(serverPorts);
            Message peerListMessage = new Message(MessageType.PEERLIST, serverPortsString, publicKeyToString(publicKey));
            writeMeThread.sendMessage(gson.toJson(peerListMessage));
        }
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
    public synchronized void notifyUpdates() {
        synchronized (SharedResources.LOCK) {
            SharedResources.LOCK.notifyAll(); // Notify all waiting threads
            Logger.log("Updating threads");
        }

    }


}
