package com.gsk.encryptomate.controller;

import com.gsk.encryptomate.model.EncryptionRequest;
import com.gsk.encryptomate.model.FileInfo;
import com.gsk.encryptomate.service.ConfigService;
import com.gsk.encryptomate.service.EncryptionService;
import com.gsk.encryptomate.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLDecoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class FileExplorerController {
    private static final Logger LOGGER = Logger.getLogger(FileExplorerController.class.getName());

    @Autowired
    private ConfigService configService;

    @Autowired
    private FileService fileService;

    @Autowired
    private EncryptionService encryptionService;

    @GetMapping("/")
    @PostMapping("/")
    public String index(@RequestParam(defaultValue = "") String path, Model model) {
        try {
            List<FileInfo> files = fileService.listFiles(path);
            model.addAttribute("fileInfos", files);
            model.addAttribute("currentPath", path);
            model.addAttribute("parentPath", fileService.getParentPath(path));
            return "index";
        } catch (Exception e) {
            model.addAttribute("error", "Error accessing directory: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/view/{path}")
    public ResponseEntity<Resource> viewFile(@PathVariable String path) {
        try {
            Resource resource = fileService.getFileResource(path);
            String contentType = fileService.getContentType(path);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/")
    public ResponseEntity<Resource> downloadFile(@RequestParam String path) {
        try {
            path = URLDecoder.decode(path, "UTF-8");
            Resource resource = fileService.getFileResource(path);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/encrypt")
    @ResponseBody
    public ResponseEntity<String> encryptFiles(@RequestBody EncryptionRequest request) {
        try {
            encryptionService.encryptFiles(request.getFilePaths());
            return ResponseEntity.ok("{\"mesage\":\"Files encrypted successfully\"}");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Encryption failed: " + e.getMessage());
        }
    }

    @PostMapping("/decrypt")
    @ResponseBody
    public ResponseEntity<String> decryptFiles(@RequestBody EncryptionRequest request) {
        try {
            encryptionService.decryptFiles(request.getFilePaths());
            return ResponseEntity.ok("{\"mesage\":\"Files decrypted successfully\"}");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Decryption failed: " + e.getMessage());
        }
    }
}