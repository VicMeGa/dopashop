package com.victor.demo.controller;

import com.victor.demo.dto.request.AddressRequest;
import com.victor.demo.dto.response.AddressResponse;
import com.victor.demo.service.AddressService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private static final Logger log = LoggerFactory.getLogger(AddressController.class);

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> findAll() {
        log.debug("[CONTROLLER] GET /api/addresses");
        return ResponseEntity.ok(addressService.findAllForCurrentUser());
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody AddressRequest request) {
        log.info("[CONTROLLER] POST /api/addresses");
        return ResponseEntity.ok(addressService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        log.info("[CONTROLLER] PUT /api/addresses/{}", id);
        return ResponseEntity.ok(addressService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("[CONTROLLER] DELETE /api/addresses/{}", id);
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
