package com.gsk.encryptomate.model;

import lombok.Data;

import java.util.List;

@Data
public class EncryptionRequest {
    private List<String> filePaths;
}