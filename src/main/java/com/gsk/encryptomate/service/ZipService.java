package com.gsk.encryptomate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class ZipService {

    @Autowired
    FileService fileService;

    @Autowired
    private ConfigService configService;

    public void zipMultiple(List<String> sourcePaths) throws IOException {
        String zipFilePath = sourcePaths.stream()
                .map(filePath -> configService.getEncryptionConfig().getRootPath() + File.separator + filePath)
                .findAny().stream()
                .map(e -> Paths.get(e).getParent().toAbsolutePath().toString())
                .collect(Collectors.joining(File.separator)) + File.separator + "archive.zip";
        Path zipFile = Paths.get(zipFilePath);
        if (Files.exists(zipFile)) Files.delete(zipFile);

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            for (String pathStr : sourcePaths) {
                Path path = Paths.get(configService.getEncryptionConfig().getRootPath() + File.separator + pathStr);
                if (Files.exists(path)) {
                    if (Files.isDirectory(path)) {
                        zipDirectory(path, path.getFileName().toString(), zos);
                    } else {
                        zipFile(path, path.getFileName().toString(), zos);
                    }
                }
            }
        }
    }

    // Unzips the zip file to the destination directory
    public void unzip(String zipFilePath) throws IOException {
        zipFilePath = configService.getEncryptionConfig().getRootPath() + File.separator + zipFilePath;
        String destDirPath = Paths.get(zipFilePath).getParent().toAbsolutePath().toString();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                File newFile = newFile(new File(destDirPath), zipEntry);
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    // Internal: Zip a directory recursively
    private void zipDirectory(Path folder, String parentPath, ZipOutputStream zos) throws IOException {
        Files.walk(folder)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    String entryName = parentPath + "/" + folder.relativize(path).toString().replace("\\", "/");
                    try {
                        zos.putNextEntry(new ZipEntry(entryName));
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        System.err.println("Error zipping directory file: " + path + " -> " + e);
                    }
                });
    }

    private void zipFile(Path file, String entryName, ZipOutputStream zos) throws IOException {
        zos.putNextEntry(new ZipEntry(entryName));
        Files.copy(file, zos);
        zos.closeEntry();
    }

    // Security check
    private File newFile(File destDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destDir, zipEntry.getName());
        String destDirPath = destDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
