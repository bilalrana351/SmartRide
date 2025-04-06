package util;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String SALT = "unique-salt"; // Use a secure method to generate a salt in production

    /**
     * Hashes a plain password using PBKDF2WithHmacSHA256.
     *
     * @param plainPassword The plain password to hash.
     * @return The hashed password as a Base64-encoded string.
     */
    public static String hashPassword(String plainPassword) {
        try {
            PBEKeySpec spec = new PBEKeySpec(plainPassword.toCharArray(), SALT.getBytes(), ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error while hashing password", e);
        }
    }

    /**
     * Verifies a plain password against a hashed password.
     *
     * @param plainPassword The plain password to verify.
     * @param hashedPassword The hashed password to compare against.
     * @return True if the passwords match, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        String hashedPlainPassword = hashPassword(plainPassword);
        return hashedPlainPassword.equals(hashedPassword);
    }
}
