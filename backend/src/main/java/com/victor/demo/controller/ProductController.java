package com.victor.demo.controller;

import com.victor.demo.dto.request.CreateProductRequest;
import com.victor.demo.dto.request.UpdateProductRequest;
import com.victor.demo.dto.response.ProductResponse;
import com.victor.demo.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@Validated
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/api/store/products")
    public List<ProductResponse> getProducts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false)
            @DecimalMin(value = "0", message = "El precio mínimo no puede ser negativo") BigDecimal priceMin,
            @RequestParam(required = false)
            @DecimalMin(value = "0", message = "El precio máximo no puede ser negativo") BigDecimal priceMax,
            @RequestParam(required = false) Long categoryId) {
        log.debug("[CONTROLLER] GET /api/store/products title={} categoryId={} priceMin={} priceMax={}",
                title, categoryId, priceMin, priceMax);
        return productService.findAll(title, priceMin, priceMax, categoryId);
    }

    @GetMapping("/api/store/products/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        log.debug("[CONTROLLER] GET /api/store/products/{}", id);
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping("/api/stores/{storeId}/products")
    public List<ProductResponse> getStoreProducts(@PathVariable Long storeId) {
        log.debug("[CONTROLLER] GET /api/stores/{}/products", storeId);
        return productService.findByStoreId(storeId);
    }

    @PostMapping("/api/stores/mine/products")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("[CONTROLLER] POST /api/stores/mine/products");
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @GetMapping("/api/stores/mine/products")
    public List<ProductResponse> getMyProducts() {
        log.debug("[CONTROLLER] GET /api/stores/mine/products");
        return productService.findMyProducts();
    }

    @PutMapping("/api/stores/mine/products/{id}/reactivate")
    public ResponseEntity<Void> reactivateProduct(@PathVariable Long id) {
        log.info("[CONTROLLER] PUT /api/stores/mine/products/{}/reactivate", id);
        productService.reactivateProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/stores/mine/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateProductRequest request) {
        log.info("[CONTROLLER] PUT /api/stores/mine/products/{}", id);
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/api/stores/mine/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("[CONTROLLER] DELETE /api/stores/mine/products/{}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
