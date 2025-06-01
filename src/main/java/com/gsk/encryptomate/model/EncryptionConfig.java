package com.gsk.encryptomate.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "encryption.config")
public class EncryptionConfig {
    private String rootPath;
    private String password;
    private String encryptionAlgorithm;
    private String keySize;
    private String paddingScheme;
    private String hashAlgorithm;
    private String keyDerivationFunction;
    private String saltLength;
    private String iterationCount;
}
