package org.example;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Blockchain {
    private List<Block> chain; // the whole blockchain

    private UTXOPool UTXOPool ;

    public Blockchain(UTXOPool UTXOPool) {
        chain = new ArrayList<>();
        this.UTXOPool = UTXOPool;
        // Add the genesis block
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

        UTXOPool.addUTXO(genesisOutput);

        genesisTransactions.add(genesisTransaction);

        return new Block(0, System.currentTimeMillis(), genesisTransactions, "0");
    }

    public UTXOPool getUTXOPool() {
        return UTXOPool;
    }

    public void setUTXOPool(UTXOPool UTXOPool) {
        this.UTXOPool = UTXOPool;
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public void addBlock(Block newBlock) {
        newBlock.mineBlock(4); // Adjust difficulty as needed
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
