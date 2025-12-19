package online.backend.ghostshare.AESUtil;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AESUtil - Secure AES-256-GCM encryption/decryption with PBKDF2 key derivation.
 *
 * Each encryption uses:
 *  - Random 16-byte salt (for PBKDF2)
 *  - Random 12-byte IV (for GCM)
 *
 * Output format:  Base64(salt || iv || ciphertext)
 */
public class AESUtil {

    private static final int SALT_LEN = 16;        // bytes
    private static final int IV_LEN = 12;          // 96 bits
    private static final int KEY_LEN = 256;        // bits
    private static final int PBKDF2_ITER = 200_000; // increase for slower brute force
    private static final SecureRandom secureRandom = new SecureRandom();

    private AESUtil() {}

    private static byte[] deriveKey(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITER, KEY_LEN);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }

    /**
     * Encrypts plaintext with password.
     * @param plaintext text to encrypt
     * @param password  user password
     * @return Base64(salt || iv || ciphertext)
     */
    public static String encrypt(String plaintext, String password) throws Exception {
        byte[] salt = new byte[SALT_LEN];
        secureRandom.nextBytes(salt);

        byte[] keyBytes = deriveKey(password.toCharArray(), salt);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        byte[] iv = new byte[IV_LEN];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        ByteBuffer bb = ByteBuffer.allocate(salt.length + iv.length + ciphertext.length);
        bb.put(salt);
        bb.put(iv);
        bb.put(ciphertext);
        return Base64.getEncoder().encodeToString(bb.array());
    }

    /**
     * Decrypts Base64(salt || iv || ciphertext) using password.
     */
    public static String decrypt(String base64Package, String password) throws Exception {
        byte[] all = Base64.getDecoder().decode(base64Package);
        ByteBuffer bb = ByteBuffer.wrap(all);

        byte[] salt = new byte[SALT_LEN];
        bb.get(salt);

        byte[] iv = new byte[IV_LEN];
        bb.get(iv);

        byte[] ciphertext = new byte[bb.remaining()];
        bb.get(ciphertext);

        byte[] keyBytes = deriveKey(password.toCharArray(), salt);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        byte[] plain = cipher.doFinal(ciphertext);
        return new String(plain, StandardCharsets.UTF_8);
    }
}