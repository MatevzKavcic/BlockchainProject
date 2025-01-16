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
import java.util.Map;
import java.util.UUID;
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

    private synchronized void handleHandshakeFromServer(Socket socket) throws Exception {


        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        //System.out.println("Connected to server: " + hostName + ":" + connectToPort);

        // read the first message from the server. it sohuld be the handshakeMessage

        String jsonMessage = in.readLine(); // Read the JSON message

        Gson gson = new Gson();
        // Message handshakeMessage = gson.fromJson(jsonMessage, Message.class); ////
        // PublicKey serverPublicKey = stringToPublicKey(handshakeMessage.getPublicKey());

        try {
            Message handshakeMessage = gson.fromJson(jsonMessage, Message.class);
            PublicKey serverPublicKey = stringToPublicKey(handshakeMessage.getPublicKey());
            // Proceed with the handshake logic

            if (handshakeMessage.getHeader()==MessageType.TRANSACTION||handshakeMessage.getHeader()==MessageType.BLOCK){
                messageQueue.put(jsonMessage);
                return;
            }

            //Logger.log("Received handshake message from server  " + handshakeMessage.getHeader() + " || body ->" + handshakeMessage.getBody() + "\n || public key -> " + handshakeMessage.getPublicKey());


            //send a new message to the server to let him know your public key and your in the body of the message(port number);

            Message responseMessage = new Message(MessageType.HANDSHAKEKEYRETURN, ""+portNumber, publicKeyToString(publicKey));
            String jsonResponse = gson.toJson(responseMessage);
            //Logger.log("Sending response handshake to server: " , LogLevel.Status);

            out.println(jsonResponse);

            ListenToMeThread listenThread = new ListenToMeThread(socket, in, messageQueue,serverPublicKey,connectedPeers);
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

            Logger.log("i have " + connectedPeers.size() + " peers connected to me. " , LogLevel.Status);
            logAllPeerBalances();
       /* for (PublicKey publicKey1 : connectedPeers.keySet()) {
            PeerInfo pInfo = connectedPeers.get(publicKey1);
            Logger.log("Server Port: " + pInfo.getServerPort() + "and their public key is " + publicKey1 , LogLevel.Success);
        }

        */

        } catch (Exception e) {
            Logger.log("Failed to parse JSON message: " + e.getMessage(), LogLevel.Error);
            Logger.log("failed inside CLIENT");
            Logger.log("sending my status to all ", LogLevel.Error);

            for (Map.Entry<PublicKey, PeerInfo> entry : connectedPeers.entrySet()) {
                PublicKey publicKeyOf = entry.getKey();
                PeerInfo peerInfo = entry.getValue();

                WriteMeThread thread = (WriteMeThread) peerInfo.getThread();
                String pkString = publicKeyToString(publicKey);
                int blockchainLenght = Blockchain.getInstance().getChain().size();
                String blockchainLenghtString = Integer.toString(blockchainLenght);
                Message m = new Message(MessageType.BLOCKERROR,blockchainLenghtString, pkString);
                String mString = gson.toJson(m);
                thread.sendMessage( mString);
                Logger.log("Sent a message :"+ m.getHeader() + " --- " +m.getBody()+" --- to -->" + generateNameFromPublicKey(publicKeyToString(publicKeyOf)));
            }


        }

    }

    // Method to handle messages from the server

    private synchronized void handleHandshakeFromServerIfSpecial(Socket socket) throws Exception {


        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        //System.out.println("Connected to server: " + hostName + ":" + connectToPort);

        // read the first message from the server. it sohuld be the handshakeMessage

        String jsonMessage = in.readLine(); // Read the JSON message... blocking method in caka na response.
        Gson gson = new Gson();

        try {
            Message handshakeMessage = gson.fromJson(jsonMessage, Message.class);
            PublicKey serverPublicKey = stringToPublicKey(handshakeMessage.getPublicKey());
            // Proceed with the handshake logic


            Logger.log("Received handshake message from server  " + handshakeMessage.getHeader() + " || body ->" + handshakeMessage.getBody() + "\n || public key -> " + handshakeMessage.getPublicKey());


            //send a new message to the server to let him know your public key and your in the body of the message(port number);

            Message responseMessage = new Message(MessageType.PEERLISTRETURN, ""+portNumber, publicKeyToString(publicKey));
            String jsonResponse = gson.toJson(responseMessage);
            Logger.log("Sending PEERLISTRETURN to server of a new Client so he knows about me and my pulic key.: " , LogLevel.Status);

            out.println(jsonResponse);


            ListenToMeThread listenThread = new ListenToMeThread(socket, in, messageQueue,serverPublicKey,connectedPeers);

            new Thread(listenThread).start(); // Run the listening thread

            WriteMeThread writeMeThread = new WriteMeThread(out);
            new Thread(writeMeThread).start(); // Run the listening thread

            PeerInfo peerInfo = new PeerInfo(socket,writeMeThread,connectToPort);

            // Store the client's information in connectedPeers
            //it stores the publicKey and the peers socket and the writemeThread;
            connectedPeers.put(serverPublicKey, peerInfo);


            Logger.log("i have " + connectedPeers.size() + "peers connected to me. " , LogLevel.Status);
            logAllPeerBalances();
        /*
        for (PublicKey publicKey1 : connectedPeers.keySet()) {
            PeerInfo pInfo = connectedPeers.get(publicKey1);
            Logger.log("Server Port: " + pInfo.getServerPort() + "and their public key is " + publicKey1 , LogLevel.Success);
        }
         */



        } catch (Exception e) {
            Logger.log("Failed to parse JSON message: " + e.getMessage(), LogLevel.Error);
            Logger.log("failed inside CLIENT");
            Logger.log("sending my status to all ", LogLevel.Error);

            for (Map.Entry<PublicKey, PeerInfo> entry : connectedPeers.entrySet()) {
                PublicKey publicKeyOf = entry.getKey();
                PeerInfo peerInfo = entry.getValue();

                WriteMeThread thread = (WriteMeThread) peerInfo.getThread();
                String pkString = publicKeyToString(publicKey);
                int blockchainLenght = Blockchain.getInstance().getChain().size();
                String blockchainLenghtString = Integer.toString(blockchainLenght);
                Message m = new Message(MessageType.BLOCKERROR,blockchainLenghtString, pkString);
                String mString = gson.toJson(m);
                thread.sendMessage( mString);
                Logger.log("Sent a message :"+ m.getHeader() + " --- " +m.getBody()+" --- to -->" + generateNameFromPublicKey(publicKeyToString(publicKeyOf)));
            }


        }



        // Message handshakeMessage = gson.fromJson(jsonMessage, Message.class);
        // PublicKey serverPublicKey = stringToPublicKey(handshakeMessage.getPublicKey());

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
    public void logAllPeerBalances() {
        UTXOPool utxoPool = UTXOPool.getInstance();
        StringBuilder balanceReport = new StringBuilder();

        balanceReport.append(String.format("\n%-30s | %-10s\n", "Peer", "Balance"));
        balanceReport.append("-".repeat(42)).append("\n");

        // Add your own balance
        String myPublicKeyString = publicKeyToString(publicKey); // Assume `publicKey` is your node's public key
        String myName = generateNameFromPublicKey(myPublicKeyString);
        int myBalance = utxoPool.getMyTotalFunds(myPublicKeyString);

        balanceReport.append(String.format("%-30s | %-10d\n", myName + " (You)", myBalance));

        // Add connected peers' balances
        for (PublicKey peer : connectedPeers.keySet()) {
            String peerName = generateNameFromPublicKey(publicKeyToString(peer));
            String peerPublicKey = publicKeyToString(peer);
            int peerBalance = utxoPool.getMyTotalFunds(peerPublicKey);

            balanceReport.append(String.format("%-30s | %-10d\n", peerName, peerBalance));
        }

        Logger.log(balanceReport.toString(), LogLevel.Info);
    }


    public static String generateNameFromPublicKey(String publicKey) {
        // Generate a UUID based on the public key hash
        UUID uuid = UUID.nameUUIDFromBytes(publicKey.getBytes());
        return uuid.toString().split("-")[0]; // Use the first part for brevity
    }

}
