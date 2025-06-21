package com.gsk.encryptomate.service;


import com.gsk.encryptomate.model.EncryptionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {

    @Autowired
    private EncryptionConfig currentConfig;

    public void saveConfigService(EncryptionConfig encryptionConfig) {
        currentConfig = encryptionConfig;
    }


    public void saveEncryptionConfig(EncryptionConfig newConfig) {
        this.currentConfig = newConfig;
    }

    public EncryptionConfig getEncryptionConfig() {
        return currentConfig;
    }

    // Possible options for selectable fields
    public String[] getEncryptionAlgorithms() {
        return new String[]{"AES"};
    }

    public Integer[] getKeySizes() {
        return new Integer[]{128, 192, 256};
    }

    public String[] getPaddingSchemes() {
        return new String[]{"AES/CBC/PKCS5Padding", "AES/ECB/PKCS5Padding", "AES/CFB/NoPadding", "AES/OFB/NoPadding", "AES/GCM/NoPadding"};
    }

    public String[] getHashAlgorithms() {
        return new String[]{"SHA-256", "SHA-512"};
    }

    public String[] getKeyDerivationFunctions() {
        return new String[]{"PBKDF2WithHmacSHA256"};
    }
}