package com.insureflow.claimintakeservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}")    String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key",    apiKey,
                "api_secret", apiSecret,
                "secure",     true
        ));
    }

    public String uploadPhoto(MultipartFile file) {
        try {
            log.info("Upload photo vers Cloudinary : {}",
                    file.getOriginalFilename());

            Map result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder",        "insureflow/sinistres",
                            "resource_type", "image",
                            "quality",       "auto",
                            "fetch_format",  "auto"
                    )
            );

            String url = (String) result.get("secure_url");
            log.info("Photo uploadée : {}", url);
            return url;

        } catch (Exception e) {
            log.error("Erreur Cloudinary : {}", e.getMessage());
            throw new RuntimeException(
                    "Erreur upload photo : " + e.getMessage()
            );
        }
    }
}