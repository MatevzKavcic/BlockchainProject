package org.example;
import com.google.gson.Gson;
import com.sun.source.tree.BlockTree;
import util.LogLevel;
import util.Logger;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MinerThread extends Thread {
    private PublicKey publicKey; // The public key of this miner
    private UTXOPool utxoPool; // Local UTXO pool for this miner
    private ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers; // Peers in the network
    private Random random; // Random generator for testing
    public Gson gson = new Gson();

    private TransactionPool transactionPool;

    private Blockchain blockchain;
    private PrivateKey privateKey;
    public MinerThread(PublicKey publicKey, PrivateKey privateKey, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers) {
        this.publicKey = publicKey;
        this.utxoPool = UTXOPool.getInstance();
        this.connectedPeers = connectedPeers;
        this.transactionPool = TransactionPool.getInstance();
        this.random = new Random();
        this.setName("Miner");
        blockchain=Blockchain.getInstance();
        this.privateKey= privateKey;
    }

    @Override
    public void run() {
        while (true) {
            if(Blockchain.getInstance()==null){
                System.out.print("! ");
            }
            else {
                startMining();
            }
        }



    }

    public void startMining() {
        // 1. Get the transactions that need to be added to the block
        transactionPool= TransactionPool.getInstance();
        blockchain = Blockchain.getInstance();

        List<Transaction> selectedTransactions = transactionPool.getTransactionsForBlock(10);

        // 2. Create a new block and add the transactions to it
        Block newBlock = createNewBlock(selectedTransactions);

        // 3. Mine the block (finding the correct nonce)
        newBlock.mineBlock(blockchain.getMiningDifficulty());

        //podpiši block
        try {
            newBlock.signBlock(privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //send the block over the network.

        brodcastNewBlock(newBlock);

        // 4. Once the block is mined, add it to the blockchain
        blockchain.addBlock(newBlock);

        // 5. Clear the transaction pool or mark the transactions as mined
        transactionPool.removeTransactions(selectedTransactions);

        Logger.log("MINED A BLOCK!",LogLevel.Warn);


    }

    private void brodcastNewBlock(Block newBlock){

        String blockString = gson.toJson(newBlock);

        Message message = new Message(MessageType.BLOCK,blockString,publicKeyToString(publicKey));
        String messageString = gson.toJson(message);

        //send the transaction to everyone.
        for (PeerInfo peer : connectedPeers.values()) {
            WriteMeThread thread =(WriteMeThread) peer.getThread();
            thread.sendMessage(messageString);
        }


    }

    private Block createNewBlock(List<Transaction> transactions) {
        Blockchain blChain = Blockchain.getInstance();
        String previousHash = blChain.getLatestBlock().getHash();

        return new Block(
                blChain.getChain().size()+1,  // Block index
                System.currentTimeMillis(),  // Timestamp
                transactions,  // Transactions to include in the block
                previousHash,  // Hash of the previous block
                0,  // Initial nonce (for mining)
                null  // Signature (can be set later)
        );
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
