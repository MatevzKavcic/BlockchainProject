package org.example;


import com.google.gson.Gson;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionManager extends Thread{


    private UTXOPool utxoPool ;

    private ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers;

    public Gson gson = new Gson();

    private PublicKey publicKey;

    private Random random; // Random generator for testing
    private Blockchain blockchain;




    public TransactionManager(UTXOPool utxoPool, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers, PublicKey publicKey, Blockchain blockchain) {
        this.utxoPool = utxoPool;
        this.connectedPeers = connectedPeers;
        this.publicKey = publicKey;
        this.blockchain = blockchain;
        this.random = new Random();
        this.setName("transaction Mannager Thread");

    }

    public void run(){


        // ce nimas blockchaina pol prasaj soseda
        if (blockchain==null) {

            //TO IMPLEMENT:
            // dokler nimas usaj 2 sosedov nesmes sodelovat....
            // da ne vedno vprasas prvega na katerega se povezes. za zdej je uredi za debuging

            while (!connectedPeers.isEmpty()) {
                List<PublicKey> peerKeys = new ArrayList<>(connectedPeers.keySet());

                Random random = new Random();
                requestBlockchain(peerKeys.get(random.nextInt(peerKeys.size())));

            }
        }


        //requestThreadPool();







        // to bo class ki booo na zacetku requestou blockchain
        //oz bo requestau za thread pool in pol ko bo dobil kaksno transakcijo jo bo moral obbdelat in ja...

    }

   /* private void requestThreadPool() {

        Message m = new Message(MessageType.REQUESTTHREADPOOL,"",publicKeyToString(publicKey));
        String mString = gson.toJson(m);

        WriteMeThread thread = (WriteMeThread) connectedPeers.get(publicKey).getThread();

        thread.sendMessage( mString);


    }


    */
    public void requestBlockchain(PublicKey sendToPublicKey) {

        //enega rendom peera dobi in poslji blockchainrequest.

        Message m = new Message(MessageType.BLOCKCHAINREQUEST,"",publicKeyToString(publicKey));
        String mString = gson.toJson(m);

        WriteMeThread thread = (WriteMeThread) connectedPeers.get(sendToPublicKey).getThread();

        thread.sendMessage( mString);

    }

    //method ki bo poslau v network prosnjo da se updejta UTXO pool tako da bojo dodali se njega.
    public void updateUTXOPool(){


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

