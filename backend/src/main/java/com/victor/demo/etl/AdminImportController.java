package com.victor.demo.etl;

import com.victor.demo.etl.CatalogImportService.ImportResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Herramienta de import de una sola vez. Deshabilitado por defecto vía
 * app.admin.import.enabled=false. Activar temporalmente
 * (ADMIN_IMPORT_ENABLED=true en .env) solo si se necesita reimportar el
 * catálogo en un entorno nuevo o limpio.
 */
@RestController
@RequestMapping("/api/admin")
@ConditionalOnProperty(name = "app.admin.import.enabled", havingValue = "true")
public class AdminImportController {

    private static final Logger log = LoggerFactory.getLogger(AdminImportController.class);

    private final CatalogImportService catalogImportService;
    private final String adminToken;

    public AdminImportController(CatalogImportService catalogImportService,
                                 @Value("${admin.import-token}") String adminToken) {
        this.catalogImportService = catalogImportService;
        this.adminToken = adminToken;
    }

    @PostMapping("/import/catalog")
    public ResponseEntity<?> importCatalog(@RequestHeader("X-Admin-Token") String token) {
        if (!adminToken.equals(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        ImportResult result = catalogImportService.importCatalog();
        log.info("Import result: {} - {} categories, {} products",
                result.status(), result.categoriesImported(), result.productsImported());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/import/catalog")
    public ResponseEntity<?> clearCatalog(@RequestHeader("X-Admin-Token") String token) {
        if (!adminToken.equals(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        catalogImportService.clearCatalog();
        log.info("Catalog cleared");
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Catalog cleared"));
    }
}
