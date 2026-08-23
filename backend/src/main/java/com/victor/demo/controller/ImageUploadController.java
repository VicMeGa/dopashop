package com.victor.demo.controller;

import com.victor.demo.dto.response.ImageUploadResponse;
import com.victor.demo.entity.Store;
import com.victor.demo.entity.User;
import com.victor.demo.repository.StoreRepository;
import com.victor.demo.security.CurrentUserProvider;
import com.victor.demo.service.ImageUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/stores/mine")
public class ImageUploadController {

    private final ImageUploadService imageUploadService;
    private final StoreRepository storeRepository;
    private final CurrentUserProvider currentUserProvider;

    public ImageUploadController(ImageUploadService imageUploadService,
                                 StoreRepository storeRepository,
                                 CurrentUserProvider currentUserProvider) {
        this.imageUploadService = imageUploadService;
        this.storeRepository = storeRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/images")
    public ResponseEntity<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        User user = currentUserProvider.getCurrentUser();
        Store store = storeRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Debes tener una tienda para subir imágenes"));
        String url = imageUploadService.uploadProductImage(file, store.getId());
        return ResponseEntity.ok(new ImageUploadResponse(url));
    }
}
