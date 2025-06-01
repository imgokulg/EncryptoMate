package com.gsk.encryptomate.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

@Service
public class SecureFileDeleter {

    private static final SecureRandom random = new SecureRandom();

    public void securelyDelete(Path path) throws IOException {
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("File does not exist or is not a regular file: " + path);
        }

        long length = Files.size(path);

        // Overwrite with random data
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rws")) {
            byte[] data = new byte[4096];
            long pos = 0;
            while (pos < length) {
                random.nextBytes(data);
                int writeLength = (int) Math.min(data.length, length - pos);
                raf.write(data, 0, writeLength);
                pos += writeLength;
            }
            raf.getFD().sync(); // Force write to disk
        }

        // Optional: Overwrite with zeros
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rws")) {
            byte[] zeros = new byte[4096];
            long pos = 0;
            while (pos < length) {
                int writeLength = (int) Math.min(zeros.length, length - pos);
                raf.write(zeros, 0, writeLength);
                pos += writeLength;
            }
            raf.getFD().sync();
        }

        // Delete file
        Files.delete(path);
    }
}
