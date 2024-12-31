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
    private List<List<Block>> forks; // Competing forks
    private UTXOPool utxoPool;



    private int miningDifficulty;

    // Private constructor to prevent external instantiation
    private Blockchain() {
        chain = new ArrayList<>();
        forks = new ArrayList<>();
        this.utxoPool = UTXOPool.getInstance(); // Assume UTXOPool is a singleton as well
        miningDifficulty= 4;
    }

    // Public method to get the single instance of Blockchain

    //!!!!! IMPORTANT this is diffrent from the SERVER NODE !
    public static synchronized Blockchain getInstance() {
        if (instance==null){
            return null;
        }
        return instance;
    }

    // Method to update or initialize the blockchain
    public static synchronized void setInstance(Blockchain newInstance) {
        if (instance == null) {
            Logger.log("seting instance of the blockchain");
            instance = newInstance;
            synchronized (SharedResources.LOCK) {
                SharedResources.LOCK.notifyAll(); // Notify all waiting threads
            }
        } else {
            Logger.log("blockchain is Alredy Set", LogLevel.Status);
            return;
        }
    }

    // Add block to the main chain or create a fork
    public synchronized void handleAddBlockForksAndSpoons(Block newBlock) {
        int newBlockIndex = newBlock.getIndex();
        int mainChainLength = chain.size();
        TransactionPool transactionPool =  TransactionPool.getInstance();

        if (newBlockIndex == mainChainLength + 1 &&
                newBlock.getPreviousHash().equals(chain.get(mainChainLength - 1).getHash()))  {
            // New block extends the main chain
            addBlock(newBlock);

            //delete the transactions from the poool.
            transactionPool.removeTransactions(newBlock.getTransactions()); // removni transakcije z bloka

            Logger.log("New block added to main chain.", LogLevel.Success);
        } else if (newBlockIndex == mainChainLength) {
            // Fork detected
            createFork(newBlock);
            Logger.log("Fork detected and saved.", LogLevel.Debug);
            resolveForks();
        } else if (isExtensionOfFork(newBlock)) {
            // New block extends one of the forks
            extendFork(newBlock);
            resolveForks(); // Check if this fork is now longer
        }
         else {
            // Block is outdated or invalid
            Logger.log("Outdated block received, ignoring.", LogLevel.Warn);
        }
    }

    private boolean isExtensionOfFork(Block newBlock) {
        for (List<Block> fork : forks) {
            Block lastBlock = fork.get(fork.size() - 1);
            if (newBlock.getPreviousHash().equals(lastBlock.getHash())) {
                return true;
            }
        }
        return false;
    }

    private void extendFork(Block newBlock) {
        for (List<Block> fork : forks) {
            Block lastBlock = fork.get(fork.size() - 1);
            if (newBlock.getPreviousHash().equals(lastBlock.getHash())) {
                fork.add(newBlock);
                Logger.log("Fork extended with new block.", LogLevel.Debug);
                return;
            }
        }
    }


    private void createFork(Block newBlock) {
        List<Block> newFork = new ArrayList<>(chain.subList(0, chain.size() - 1));
        newFork.add(newBlock);
        forks.add(newFork);
        Logger.log("Fork created due to block conflict.", LogLevel.Warn);
    }

    public synchronized void resolveForks() {
        for (List<Block> fork : forks) {
            if (fork.size() > chain.size()) {
                // Determine the fork point
                int forkPoint = findForkPoint(chain, fork);

                // Restore transactions from blocks after the fork point
                restoreTransactionsFromFork(chain, forkPoint);

                // Switch to the longer fork
                chain = fork;
                forks.remove(fork);

                Logger.log("Switched to the longer fork.", LogLevel.Warn);
                break;
            }
        }
    }

    // Helper method to find the index of the fork point (common ancestor)
    private int findForkPoint(List<Block> mainChain, List<Block> fork) {
        int forkPoint = 0;
        for (int i = 0; i < Math.min(mainChain.size(), fork.size()); i++) {
            if (mainChain.get(i).equals(fork.get(i))) {
                forkPoint = i;
            } else {
                break;
            }
        }
        return forkPoint;
    }

    // Restore transactions from discarded blocks in the old chain
    private void restoreTransactionsFromFork(List<Block> oldChain, int forkPoint) {
        TransactionPool transactionPool = TransactionPool.getInstance();

        for (int i = forkPoint + 1; i < oldChain.size(); i++) {
            Block block = oldChain.get(i);
            for (Transaction tx : block.getTransactions()) {
                transactionPool.addTransaction(tx); // Return transactions to the pool
            }
        }

        Logger.log("Transactions restored from discarded blocks.", LogLevel.Debug);
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

    public synchronized void addBlock(Block newBlock) {
        chain.add(newBlock);
        Logger.log("ADDED A BLOCK !on index-> "+ newBlock.getIndex(),LogLevel.Success);
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
