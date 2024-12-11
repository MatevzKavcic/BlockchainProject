package org.example;
import com.google.gson.Gson;
import util.LogLevel;
import util.Logger;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MinerThread extends Thread {
    private PublicKey publicKey; // The public key of this miner
    private UTXOPool utxoPool; // Local UTXO pool for this miner
    private ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers; // Peers in the network
    private Random random; // Random generator for testing
    public Gson gson = new Gson();

    private TransactionPool transactionPool;

    public MinerThread(PublicKey publicKey, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers, TransactionPool transactionPool) {
        this.publicKey = publicKey;
        this.utxoPool = UTXOPool.getInstance();
        this.connectedPeers = connectedPeers;
        this.transactionPool = transactionPool;
        this.random = new Random();
        this.setName("Miner");
    }

    @Override
    public void run() {

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
