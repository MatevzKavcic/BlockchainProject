
package org.example;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashingUtils {
    public static String applySHA256(String input) {
        try {
            // Get a SHA-256 digest instance
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Apply the hash computation
            byte[] hashBytes = digest.digest(input.getBytes("UTF-8"));

            // Convert the byte array into a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b); // Mask for byte conversion
                if (hex.length() == 1) hexString.append('0'); // Add leading zero if necessary
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Error while hashing with SHA-256", e);
        }
    }
}
