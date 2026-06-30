package com.insureflow.claimintakeservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    //  Utilise Cloudinary au lieu du stockage local
    private final CloudinaryService cloudinaryService;

    public List<String> storeFiles(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();

        if (files == null || files.isEmpty()) return urls;

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            // Upload vers Cloudinary
            String cloudinaryUrl =
                    cloudinaryService.uploadPhoto(file);
            urls.add(cloudinaryUrl);

            log.info("Photo stockée dans Cloudinary : {}",
                    cloudinaryUrl);
        }

        return urls;
    }
}