package org.example;

import util.LogLevel;
import util.Logger;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Blockchain {
    private static Blockchain instance;  // Static instance of the singleton
    private List<Block> chain; // the whole blockchain
    private UTXOPool utxoPool;



    private int miningDifficulty;

    // Private constructor to prevent external instantiation
    private Blockchain() {
        chain = new ArrayList<>();
        this.utxoPool = UTXOPool.getInstance(); // Assume UTXOPool is a singleton as well
        miningDifficulty= 6;
    }

    // Public method to get the single instance of Blockchain
    public static synchronized Blockchain getInstance() {
        if (instance==null){
            instance = new Blockchain();
        }
        return instance;
    }

    // Method to update or initialize the blockchain
    public static synchronized void setInstance(Blockchain newInstance) {
        if (instance == null) {
            instance = newInstance;
            synchronized (SharedResources.LOCK) {
                SharedResources.LOCK.notifyAll(); // Notify all waiting threads
            }
        } else {
            Logger.log("blockchain is Alredy Set", LogLevel.Status);
            return;
        }
    }
    public int getMiningDifficulty() {
        return miningDifficulty;
    }

    private Block createGenesisBlock(PublicKey publicKey) {
        List<Transaction> genesisTransactions = new ArrayList<>();

        List<TransactionOutput> outputs = new ArrayList<>();
        List<TransactionInput> inputs;

        TransactionOutput genesisOutput = new TransactionOutput(publicKeyToString(publicKey),100,"GENESIS_TRANSACTION");
        outputs.add(genesisOutput);

        Transaction genesisTransaction = new Transaction(
                "GENESIS", // Sender is a placeholder
                publicKeyToString(publicKey),
                100,
                new ArrayList<>(), // No inputs for the genesis transaction
                outputs,
                "GENESIS TRANSACTION"
        );

        utxoPool.addUTXO(genesisOutput);

        genesisTransactions.add(genesisTransaction);
        Block genesisBlock = new Block(
                0, // Index
                System.currentTimeMillis(), // Current timestamp
                genesisTransactions, // Transactions
                "0", // Previous hash
                0, // Nonce (initial value)
                null // Signature (initially null)
        );

        return genesisBlock;
    }

    public UTXOPool getUTXOPool() {
        return utxoPool;
    }

    public void setUTXOPool(UTXOPool UTXOPool) {
        this.utxoPool = UTXOPool;
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public void addBlock(Block newBlock) {
        chain.add(newBlock);
    }

    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            // Check hash validity
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                return false;
            }
            // Check previous hash linkage
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                return false;
            }
        }
        return true;
    }

    // Getter for the chain
    public List<Block> getChain() {
        return chain;
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


    public void addGenesisBlock(PublicKey publicKey) {

        chain.add(createGenesisBlock(publicKey));


    }
}
