package com.gsk.encryptomate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class EncryptomateApplication {

    private static final Logger LOGGER = Logger.getLogger(EncryptomateApplication.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(EncryptomateApplication.class, args);
    }

    // Create the root directory if it doesn't exist on startup
    @Bean
    CommandLineRunner init(@Value("${encryption.config.root-path}") String rootDir) {
        return args -> {
            Path rootPath = Paths.get(rootDir);
            if (Files.notExists(rootPath)) {
                try {
                    Files.createDirectories(rootPath);
                    LOGGER.info("Created Root directory: " + rootDir);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            } else {
                LOGGER.info("Root directory already exists: " + rootDir);
            }
        };
    }
}
