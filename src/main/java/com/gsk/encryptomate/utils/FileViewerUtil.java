package com.gsk.encryptomate.utils;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileViewerUtil {

    public static ResponseEntity<?> getResponseForFile(Path filePath) throws IOException {
        String mimeType = Files.probeContentType(filePath);

        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        if (mimeType.startsWith("text")) {
            String content = new String(Files.readAllBytes(filePath));
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(content);
        } else if (mimeType.startsWith("image")) {
            byte[] imageBytes = Files.readAllBytes(filePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(imageBytes);
        } else if (mimeType.startsWith("video") || mimeType.startsWith("audio")) {
            // Stream the media file
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(new InputStreamResource(Files.newInputStream(filePath)));
        } else {
            // Download for other file types
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + filePath.getFileName() + "\"")
                    .body(new InputStreamResource(Files.newInputStream(filePath)));
        }
    }
}