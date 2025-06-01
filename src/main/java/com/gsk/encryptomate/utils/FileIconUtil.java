package com.gsk.encryptomate.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FileIconUtil {

    private static final Map<String, String> EXTENSION_ICON_MAP = new HashMap<>() {{
        put("pdf", "pdf-icon.png");
        put("doc", "word-icon.png");
        put("docx", "word-icon.png");
        put("", "folder-icon.png");
    }};

    public static String getIconForFile(Path path) {
        if (Files.isDirectory(path)) {
            return "folder-icon.png";
        }

        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            String extension = fileName.substring(dotIndex + 1).toLowerCase();
            return EXTENSION_ICON_MAP.getOrDefault(extension, "file-icon.png");
        }
        return "file-icon.png";
    }
}