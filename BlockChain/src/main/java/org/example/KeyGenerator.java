package org.example;


import java.security.*;

public class KeyGenerator {

    private PublicKey publicKey;
    private PrivateKey privateKey;

    public KeyGenerator() {
        try {
            // Initialize KeyPairGenerator for RSA
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // Key size: 2048 bits

            // Generate the key pair
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            this.publicKey = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing RSA algorithm", e);
        }
    }

    // Getters for keys
    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
