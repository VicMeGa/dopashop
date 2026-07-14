package com.victor.demo.etl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlatziApiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public PlatziApiClient(@Value("${app.api.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<JsonNode> fetchAllCategories() {
        JsonNode body = restClient.get()
                .uri("/categories")
                .retrieve()
                .body(JsonNode.class);
        List<JsonNode> result = new ArrayList<>();
        if (body != null && body.isArray()) {
            body.forEach(result::add);
        }
        return result;
    }

    public List<JsonNode> fetchAllProducts() {
        JsonNode body = restClient.get()
                .uri("/products")
                .retrieve()
                .body(JsonNode.class);
        List<JsonNode> result = new ArrayList<>();
        if (body != null && body.isArray()) {
            body.forEach(result::add);
        }
        return result;
    }
}
