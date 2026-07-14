package com.victor.demo.etl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.victor.demo.entity.Category;
import com.victor.demo.entity.Product;
import com.victor.demo.repository.CategoryRepository;
import com.victor.demo.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class CatalogImportService {

    private static final Logger log = LoggerFactory.getLogger(CatalogImportService.class);
    private static final Random RANDOM = new Random();

    private final PlatziApiClient platziApiClient;
    private final ImageMigrationService imageMigrationService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public CatalogImportService(PlatziApiClient platziApiClient,
                                ImageMigrationService imageMigrationService,
                                CategoryRepository categoryRepository,
                                ProductRepository productRepository) {
        this.platziApiClient = platziApiClient;
        this.imageMigrationService = imageMigrationService;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.objectMapper = new ObjectMapper();
    }

    public ImportResult importCatalog() {
        if (categoryRepository.count() > 0 || productRepository.count() > 0) {
            return new ImportResult("ABORTED", "Data already exists. Import skipped.",
                    0, 0, 0);
        }

        int categoryCount = 0;
        int productCount = 0;
        int failedImages = 0;

        Map<Integer, Category> platziCategoryMap = new HashMap<>();

        List<JsonNode> platziCategories = platziApiClient.fetchAllCategories();
        for (JsonNode cat : platziCategories) {
            int platziId = cat.get("id").asInt();
            String name = cat.get("name").asText();
            String imageUrl = cat.has("image") && !cat.get("image").isNull()
                    ? cat.get("image").asText() : null;

            String minioImageUrl = null;
            if (imageUrl != null && !imageUrl.isBlank()) {
                List<String> migrated = imageMigrationService.migrateImages(
                        List.of(imageUrl), "categories/" + platziId);
                if (migrated.size() == 1) {
                    minioImageUrl = migrated.get(0);
                } else {
                    failedImages++;
                }
            }

            Category category = new Category();
            category.setName(name);
            category.setImageUrl(minioImageUrl);
            categoryRepository.save(category);
            platziCategoryMap.put(platziId, category);
            categoryCount++;
        }
        log.info("Imported {} categories", categoryCount);

        List<JsonNode> platziProducts = platziApiClient.fetchAllProducts();
        for (JsonNode prod : platziProducts) {
            int platziId = prod.get("id").asInt();
            String title = prod.get("title").asText();
            String description = prod.has("description") && !prod.get("description").isNull()
                    ? prod.get("description").asText() : null;
            BigDecimal price = BigDecimal.valueOf(prod.get("price").asDouble())
                    .setScale(2, RoundingMode.HALF_UP);
            JsonNode categoryNode = prod.get("category");
            int categoryPlatziId = categoryNode.get("id").asInt();
            Category category = platziCategoryMap.get(categoryPlatziId);
            if (category == null) {
                log.warn("Product {} references unknown category {}, skipping", platziId, categoryPlatziId);
                continue;
            }

            List<String> externalImages = new ArrayList<>();
            JsonNode imagesNode = prod.get("images");
            if (imagesNode != null && imagesNode.isArray()) {
                for (JsonNode img : imagesNode) {
                    if (!img.isNull()) {
                        String url = img.asText();
                        if (!url.isBlank()) externalImages.add(url);
                    }
                }
            }

            List<String> minioUrls = imageMigrationService.migrateImages(
                    externalImages, "products/" + platziId);
            failedImages += (externalImages.size() - minioUrls.size());

            String imageUrlsJson = minioUrls.isEmpty() ? null
                    : toJsonArray(minioUrls);

            int stock = 10 + RANDOM.nextInt(91);

            Product product = new Product();
            product.setTitle(title);
            product.setDescription(description);
            product.setPrice(price);
            product.setStock(stock);
            product.setImageUrls(imageUrlsJson);
            product.setCategory(category);
            productRepository.save(product);
            productCount++;
        }
        log.info("Imported {} products", productCount);

        return new ImportResult("SUCCESS", "Catalog imported successfully.",
                categoryCount, productCount, failedImages);
    }

    public void clearCatalog() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        log.info("Catalog cleared (products deleted, categories deleted)");
    }

    private String toJsonArray(List<String> urls) {
        try {
            return objectMapper.writeValueAsString(urls);
        } catch (Exception e) {
            return "[]";
        }
    }

    public record ImportResult(String status, String message,
                               int categoriesImported, int productsImported,
                               int imagesFailed) {}
}
