package org.example;


import java.security.*;
import java.util.Base64;

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

    public String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
