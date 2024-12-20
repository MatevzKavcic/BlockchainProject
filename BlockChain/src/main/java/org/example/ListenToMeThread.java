package org.example;

import util.LogLevel;
import util.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ListenToMeThread implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private final BlockingQueue<String> messageQueue;
    private final PublicKey peerPublicKey;
    private ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers; // Peers in the network




    public ListenToMeThread(Socket socket, BufferedReader in, BlockingQueue<String> messageQueue, PublicKey peerPublicKey,ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers) {
        this.socket = socket;
        this.in = in;
        this.messageQueue = messageQueue;
        this.peerPublicKey = peerPublicKey;
        this.connectedPeers=connectedPeers;
    }



    @Override
    public void run() {

        try {
            String message;
            while ((message = in.readLine()) != null) {
                messageQueue.put(message); // Add message to queue
            }
        } catch (IOException e) {
            Logger.log("Peer disconnected: " + peerPublicKey, LogLevel.Error);
            handleDisconnection(peerPublicKey);
        } catch (InterruptedException e) {
            Logger.log("Listening thread interrupted for peer: " + peerPublicKey, LogLevel.Debug);
        } finally {
            cleanup();
        }
    }


    private void cleanup() {
        try {
            socket.close();
        } catch (IOException e) {
            Logger.log("Error closing socket for peer: " + generateNameFromPublicKey(publicKeyToString(peerPublicKey)), LogLevel.Error);
        }
    }

    private void handleDisconnection(PublicKey peerPublicKey) {
        PeerInfo peerInfo = connectedPeers.remove(peerPublicKey);
        if (peerInfo != null) {
            // Interrupt the WriteMeThread
            peerInfo.getThread().interrupt();

            // Close the socket
            try {
                peerInfo.getSocket().close();
            } catch (IOException e) {
                Logger.log("Failed to close socket for peer: " + peerPublicKey, LogLevel.Error);
            }

            Logger.log("Disconnected peer removed: " + generateNameFromPublicKey(publicKeyToString(peerPublicKey)), LogLevel.Status);
        }
    }


    public static String generateNameFromPublicKey(String publicKey) {
        // Generate a UUID based on the public key hash
        UUID uuid = UUID.nameUUIDFromBytes(publicKey.getBytes());
        return uuid.toString().split("-")[0]; // Use the first part for brevity
    }

    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }


}
