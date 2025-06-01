package com.gsk.encryptomate.service;

import com.gsk.encryptomate.model.EncryptionConfig;
import com.gsk.encryptomate.model.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {

    @Autowired
    private EncryptionConfig encryptionConfig;

    public List<FileInfo> listFiles(String relativePath) throws Exception {
        Path fullPath = Paths.get(encryptionConfig.getRootPath(), relativePath).normalize();

        if (!fullPath.startsWith(Paths.get(encryptionConfig.getRootPath()))) {
            throw new SecurityException("Access denied");
        }

        File directory = fullPath.toFile();
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Directory does not exist");
        }

        File[] files = directory.listFiles();
        List<FileInfo> fileInfos = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setName(file.getName());
                fileInfo.setPath(getRelativePath(file.getAbsolutePath()));
                fileInfo.setDirectory(file.isDirectory());
                fileInfo.setFileUri(file.toURI());
                fileInfo.setSize(file.isDirectory() ? 0L : file.length());
                fileInfo.setLastModified(LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(file.lastModified()),
                        ZoneId.systemDefault()
                ));
                fileInfo.setFileType(getFileType(file.getName()));
                fileInfo.setIcon(getFileIcon(file));
                fileInfos.add(fileInfo);
            }
        }

        return fileInfos.stream()
                .sorted((a, b) -> {
                    if (a.isDirectory() && !b.isDirectory()) return -1;
                    if (!a.isDirectory() && b.isDirectory()) return 1;
                    return a.getName().compareToIgnoreCase(b.getName());
                })
                .collect(Collectors.toList());
    }

    public Resource getFileResource(String relativePath) throws Exception {
        Path fullPath = Paths.get(encryptionConfig.getRootPath(), relativePath).normalize();

        if (!fullPath.startsWith(Paths.get(encryptionConfig.getRootPath()))) {
            throw new SecurityException("Access denied");
        }

        File file = fullPath.toFile();
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException("File does not exist");
        }

        return new FileSystemResource(file);
    }

    public String getContentType(String relativePath) throws Exception {
        Path fullPath = Paths.get(encryptionConfig.getRootPath(), relativePath).normalize();
        String contentType = Files.probeContentType(fullPath);
        return contentType != null ? contentType : "application/octet-stream";
    }

    public String getParentPath(String currentPath) {
        if (currentPath == null || currentPath.isEmpty()) {
            return null;
        }
        Path path = Paths.get(currentPath);
        Path parent = path.getParent();
        return parent != null ? parent.toString().replace("\\", "/") : "";
    }

    private String getRelativePath(String absolutePath) {
        Path absolute = Paths.get(absolutePath);
        Path root = Paths.get(encryptionConfig.getRootPath());
        return root.relativize(absolute).toString().replace("\\", "/");
    }

    private String getFileType(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }

    private String getFileIcon(File file) {
        if (file.isDirectory()) {
            return "fas fa-folder";
        }
        String extension = getFileType(file.getName());
        return switch (extension) {
            case "pdf" -> "fas fa-file-pdf";
            case "doc", "docx" -> "fas fa-file-word";
            case "xls", "xlsx" -> "fas fa-file-excel";
            case "ppt", "pptx" -> "fas fa-file-powerpoint";
            case "txt", "log" -> "fas fa-file-alt";
            case "jpg", "jpeg", "png", "gif", "bmp" -> "fas fa-file-image";
            case "mp4", "avi", "mov", "wmv" -> "fas fa-file-video";
            case "mp3", "wav", "flac", "aac" -> "fas fa-file-audio";
            case "zip", "rar", "7z" -> "fas fa-file-archive";
            case "java", "js", "py", "cpp", "html", "css" -> "fas fa-file-code";
            default -> "fas fa-file";
        };
    }
}