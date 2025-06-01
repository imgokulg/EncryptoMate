package com.gsk.encryptomate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file.explorer")
@Data
public class FileExplorerProperties {

    private String rootPath;
    private long maxFileSize = 500 * 1024 * 1024; // 500MB default
    private boolean allowEncryption = true;
    private boolean allowDecryption = true;
}
