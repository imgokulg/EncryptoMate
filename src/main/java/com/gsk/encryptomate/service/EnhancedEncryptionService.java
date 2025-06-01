package com.gsk.encryptomate.service;

import com.gsk.encryptomate.exception.EncryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

@Service
public class EnhancedEncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedEncryptionService.class);
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final String ENCRYPTED_EXTENSION = ".encrypted";

    public void encryptFiles(List<String> filePaths, String password) {
        if (filePaths == null || filePaths.isEmpty()) {
            throw new EncryptionException("No files selected for encryption");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new EncryptionException("Password cannot be empty");
        }

        if (password.length() < 8) {
            throw new EncryptionException("Password must be at least 8 characters long");
        }

        try {
            SecretKeySpec key = generateKeyFromPassword(password);

            for (String filePath : filePaths) {
                try {
                    encryptFile(filePath, key);
                    logger.info("Successfully encrypted file: {}", filePath);
                } catch (Exception e) {
                    logger.error("Failed to encrypt file: {}", filePath, e);
                    throw new EncryptionException("Failed to encrypt file: " + filePath + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new EncryptionException("Encryption operation failed", e);
        }
    }

    public void decryptFiles(List<String> filePaths, String password) {
        if (filePaths == null || filePaths.isEmpty()) {
            throw new EncryptionException("No files selected for decryption");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new EncryptionException("Password cannot be empty");
        }

        try {
            SecretKeySpec key = generateKeyFromPassword(password);

            for (String filePath : filePaths) {
                try {
                    if (!filePath.endsWith(ENCRYPTED_EXTENSION)) {
                        throw new EncryptionException("File is not encrypted: " + filePath);
                    }
                    decryptFile(filePath, key);
                    logger.info("Successfully decrypted file: {}", filePath);
                } catch (Exception e) {
                    logger.error("Failed to decrypt file: {}", filePath, e);
                    throw new EncryptionException("Failed to decrypt file: " + filePath + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new EncryptionException("Decryption operation failed", e);
        }
    }

    private void encryptFile(String filePath, SecretKeySpec key) throws Exception {
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }

        if (Files.isDirectory(path)) {
            throw new IOException("Cannot encrypt directory: " + filePath);
        }

        if (filePath.endsWith(ENCRYPTED_EXTENSION)) {
            throw new IOException("File is already encrypted: " + filePath);
        }

        byte[] fileContent = Files.readAllBytes(path);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        byte[] encryptedContent = cipher.doFinal(fileContent);
        byte[] encryptedWithIv = new byte[iv.length + encryptedContent.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encryptedContent, 0, encryptedWithIv, iv.length, encryptedContent.length);

        String encryptedFilePath = filePath + ENCRYPTED_EXTENSION;
        Files.write(Paths.get(encryptedFilePath), encryptedWithIv);

        // Delete original file after successful encryption
        Files.delete(path);
    }

    private void decryptFile(String filePath, SecretKeySpec key) throws Exception {
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }

        byte[] fileContent = Files.readAllBytes(path);

        if (fileContent.length < GCM_IV_LENGTH + GCM_TAG_LENGTH) {
            throw new IOException("Invalid encrypted file format");
        }

        byte[] iv = Arrays.copyOfRange(fileContent, 0, GCM_IV_LENGTH);
        byte[] encryptedContent = Arrays.copyOfRange(fileContent, GCM_IV_LENGTH, fileContent.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        byte[] decryptedContent = cipher.doFinal(encryptedContent);

        String originalFilePath = filePath.substring(0, filePath.length() - ENCRYPTED_EXTENSION.length());
        Files.write(Paths.get(originalFilePath), decryptedContent);

        // Delete encrypted file after successful decryption
        Files.delete(path);
    }

    private SecretKeySpec generateKeyFromPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(password.getBytes("UTF-8"));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public boolean isEncryptedFile(String fileName) {
        return fileName != null && fileName.endsWith(ENCRYPTED_EXTENSION);
    }

    public String getOriginalFileName(String encryptedFileName) {
        if (isEncryptedFile(encryptedFileName)) {
            return encryptedFileName.substring(0, encryptedFileName.length() - ENCRYPTED_EXTENSION.length());
        }
        return encryptedFileName;
    }
}