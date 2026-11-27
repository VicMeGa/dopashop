package com.victor.demo.etl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class ImageMigrationService {

    private static final Logger log = LoggerFactory.getLogger(ImageMigrationService.class);
    private static final List<String> IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";
    private static final int PRE_DOWNLOAD_DELAY_MS = 200;
    private static final int MAX_RETRIES = 3;
    private static final int BASE_RETRY_DELAY_MS = 500;

    private final MinioClient minioClient;
    private final RestClient httpClient;
    private final String bucket;
    private final String endpoint;

    public ImageMigrationService(MinioClient minioClient,
                                 @Value("${minio.bucket-products}") String bucket,
                                 @Value("${minio.endpoint}") String endpoint) {
        this.minioClient = minioClient;
        this.httpClient = RestClient.create();
        this.bucket = bucket;
        this.endpoint = endpoint;
    }

    public List<String> migrateImages(List<String> externalUrls, String prefix) {
        List<String> publicUrls = new ArrayList<>();
        for (int i = 0; i < externalUrls.size(); i++) {
            String url = externalUrls.get(i);
            try {
                Thread.sleep(PRE_DOWNLOAD_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            try {
                byte[] data = downloadWithRetry(url);
                if (data == null || data.length == 0) {
                    log.warn("Empty response for image {}", url);
                    continue;
                }
                String extension = extractExtension(url);
                String objectName = prefix + "/" + i + "." + extension;
                String contentType = switch (extension) {
                    case "png" -> "image/png";
                    case "gif" -> "image/gif";
                    case "webp" -> "image/webp";
                    default -> "image/jpeg";
                };
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(new ByteArrayInputStream(data), data.length, -1)
                        .contentType(contentType)
                        .build());
                publicUrls.add(endpoint + "/" + bucket + "/" + objectName);
            } catch (Exception e) {
                log.error("Failed to migrate image {}: {}", url, e.getMessage());
            }
        }
        return publicUrls;
    }

    private byte[] downloadWithRetry(String url) {
        int delay = BASE_RETRY_DELAY_MS;
        for (int attempt = 0; ; attempt++) {
            try {
                return httpClient.get().uri(url)
                        .header("User-Agent", USER_AGENT)
                        .retrieve()
                        .body(byte[].class);
            } catch (HttpClientErrorException.TooManyRequests e) {
                if (attempt >= MAX_RETRIES) {
                    log.error("Failed after {} retries: {}", MAX_RETRIES, url);
                    throw e;
                }
                log.warn("Rate limited for {}, retrying in {}ms (attempt {}/{})", url, delay, attempt + 1, MAX_RETRIES);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
                delay *= 2;
            }
        }
    }

    private String extractExtension(String url) {
        String path = url.contains("?") ? url.substring(0, url.indexOf('?')) : url;
        int dot = path.lastIndexOf('.');
        if (dot > 0 && dot < path.length() - 1) {
            String ext = path.substring(dot + 1).toLowerCase();
            if (IMAGE_EXTENSIONS.contains(ext)) return ext;
        }
        return "jpg";
    }
}
