package org.example;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

public class Blockchain {
    private List<Block> chain; // the whole blockchain

    private UTXOPool UTXOPool ;


    public Blockchain(UTXOPool UTXOPool) {
        chain = new ArrayList<>();
        // Add the genesis block
        chain.add(createGenesisBlock());
        this.UTXOPool = UTXOPool;
    }

    private Block createGenesisBlock() {
        return new Block(0, System.currentTimeMillis(), new ArrayList<>(), "0");
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
}
