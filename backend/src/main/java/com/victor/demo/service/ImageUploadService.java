package com.victor.demo.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageUploadService {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final String USER_PRODUCTS_PREFIX = "user-products";

    private final MinioClient minioClient;
    private final String bucket;
    private final String endpoint;

    public ImageUploadService(MinioClient minioClient,
                              @Value("${minio.bucket-products}") String bucket,
                              @Value("${minio.endpoint}") String endpoint) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        this.endpoint = endpoint;
    }

    public String uploadProductImage(MultipartFile file, Long storeId) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo está vacío");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "El archivo debe ser una imagen");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "La imagen supera el tamaño máximo de 5MB");
        }
        String objectName = USER_PRODUCTS_PREFIX + "/" + storeId + "/"
                + UUID.randomUUID() + "-" + sanitizeFileName(file.getOriginalFilename());
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo subir la imagen");
        }
        return endpoint + "/" + bucket + "/" + objectName;
    }

    private String sanitizeFileName(String original) {
        if (original == null || original.isBlank()) return "imagen";
        String name = Paths.get(original.replace('\\', '/')).getFileName().toString();
        String cleaned = name.replaceAll("[^A-Za-z0-9._-]", "_");
        return cleaned.isBlank() ? "imagen" : cleaned;
    }
}
