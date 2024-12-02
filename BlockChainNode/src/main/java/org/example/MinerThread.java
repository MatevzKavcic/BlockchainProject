package org.example;
import com.google.gson.Gson;

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

    public MinerThread(PublicKey publicKey, UTXOPool utxoPool, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers) {
        this.publicKey = publicKey;
        this.utxoPool = utxoPool;
        this.connectedPeers = connectedPeers;
        this.random = new Random();
        this.setName("Miner");
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Sleep for 20 seconds
                Thread.sleep(20000);

                // Create a random transaction
                Transaction transaction = createRandomTransaction();
                if (transaction != null) {
                    broadcastTransaction(transaction);
                } else {
                    System.out.println("Insufficient funds or no peers to send transaction.");
                }
            } catch (InterruptedException e) {
                System.out.println("MinerThread interrupted: " + e.getMessage());
                break;
            }
        }
    }

    private Transaction createRandomTransaction() {
        // Get a random recipient from connected peers
        if (connectedPeers.isEmpty()) return null; // No peers to send to

        List<PublicKey> peerKeys = new ArrayList<>(connectedPeers.keySet());
        PublicKey recipientKey = peerKeys.get(random.nextInt(peerKeys.size()));
        String recipientPublicKey = recipientKey.toString();

        // Random amount to send
        double amount = random.nextDouble() * 10 + 1; // Between 1 and 10

        // Gather UTXOs to cover the amount
        List<TransactionInput> inputs = new ArrayList<>();
        List<TransactionOutput> outputs = new ArrayList<>();
        double total = 0;

        String senderPublicKey = publicKeyToString(publicKey); // moj public key.


        //loopni skozi use UTXO in najdi kolko fundou imas
        for (TransactionOutput output : utxoPool.getUTXOPool().values()) {
            // dobi vse UTXO ki so moji
            if (output.isMine(senderPublicKey)) {
                inputs.add(new TransactionInput(output.getId()));
                total += output.getAmount();
                utxoPool.removeUTXO(output.getId()); // Mark as spent
                if (total >= amount) break;
            }
        }

        if (total < amount) {
            System.out.println("Insufficient funds for transaction. Needed: " + amount + ", Available: " + total);
            return null;
        }

        // Create outputs
        // nov output ki je unique vedno
        outputs.add(new TransactionOutput(recipientPublicKey, amount, UUID.randomUUID().toString()));
        if (total > amount) {
            outputs.add(new TransactionOutput(senderPublicKey, total - amount, UUID.randomUUID().toString())); // Change
        }

        // Create and return the transaction
        return new Transaction(senderPublicKey, recipientPublicKey, amount, inputs, outputs);
    }

    private void broadcastTransaction(Transaction transaction) {

        String transactionString = gson.toJson(transaction);
        Message message = new Message(MessageType.TRANSACTION,transactionString,publicKeyToString(publicKey));
        String messageString = gson.toJson(message);

        for (PeerInfo peer : connectedPeers.values()) {
            WriteMeThread thread =(WriteMeThread) peer.getThread();
            thread.sendMessage(messageString);
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
