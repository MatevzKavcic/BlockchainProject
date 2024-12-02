package org.example;


import com.google.gson.Gson;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionManager extends Thread{


    private UTXOPool utxoPool ;

    private ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers;

    public Gson gson = new Gson();

    private PublicKey publicKey;



    public TransactionManager(UTXOPool utxoPool, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers, PublicKey publicKey) {
        this.utxoPool = utxoPool;
        this.connectedPeers = connectedPeers;
        this.publicKey = publicKey;
    }

    public void run(){


        // to bo class ki booo na zacetku requestou blockchain
        //oz bo requestau za thread pool in pol ko bo dobil kaksno transakcijo jo bo moral obbdelat in ja...

    }

    public void requestBlockchain(PublicKey publicKey) {

        //enega rendom peera dobi in poslji blockchainrequest.

        Message m = new Message(MessageType.BLOCKCHAINREQUEST,"",publicKeyToString(publicKey));
        String mString = gson.toJson(m);

        WriteMeThread thread = (WriteMeThread) connectedPeers.get(publicKey).getThread();

        thread.sendMessage( mString);

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

