package com.victor.demo.service;

import com.victor.demo.dto.request.CreateProductRequest;
import com.victor.demo.dto.request.UpdateProductRequest;
import com.victor.demo.dto.response.ProductResponse;
import com.victor.demo.entity.Category;
import com.victor.demo.entity.Product;
import com.victor.demo.entity.Store;
import com.victor.demo.entity.User;
import com.victor.demo.mapper.ProductMapper;
import com.victor.demo.repository.CategoryRepository;
import com.victor.demo.repository.ProductRepository;
import com.victor.demo.repository.StoreRepository;
import com.victor.demo.repository.spec.ProductSpecifications;
import com.victor.demo.security.CurrentUserProvider;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final CurrentUserProvider currentUserProvider;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          StoreRepository storeRepository,
                          CurrentUserProvider currentUserProvider) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<ProductResponse> findAll(String title, BigDecimal priceMin,
                                          BigDecimal priceMax, Long categoryId) {
        var spec = ProductSpecifications.withFilters(title, priceMin, priceMax, categoryId);
        return productRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .map(ProductMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    public List<ProductResponse> findByStoreId(Long storeId) {
        return productRepository.findByStoreIdAndActiveTrue(storeId)
                .stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    public List<ProductResponse> findMyProducts() {
        Store store = getStoreForCurrentUser();
        return productRepository.findByStoreId(store.getId())
                .stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Store store = getStoreForCurrentUser();
        Category category = findCategory(request.categoryId());
        Product product = new Product();
        product.setTitle(request.title());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImageUrls(ProductMapper.serializeImages(request.imageUrls()));
        product.setCategory(category);
        product.setStore(store);
        product.setActive(true);
        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, UpdateProductRequest request) {
        Product product = getOwnedProduct(productId);
        if (request.title() != null) product.setTitle(request.title());
        if (request.description() != null) product.setDescription(request.description());
        if (request.price() != null) product.setPrice(request.price());
        if (request.stock() != null) product.setStock(request.stock());
        if (request.imageUrls() != null) product.setImageUrls(ProductMapper.serializeImages(request.imageUrls()));
        if (request.categoryId() != null) product.setCategory(findCategory(request.categoryId()));
        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = getOwnedProduct(productId);
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public void reactivateProduct(Long productId) {
        Product product = getOwnedProduct(productId);
        if (!Boolean.TRUE.equals(product.getActive())) {
            product.setActive(true);
            productRepository.save(product);
        }
    }

    private Store getStoreForCurrentUser() {
        User user = currentUserProvider.getCurrentUser();
        return storeRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Debes tener una tienda para gestionar productos"));
    }

    private Product getOwnedProduct(Long productId) {
        Store store = getStoreForCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        if (!product.getStore().getId().equals(store.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El producto no pertenece a tu tienda");
        }
        return product;
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }
}
