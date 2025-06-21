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
    private String formattedSize;
    private URI fileUri;
    private LocalDateTime lastModified;
    private String fileType;
    private String icon;
    public void setSize(long size) {
        this.size = size;
        this.formattedSize = size == 0 ? "0 Bytes" :
                String.format("%." + (Math.max(2, 0)) + "f %s",
                        size / Math.pow(1024, (int) (Math.log(size) / Math.log(1024))),
                        new String[]{"Bytes", "KB", "MB", "GB", "TB", "PB"}[(int) (Math.log(size) / Math.log(1024))]);
    }
}