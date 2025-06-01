package com.gsk.encryptomate.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

public class CryptoUtil {
    private static final String ALGO = "AES";

    public static void encrypt(File file, String password) throws Exception {
        SecretKey key = getKeyFromPassword(password);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        doCrypto(cipher, file);
    }

    public static void decrypt(File file, String password) throws Exception {
        SecretKey key = getKeyFromPassword(password);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        doCrypto(cipher, file);
    }

    private static SecretKey getKeyFromPassword(String password) throws Exception {
        byte[] key = Arrays.copyOf(password.getBytes(StandardCharsets.UTF_8), 32);
        return new SecretKeySpec(key, ALGO);
    }

    private static void doCrypto(Cipher cipher, File file) throws Exception {
        byte[] input = Files.readAllBytes(file.toPath());
        byte[] output = cipher.doFinal(input);
        Files.write(file.toPath(), output);
    }
}