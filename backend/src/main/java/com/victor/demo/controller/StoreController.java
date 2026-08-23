package com.victor.demo.controller;

import com.victor.demo.dto.request.StoreRequest;
import com.victor.demo.dto.response.StoreResponse;
import com.victor.demo.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(@Valid @RequestBody StoreRequest request) {
        return ResponseEntity.ok(storeService.createStore(request));
    }

    @GetMapping("/mine")
    public ResponseEntity<StoreResponse> findMine() {
        return ResponseEntity.ok(storeService.findMine());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.findById(id));
    }
}
