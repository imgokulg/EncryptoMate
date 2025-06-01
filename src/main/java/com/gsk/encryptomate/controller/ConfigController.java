package com.gsk.encryptomate.controller;


import com.gsk.encryptomate.model.EncryptionConfig;
import com.gsk.encryptomate.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @GetMapping
    public String showConfigForm(Model model) {
        model.addAttribute("encryptionConfig", configService.getEncryptionConfig());
        model.addAttribute("encryptionAlgorithms", configService.getEncryptionAlgorithms());
        model.addAttribute("keySizes", configService.getKeySizes());

        model.addAttribute("paddingSchemes", configService.getPaddingSchemes());
        model.addAttribute("hashAlgorithms", configService.getHashAlgorithms());
        model.addAttribute("keyDerivationFunctions", configService.getKeyDerivationFunctions());
        return "config";
    }

    @PostMapping
    public String saveConfig(@ModelAttribute EncryptionConfig encryptionConfig, Model model) {
        configService.saveConfigService(encryptionConfig);
        model.addAttribute("message", "Configuration saved successfully!");
        model.addAttribute("encryptionConfig", configService.getEncryptionConfig()); // Load updated config
        model.addAttribute("encryptionAlgorithms", configService.getEncryptionAlgorithms());
        model.addAttribute("keySizes", configService.getKeySizes());
        model.addAttribute("paddingSchemes", configService.getPaddingSchemes());
        model.addAttribute("hashAlgorithms", configService.getHashAlgorithms());
        model.addAttribute("keyDerivationFunctions", configService.getKeyDerivationFunctions());
        return "config";
    }
}