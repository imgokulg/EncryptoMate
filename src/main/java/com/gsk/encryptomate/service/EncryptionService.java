package com.gsk.encryptomate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

@Service
public class EncryptionService {
    @Autowired
    SecureFileDeleter secureFileDeleter;

    @Autowired
    private ConfigService configService;

    private static final String ENCRYPTED_EXTENSION = ".encrypted";

    public void encryptFiles(List<String> filePaths) throws Exception {
        for (String filePath : filePaths) {
            filePath = configService.getEncryptionConfig().getRootPath() + File.separator + filePath;
            encryptFile(filePath);
        }
    }

    public void decryptFiles(List<String> filePaths) throws Exception {

        for (String filePath : filePaths) {
            filePath = configService.getEncryptionConfig().getRootPath() + File.separator + filePath;
            decryptFile(filePath);
        }
    }

    private void encryptFile(String filePath) throws Exception {
        Path path = Paths.get(filePath);
        byte[] fileContent = Files.readAllBytes(path);

        byte[] salt = generateSalt(Integer.parseInt(configService.getEncryptionConfig().getSaltLength()));
        SecretKey key = generateKeyFromPassword(configService.getEncryptionConfig().getPassword(), salt);

        Cipher cipher = Cipher.getInstance(configService.getEncryptionConfig().getPaddingScheme());

        byte[] iv = new byte[16]; // For AES-CBC, IV should be 16 bytes
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encryptedContent = cipher.doFinal(fileContent);

        // Store: salt + iv + ciphertext
        byte[] finalBytes = new byte[salt.length + iv.length + encryptedContent.length];
        System.arraycopy(salt, 0, finalBytes, 0, salt.length);
        System.arraycopy(iv, 0, finalBytes, salt.length, iv.length);
        System.arraycopy(encryptedContent, 0, finalBytes, salt.length + iv.length, encryptedContent.length);


        String encryptedFilePath = path.getParent().toAbsolutePath() + File.separator + encryptText(path.getFileName().toString()) + ENCRYPTED_EXTENSION;
        Files.write(Paths.get(encryptedFilePath), finalBytes);

        secureFileDeleter.securelyDelete(path);
    }

    private void decryptFile(String filePath) throws Exception {
        if (!filePath.endsWith(ENCRYPTED_EXTENSION)) {
            throw new IllegalArgumentException("File is not encrypted");
        }
        Path path = Paths.get(filePath);
        byte[] fileContent = Files.readAllBytes(path);

        int saltLength = Integer.parseInt(configService.getEncryptionConfig().getSaltLength());

        byte[] salt = Arrays.copyOfRange(fileContent, 0, saltLength);
        byte[] iv = Arrays.copyOfRange(fileContent, saltLength, saltLength + 16); // 16 for CBC IV
        byte[] encryptedContent = Arrays.copyOfRange(fileContent, saltLength + 16, fileContent.length);

        SecretKey key = generateKeyFromPassword(configService.getEncryptionConfig().getPassword(), salt);

        Cipher cipher = Cipher.getInstance(configService.getEncryptionConfig().getPaddingScheme());
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

        byte[] decryptedContent = cipher.doFinal(encryptedContent);

        Path originalFile = Paths.get(filePath.substring(0, filePath.length() - ENCRYPTED_EXTENSION.length()));
        String originalFilePath = originalFile.getParent().toAbsolutePath() + File.separator + decryptText(originalFile.getFileName().toString());
        Files.write(Paths.get(originalFilePath), decryptedContent);

        secureFileDeleter.securelyDelete(path);
    }

    public String encryptText(String plainText) throws Exception {
        byte[] salt = generateSalt(Integer.parseInt(configService.getEncryptionConfig().getSaltLength()));
        SecretKey key = generateKeyFromPassword(configService.getEncryptionConfig().getPassword(), salt);

        Cipher cipher = Cipher.getInstance(configService.getEncryptionConfig().getPaddingScheme());
        byte[] iv = new byte[16]; // 16 bytes for AES-CBC
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Combine salt + iv + ciphertext
        byte[] combined = new byte[salt.length + iv.length + encryptedBytes.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(iv, 0, combined, salt.length, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, salt.length + iv.length, encryptedBytes.length);

        return bytesToHex(combined);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return result;
    }

    public String decryptText(String encryptedHexText) throws Exception {
        byte[] combined = hexToBytes(encryptedHexText);

        int saltLength = Integer.parseInt(configService.getEncryptionConfig().getSaltLength());
        byte[] salt = Arrays.copyOfRange(combined, 0, saltLength);
        byte[] iv = Arrays.copyOfRange(combined, saltLength, saltLength + 16); // 16 bytes IV
        byte[] encryptedBytes = Arrays.copyOfRange(combined, saltLength + 16, combined.length);

        SecretKey key = generateKeyFromPassword(configService.getEncryptionConfig().getPassword(), salt);

        Cipher cipher = Cipher.getInstance(configService.getEncryptionConfig().getPaddingScheme());
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }


    private SecretKey generateKeyFromPassword(String password, byte[] salt) throws Exception {
        int iterationCount = Integer.parseInt(configService.getEncryptionConfig().getIterationCount());
        int keySize = Integer.parseInt(configService.getEncryptionConfig().getKeySize());
        String kdf = configService.getEncryptionConfig().getKeyDerivationFunction();

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keySize);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(kdf);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, configService.getEncryptionConfig().getEncryptionAlgorithm());
    }

    private byte[] generateSalt(int length) {
        byte[] salt = new byte[length];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
}