package com.victor.demo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/store")
public class StoreProxyController {

    private final RestClient storeRestClient;

    public StoreProxyController(RestClient storeRestClient) {
        this.storeRestClient = storeRestClient;
    }

    @GetMapping("/products")
    public JsonNode getProducts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Double priceMin,
            @RequestParam(required = false) Double priceMax,
            @RequestParam(required = false) Integer categoryId) {
        return storeRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/products");
                    if (title != null) uriBuilder.queryParam("title", title);
                    if (priceMin != null) uriBuilder.queryParam("price_min", priceMin);
                    if (priceMax != null) uriBuilder.queryParam("price_max", priceMax);
                    if (categoryId != null) uriBuilder.queryParam("categoryId", categoryId);
                    return uriBuilder.build();
                })
                .retrieve()
                .body(JsonNode.class);
    }

    @GetMapping("/products/{id}")
    public JsonNode getProduct(@PathVariable int id) {
        return storeRestClient.get()
                .uri("/products/{id}", id)
                .retrieve()
                .body(JsonNode.class);
    }

    @GetMapping("/categories")
    public JsonNode getCategories() {
        return storeRestClient.get()
                .uri("/categories")
                .retrieve()
                .body(JsonNode.class);
    }

    @GetMapping("/categories/{id}")
    public JsonNode getCategory(@PathVariable int id) {
        return storeRestClient.get()
                .uri("/categories/{id}", id)
                .retrieve()
                .body(JsonNode.class);
    }
}
