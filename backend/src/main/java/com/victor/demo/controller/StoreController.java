package com.victor.demo.controller;

import com.victor.demo.dto.request.StoreRequest;
import com.victor.demo.dto.response.StoreResponse;
import com.victor.demo.service.StoreService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private static final Logger log = LoggerFactory.getLogger(StoreController.class);

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(@Valid @RequestBody StoreRequest request) {
        log.info("[CONTROLLER] POST /api/stores");
        return ResponseEntity.ok(storeService.createStore(request));
    }

    @GetMapping("/mine")
    public ResponseEntity<StoreResponse> findMine() {
        log.debug("[CONTROLLER] GET /api/stores/mine");
        return ResponseEntity.ok(storeService.findMine());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> findById(@PathVariable Long id) {
        log.debug("[CONTROLLER] GET /api/stores/{}", id);
        return ResponseEntity.ok(storeService.findById(id));
    }
}
