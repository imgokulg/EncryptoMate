package com.gsk.encryptomate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo {
    private String name;
    private String path;
    private boolean isDirectory;
    private long size;
    private URI fileUri;
    private LocalDateTime lastModified;
    private String fileType;
    private String icon;
    public String getFormattedSize() {
        if (isDirectory) return "-";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
}